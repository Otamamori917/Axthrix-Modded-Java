package axthrix.world.types.abilities;


import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import axthrix.world.util.draw.AxDrawf;
import axthrix.world.util.ui.ShieldPlatesBar;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.DrawPart.PartParams;
import mindustry.entities.part.RegionPart;
import mindustry.game.EventType;
import mindustry.gen.Bullet;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;

/**
 * A physical armor plate with its own durability pool, separate from unit health.
 * One ability instance is one plate - attach several ShieldPlateAbility instances (each with
 * a unique plateIndex) to a unit type for multiple independent plates (front/back/side, etc.)
 *
 * @author Otamamori
 */
public class ShieldPlateAbility extends Ability {

    /** Index of this plate among all plates on the same unit. Must be unique per unit type, starting at 0. Plate 0 is the shared bookkeeping coordinator. */
    public int plateIndex = 0;

    /** Maximum durability of this plate, in the same units as unit health. */
    public float maxDurability = 100f;

    /** If true, a destroyed plate begins autonomous reconstruction after {@link #rebuildDelay}. If false, destruction is permanent. */
    public boolean canRebuild = false;

    /** Duration in frames required to go from 0 to max durability once construction is actively progressing. Ignored if canRebuild is false. */
    public float buildTime = 300f;

    /** Wait time in frames before construction starts, and the time restarted from full every time combat interrupts construction. Ignored if canRebuild is false. */
    public float rebuildDelay = 180f;

    /** If true, construction only progresses while no enemy units are within combatCheckRange. If false, construction ignores combat entirely and runs uninterrupted on its own timers. */
    public boolean requireOutOfCombat = true;

    /** Standalone detection radius used only when requireOutOfCombat is true. Independent of the unit's own weapon or target range. */
    public float combatCheckRange = 120f;

    /** Fraction (0-1) of incoming damage absorbed by the plate on a piercing hit. The remainder is applied to the unit in the same call. */
    public float pierceAbsorbRatio = 0.5f;

    /** Degrees offset from the unit's forward facing that this plate's coverage arc is centered on. 0 = front, 180 = back, 90/-90 = sides. */
    public float facingAngle = 0f;

    /** Total width in degrees of the arc this plate protects, centered on facingAngle. E.g. 90 covers 45 degrees to either side. */
    public float coverageArc = 90f;

    /** How close (in degrees) a gap hit's angle must be to this plate's facingAngle to be considered "nearby" for big-bullet gap splashing. */
    public float gapAssistRange = 60f;

    /** Fraction (0-1) of a big-bullet gap hit absorbed by a single nearby plate when only one plate is within gapAssistRange. The remainder goes to the unit. */
    public float gapAbsorbRatio = 0.5f;

    /** bullet.type.hitSize at or above this value is treated as a "big" bullet for gap-splash behavior; below it, gap hits pass straight through to the unit untouched. */
    public static float bigBulletSizeThreshold = 9f;

    /**
     * a {@link RegionPart}  used to draw this plate - load() resolves it as "{unitName}-{suffix}"
     */
    public RegionPart part = new RegionPart();

    /** Params instance reused every frame for this plate instance, avoiding per-frame allocation. */
    protected transient PartParams params = new PartParams();

    /** Sound played at the exact coordinate frame the plate breaks. */
    public Sound destroySound = Sounds.rockBreak;

    /** The specific weapon on the unit that this plate should mirror basic animation progress variables from.
     *  remember that all seq start at 0 so the first weapon is slot 0
     */
    public int weaponSlotIndex = 0;


    /** Per-plate state, keyed by a packed (unit id, plateIndex) key so multiple plates never share durability. */
    protected static final IntMap<ShieldState> plateStates = new IntMap<>();

    /** Shared per-unit bookkeeping, keyed by unit id. Only touched by the plateIndex == 0 instance. */
    protected static final IntMap<UnitTrack> unitTracks = new IntMap<>();

    @Override
    public void init(UnitType type) {
        part.turretShading = false;
        part.load(type.name);
    }

    @Override
    public void created(Unit unit) {
        ShieldState state = stateFor(unit);
        state.durability = maxDurability;
        state.underConstruction = false;
        state.building = false;
        state.pausedForCombat = false;
        state.delayRemaining = rebuildDelay;
        state.buildRemaining = buildTime;
        state.smoothRebuildProgress = 1f;
        state.hitImpulse = 0f;
        state.spawnedAt = Time.time;

        if (plateIndex == 0) {
            UnitTrack track = trackFor(unit);
            track.lastHealth = unit.health;
            track.lastFrame = -1f;
        }
    }

