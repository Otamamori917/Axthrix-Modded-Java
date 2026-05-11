package axthrix.world.types.perks;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

public abstract class Perk {

    // ---- Configuration ----

    public String name = "perk";

    /** Number of trigger events required to gain one stack. Range: >= 1. */
    public int hitsPerStack = 2;

    /** Maximum number of stacks. Range: >= 1. */
    public int maxStacks = 1;

    /** Minimum distance in world units for a hit to count (HITS mode). Range: >= 0. */
    public float minRange = 0f;

    /** Maximum distance in world units for a hit to count (HITS mode). */
    public float maxRange = Float.MAX_VALUE;

    public boolean decaysOnMiss = true;
    public boolean decaysOverTime = false;

    /** Seconds idle before full reset, if decaysOverTime is true. Range: > 0. */
    public float decayTime = 5f;

    public boolean consumesOnActivate = true;

    /**
     * How this perk accumulates stacks.
     * HITS: landing hits. RECEIVE_HITS: taking damage. DISTANCE: traveling (units only).
     */
    public TriggerMode triggerMode = TriggerMode.HITS;

    /**
     * World units of travel required to gain one stack (DISTANCE mode only).
     */
    public float distancePerStack = 8f * 20f;

    /** Lerp speed for smoothProgress and smoothActivated per tick. Range: 0.0-1.0. */
    public float smoothSpeed = 0.05f;

    /**
     * Reload speed reduction as a fraction per application.
     * Range: 0.0-1.0.
     */
    public float reloadBuff = 0f;

    /**
     * Damage boost as a fraction per application.
     */
    public float damageBuff = 0f;

    /**
     * Flat armor added per application (units only).
     */
    public float resistanceBuff = 0f;

    /**
     * Range boost in world units per application (turrets only).
     */
    public float rangeBuff = 0f;

    /**
     * Speed multiplier boost per application (units only).
     */
    public float speedBuff = 0f;

    // ---- Runtime State ----

    public int currentStacks = 0;
    public int hitProgress = 0;
    public float idleTimer = 0f;
    public boolean isActivated = false;
    public boolean pendingShot = false;

    /** Smoothed progress (0.0-1.0). Use for part params and visuals. */
    public float smoothProgress = 0f;

    /** Smoothed activated value (0.0-1.0). Use for the activated part param. */
    public float smoothActivated = 0f;

    // Internal distance tracking
    private float lastX = Float.NaN, lastY = Float.NaN;
    private float distanceAccum = 0f;

    // ---- Abstract Methods ----

