package axthrix.world.types.unittypes;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Nullable;
import axthrix.content.AxFactions;
import axthrix.world.types.ai.JormungandrAi;
import axthrix.world.types.block.defense.JormungandrNest;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.part.DrawPart;
import mindustry.gen.Unit;
import mindustry.graphics.MultiPacker;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.entities.units.WeaponMount;
import mindustry.world.Block;
import mindustry.world.Tile;

import java.util.HashMap;

import static arc.math.Mathf.dst;

public class JormungandrUnitType extends AxUnitType {

    // ---- lifecycle ----
    @Nullable public JormungandrUnitType nextStage = null;
    public boolean finalStage = false;
    public boolean isHatchling = false;

    // ---- nest ----
    public boolean canNest = true;
    @Nullable public Block nestBlock = null;
    public float resourceProgressValue = 60f;

    // ---- egg laying ----
    public int eggBatchCount = 3;
    public float eggSpread = 40f;
    @Nullable public Block eggBlock = null;

    // ---- resource ----
    public Item nestResource = Items.copper;
    public float nestSeekRange = 200f;

    // ---- combat ----
    public float lungeRange = 80f;

    // ---- rendering ----
    /**
     * Number of body segments. Drives sprite loading:
     *   {name}-segment0 ... {name}-segment{N-2} = body
     *   {name}-segment{N-1}                     = tail
     * Outlines: {name}-segment-outline0 ... {name}-segment-outline{N-1}
     */
    public int bodySegments = 5;

    /** World-unit distance between each segment at scale 1.0. */
    public float segmentSpacing = 6f;

    /** Scale at stageTick=0. */
    public float minScale = 0.6f;

    /** Scale at stageTick=stageTime. */
    public float maxScale = 1.0f;

    // ---- segment parts ----
    /**
     * Parts to draw at specific segment positions.
     * Each entry maps a segment index to a DrawPart.
     * The part's x/y offsets are relative to the segment's world position and rotation.
     */
    public Seq<SegmentPart> segmentParts = new Seq<>();

    /** Binds a DrawPart to a specific segment index. */
    public static class SegmentPart {
        public int segmentIndex;
        public DrawPart part;

        public SegmentPart(int segmentIndex, DrawPart part) {
            this.segmentIndex = segmentIndex;
            this.part = part;
        }
    }

    // Loaded in load()
    public TextureRegion headOutlineRegion;
    public TextureRegion[] segmentRegions;
    public TextureRegion[] segmentOutlineRegions;

    // ---- stage timing ----
    public float stageTime = 1800f;
    public float nestAbandonFraction = 0.95f;

    // ---- needs ----
    public float maxNeeds = 1000f;
    public float needsDrainRate = 0.5f;
    public float needsDrainRateResting = 0.2f;
    /** Needs drained per tick while actively building a nest (much lower — snake is focused). */
    public float needsDrainRateBuilding = 0.05f;
    public float needsRestoreAmount = 300f;
    public float lowNeedsThreshold = 0.2f;
    public float criticalNeedsThreshold = 0.1f;
    public float needsDamageRate = 0.3f;
    public float needsSpeedMult = 0.5f;
    public float restHealRate = 0.5f;
    public Seq<Block> foodBlocks = new Seq<>();
    public float foodSeekRange = 240f;
    public boolean huntsUnits = false;
    public Seq<UnitType> preyTypes = new Seq<>();

    // ---- segment growth ----
    /**
     * If true, segments are hidden at spawn and revealed as the unit ages.
     * Configure via segmentGrowthMap.
     */
    public boolean growSegments = false;

    /**
     * Each entry is float[]{segmentIndex, lifeThreshold}.
     * Segment at segmentIndex is hidden until stageTick/stageTime >= lifeThreshold.
     * Segments not listed are always visible.
     * Example: new float[]{12, 0.2f} hides segment 12 until 20% of life has passed.
     */
    public Seq<float[]> segmentGrowthMap = new Seq<>();

    // ---- age-up coil ----
    /** Ticks the unit stays coiled before spawning the next stage and dying. */
    public float agingCoilDuration = 120f;

    /**
     * Probability (0–1) that this unit advances to nextStage when its time is up.
     * -1 = always advances (default). 0 = never advances. 0.15 = 15% chance.
     * Useful for making rare stages like the elder — set on the adult stage.
     */
    public float chanceToAge = -1f;