    @Override
    public void death(Unit unit) {
        plateStates.remove(key(unit.id, plateIndex));
        if (plateIndex == 0) unitTracks.remove(unit.id);
    }

    @Override
    public void update(Unit unit) {
        ShieldState state = stateFor(unit);

        if (plateIndex == 0) {
            resolveSharedFrame(unit);
        }

        state.hitImpulse = Mathf.lerpDelta(state.hitImpulse, 0f, 0.15f);


        if (state.durability <= 0f || state.underConstruction) {
            updateRebuild(unit, state);
        }

        float targetRebuildProgress = buildTime > 0f ? 1f - (state.buildRemaining / buildTime) : 1f;
        state.smoothRebuildProgress = Mathf.lerpDelta(state.smoothRebuildProgress, targetRebuildProgress, 0.1f);
    }



    /** Passive, non-directional fallback: detects unexplained health drops for the whole unit and heal-scaling. Runs once per frame via plate 0. */
    protected void resolveSharedFrame(Unit unit) {
        UnitTrack track = trackFor(unit);

        if (track.lastFrame == Time.time) return;
        track.lastFrame = Time.time;

        float delta = track.lastHealth - unit.health;
        track.lastHealth = unit.health;

        if (delta > 0.001f) {
            ShieldPlateAbility active = lowestActivePlate(unit);
            if (active != null) {
                ShieldState activeState = active.stateFor(unit);
                float absorbed = Math.min(delta, activeState.durability);
                activeState.durability -= absorbed;
                activeState.hitImpulse = 1f;
                unit.health = Math.min(unit.maxHealth, unit.health + absorbed);
                track.lastHealth = unit.health;
                if (activeState.durability <= 0f && active.canRebuild) {
                    active.beginRebuild(activeState);
                }
            }
        } else if (delta < -0.001f) {
            float healedAmount = -delta;
            float preHealHealth = unit.health - healedAmount;
            float missingBefore = unit.maxHealth - preHealHealth;
            if (missingBefore > 0.001f) {
                float healFraction = healedAmount / missingBefore;
                for (ShieldPlateAbility plate : allPlates(unit)) {
                    ShieldState s = plate.stateFor(unit);

                    boolean eligible = (s.durability > 0f && !s.underConstruction) || (plate.canRebuild && !s.underConstruction);

                    if (!eligible) continue;
                    float missingDurability = plate.maxDurability - s.durability;
                    if (missingDurability <= 0f) continue;
                    s.durability = Math.min(plate.maxDurability, s.durability + missingDurability * healFraction);

                    if (s.durability >= plate.maxDurability) {
                        s.underConstruction = false;
                    }
                }

            }
        }
    }

    protected void updateRebuild(Unit unit, ShieldState state) {
        if (!canRebuild) return;

        boolean combatActive = requireOutOfCombat && enemyNearby(unit);
        state.combatBlocking = combatActive;

        if (combatActive) {
            state.pausedForCombat = true;
            return;
        }

        if (state.pausedForCombat) {
            state.pausedForCombat = false;
            state.building = false;
            state.delayRemaining = rebuildDelay;
        }

        if (!state.building) {
            state.delayRemaining -= Time.delta;
            if (state.delayRemaining <= 0f) {
                state.building = true;
            }
            return;
        }

        state.buildRemaining -= Time.delta;
        float progress = Mathf.clamp(1f - (state.buildRemaining / buildTime));
        state.durability = maxDurability * progress;

        if (state.buildRemaining <= 0f) {
            state.durability = maxDurability;
            state.underConstruction = false;
            state.building = false;
        }
    }
    protected void beginRebuild(ShieldState state) {
        state.underConstruction = true;
        state.building = false;
        state.pausedForCombat = false;
        state.delayRemaining = rebuildDelay;
        state.buildRemaining = buildTime;
    }

    protected boolean enemyNearby(Unit unit) {
        return Units.closestEnemy(unit.team, unit.x, unit.y, combatCheckRange, u -> true) != null;
    }

