package axthrix.world.types.weapontypes;

import arc.scene.ui.layout.Table;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import axthrix.world.types.perks.*;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.AxStatValues;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Styles;

/**
 * A weapon type that supports the modular perk system.
 * Uses the same static stateMap as PerkTurretType — keyed by unit ID + perk name.
 *
 * Hit/miss routing:
 *   In your bullet type's hit() override:
 *     if(b.owner instanceof Unit u) for(WeaponMount m : u.mounts) if(m.weapon instanceof PerkWeapon pw) pw.onHit(u, m, x, y, b.x, b.y);
 *   In your bullet type's despawned():
 *     if(b.data != Boolean.TRUE && b.owner instanceof Unit u) for(WeaponMount m : u.mounts) if(m.weapon instanceof PerkWeapon pw) pw.onMiss(u, m);
 */
public class PerkWeapon extends Weapon {

    public Seq<Perk> perks = new Seq<>();

    /**
     * Index into perks that drives AxPartParms.perkparams (part params 0 and 1).
     * Defaults to 0. Clamped to valid range automatically.
     */
    public int primaryPerkIndex = 0;

    // Tracks last-fired tick per unit for decay timer
    private final ObjectIntMap<Integer> unitFiredTick = new ObjectIntMap<>();

    public PerkWeapon(String name) {
        super(name);
    }

    public PerkWeapon() {
        super();
    }

    public PerkWeapon withPerk(Perk perk) {
        perks.add(perk);
        return this;
    }

    /** Gets PerkStateData for a given unit and perk using the unit's id as owner key. */
    public PerkStateData getState(Unit unit, Perk perk) {
        return Perk.getState(unit.id, perk.name);
    }

    @Override
    public void addStats(UnitType unit, Table t) {
        Table extra = new Table();
        extra.left().defaults().padRight(3).left();
        AxStatValues.appendCustomStats(extra, bullet);
        t.row();
        t.table(Styles.grayPanel, et -> et.add(extra).left().grow()).padLeft(5).padTop(0).padBottom(5).growX().margin(10);
        super.addStats(unit, t);
    }

    // ---- Update ----

    @Override
    public void update(Unit unit, WeaponMount mount) {
        super.update(unit, mount);

        int lastFired = unitFiredTick.get(unit.id, -1);
        boolean fired = lastFired == (int) arc.util.Time.time;

        for(Perk perk : perks) {
            PerkStateData s = getState(unit, perk);

            // RangePerk needs the weapon mount for target access
            if(perk instanceof RangePerk rp) {
                rp.updateForUnit(unit, mount, s);
            } else {
                perk.update(unit, null, s);
            }

            perk.tickTimer(unit, null, s, fired);

            // Apply unit-side buffs for non-special perks
            if(perk instanceof ShotBuffPerk sb && !sb.resetOnShot && s.currentStacks > 0) {
                perk.applyStackingBuffsToUnit(unit, s);
            } else if(perk instanceof DurationPerk dp && s.buffActive) {
                perk.applyFlatBuffsToUnit(unit, 1f);
            } else if(perk instanceof SpeedPerk sp) {
                // SpeedPerk handles its own application in update()
            }
        }
    }

    // ---- Shoot ----

    @Override
    public void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        super.shoot(unit, mount, shootX, shootY, rotation);
        unitFiredTick.put(unit.id, (int) arc.util.Time.time);

        for(Perk perk : perks) {
            PerkStateData s = getState(unit, perk);
            if(perk instanceof ShotBuffPerk sb && sb.resetOnShot) {
                sb.consumeShot(unit, null, s);
            }
        }
    }

    // ---- Hit / Miss Routing ----

    public void onHit(Unit unit, WeaponMount mount, float targetX, float targetY, float shooterX, float shooterY) {
        float dist = arc.math.Mathf.dst(shooterX, shooterY, targetX, targetY);
        for(Perk perk : perks) {
            perk.registerHit(unit, null, getState(unit, perk), targetX, targetY, dist);
        }
    }

    public void onMiss(Unit unit, WeaponMount mount) {
        for(Perk perk : perks) {
            perk.registerMiss(unit, null, getState(unit, perk));
        }
    }

    // ---- Draw ----

    @Override
    public void draw(Unit unit, WeaponMount mount) {
        if(parts.size > 0 && !perks.isEmpty()) {
            Perk primary = perks.get(Math.min(primaryPerkIndex, perks.size - 1));
            PerkStateData ps = getState(unit, primary);
            AxPartParms.perkparams.set(ps.smoothProgress, ps.smoothActivated);
        }

        super.draw(unit, mount);

        if(!perks.isEmpty()) {
            float wx = unit.x + arc.math.Angles.trnsx(unit.rotation - 90f, x, y);
            float wy = unit.y + arc.math.Angles.trnsy(unit.rotation - 90f, x, y);
            float wr = mount.rotation;

            for(Perk perk : perks) {
                perk.draw(wx, wy, wr, getState(unit, perk));
            }
        }
    }
}