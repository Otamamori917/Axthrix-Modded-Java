package axthrix.world.types.perks;

import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

import java.util.HashMap;

public abstract class Perk {

    // ---- Static state storage ----

    /**
     * Universal state map: ownerID -> (perkName -> PerkStateData).
     * Keyed by the building/unit ID so bullet callbacks can look up state
     * without needing Boolean flags on the bullet itself.
     */
    public static final HashMap<Integer, HashMap<String, PerkStateData>> stateMap = new HashMap<>();

    /**
     * Gets or creates the PerkStateData for a given owner ID and perk name.
     */
    public static PerkStateData getState(int ownerId, String perkName) {
        return stateMap
                .computeIfAbsent(ownerId, k -> new HashMap<>())
                .computeIfAbsent(perkName, k -> new PerkStateData());
    }

    /**
     * Removes all perk state for a given owner. Call when a turret/unit is destroyed.
     */
    public static void clearState(int ownerId) {
        stateMap.remove(ownerId);
    }

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

    /** How this perk accumulates stacks. */
    public TriggerMode triggerMode = TriggerMode.HITS;

    /** World units of travel required to gain one stack (DISTANCE mode only). */
    public float distancePerStack = 8f * 20f;

    /** Lerp speed for smoothProgress and smoothActivated per tick. */
    public float smoothSpeed = 0.05f;

    // ---- Buff fields ----

    /** Reload speed reduction as a fraction per application. Range: 0.0-1.0. */
    public float reloadBuff = 0f;

    /** Damage boost as a fraction per application. Range: >= 0. */
    public float damageBuff = 0f;

    /** Flat armor added per application (units only). Range: >= 0. */
    public float resistanceBuff = 0f;

    /** Range boost in world units per application (turrets only). Range: >= 0. */
    public float rangeBuff = 0f;

    /** Speed multiplier boost per application (units only). Range: >= 0. */
    public float speedBuff = 0f;

    // ---- Abstract Methods ----