    @Override
    public void draw(Unit unit) {
        ShieldState state = stateFor(unit);

        boolean activelyBuilding = canRebuild && state.underConstruction;
        if (state.durability <= 0.01f && !activelyBuilding) return;

        float rotationBase = unit.rotation - 90f;
        float fixedPlateRotation = unit.rotation;

        float targetX = unit.x + arc.math.Angles.trnsx(rotationBase, part.x, part.y);
        float targetY = unit.y + arc.math.Angles.trnsy(rotationBase, part.x, part.y);

        float weaponWarmup = 0f;
        float weaponReload = 0f;
        float weaponHeat = 0f;
        float weaponRecoilProgress = 0f;

        if (unit.mounts != null && weaponSlotIndex >= 0 && weaponSlotIndex < unit.mounts.length) {
            var mount = unit.mounts[weaponSlotIndex];
            float rawRecoilProgress = (part.recoilIndex >= 0 && mount.recoils != null && part.recoilIndex < mount.recoils.length)
                    ? mount.recoils[part.recoilIndex]
                    : mount.recoil;

            weaponWarmup = mount.warmup;
            weaponReload = mount.weapon.reload > 0f ? 1f - (mount.reload / mount.weapon.reload) : 0f;
            weaponHeat = mount.heat;
            weaponRecoilProgress = rawRecoilProgress;
        }

        float durFraction = maxDurability > 0f ? state.durability / maxDurability : 0f;
        float rebuildProgress = buildTime > 0f ? arc.math.Mathf.clamp(1f - (state.buildRemaining / buildTime)) : 1f;

        axthrix.world.util.AxPartParms.shieldparams.set(
                durFraction,
                rebuildProgress,
                state.smoothRebuildProgress,
                weaponWarmup,
                weaponReload,
                weaponRecoilProgress,
                weaponHeat
        );

        params.set(
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                targetX,
                targetY,
                fixedPlateRotation
        );
        params.life = Time.time - state.spawnedAt;
        params.sideOverride = -1;

        if (activelyBuilding) {
            drawMaterialize(unit, state, targetX, targetY, fixedPlateRotation, rebuildProgress);
            return;
        }

        float elapsedFrames = Time.time - state.lastHitTime;
        boolean flashing = elapsedFrames < 5f;

        float prevZ = Draw.z();
        float unitBaseLayer = prevZ;
        if (unit.type != null) {
            unitBaseLayer = unit.isFlying() ? unit.type.flyingLayer : unit.type.groundLayer;
        }
        float determinedZ = part.layer > 0 ? part.layer : unitBaseLayer + 0.005f;

        Draw.z(determinedZ);

        if (flashing) {
            float flashAlpha = 1f - (elapsedFrames / 5f);
            Draw.mixcol(Color.white, flashAlpha);
        }

        part.draw(params);

        if (flashing) {
            Draw.mixcol();
        }

        Draw.z(prevZ);
    }


    protected void drawMaterialize(Unit unit, ShieldState state, float targetX, float targetY, float fixedPlateRotation, float buildFraction) {
        TextureRegion icon = part.regions != null && part.regions.length > 0 ? part.regions[0] : null;
        if (icon == null && unit.type != null) icon = unit.type.fullIcon;
        if (icon == null) return;

        float unitBaseLayer = mindustry.graphics.Layer.flyingUnit;
        if (unit.type != null) {
            unitBaseLayer = unit.isFlying() ? unit.type.flyingLayer : unit.type.groundLayer;
        }
        float baseZ = part.layer > 0 ? part.layer : unitBaseLayer + 0.005f;

        float prog = part.progress.getClamp(params, part.clampProgress);
        float mx = part.moveX * prog;
        float my = part.moveY * prog;
        float mr = part.moveRot * prog + part.rotation;

        int len = part.mirror ? 2 : 1;
        final Color teamColor = unit.team.color;
        final float timeParam = -Time.time / 4f;

        final TextureRegion finalIcon = icon;

        for (int s = 0; s < len; s++) {
            final float sign = (s == 0 ? 1f : -1f);

            TextureRegion sideIcon = (part.regions != null && part.regions.length > s) ? part.regions[s] : finalIcon;
            TextureRegion sideOutline = (part.outline && part.outlines != null && part.outlines.length > s) ? part.outlines[s] : null;

            Tmp.v1.set(mx * sign, my).rotateRadExact((fixedPlateRotation - 90) * arc.math.Mathf.degRad);

            final float finalX = targetX + Tmp.v1.x;
            final float finalY = targetY + Tmp.v1.y;
            final float matRotation = mr * sign + fixedPlateRotation - 90f;

            final TextureRegion currentIcon = sideIcon;
            final TextureRegion currentOutline = sideOutline;

            if (currentOutline != null) {
                float outlineZ = baseZ + part.outlineLayerOffset;
                Draw.draw(outlineZ, () -> {
                    AxDrawf.materialize(finalX, finalY, currentOutline, teamColor, matRotation, 0.1f, buildFraction, timeParam);
                });
            }

            Draw.draw(baseZ, () -> {
                AxDrawf.materialize(
                        finalX,
                        finalY,
                        currentIcon,
                        teamColor,
                        matRotation,
                        0.1f,
                        buildFraction,
                        timeParam
                );
            });

            if (part.children != null && part.children.size > 0) {
                DrawPart.PartParams childParam = new DrawPart.PartParams();
                childParam.set(
                        params.warmup, params.reload, params.smoothReload, params.heat, params.recoil, params.charge,
                        finalX, finalY, mr * sign + fixedPlateRotation
                );
                childParam.sideMultiplier = params.sideMultiplier;
                childParam.life = params.life;
                childParam.sideOverride = s;

                for (var child : part.children) {
                    if (child instanceof RegionPart childRegion) {
                        drawChildMaterialize(unit, state, childRegion, childParam, buildFraction, timeParam, teamColor);
                    } else {
                        child.draw(childParam);
                    }
                }
            }
        }
    }