    // ---- brooding ----
    /** Ticks between each egg batch. Only active when eggBlock is set. */
    public float broodInterval = 1200f;

    /**
     * Fraction of stageTime at which brooding begins.
     * e.g. 0.4f = snake starts laying eggs at 40% of its life.
     */
    public float broodStartFraction = 0.4f;

    public final HashMap<Unit, Float> broodTimer = new HashMap<>();
    public final HashMap<Unit, Integer> broodBatchesLeft = new HashMap<>();

    // ---- per-unit state ----
    public final HashMap<Unit, Float> stageTick = new HashMap<>();
    public final HashMap<Unit, JormungandrNest.JormungandrNestBuild> activeBuild = new HashMap<>();
    public final HashMap<Unit, Boolean> buildingNest = new HashMap<>();
    public final HashMap<Unit, Boolean> nestDestroyed = new HashMap<>();
    public final HashMap<Unit, Float> needs = new HashMap<>();
    public final HashMap<Unit, Boolean> resting = new HashMap<>();
    public final HashMap<Unit, float[]> posHistory = new HashMap<>();
    public final HashMap<Unit, Integer> posHistoryHead = new HashMap<>();

    /**
     * Cached world positions of each segment from the last draw call.
     * float[] layout: [x0, y0, angle0, x1, y1, angle1, ...]
     * Used as fallback when the unit is stationary so segments don't snap
     * when the head rotates.
     */
    public final HashMap<Unit, float[]> lastSegPos = new HashMap<>();
    /** False until the spawn-coil cache has been translated to world coordinates. */
    public final HashMap<Unit, Boolean> segPosAnchored = new HashMap<>();

    /** True when the unit has finished its stage and is coiling up to age. */
    public final HashMap<Unit, Boolean> agingUp = new HashMap<>();

    /** Ticks elapsed in the aging coil phase. */
    public final HashMap<Unit, Float> agingTick = new HashMap<>();

    /** World position where the aging coil is centered. */
    public final HashMap<Unit, float[]> agingCoilPos = new HashMap<>();


    public JormungandrUnitType(String name) {
        super(name);
        factions.add(AxFactions.ikatusa);
        controller = u -> new JormungandrAi();
        drawBody = false;
        drawCell = false;
    }


    @Override
    public void load() {
        super.load();
        headOutlineRegion = Core.atlas.find(name + "-outline", (TextureRegion) null);

        segmentRegions = new TextureRegion[bodySegments];
        segmentOutlineRegions = new TextureRegion[bodySegments];
        for (int i = 0; i < bodySegments; i++) {
            segmentRegions[i] = Core.atlas.find(name + "-segment" + i, region);
            segmentOutlineRegions[i] = Core.atlas.find(name + "-segment-outline" + i, (TextureRegion) null);
        }
        for (SegmentPart sp : segmentParts) {
            sp.part.load(name);
        }
        clipSize = Math.max(clipSize, bodySegments * segmentSpacing * maxScale * 2f);
    }

    @Override
    public Unit create(mindustry.game.Team team) {
        Unit unit = super.create(team);
        float scale = minScale;
        float spacing = segmentSpacing * scale;
        float[] nc = new float[bodySegments * 3];
        float loops = 1.5f;
        float maxR = spacing * (bodySegments / (Mathf.PI2 * loops));
        for (int s = 0; s < bodySegments; s++) {
            float frac = (float) s / bodySegments;
            float a = frac * Mathf.PI2 * loops;
            float r = frac * maxR;
            nc[s * 3]     = Mathf.cos(a) * r;
            nc[s * 3 + 1] = Mathf.sin(a) * r;
            nc[s * 3 + 2] = a * Mathf.radDeg + 90f;
        }
        lastSegPos.put(unit, nc);
        segPosAnchored.put(unit, false);
        return unit;
    }

    /**
     * Called by update() on each brood interval.
     * Tells the AI to lay one egg batch via the existing EggLayState machine.
     */
    public void triggerBroodBatch(Unit unit) {
        if (unit.controller() instanceof JormungandrAi ai) {
            if (ai.eggLayState == JormungandrAi.EggLayState.INACTIVE
                    || ai.eggLayState == JormungandrAi.EggLayState.DONE) {
                ai.eggsLeft = 1;
                ai.eggLayState = JormungandrAi.EggLayState.FIND_TILE;
            }
        }
    }

