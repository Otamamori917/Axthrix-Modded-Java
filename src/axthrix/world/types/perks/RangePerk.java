//package axthrix.world.types.perks;
//
//import arc.math.Mathf;
//import mindustry.gen.Posc;
//import mindustry.gen.Teamc;
//import mindustry.gen.Unit;
//import mindustry.world.blocks.defense.turrets.Turret;
//
///**
// * Applies buff fields proportional to the distance to the current target.
// *
// * @invertRange = false (default): farther target = more buff (sniper bonus).
// * @invertRange = true:            closer target = more buff (brawler bonus).
// *
// * Buffs aren't stacked buff set is scaled by the requiredDistance
// */
//public class RangePerk extends Perk {
//
//    /**
//     * The distance (world units) at which full buff is received.
//     * Below this distance (or above if invertRange = true) the buff scales down linearly.
//     */
//    public float requiredDistance = 100f * 8f;
//
//    /**
//     * If false: farther = more buff (full buff at requiredDistance, zero at 0).
//     * If true:  closer = more buff (full buff at 0, zero at requiredDistance).
//     */
//    public boolean invertRange = false;
//
//    // Current scale updated each tick — used by PerkTurretTypeBuild
//    private float currentScale = 0f;
//
//    public RangePerk() {
//        name = "Range";
//        hitsPerStack = Integer.MAX_VALUE;
//        maxStacks = 0;
//        consumesOnActivate = false;
//        decaysOnMiss = false;
//    }
//
//    @Override
//    public void onStack(Unit unit, Turret.TurretBuild turret) {}
//
//    @Override
//    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {}
//
//    @Override
//    public void update(Unit unit, Turret.TurretBuild turret) {
//        // Smooth lerp only — no stack logic
//        float step = smoothSpeed * arc.util.Time.delta;
//        smoothProgress = Mathf.approachDelta(smoothProgress, currentScale, step);
//        smoothActivated = smoothProgress;
//
//        if(unit != null) {
//            // For units, compute distance to current target
//            Teamc target = unit.target;
//            if(target != null) {
//                float dist = Mathf.dst(unit.x, unit.y, target.x(), target.y());
//                currentScale = computeScale(dist);
//                applyFlatBuffsToUnit(unit, currentScale);
//            } else {
//                currentScale = 0f;
//            }
//        }
//        // For turrets, currentScale is set externally via updateForTurret()
//    }
//
//    /**
//     * Updates the current buff scale for a turret based on its current target's distance.
//     * Call from PerkTurretTypeBuild.updateTile() each tick.
//     *
//     * @param turret The turret build.
//     */
//    public void updateForTurret(Turret.TurretBuild turret) {
//        float dist = Mathf.dst(turret.x, turret.y, turret.target.x(), turret.target.y());
//        currentScale = computeScale(dist);
//    }
//
//    /**
//     * Returns the current buff scale (0-1). Used by PerkTurretTypeBuild.
//     */
//    public float getCurrentScale() {
//        return currentScale;
//    }
//
//    private float computeScale(float distance) {
//        if(requiredDistance <= 0f) return 0f;
//        float ratio = Mathf.clamp(distance / requiredDistance);
//        return invertRange ? (1f - ratio) : ratio;
//    }
//
//    @Override
//    public float getProgress() {
//        return currentScale;
//    }
//
//    @Override
//    public Perk copy() {
//        RangePerk copy = new RangePerk();
//        copyBaseTo(copy);
//        copy.requiredDistance = requiredDistance;
//        copy.invertRange = invertRange;
//        return copy;
//    }
//}