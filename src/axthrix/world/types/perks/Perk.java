package axthrix.world.types.perks;

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

    // ---- Runtime State (per instance via copy()) ----

    /** Current stack count. Range: 0 to maxStacks. */
    public int currentStacks = 0;

    /** Hits accumulated toward the next stack. Range: 0 to hitsPerStack - 1. */
    public int hitProgress = 0;

    /** Internal idle timer in seconds. Counts up when not firing. */
    public float idleTimer = 0f;

    /** Whether this perk is currently at max stacks. */
    public boolean isActivated = false;

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
     */
    public void update(Unit unit, Turret.TurretBuild turret) {}

    /**
     * Draw hook for custom visuals tied to this perk's state.
     * Called during the weapon/turret draw phase.
     * @param x World X of the weapon origin.
     * @param y World Y of the weapon origin.
     * @param rotation Current weapon rotation in degrees.
     */
    public void draw(float x, float y, float rotation) {}

    // ---- Concrete Logic ----

    /**
     * Register a valid hit. Increments hit progress and may add a stack or trigger max-stack.
     * @param unit Owner unit. Null if turret.
     * @param turret Owner turret. Null if unit.
     * @param targetX World X of hit target.
     * @param targetY World Y of hit target.
     * @param distance World-unit distance from shooter to target.
     */
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

    /**
     * Register a miss. Reduces stacks by 1 if decaysOnMiss is true.
     */
    public void registerMiss(Unit unit, Turret.TurretBuild turret) {
        if(!decaysOnMiss || currentStacks <= 0) return;
        currentStacks--;
        hitProgress = 0;
        isActivated = false;
        onMiss(unit, turret);
    }

    /**
     * Tick the idle timer and handle time-based full decay. Call every game tick.
     * @param fired Whether the weapon fired this tick.
     */
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

    /**
     * Fully resets stacks and progress to zero.
     */
    public void reset(Unit unit, Turret.TurretBuild turret) {
        currentStacks = 0;
        hitProgress = 0;
        idleTimer = 0f;
        isActivated = false;
        onReset(unit, turret);
    }

    /**
     * Returns perk charge progress as a 0-1 float.
     * Combines stack count and intra-stack hit progress for smooth visual feedback.
     */
    public float getProgress() {
        if(maxStacks <= 0) return 0f;
        float stackFraction = (float) currentStacks / maxStacks;
        float hitFraction = (hitsPerStack <= 0) ? 0f : ((float) hitProgress / hitsPerStack) / maxStacks;
        return Math.min(1f, stackFraction + hitFraction);
    }

    /**
     * Returns 1f if at max stacks (activated), 0f otherwise.
     * Used for part params.
     */
    public float getActivatedParam() {
        return isActivated ? 1f : 0f;
    }

    /**
     * Creates a deep copy of this perk for use on a new unit/turret instance.
     * Subclasses must copy all configuration fields.
     */
    public abstract Perk copy();

    /** Copies all base WeaponPerk fields into the target perk. Call from subclass copy(). */
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
    }
}