    @Override
    public void createIcons(MultiPacker packer) {
        super.createIcons(packer);
        if (!outlines) return;

        for (int i = 0; i < bodySegments; i++) {
            TextureRegion seg = Core.atlas.find(name + "-segment" + i);
            if (seg != null && seg.found()) {
                makeOutline(packer, seg, name + "-segment-outline" + i, outlineColor, outlineRadius);
            }
        }
    }


    @Override
    public void update(Unit unit) {
        super.update(unit);

        stageTick.putIfAbsent(unit, 0f);
        buildingNest.putIfAbsent(unit, false);
        needs.putIfAbsent(unit, maxNeeds);
        resting.putIfAbsent(unit, false);
        int capacity = historyCapacity();
        float[] hist = posHistory.computeIfAbsent(unit, u -> new float[capacity * 3]);
        int head = posHistoryHead.getOrDefault(unit, 0);
        int prevIdx = ((head - 1) % capacity + capacity) % capacity;
        float prevX = hist[prevIdx * 3];
        float prevY = hist[prevIdx * 3 + 1];
        float prevCumDist = hist[prevIdx * 3 + 2];

        float stepDist = dst(unit.x, unit.y, prevX, prevY);
        hist[head * 3]     = unit.x;
        hist[head * 3 + 1] = unit.y;
        hist[head * 3 + 2] = prevCumDist + stepDist;
        posHistoryHead.put(unit, (head + 1) % capacity);

        float[] segCache = lastSegPos.get(unit);
        if (segCache != null) {
            int visSegs = getVisibleSegments(unit);
            float segHitRadius = hitSize * 0.5f * currentScale(unit);
            mindustry.gen.Groups.unit.intersect(
                    unit.x - bodySegments * segmentSpacing, unit.y - bodySegments * segmentSpacing,
                    bodySegments * segmentSpacing * 2f, bodySegments * segmentSpacing * 2f,
                    other -> {
                        if (other == unit || other.team == unit.team || other.isFlying()) return;
                        for (int si = 0; si < visSegs; si++) {
                            float sx = segCache[si * 3];
                            float sy = segCache[si * 3 + 1];
                            float dx = other.x - sx;
                            float dy = other.y - sy;
                            float len = Mathf.len(dx, dy);
                            float minLen = segHitRadius + other.hitSize * 0.5f;
                            if (len < minLen) {
                                if (len == 0f) { dx = 0.1f; len = 0.1f; }
                                dx /= len; dy /= len;
                                float push = (minLen - len) + 0.2f;
                                other.vel.add(dx * push * 0.1f, dy * push * 0.1f);
                                other.x += dx * push;
                                other.y += dy * push;
                            }
                        }
                    }
            );
        }
        boolean isResting = resting.getOrDefault(unit, false);
        boolean isBuilding = buildingNest.getOrDefault(unit, false);
        float drain = isResting ? needsDrainRateResting
                : isBuilding ? needsDrainRateBuilding
                : needsDrainRate;
        float currentNeeds = Math.max(0f, needs.get(unit) - drain);
        needs.put(unit, currentNeeds);

        if (currentNeeds / maxNeeds < criticalNeedsThreshold) {
            unit.damage(needsDamageRate);
        }

        if (isResting && unit.health < unit.maxHealth) {
            unit.heal(restHealRate);
        }

        if (activeBuild.containsKey(unit)) {
            JormungandrNest.JormungandrNestBuild nb = activeBuild.get(unit);
            if (nb == null || !nb.isAdded()) {
                activeBuild.remove(unit);
                buildingNest.put(unit, false);
                nestDestroyed.put(unit, true);
                resting.put(unit, false);
            } else if (nb.healthf() >= 1f && !nb.drawCoiled) {
                finishNest(unit, nb);
            }
        }

        float ticks = stageTick.get(unit);

        if (agingUp.getOrDefault(unit, false)) {
            float at = agingTick.getOrDefault(unit, 0f) + 1f;
            agingTick.put(unit, at);
            unit.vel.setZero();
            if (at >= agingCoilDuration) {

                boolean ages = nextStage == null || chanceToAge < 0f || Mathf.random() < chanceToAge;
                if (nextStage != null && ages) {
                    Unit u = nextStage.create(unit.team);
                    float[] cp = agingCoilPos.getOrDefault(unit, new float[]{unit.x, unit.y});
                    u.set(cp[0], cp[1]);
                    u.rotation = unit.rotation();

                    if (u.type instanceof JormungandrUnitType nt) {
                        float scale = nt.minScale;
                        float spacing = nt.segmentSpacing * scale;
                        float[] nc = new float[nt.bodySegments * 3];
                        float loops = 1.5f;
                        float maxR = spacing * (nt.bodySegments / (Mathf.PI2 * loops));
                        for (int s = 0; s < nt.bodySegments; s++) {
                            float frac = (float) s / nt.bodySegments;
                            float a = frac * Mathf.PI2 * loops;
                            float r = frac * maxR;
                            nc[s * 3]     = cp[0] + Mathf.cos(a) * r;
                            nc[s * 3 + 1] = cp[1] + Mathf.sin(a) * r;
                            nc[s * 3 + 2] = a * Mathf.radDeg + 90f;
                        }
                        nt.lastSegPos.put(u, nc);
                    }
                    if (!Vars.net.client()) u.add();
                }
                fullCleanup(unit);
                return;
            }

            return;
        }

        if (ticks >= stageTime) {

            agingUp.put(unit, true);
            agingTick.put(unit, 0f);
            agingCoilPos.put(unit, new float[]{unit.x, unit.y});
            resting.put(unit, true); // forces coil draw
            return;
        }

        stageTick.put(unit, ticks + 1f);


        if (eggBlock != null && ticks / stageTime >= broodStartFraction) {
            broodBatchesLeft.putIfAbsent(unit, eggBatchCount);
            broodTimer.merge(unit, 1f, Float::sum);
            int left = broodBatchesLeft.getOrDefault(unit, 0);
            if (left > 0 && broodTimer.getOrDefault(unit, 0f) >= broodInterval) {
                broodTimer.put(unit, 0f);
                triggerBroodBatch(unit);
                broodBatchesLeft.put(unit, left - 1);
            }
        }
    }


