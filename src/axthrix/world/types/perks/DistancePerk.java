package axthrix.world.types.perks;

import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Stacks accumulate as the unit travels. On shoot, all stacks are consumed
 * and the buff fields are applied scaled by stacks held at the time of firing.
 *
 * @| This is a unit-only perk
 *
 * @| distancePerStack (from Perk base) controls how far to travel per stack.
 * @| triggerMode is incompatible
 */
public class DistancePerk extends Perk {

    /**
     * How many ticks the per-shot buff lasts after shooting.
     * Set to 1 for a true single-shot buff, higher for a short burst window.
     */
    public float shotBuffDuration = 1f;

    // ---- Runtime state ----

    /** Stacks held when the shot fired — used to scale the buff during shotBuffDuration. */
    private int stacksOnFire = 0;

    /** Remaining ticks of the active per-shot buff. */
    private float shotBuffTimer = 0f;

    public DistancePerk() {
        name = "Distance";
        triggerMode = TriggerMode.DISTANCE;
        consumesOnActivate = false;
        decaysOnMiss = false;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {}

    @Override
    public void update(Unit unit, Turret.TurretBuild turret) {
        super.update(unit, turret);

        if(shotBuffTimer > 0f) {
            shotBuffTimer -= arc.util.Time.delta;
            if(unit != null && stacksOnFire > 0) {
                // Apply buff scaled by stacks held at fire time
                if(reloadBuff > 0f) unit.reloadMultiplier *= Math.max(0.1f, 1f - reloadBuff * stacksOnFire);
                if(damageBuff > 0f) unit.damageMultiplier += damageBuff * stacksOnFire;
                if(resistanceBuff > 0f) unit.armor += resistanceBuff * stacksOnFire;
                if(speedBuff > 0f) unit.speedMultiplier += speedBuff * stacksOnFire;
            }
            if(shotBuffTimer <= 0f) stacksOnFire = 0;
        }
    }

    /**
     * Call when the unit/turret fires. Consumes all stacks and starts the shot buff window.
     * For turrets, PerkTurretTypeBuild calls this and uses getConsumedStacksForTurret()
     * to apply the multiplier to the bullet.
     */
    public void onShoot(Unit unit, Turret.TurretBuild turret) {
        if(currentStacks <= 0) return;
        stacksOnFire = currentStacks;
        currentStacks = 0;
        hitProgress = 0;
        isActivated = false;
        shotBuffTimer = shotBuffDuration;
    }

    /**
     * Returns the damage multiplier for the turret's next bullet based on stacks consumed.
     * Only valid to call right after onShoot() — returns 1f if no stacks were consumed.
     */
    public float getConsumedDamageMultiplier() {
        if(stacksOnFire <= 0) return 1f;
        return 1f + damageBuff * stacksOnFire;
    }

    /**
     * Returns the reload multiplier for the turret based on stacks consumed.
     */
    public float getConsumedReloadMultiplier() {
        if(stacksOnFire <= 0) return 1f;
        return Math.max(0.1f, 1f - reloadBuff * stacksOnFire);
    }

    /**
     * Returns the range bonus for the turret based on stacks consumed.
     */
    public float getConsumedRangeBonus() {
        if(stacksOnFire <= 0) return 0f;
        return rangeBuff * stacksOnFire;
    }

    @Override
    public Perk copy() {
        DistancePerk copy = new DistancePerk();
        copyBaseTo(copy);
        copy.shotBuffDuration = shotBuffDuration;
        return copy;
    }
}