    protected void drawChildMaterialize(Unit unit, ShieldState state, RegionPart child, mindustry.entities.part.DrawPart.PartParams cParams, float buildFraction, float timeParam, Color teamColor) {
        TextureRegion childIcon = child.regions != null && child.regions.length > 0 ? child.regions[0] : null;
        if (childIcon == null) return;

        float z = child.layer > 0 ? child.layer : Layer.groundUnit + 1.05f;

        float prog = child.progress.getClamp(cParams, child.clampProgress);
        float mx = child.moveX * prog;
        float my = child.moveY * prog;
        float mr = child.moveRot * prog + child.rotation;

        int len = child.mirror && cParams.sideOverride == -1 ? 2 : 1;

        for (int s = 0; s < len; s++) {
            int i = cParams.sideOverride == -1 ? s : cParams.sideOverride;
            float sign = (i == 0 ? 1f : -1f) * cParams.sideMultiplier;

            TextureRegion sideIcon = child.regions[Math.min(i, child.regions.length - 1)];
            TextureRegion sideOutline = (child.outline && child.outlines != null && child.outlines.length > 0)
                    ? child.outlines[Math.min(i, child.outlines.length - 1)]
                    : null;

            Tmp.v1.set((child.x + mx) * sign, child.y + my).rotateRadExact((cParams.rotation - 90) * arc.math.Mathf.degRad);

            final float finalX = cParams.x + Tmp.v1.x;
            final float finalY = cParams.y + Tmp.v1.y;
            final float matRotation = mr * sign + cParams.rotation - 90f;

            final TextureRegion finalIcon = sideIcon;
            final TextureRegion finalOutline = sideOutline;

            if (finalOutline != null) {
                float outlineZ = z + child.outlineLayerOffset;
                Draw.draw(outlineZ, () -> {
                    AxDrawf.materialize(finalX, finalY, finalOutline, teamColor, matRotation, 0.1f, buildFraction, timeParam);
                });
            }

            Draw.draw(z, () -> {
                AxDrawf.materialize(finalX, finalY, finalIcon, teamColor, matRotation, 0.1f, buildFraction, timeParam);
            });
        }
    }






    @Override
    public void displayBars(Unit unit, Table bars) {
        if (plateIndex == 0) {
            bars.add(new ShieldPlatesBar(unit)).row();
        }
    }


    /** Returns this plate's persistent state for the given unit, creating it if absent. */
    public ShieldState stateFor(Unit unit) {
        int k = key(unit.id, plateIndex);
        ShieldState state = plateStates.get(k);
        if (state == null) {
            state = new ShieldState();
            state.durability = maxDurability;
            state.smoothRebuildProgress = 1f;
            state.spawnedAt = Time.time;
            plateStates.put(k, state);
        }
        return state;
    }

    /** Returns the shared per-unit tracking record, creating it if absent. */
    protected static UnitTrack trackFor(Unit unit) {
        UnitTrack track = unitTracks.get(unit.id);
        if (track == null) {
            track = new UnitTrack();
            track.lastHealth = unit.health;
            track.lastFrame = -1f;
            unitTracks.put(unit.id, track);
        }
        return track;
    }