    @Override
    public void draw(Unit unit) {
        if (unit.inFogTo(Vars.player.team())) return;

        float scale = currentScale(unit);
        boolean isAging = agingUp.getOrDefault(unit, false);
        boolean coiled = isAging || (resting.getOrDefault(unit, false) && isPhysicallyAtNest(unit));

        float z = unit.elevation > 0.5f || flying ? flyingLayer : groundLayer + Mathf.clamp(hitSize / 4000f, 0f, 0.01f);

        float drawX = unit.x, drawY = unit.y;
        if (isAging) {
            float[] cp = agingCoilPos.get(unit);
            if (cp != null) { drawX = cp[0]; drawY = cp[1]; }
        } else if (coiled) {
            JormungandrAi ai = getAi(unit);
            if (ai != null && ai.nestSiteChosen) {
                Tile nt = Vars.world.tileWorld(ai.nestSiteX, ai.nestSiteY);
                if (nt != null) { drawX = nt.worldx(); drawY = nt.worldy(); }
            }
        }

        if (unit.isFlying() || shadowElevation > 0f) {
            Draw.z(Math.min(80f, z - 1f));
            drawShadow(unit);
        }

        Draw.z(z - 0.02f);
        if (drawSoftShadow) drawSoftShadow(unit);

        Draw.z(z - 0.01f);
        if (coiled) {
            drawCoiled(unit, scale);
        } else {
            drawSlithering(unit, scale);
        }


        Draw.z(z);
        applyColor(unit);
        applyOutlineColor(unit);
        for (WeaponMount mount : unit.mounts) {
            if (!mount.weapon.top) {
                float wz = Draw.z();
                Draw.z(wz + mount.weapon.layerOffset);
                mount.weapon.drawOutline(unit, mount);
                Draw.z(wz);
            }
        }
        Draw.reset();


        Draw.z(z);
        if (headOutlineRegion != null && headOutlineRegion.found()) {
            applyColor(unit);
            applyOutlineColor(unit);
            Draw.scl(scale, scale);
            Draw.rect(headOutlineRegion, drawX, drawY, unit.rotation - 90f);
            Draw.scl(1f, 1f);
            Draw.reset();
        }


        Draw.z(z);
        applyColor(unit);
        Draw.scl(scale, scale);
        Draw.rect(region, drawX, drawY, unit.rotation - 90f);
        Draw.scl(1f, 1f);
        Draw.reset();


        if (drawCell) {
            drawCell(unit);
        }


        if (engineLayer > 0f) Draw.z(engineLayer);
        if (engines.size > 0) drawEngines(unit);


        Draw.z(z);
        applyColor(unit);
        Draw.scl(scale, scale);
        for (WeaponMount mount : unit.mounts) {
            mount.weapon.draw(unit, mount);
        }
        Draw.scl(1f, 1f);
        Draw.reset();


        if (drawItems) drawItems(unit);


        if (parts.size > 0) {
            Draw.z(z);
            Draw.scl(scale, scale);
            for (int i = 0; i < parts.size; i++) {
                DrawPart part = parts.get(i);
                WeaponMount mount = unit.mounts.length > part.weaponIndex ? unit.mounts[part.weaponIndex] : null;
                if (mount != null) {
                    DrawPart.params.set(mount.warmup, mount.reload / mount.weapon.reload,
                            mount.smoothReload, mount.heat, mount.recoil, mount.charge,
                            unit.x, unit.y, unit.rotation);
                } else {
                    DrawPart.params.set(0f, 0f, 0f, 0f, 0f, 0f, unit.x, unit.y, unit.rotation);
                }
                applyColor(unit);
                part.draw(DrawPart.params);
            }
            Draw.scl(1f, 1f);
            Draw.reset();
        }


        for (var ability : unit.abilities) {
            Draw.reset();
            ability.draw(unit);
        }

        drawLight(unit);
        if (unit.shieldAlpha > 0f && drawShields) drawShield(unit);

        Draw.reset();
    }



