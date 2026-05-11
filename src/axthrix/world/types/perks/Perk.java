package axthrix.world.types.perks;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Abstract base class for all weapon perks.
 * Perks are modular effects that activate based on hit streaks and stacks.
 * Each perk maintains its own independent stack/streak counter per unit/turret instance.
 *
 * Subclass this and implement onStack, onMaxStack, and copy at minimum.
 */
public abstract class Perk {

    // ---- Configuration ----

    /** Display name of this perk, used for UI/debug. */
    public String name = "perk";

    /**
     * Number of valid hits required to gain one stack.
     * Range: >= 1.
     */
    public int hitsPerStack = 2;

    /**
     * Maximum number of stacks this perk can accumulate.
     * Range: >= 1.
     */
    public int maxStacks = 1;

    /**
     * Minimum distance in world units the target must be for a hit to count.
     * Set to 0 to disable. Range: >= 0.
     */
    public float minRange = 0f;

    /**
     * Maximum distance in world units for a hit to count.
     * Set to Float.MAX_VALUE to disable.
     */
    public float maxRange = Float.MAX_VALUE;

    /**
     * Whether a miss reduces current stacks by 1.
     * If false, misses have no effect.
     */
    public boolean decaysOnMiss = true;

    /**
     * Whether stacks fully reset after a period of not firing.
     * Requires decayTime > 0.
     */
    public boolean decaysOverTime = false;

    /**
     * Seconds of not firing before stacks fully reset, if decaysOverTime is true.
     * Range: > 0.
     */
    public float decayTime = 5f;

    /**
     * Whether this perk resets to 0 stacks after firing off at max stacks.
     * If false, stays at max and re-triggers every hitsPerStack hits.
     */
    public boolean consumesOnActivate = true;

    /**
     * How fast smoothProgress lerps toward the raw target progress value per tick.
     * Range: 0.0 to 1.0. Default: 0.05f.
     */
    public float smoothSpeed = 0.05f;

    // ---- Runtime State (per instance via copy()) ----

    /** Current stack count. Range: 0 to maxStacks. */
    public int currentStacks = 0;

    /** Hits accumulated toward the next stack. Range: 0 to hitsPerStack - 1. */
    public int hitProgress = 0;

    /** Internal idle timer in seconds. Counts up when not firing. */
    public float idleTimer = 0f;

    /** Whether this perk is currently at max stacks. */
    public boolean isActivated = false;

    /**
     * Whether this perk has a shot queued to replace the turret's next normal shot.
     * Set to true in onMaxStack by subclasses that use shot-replacement behavior.
     * Cleared automatically by PerkTurretTypeBuild after the perk shot fires via useAmmo().
     */
    public boolean pendingShot = false;

    /**
     * Smoothed progress value that lerps toward getProgress() each tick.
     * Use this for part params and visuals instead of getProgress() directly.
     * Range: 0.0 to 1.0.
     */
    public float smoothProgress = 0f;

    /**
     * Smoothed activated value that lerps toward getActivatedTarget() each tick.
     * Use this for the activated part param instead of getActivatedParam() directly.
     * Range: 0.0 to 1.0.
     */
    public float smoothActivated = 0f;

    // ---- Abstract Methods ----

    /**
     * Called when a stack is gained but max stacks not yet reached.
     * @param unit Owner unit. Null if turret.
     * @param turret Owner turret build. Null if unit.
     */
    public abstract void onStack(Unit unit, Turret.TurretBuild turret);

