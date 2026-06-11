package axthrix.world.types.perks;

import arc.math.Mathf;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;
/// the farther the target (or closer via @invertRange) the larger the bonus up to max
public class RangePerk extends Perk {

    /** Distance (world units) at which full buff is received. Range: > 0. */
    public float requiredDistance = 100f * 8f;

    /**
     * If false: farther = more buff (sniper bonus).
     * If true:  closer = more buff (brawler bonus).
     */
    public boolean invertRange = false;

    public RangePerk() {
        name = "Range";
        hitsPerStack = Integer.MAX_VALUE;
        maxStacks = 0;
        consumesOnActivate = false;
        decaysOnMiss = false;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY) {}

    @Override
    public void update(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        // Smooth lerp only — scale is set externally via updateForTurret or updateForUnit
        float step = smoothSpeed * arc.util.Time.delta;
        s.smoothProgress = Mathf.approachDelta(s.smoothProgress, s.currentScale, step);
        s.smoothActivated = s.smoothProgress;
        // Unit buff application happens in updateForUnit — not here, since we need the mount
    }

    /**
     * Updates the current buff scale for a turret based on its current target's distance.
     * Call from PerkTurretTypeBuild.updateTile() each tick.
     */
    public void updateForTurret(Turret.TurretBuild turret, PerkStateData s) {
        if(turret.target == null) {
            s.currentScale = 0f;
            return;
        }
        float dist = Mathf.dst(turret.x, turret.y, turret.target.x(), turret.target.y());
        s.currentScale = computeScale(dist);
    }

    /**
     * Updates the current buff scale for a unit weapon based on the weapon mount's target.
     * Call from PerkWeapon.update() each tick, passing the WeaponMount.
     * Also applies the buff to the unit.
     */
    public void updateForUnit(Unit unit, WeaponMount mount, PerkStateData s) {
        if(mount.target == null) {
            s.currentScale = 0f;
            return;
        }
        float dist = Mathf.dst(unit.x, unit.y, mount.target.x(), mount.target.y());
        s.currentScale = computeScale(dist);

        float step = smoothSpeed * arc.util.Time.delta;
        s.smoothProgress = Mathf.approachDelta(s.smoothProgress, s.currentScale, step);
        s.smoothActivated = s.smoothProgress;

        if(s.currentScale > 0f) applyFlatBuffsToUnit(unit, s.currentScale);
    }

    public float getCurrentScale(PerkStateData s) {
        return s.currentScale;
    }

    private float computeScale(float distance) {
        if(requiredDistance <= 0f) return 0f;
        float ratio = Mathf.clamp(distance / requiredDistance);
        return invertRange ? (1f - ratio) : ratio;
    }

    @Override
    public float getProgress(PerkStateData s) {
        return s.currentScale;
    }

    @Override
    public Perk copy() {
        RangePerk copy = new RangePerk();
        copyBaseTo(copy);
        copy.requiredDistance = requiredDistance;
        copy.invertRange = invertRange;
        return copy;
    }
}