    protected void drawSlithering(Unit unit, float scale) {
        float[] hist = posHistory.get(unit);
        if (hist == null || segmentRegions == null) return;

        int capacity = historyCapacity();
        int head = posHistoryHead.getOrDefault(unit, 0);
        float scaledSpacing = segmentSpacing * scale;
        int visibleSegs = getVisibleSegments(unit);


        float[] cached = lastSegPos.computeIfAbsent(unit, u -> {
            float[] arr = new float[bodySegments * 3];
            for (int s = 0; s < bodySegments; s++) {
                arr[s * 3]     = unit.x - Mathf.cosDeg(unit.rotation) * scaledSpacing * (s + 1);
                arr[s * 3 + 1] = unit.y - Mathf.sinDeg(unit.rotation) * scaledSpacing * (s + 1);
                arr[s * 3 + 2] = unit.rotation;
            }
            return arr;
        });


        if (!segPosAnchored.getOrDefault(unit, true)) {
            for (int s = 0; s < bodySegments; s++) {
                cached[s * 3]     += unit.x;
                cached[s * 3 + 1] += unit.y;
            }
            segPosAnchored.put(unit, true);
        }

        int headIdx = ((head - 1) % capacity + capacity) % capacity;
        float headCumDist = hist[headIdx * 3 + 2];


        for (int seg = 0; seg < visibleSegs; seg++) {
            float targetDist = headCumDist - scaledSpacing * (seg + 1);
            if (targetDist < 0f) {
                drawSegment(unit, seg, cached[seg * 3], cached[seg * 3 + 1], cached[seg * 3 + 2], scale);
                continue;
            }

            float sx = cached[seg * 3], sy = cached[seg * 3 + 1], angle = cached[seg * 3 + 2];

            for (int i = 1; i < capacity; i++) {
                int idxA = ((head - i)     % capacity + capacity) % capacity;
                int idxB = ((head - i - 1) % capacity + capacity) % capacity;
                float cdA = hist[idxA * 3 + 2];
                float cdB = hist[idxB * 3 + 2];

                if (cdB > cdA) break;

                if (cdA >= targetDist && cdB <= targetDist) {
                    float span = cdA - cdB;
                    float t = span > 0.001f ? (targetDist - cdB) / span : 0f;
                    sx = Mathf.lerp(hist[idxB * 3], hist[idxA * 3], t);
                    sy = Mathf.lerp(hist[idxB * 3 + 1], hist[idxA * 3 + 1], t);
                    float ax = hist[idxA * 3] - hist[idxB * 3];
                    float ay = hist[idxA * 3 + 1] - hist[idxB * 3 + 1];
                    angle = (ax * ax + ay * ay > 0.001f) ? Mathf.angle(ax, ay) : cached[seg * 3 + 2];
                    break;
                }
            }

            cached[seg * 3]     = sx;
            cached[seg * 3 + 1] = sy;
            cached[seg * 3 + 2] = angle;

            drawSegment(unit, seg, sx, sy, angle + 180, scale);
        }
    }