    /**
     * Called when max stacks is reached.
     * @param unit Owner unit. Null if turret.
     * @param turret Owner turret build. Null if unit.
     * @param targetX World X of the hit that triggered max stacks.
     * @param targetY World Y of the hit that triggered max stacks.
     */
    public abstract void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY);

    /**
     * Called after a miss reduces stacks, if decaysOnMiss is true.
     * Override for custom on-miss behavior.
     */
    public void onMiss(Unit unit, Turret.TurretBuild turret) {}

    /**
     * Called when stacks are fully reset (timer or manual).
     * Override for custom on-reset behavior.
     */
    public void onReset(Unit unit, Turret.TurretBuild turret) {}

    /**
     * Per-tick update. Called every game tick from the weapon/turret update.
     * Always call super.update() from subclasses to keep smoothProgress ticking.
     */
    public void update(Unit unit, Turret.TurretBuild turret) {
        float targetProgress = getProgress();
        float targetActivated = (isActivated || pendingShot) ? 1f : 0f;
        float step = smoothSpeed * Time.delta;
        smoothProgress = Mathf.approachDelta(smoothProgress, targetProgress, step);
        smoothActivated = Mathf.approachDelta(smoothActivated, targetActivated, step);
    }

    /**
     * Draw hook for custom visuals tied to this perk's state.
     * Called during the weapon/turret draw phase.
     * @param x World X of the weapon origin.
     * @param y World Y of the weapon origin.
     * @param rotation Current weapon rotation in degrees.
     */
    public void draw(float x, float y, float rotation) {}

    // ---- Shot replacement hooks ----

    /**
     * Returns the BulletType that should replace the turret's next normal shot,
     * or null if this perk does not use shot replacement.
     * Only queried when pendingShot is true.
     */
    public BulletType getPendingBullet() {
        return null;
    }

    /**
     * Called by PerkTurretTypeBuild immediately after the pending perk shot fires via useAmmo().
     * Override to apply post-shot effects such as a reload speed boost.
     * @param turret The turret build that fired the perk shot.
     */
    public void onPendingShotFired(Turret.TurretBuild turret) {}

    // ---- Concrete Logic ----

    public void registerHit(Unit unit, Turret.TurretBuild turret, float targetX, float targetY, float distance) {
        if(distance < minRange || distance > maxRange) return;

        idleTimer = 0f;
        hitProgress++;

        if(hitProgress >= hitsPerStack) {
            hitProgress = 0;
            if(currentStacks < maxStacks) {
                currentStacks++;
            }
            isActivated = currentStacks >= maxStacks;

            if(isActivated) {
                onMaxStack(unit, turret, targetX, targetY);
                if(consumesOnActivate) {
                    currentStacks = 0;
                    isActivated = false;
                }
            } else {
                onStack(unit, turret);
            }
        }
    }

    public void registerMiss(Unit unit, Turret.TurretBuild turret) {
        if(!decaysOnMiss || currentStacks <= 0) return;
        currentStacks--;
        hitProgress = 0;
        isActivated = false;
        onMiss(unit, turret);
    }

    public void tickTimer(Unit unit, Turret.TurretBuild turret, boolean fired) {
        if(!decaysOverTime) return;
        if(fired) {
            idleTimer = 0f;
        } else {
            idleTimer += arc.Core.graphics.getDeltaTime();
            if(idleTimer >= decayTime && currentStacks > 0) {
                reset(unit, turret);
            }
        }
    }

    public void reset(Unit unit, Turret.TurretBuild turret) {
        currentStacks = 0;
        hitProgress = 0;
        idleTimer = 0f;
        isActivated = false;
        pendingShot = false;
        onReset(unit, turret);
        // smoothProgress and smoothActivated lerp back down naturally via update()
    }

    /**
     * Returns the raw perk charge progress as a 0-1 float.
     * Combines stack count and intra-stack hit progress.
     * For visuals and part params, use smoothProgress instead.
     */
    public float getProgress() {
        if(maxStacks <= 0) return 0f;
        float stackFraction = (float)currentStacks / maxStacks;
        float hitFraction = (hitsPerStack <= 0) ? 0f : ((float)hitProgress / hitsPerStack) / maxStacks;
        return Math.min(1f, stackFraction + hitFraction);
    }

    /**
     * Returns smoothActivated for use as a part param.
     * Lerps to 1f when activated or pendingShot, back to 0f otherwise.
     */
    public float getActivatedParam() {
        return smoothActivated;
    }

    public abstract Perk copy();

    protected void copyBaseTo(Perk target) {
        target.name = name;
        target.hitsPerStack = hitsPerStack;
        target.maxStacks = maxStacks;
        target.minRange = minRange;
        target.maxRange = maxRange;
        target.decaysOnMiss = decaysOnMiss;
        target.decaysOverTime = decaysOverTime;
        target.decayTime = decayTime;
        target.consumesOnActivate = consumesOnActivate;
        target.smoothSpeed = smoothSpeed;
    }
}