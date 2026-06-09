//package axthrix.world.types.perks;
//
//import arc.math.Mathf;
//import mindustry.gen.Unit;
//import mindustry.world.blocks.defense.turrets.Turret;
//
//public class RangePerk extends Perk {
//
//    /** Distance (world units) at which full buff is received. Range: > 0. */
//    public float requiredDistance = 100f * 8f;
//
//    /**
//     * If false: farther = more buff (sniper bonus).
//     * If true:  closer = more buff (brawler bonus).
//     */
//    public boolean invertRange = false;
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
//    public void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}
//
//    @Override
//    public void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY) {}
//
//    @Override
//    public void update(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
//        float step = smoothSpeed * arc.util.Time.delta;
//        s.smoothProgress = Mathf.approachDelta(s.smoothProgress, s.currentScale, step);
//        s.smoothActivated = s.smoothProgress;
//
//        if(unit != null) {
//            mindustry.gen.Teamc target = unit.target;
//            if(target != null) {
//                float dist = Mathf.dst(unit.x, unit.y, target.x(), target.y());
//                s.currentScale = computeScale(dist);
//                applyFlatBuffsToUnit(unit, s.currentScale);
//            } else {
//                s.currentScale = 0f;
//            }
//        }
//    }
//
//    public void updateForTurret(Turret.TurretBuild turret, PerkStateData s) {
//        if(turret.target == null) {
//            s.currentScale = 0f;
//            return;
//        }
//        float dist = Mathf.dst(turret.x, turret.y, turret.target.x(), turret.target.y());
//        s.currentScale = computeScale(dist);
//    }
//
//    public float getCurrentScale(PerkStateData s) {
//        return s.currentScale;
//    }
//
//    private float computeScale(float distance) {
//        if(requiredDistance <= 0f) return 0f;
//        float ratio = Mathf.clamp(distance / requiredDistance);
//        return invertRange ? (1f - ratio) : ratio;
//    }
//
//    @Override
//    public float getProgress(PerkStateData s) {
//        return s.currentScale;
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