    protected void drawSegment(Unit unit, int index, float sx, float sy, float angle, float scale) {
        TextureRegion seg = segmentRegions[index];
        TextureRegion segOutline = segmentOutlineRegions[index];

        Draw.scl(scale, scale);

        if (segOutline != null && segOutline.found()) {
            applyOutlineColor(unit);
            Draw.rect(segOutline, sx, sy, angle + 90f);
        }

        applyColor(unit);
        Draw.rect(seg, sx, sy, angle + 90f);

        Draw.scl(1f, 1f);
        Draw.reset();

        if (!segmentParts.isEmpty()) {
            for (SegmentPart sp : segmentParts) {
                if (sp.segmentIndex != index) continue;
                DrawPart.params.set(0f, 0f, 0f, 0f, 0f, 0f, sx, sy, angle + 90f);
                Draw.scl(scale, scale);
                applyColor(unit);
                sp.part.draw(DrawPart.params);
                Draw.scl(1f, 1f);
                Draw.reset();
            }
        }
    }
    protected void drawCoiled(Unit unit, float scale) {
        if (segmentRegions == null) return;

        float cx = unit.x, cy = unit.y;
        float[] agingPos = agingCoilPos.get(unit);
        if (agingPos != null) {
            cx = agingPos[0]; cy = agingPos[1];
        } else {
            JormungandrAi ai = getAi(unit);
            if (ai != null && ai.nestSiteChosen) {
                Tile t = Vars.world.tileWorld(ai.nestSiteX, ai.nestSiteY);
                if (t != null) { cx = t.worldx(); cy = t.worldy(); }
            }
        }

        int total = getVisibleSegments(unit);
        float loopsInCoil = 1.5f;
        float scaledSpacing = segmentSpacing * scale;
        float maxRadius = scaledSpacing * (total / (Mathf.PI2 * loopsInCoil));

        float coilT = agingPos != null
                ? Mathf.clamp(agingTick.getOrDefault(unit, 0f) / agingCoilDuration)
                : 1f;

        float[] cached = lastSegPos.get(unit);

        for (int i = 0; i < total; i++) {
            float frac = (float) i / total;
            float angle = frac * Mathf.PI2 * loopsInCoil;
            float radius = frac * maxRadius;
            float targetX = cx + Mathf.cos(angle) * radius;
            float targetY = cy + Mathf.sin(angle) * radius;
            float targetAngle = angle * Mathf.radDeg + 90f;

            float sx, sy, drawAngle;
            if (coilT >= 1f || cached == null || i * 3 + 2 >= cached.length) {
                sx = targetX; sy = targetY; drawAngle = targetAngle;
            } else {
                sx = Mathf.lerp(cached[i * 3], targetX, coilT);
                sy = Mathf.lerp(cached[i * 3 + 1], targetY, coilT);
                drawAngle = Mathf.slerpDelta(cached[i * 3 + 2], targetAngle, coilT);
            }

            drawSegment(unit, i, sx, sy, drawAngle+180, scale);
        }
    }

    @Nullable
    protected JormungandrAi getAi(Unit unit) {
        if (unit.controller() instanceof JormungandrAi ai) return ai;
        return null;
    }

    /** Capacity of the position history ring buffer.
     *  Each entry is 3 floats: x, y, cumulativeDistance.
     *  Sized so even the slowest unit has enough path history to place every segment. */
    protected int historyCapacity() {
        return (int)((bodySegments * segmentSpacing * maxScale) / 0.05f) + 100;
    }

    /** True only when the unit is close enough to the nest center to visually coil. */
    public boolean isPhysicallyAtNest(Unit unit) {
        JormungandrAi ai = getAi(unit);
        if (ai == null || !ai.nestSiteChosen) return false;
        Tile t = Vars.world.tileWorld(ai.nestSiteX, ai.nestSiteY);
        if (t == null) return false;
        return unit.within(t.worldx(), t.worldy(), 12f);
    }

    public float currentScale(Unit unit) {
        float t = stageTick.getOrDefault(unit, 0f) / stageTime;
        return Mathf.lerp(minScale, maxScale, Mathf.clamp(t));
    }