    /** All ShieldPlateAbility instances currently on the given unit, in list order. */
    protected static Seq<ShieldPlateAbility> allPlates(Unit unit) {
        Seq<ShieldPlateAbility> out = new Seq<>();
        for (mindustry.entities.abilities.Ability a : unit.abilities) {
            if (a instanceof ShieldPlateAbility sp) out.add(sp);
        }
        return out;
    }

    /** The lowest-plateIndex plate on this unit that still has durability remaining, or null if all are depleted. Used only by the non-directional passive fallback. */
    protected static ShieldPlateAbility lowestActivePlate(Unit unit) {
        ShieldPlateAbility best = null;
        for (ShieldPlateAbility plate : allPlates(unit)) {
            ShieldState s = plate.stateFor(unit);
            if (s.durability > 0f && (best == null || plate.plateIndex < best.plateIndex)) {
                best = plate;
            }
        }
        return best;
    }

    /**
     * Global damage hook - register once with
     * {@code Events.on(EventType.UnitDamageEvent.class, ShieldPlateAbility::onUnitDamage);}
     * inside your ModLoader's init
     * and every bullet in the mod (and any vanilla bullet) should respect shield plates automatically,
     */
    public static void onUnitDamage(EventType.UnitDamageEvent event) {
        Unit unit = event.unit;
        Bullet bullet = event.bullet;
        if (bullet == null) return;
        if (allPlates(unit).isEmpty()) return;

        float dealt = bullet.damage;
        if (dealt <= 0.001f) return;

        boolean pierce = bullet.type != null && bullet.type.pierce;
        float shouldRemain = computeAbsorb(unit, bullet, dealt, pierce);
        float refund = dealt - shouldRemain;

        if (refund > 0.001f) {
            unit.health = Math.min(unit.maxHealth, unit.health + refund);
        }

        UnitTrack track = trackFor(unit);
        track.lastHealth = unit.health;
        track.lastFrame = Time.time;
    }

    /** Pure computation used by onUnitDamage() - mutates plate durability, returns the portion that should reach the unit. Does not touch UnitTrack. */
    protected static float computeAbsorb(Unit unit, Bullet bullet, float incomingDamage, boolean pierce) {
        Seq<ShieldPlateAbility> plates = allPlates(unit);
        if (plates.isEmpty()) return incomingDamage;

        plates.sort((a, b) -> Integer.compare(a.plateIndex, b.plateIndex));

        float hitAngle = Angles.angle(unit.x, unit.y, bullet.x, bullet.y);
        float relativeAngle = Mathf.mod(hitAngle - unit.rotation, 360f);

        ShieldPlateAbility direct = null;
        for (ShieldPlateAbility p : plates) {
            ShieldState s = p.stateFor(unit);
            if (s.durability <= 0f) continue;
            if (Angles.angleDist(relativeAngle, p.facingAngle) <= p.coverageArc / 2f) {
                direct = p;
                break;
            }
        }

        if (direct != null) {
            return applySplit(unit, direct, incomingDamage, pierce);
        }

        boolean bigBullet = bullet.type != null && bullet.type.hitSize >= bigBulletSizeThreshold;
        if (!bigBullet) return incomingDamage;

        Seq<ShieldPlateAbility> nearby = new Seq<>();
        for (ShieldPlateAbility p : plates) {
            ShieldState s = p.stateFor(unit);
            if (s.durability <= 0f) continue;
            if (Angles.angleDist(relativeAngle, p.facingAngle) <= p.gapAssistRange) nearby.add(p);
        }
        nearby.sort((a, b) -> Float.compare(
                Angles.angleDist(relativeAngle, a.facingAngle),
                Angles.angleDist(relativeAngle, b.facingAngle)
        ));

        if (nearby.size >= 2) {
            float half = incomingDamage / 2f;
            float r1 = applySplit(unit, nearby.get(0), half, pierce);
            float r2 = applySplit(unit, nearby.get(1), half, pierce);
            return r1 + r2;
        } else if (nearby.size == 1) {
            ShieldPlateAbility p = nearby.first();
            float shieldPortion = incomingDamage * p.gapAbsorbRatio;
            float unitPortion = incomingDamage - shieldPortion;
            return unitPortion + applySplit(unit, p, shieldPortion, pierce);
        } else {
            return incomingDamage;
        }
    }