    public abstract void onStack(Unit unit, Turret.TurretBuild turret);
    public abstract void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY);
    public void onMiss(Unit unit, Turret.TurretBuild turret) {}
    public void onReset(Unit unit, Turret.TurretBuild turret) {}

    /**
     * Per-tick update. Always call super.update() from subclasses.
     * Handles smoothing, distance tracking, and idle timer.
     */
    public void update(Unit unit, Turret.TurretBuild turret) {
        // Distance tracking
        if(triggerMode == TriggerMode.DISTANCE && unit != null) {
            if(!Float.isNaN(lastX)) {
                float dx = unit.x - lastX;
                float dy = unit.y - lastY;
                distanceAccum += Mathf.len(dx, dy);
                while(distanceAccum >= distancePerStack) {
                    distanceAccum -= distancePerStack;
                    gainStack(unit, turret, unit.x, unit.y);
                }
            }
            lastX = unit.x;
            lastY = unit.y;
        }

        // Smooth progress lerp
        float step = smoothSpeed * Time.delta;
        smoothProgress = Mathf.approachDelta(smoothProgress, getProgress(), step);
        smoothActivated = Mathf.approachDelta(smoothActivated, (isActivated || pendingShot) ? 1f : 0f, step);
    }

    public void draw(float x, float y, float rotation) {}

    // ---- Shot replacement hooks ----

    public BulletType getPendingBullet() { return null; }
    public void onPendingShotFired(Turret.TurretBuild turret) {}

    // ---- Buff application helpers ----

    /**
     * Applies stacking buffs to a unit scaled by stacks * buff fields.
     * Call from subclass update() for stacking perks.
     */
    protected void applyStackingBuffsToUnit(Unit unit) {
        if(unit == null || currentStacks <= 0) return;
        if(reloadBuff > 0f) unit.reloadMultiplier *= Math.max(0.1f, 1f - reloadBuff * currentStacks);
        if(damageBuff > 0f) unit.damageMultiplier += damageBuff * currentStacks;
        if(resistanceBuff > 0f) unit.armor += resistanceBuff * currentStacks;
        if(speedBuff > 0f) unit.speedMultiplier += speedBuff * currentStacks;
    }

    /**
     * Applies flat (non-stacking) buffs to a unit at full buff field value scaled by t (0-1).
     * Call from subclass update() for non-stacking perks.
     * @param t Scale factor 0.0-1.0 (1.0 = full buff, 0.5 = half, etc.)
     */
    protected void applyFlatBuffsToUnit(Unit unit, float t) {
        if(unit == null || t <= 0f) return;
        if(reloadBuff > 0f) unit.reloadMultiplier *= Math.max(0.1f, 1f - reloadBuff * t);
        if(damageBuff > 0f) unit.damageMultiplier += damageBuff * t;
        if(resistanceBuff > 0f) unit.armor += resistanceBuff * t;
        if(speedBuff > 0f) unit.speedMultiplier += speedBuff * t;
    }

    /**
     * Returns the stacking reload multiplier for turrets (stacking perks).
     * 1.0 = no boost. Used by PerkTurretTypeBuild.
     */
    public float getTurretReloadMultiplier() {
        if(reloadBuff <= 0f || currentStacks <= 0) return 1f;
        return Math.max(0.1f, 1f - reloadBuff * currentStacks);
    }

    /**
     * Returns the stacking damage multiplier for turrets (stacking perks).
     * 1.0 = no boost. Used by PerkTurretTypeBuild.
     */
    public float getTurretDamageMultiplier() {
        if(damageBuff <= 0f || currentStacks <= 0) return 1f;
        return 1f + damageBuff * currentStacks;
    }

    /**
     * Returns the flat reload multiplier for turrets scaled by t (non-stacking perks).
     * 1.0 = no boost. Used by PerkTurretTypeBuild.
     */
    public float getTurretReloadMultiplierFlat(float t) {
        if(reloadBuff <= 0f || t <= 0f) return 1f;
        return Math.max(0.1f, 1f - reloadBuff * t);
    }

    /**
     * Returns the flat damage multiplier for turrets scaled by t (non-stacking perks).
     * 1.0 = no boost. Used by PerkTurretTypeBuild.
     */
    public float getTurretDamageMultiplierFlat(float t) {
        if(damageBuff <= 0f || t <= 0f) return 1f;
        return 1f + damageBuff * t;
    }

    /**
     * Returns the range bonus in world units for turrets scaled by t.
     * Used by PerkTurretTypeBuild.
     */
    public float getTurretRangeBonus(float t) {
        if(rangeBuff <= 0f || t <= 0f) return 0f;
        return rangeBuff * t;
    }

    // ---- Trigger routing ----

    public void registerHit(Unit unit, Turret.TurretBuild turret, float targetX, float targetY, float distance) {
        if(triggerMode != TriggerMode.HITS) return;
        if(distance < minRange || distance > maxRange) return;
        idleTimer = 0f;
        advanceProgress(unit, turret, targetX, targetY);
    }

    public void registerReceivedHit(Unit unit, Turret.TurretBuild turret) {
        if(triggerMode != TriggerMode.RECEIVE_HITS) return;
        float cx = unit != null ? unit.x : turret.x;
        float cy = unit != null ? unit.y : turret.y;
        advanceProgress(unit, turret, cx, cy);
    }

    public void registerMiss(Unit unit, Turret.TurretBuild turret) {
        if(triggerMode == TriggerMode.DISTANCE) return;
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
            if(idleTimer >= decayTime && currentStacks > 0) reset(unit, turret);
        }
    }

    public void reset(Unit unit, Turret.TurretBuild turret) {
        currentStacks = 0;
        hitProgress = 0;
        idleTimer = 0f;
        isActivated = false;
        pendingShot = false;
        distanceAccum = 0f;
        lastX = Float.NaN;
        lastY = Float.NaN;
        onReset(unit, turret);
    }

    // ---- Internal ----

    private void advanceProgress(Unit unit, Turret.TurretBuild turret, float tx, float ty) {
        hitProgress++;
        if(hitProgress >= hitsPerStack) {
            hitProgress = 0;
            gainStack(unit, turret, tx, ty);
        }
    }

    private void gainStack(Unit unit, Turret.TurretBuild turret, float tx, float ty) {
        if(currentStacks < maxStacks) currentStacks++;
        isActivated = currentStacks >= maxStacks;

        if(isActivated) {
            onMaxStack(unit, turret, tx, ty);
            if(consumesOnActivate) {
                currentStacks = 0;
                isActivated = false;
            }
        } else {
            onStack(unit, turret);
        }
    }

    /**
     * Defines how a perk accumulates stacks.
     *
     * @HITS         stacks gained by landing hits on enemies (default).
     * @RECEIVE_HITS stacks gained by taking damage from enemies.
     * @DISTANCE     stacks gained by traveling a set distance (units only).
     */
    public enum TriggerMode {
        HITS,
        RECEIVE_HITS,
        DISTANCE
    }

    public float getProgress() {
        if(maxStacks <= 0) return 0f;
        float stackFraction = (float)currentStacks / maxStacks;
        float hitFraction = (hitsPerStack <= 0) ? 0f : ((float)hitProgress / hitsPerStack) / maxStacks;
        return Math.min(1f, stackFraction + hitFraction);
    }

    public float getActivatedParam() { return smoothActivated; }

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
        target.triggerMode = triggerMode;
        target.distancePerStack = distancePerStack;
        target.smoothSpeed = smoothSpeed;
        target.reloadBuff = reloadBuff;
        target.damageBuff = damageBuff;
        target.resistanceBuff = resistanceBuff;
        target.rangeBuff = rangeBuff;
        target.speedBuff = speedBuff;
    }
}