    /**
     * Returns the number of segments currently visible for this unit.
     * When growSegments=false, always returns bodySegments.
     * When growSegments=true, segments listed in segmentGrowthMap are hidden
     * until the corresponding life threshold is reached.
     */
    public int getVisibleSegments(Unit unit) {
        if (!growSegments || segmentGrowthMap.isEmpty()) return bodySegments;
        float life = stageTick.getOrDefault(unit, 0f) / stageTime;
        int visible = bodySegments;
        for (float[] entry : segmentGrowthMap) {
            int segIdx = (int) entry[0];
            float threshold = entry[1];
            if (life < threshold && segIdx < visible) {
                visible = segIdx;
            }
        }
        return Math.max(1, visible);
    }


    public void startNest(Unit unit, float siteX, float siteY) {
        if (nestBlock == null) return;
        Tile center = Vars.world.tileWorld(siteX, siteY);
        if (center == null) return;

        int size = nestBlock.size;
        boolean isEven = (size % 2 == 0);
        int offset = isEven ? 1 : 0;

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int tx = (center.x + x - size / 2) + offset;
                int ty = (center.y + y - size / 2) + offset;
                Tile t = Vars.world.tile(tx, ty);
                if (t == null || t.build != null || t.block().solid) return;
            }
        }

        center.setNet(nestBlock, unit.team, (int) unit.rotation / 90);

        if (center.build instanceof JormungandrNest.JormungandrNestBuild nb) {
            nb.health = 1f;
            activeBuild.put(unit, nb);
            buildingNest.put(unit, true);
            if (isEven && unit.controller() instanceof JormungandrAi ai) {
                ai.nestSiteX = center.worldx();
                ai.nestSiteY = center.worldy();
            }
        }
    }

    public void addNestProgress(Unit unit, float amount) {
        JormungandrNest.JormungandrNestBuild nb = activeBuild.get(unit);
        if (nb == null || !nb.isAdded()) return;
        nb.heal(amount);
    }

    private void finishNest(Unit unit, JormungandrNest.JormungandrNestBuild nb) {
        nb.health = nb.maxHealth;
        nb.onSnakeNested(this, unit);
        activeBuild.remove(unit);
        buildingNest.put(unit, false);
    }

    // -----------------------------------------------------------------------
    //  eggs
    // -----------------------------------------------------------------------

    public void layEggAt(Unit unit, Tile tile) {
        if (eggBlock == null || tile == null) return;
        tile.setNet(eggBlock, unit.team, 0);
    }

    // -----------------------------------------------------------------------
    //  state helpers
    // -----------------------------------------------------------------------

    public boolean shouldAbandonNest(Unit unit) {
        return stageTick.getOrDefault(unit, 0f) / stageTime >= nestAbandonFraction;
    }

    public float getNeedsSpeedMult(Unit unit) {
        return (needs.getOrDefault(unit, maxNeeds) / maxNeeds < criticalNeedsThreshold) ? needsSpeedMult : 1f;
    }

    public float getNeedsFraction(Unit unit) {
        return needs.getOrDefault(unit, maxNeeds) / maxNeeds;
    }

    public void restoreNeeds(Unit unit, float amount) {
        needs.put(unit, Math.min(maxNeeds, needs.getOrDefault(unit, 0f) + amount));
    }

    public boolean isAgingUp(Unit unit) { return agingUp.getOrDefault(unit, false); }
    public void setResting(Unit unit, boolean value) { resting.put(unit, value); }
    public boolean hasActiveBuild(Unit unit) { return activeBuild.containsKey(unit); }

    public boolean pollNestDestroyed(Unit unit) {
        Boolean val = nestDestroyed.remove(unit);
        return val != null && val;
    }

    private void fullCleanup(Unit unit) {
        stageTick.remove(unit);
        buildingNest.remove(unit);
        activeBuild.remove(unit);
        nestDestroyed.remove(unit);
        needs.remove(unit);
        resting.remove(unit);
        posHistory.remove(unit);
        posHistoryHead.remove(unit);
        lastSegPos.remove(unit);
        segPosAnchored.remove(unit);
        agingUp.remove(unit);
        agingTick.remove(unit);
        agingCoilPos.remove(unit);
        broodTimer.remove(unit);
        broodBatchesLeft.remove(unit);
        unit.remove();
    }
}