    public abstract void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s);
    public abstract void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY);
    public void onMiss(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}
    public void onReset(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}

    /**
     * Per-tick update. Always call super.update() from subclasses.
     */
    public void update(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        // Distance tracking
        if(triggerMode == TriggerMode.DISTANCE && unit != null) {
            if(!Float.isNaN(s.lastX)) {
                float dx = unit.x - s.lastX;
                float dy = unit.y - s.lastY;
                s.distanceAccum += Mathf.len(dx, dy);
                while(s.distanceAccum >= distancePerStack) {
                    s.distanceAccum -= distancePerStack;
                    gainStack(unit, turret, s, unit.x, unit.y);
                }
            }
            s.lastX = unit.x;
            s.lastY = unit.y;
        }

        // Smooth progress lerp
        float step = smoothSpeed * Time.delta;
        s.smoothProgress = Mathf.approachDelta(s.smoothProgress, getProgress(s), step);
        s.smoothActivated = Mathf.approachDelta(s.smoothActivated, (s.isActivated || s.pendingShot) ? 1f : 0f, step);
    }

    public void draw(float x, float y, float rotation, PerkStateData s) {}

    // ---- Shot replacement hooks ----

    public BulletType getPendingBullet() { return null; }

    public void onPendingShotFired(Turret.TurretBuild turret, PerkStateData s) {}

    // ---- Buff application helpers ----

    protected void applyStackingBuffsToUnit(Unit unit, PerkStateData s) {
        if(unit == null || s.currentStacks <= 0) return;
        if(reloadBuff > 0f) unit.reloadMultiplier *= Math.max(0.1f, 1f - reloadBuff * s.currentStacks);
        if(damageBuff > 0f) unit.damageMultiplier += damageBuff * s.currentStacks;
        if(resistanceBuff > 0f) unit.armor += resistanceBuff * s.currentStacks;
        if(speedBuff > 0f) unit.speedMultiplier += speedBuff * s.currentStacks;
    }

    protected void applyFlatBuffsToUnit(Unit unit, float t) {
        if(unit == null || t <= 0f) return;
        if(reloadBuff > 0f) unit.reloadMultiplier *= Math.max(0.1f, 1f - reloadBuff * t);
        if(damageBuff > 0f) unit.damageMultiplier += damageBuff * t;
        if(resistanceBuff > 0f) unit.armor += resistanceBuff * t;
        if(speedBuff > 0f) unit.speedMultiplier += speedBuff * t;
    }

    public float getTurretReloadMultiplier(PerkStateData s) {
        if(reloadBuff <= 0f || s.currentStacks <= 0) return 1f;
        return Math.max(0.1f, 1f - reloadBuff * s.currentStacks);
    }

    public float getTurretDamageMultiplier(PerkStateData s) {
        if(damageBuff <= 0f || s.currentStacks <= 0) return 1f;
        return 1f + damageBuff * s.currentStacks;
    }

    public float getTurretReloadMultiplierFlat(float t) {
        if(reloadBuff <= 0f || t <= 0f) return 1f;
        return Math.max(0.1f, 1f - reloadBuff * t);
    }

    public float getTurretDamageMultiplierFlat(float t) {
        if(damageBuff <= 0f || t <= 0f) return 1f;
        return 1f + damageBuff * t;
    }

    public float getTurretRangeBonus(float t) {
        if(rangeBuff <= 0f || t <= 0f) return 0f;
        return rangeBuff * t;
    }

    // ---- Trigger routing ----

    public void registerHit(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY, float distance) {
        if(triggerMode != TriggerMode.HITS) return;
        if(distance < minRange || distance > maxRange) return;
        s.idleTimer = 0f;
        advanceProgress(unit, turret, s, targetX, targetY);
    }

    public void registerReceivedHit(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        if(triggerMode != TriggerMode.RECEIVE_HITS) return;
        float cx = unit != null ? unit.x : turret.x;
        float cy = unit != null ? unit.y : turret.y;
        advanceProgress(unit, turret, s, cx, cy);
    }

    public void registerMiss(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        if(triggerMode == TriggerMode.DISTANCE) return;
        if(!decaysOnMiss || s.currentStacks <= 0) return;
        s.currentStacks--;
        s.hitProgress = 0;
        s.isActivated = false;
        onMiss(unit, turret, s);
    }

    public void tickTimer(Unit unit, Turret.TurretBuild turret, PerkStateData s, boolean fired) {
        if(!decaysOverTime) return;
        if(fired) {
            s.idleTimer = 0f;
        } else {
            s.idleTimer += arc.Core.graphics.getDeltaTime();
            if(s.idleTimer >= decayTime && s.currentStacks > 0) reset(unit, turret, s);
        }
    }

    public void reset(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        s.reset();
        onReset(unit, turret, s);
    }

    // ---- Internal ----

    private void advanceProgress(Unit unit, Turret.TurretBuild turret, PerkStateData s, float tx, float ty) {
        s.hitProgress++;
        if(s.hitProgress >= hitsPerStack) {
            s.hitProgress = 0;
            gainStack(unit, turret, s, tx, ty);
        }
    }

    private void gainStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float tx, float ty) {
        if(s.currentStacks < maxStacks) s.currentStacks++;
        s.isActivated = s.currentStacks >= maxStacks;

        if(s.isActivated) {
            onMaxStack(unit, turret, s, tx, ty);
            if(consumesOnActivate) {
                s.currentStacks = 0;
                s.isActivated = false;
            }
        } else {
            onStack(unit, turret, s);
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

    public float getProgress(PerkStateData s) {
        if(maxStacks <= 0) return 0f;
        float stackFraction = (float)s.currentStacks / maxStacks;
        float hitFraction = (hitsPerStack <= 0) ? 0f : ((float)s.hitProgress / hitsPerStack) / maxStacks;
        return Math.min(1f, stackFraction + hitFraction);
    }

    public float getActivatedParam(PerkStateData s) { return s.smoothActivated; }

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