    /** Applies a damage amount against a single plate, respecting pierce, and returns the leftover that spills to the unit. */
    protected static float applySplit(Unit unit, ShieldPlateAbility plate, float amount, boolean pierce) {
        ShieldState state = plate.stateFor(unit);
        float shieldShare = pierce ? amount * plate.pierceAbsorbRatio : amount;
        float unitDirectShare = amount - shieldShare;

        float absorbed = Math.min(shieldShare, state.durability);
        state.durability -= absorbed;
        state.hitImpulse = 1f;
        state.lastHitTime = Time.time;

        float spill = shieldShare - absorbed;

        if (state.durability <= 0f && plate.canRebuild) {
            Tmp.v1.set(plate.part.x, plate.part.y).rotate(unit.rotation);
            float worldX = unit.x + Tmp.v1.x;
            float worldY = unit.y + Tmp.v1.y;

            // FIX: Trigger your customizable destroySound directly at the exact split coordinate!
            if (plate.destroySound != null) {
                plate.destroySound.at(worldX, worldY);
            }

            // Standard generic explosion effect fallback
            mindustry.content.Fx.smoke.at(worldX, worldY);

            plate.beginRebuild(state);
        }

        return unitDirectShare + spill;
    }



    protected static int key(int unitId, int plateIndex) {
        return (unitId << 8) | (plateIndex & 0xFF);
    }


    /**
     * Global plate save hook
     * Create a save state file if you haven't already
     * {@code
     * public class MyWorldState {
     *     public static void load() {
     *         SaveVersion.addCustomChunk("plateShields", new SaveVersion.CustomChunk(){
     *                   @Override
     *                   public void write(DataOutput stream) throws IOException {
     *                       ShieldPlateAbility.writeSaveData(stream);
     *                   }
     *                   @Override
     *                   public void read(DataInput stream) throws IOException {
     *                      ShieldPlateAbility.readSaveData(stream);
     *                   }
     *               });
     *     }
     * }}
     * then inside your ModLoader's loadContent() above any other .load()
     * {@code MyWorldState.load();}
     * so that any shield plate auto saves durability and build progress,
     */
    public static void writeSaveData(DataOutput stream) throws IOException {
        stream.writeInt(plateStates.size);
        for (IntMap.Entry<ShieldState> entry : plateStates.entries()) {
            int unitId = entry.key >> 8;
            int plateIdx = entry.key & 0xFF;
            ShieldState s = entry.value;
            stream.writeInt(unitId);
            stream.writeInt(plateIdx);
            stream.writeFloat(s.durability);
            stream.writeBoolean(s.underConstruction);
            stream.writeBoolean(s.building);
            stream.writeBoolean(s.pausedForCombat);
            stream.writeFloat(s.delayRemaining);
            stream.writeFloat(s.buildRemaining);
            stream.writeFloat(s.smoothRebuildProgress);
            stream.writeFloat(Time.time - s.spawnedAt);
        }
    }

    public static void readSaveData(DataInput stream) throws IOException {
        plateStates.clear();
        unitTracks.clear();

        int count = stream.readInt();
        for (int i = 0; i < count; i++) {
            int unitId = stream.readInt();
            int plateIdx = stream.readInt();

            ShieldState s = new ShieldState();
            s.durability = stream.readFloat();
            s.underConstruction = stream.readBoolean();
            s.building = stream.readBoolean();
            s.pausedForCombat = stream.readBoolean();
            s.delayRemaining = stream.readFloat();
            s.buildRemaining = stream.readFloat();
            s.smoothRebuildProgress = stream.readFloat();
            float elapsed = stream.readFloat();
            s.spawnedAt = Time.time - elapsed;

            plateStates.put(key(unitId, plateIdx), s);
        }
    }

    /** Persistent state for a single plate on a single unit. */
    public static class ShieldState {
        public float durability;
        public boolean underConstruction;
        boolean building;
        boolean pausedForCombat;
        boolean combatBlocking;
        float delayRemaining;
        public float buildRemaining;
        float smoothRebuildProgress;
        float hitImpulse;
        float spawnedAt;
        public float lastHitTime = -999f;
    }

    /** Shared per-unit bookkeeping used by the plateIndex == 0 coordinator. */
    protected static class UnitTrack {
        float lastHealth;
        float lastFrame;
    }
}