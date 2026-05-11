package axthrix.world.types.perks;

import arc.math.Mathf;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Applies a continuous buff proportional to the unit's current speed vs requiredSpeed.
 * Buffs aren't stacked buff set is applied at flat value scaled by the speed ratio each tick.
 */
public class SpeedPerk extends Perk {

    /**
     * The speed (in world units/tick) at which full buff is received.
     * Moving slower gives a proportionally smaller buff.
     */
    public float requiredSpeed = 8f;

    public SpeedPerk() {
        name = "Speed";
        // No stack system needed — disable it
        hitsPerStack = Integer.MAX_VALUE;
        maxStacks = 0;
        consumesOnActivate = false;
        decaysOnMiss = false;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {}

    @Override
    public void update(Unit unit, Turret.TurretBuild turret) {
        // Skip Perk.update() stack/distance logic — this perk doesn't use stacks
        // Still need smooth lerp so call it manually
        float step = smoothSpeed * arc.util.Time.delta;
        smoothProgress = Mathf.approachDelta(smoothProgress, getSpeedRatio(unit), step);
        smoothActivated = smoothProgress;

        if(unit == null) return;

        float ratio = getSpeedRatio(unit);
        if(ratio <= 0f) return;

        applyFlatBuffsToUnit(unit, ratio);
    }

    /**
     * Returns the current speed ratio 0-1 based on the unit's velocity vs requiredSpeed.
     * Returns 0 if unit is null or stationary.
     */
    public float getSpeedRatio(Unit unit) {
        if(unit == null || requiredSpeed <= 0f) return 0f;
        float currentSpeed = Mathf.len(unit.vel.x, unit.vel.y);
        return Mathf.clamp(currentSpeed / requiredSpeed);
    }

    /**
     * Returns the turret reload multiplier scaled by speed ratio.
     * Always 1f since this perk is unit-only, but exposed for completeness.
     */
    public float getTurretReloadForSpeed(Unit unit) {
        return getTurretReloadMultiplierFlat(getSpeedRatio(unit));
    }

    @Override
    public float getProgress() {
        return 0f; // No stack progress to show
    }

    @Override
    public Perk copy() {
        SpeedPerk copy = new SpeedPerk();
        copyBaseTo(copy);
        copy.requiredSpeed = requiredSpeed;
        return copy;
    }
}