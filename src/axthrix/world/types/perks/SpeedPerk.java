package axthrix.world.types.perks;

import arc.math.Mathf;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

public class SpeedPerk extends Perk {

    /** Speed (world units/tick) at which full buff is received. Range: > 0. */
    public float requiredSpeed = 8f;

    public SpeedPerk() {
        name = "Speed";
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
        float ratio = getSpeedRatio(unit);
        float step = smoothSpeed * arc.util.Time.delta;
        s.smoothProgress = Mathf.approachDelta(s.smoothProgress, ratio, step);
        s.smoothActivated = s.smoothProgress;
        s.currentScale = ratio;

        if(unit == null || ratio <= 0f) return;
        applyFlatBuffsToUnit(unit, ratio);
    }

    public float getSpeedRatio(Unit unit) {
        if(unit == null || requiredSpeed <= 0f) return 0f;
        float currentSpeed = Mathf.len(unit.vel.x, unit.vel.y);
        return Mathf.clamp(currentSpeed / requiredSpeed);
    }

    @Override
    public float getProgress(PerkStateData s) {
        return s.currentScale;
    }

    @Override
    public Perk copy() {
        SpeedPerk copy = new SpeedPerk();
        copyBaseTo(copy);
        copy.requiredSpeed = requiredSpeed;
        return copy;
    }
}