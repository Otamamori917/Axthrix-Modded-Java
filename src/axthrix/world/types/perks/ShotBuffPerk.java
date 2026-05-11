package axthrix.world.types.perks;

import axthrix.world.types.block.defense.PerkTurretType;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Buffs the next shot based on accumulated hit stacks.
 * Buff are stacked and scales additively up to maxStacks.
 *
 * @| Supports all TriggerModes.
 */
public class ShotBuffPerk extends Perk {

    /**
     * If true, all stacks are consumed when the turret/unit fires.
     * If false, stacks persist until a miss or decay timer clears them.
     */
    public boolean resetOnShot = true;

    // Tracks whether we're waiting to apply the buff on the next shot (resetOnShot = true)
    private boolean buffPending = false;

    public ShotBuffPerk() {
        name = "ShotBuff";
        consumesOnActivate = false;
        decaysOnMiss = true;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {
        if(resetOnShot) buffPending = true;
    }

    @Override
    public void onReset(Unit unit, Turret.TurretBuild turret) {
        buffPending = false;
    }

    @Override
    public void update(Unit unit, Turret.TurretBuild turret) {
        super.update(unit, turret);

        // For units with resetOnShot = false, apply buff every tick while stacks are held
        if(unit != null && !resetOnShot && currentStacks > 0) {
            applyStackingBuffsToUnit(unit);
        }
    }

    /**
     * Returns the damage multiplier to apply to the next turret shot.
     * For resetOnShot = true: only non-zero if buffPending.
     * For resetOnShot = false: always returns stacked value.
     */
    public float getShotDamageMultiplier() {
        if(resetOnShot) {
            return buffPending ? getTurretDamageMultiplier() : 1f;
        }
        return getTurretDamageMultiplier();
    }

    /**
     * Returns the reload multiplier to apply.
     * Same pending logic as getShotDamageMultiplier().
     */
    public float getShotReloadMultiplier() {
        if(resetOnShot) {
            return buffPending ? getTurretReloadMultiplier() : 1f;
        }
        return getTurretReloadMultiplier();
    }

    /**
     * Called by PerkTurretTypeBuild after the buffed shot fires (resetOnShot = true only).
     * Clears all stacks and the pending flag.
     */
    public void consumeShot(Unit unit, Turret.TurretBuild turret) {
        if(!resetOnShot) return;
        buffPending = false;
        currentStacks = 0;
        hitProgress = 0;
        isActivated = false;
    }

    @Override
    public Perk copy() {
        ShotBuffPerk copy = new ShotBuffPerk();
        copyBaseTo(copy);
        copy.resetOnShot = resetOnShot;
        return copy;
    }
}