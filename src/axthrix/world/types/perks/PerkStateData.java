package axthrix.world.types.perks;

/**
 * Flat data class holding all possible runtime state for any perk type.
 * One instance per (turret/unit id, perk name) pair, stored in Perk.stateMap.
 *
 * Fields are shared across perk types — each perk uses only the fields it needs.
 */
public class PerkStateData {

    // ---- Universal stack / hit tracking ----

    /** Current stack count. Range: 0 to maxStacks. */
    public int currentStacks = 0;

    /** Hits accumulated toward the next stack. Range: 0 to hitsPerStack - 1. */
    public int hitProgress = 0;

    /** Whether this perk is currently at max stacks. */
    public boolean isActivated = false;

    /** Internal idle timer in seconds for decaysOverTime. */
    public float idleTimer = 0f;

    // ---- Shot replacement (BulletPerk) ----

    /** Whether a perk bullet is queued to replace the next normal shot. */
    public boolean pendingShot = false;

    // ---- Smooth visuals ----

    /** Smoothed progress value (0.0-1.0). Used for part params and visuals. */
    public float smoothProgress = 0f;

    /** Smoothed activated value (0.0-1.0). Used for the activated part param. */
    public float smoothActivated = 0f;

    // ---- DurationPerk ----

    /** Whether the duration buff is currently active. */
    public boolean buffActive = false;

    /** Remaining ticks of the active duration buff. */
    public float buffTimer = 0f;

    // ---- DistancePerk ----

    /** Stacks held at the moment the shot fired — used to scale the buff during shotBuffDuration. */
    public int stacksOnFire = 0;

    /** Remaining ticks of the active per-shot distance buff. */
    public float shotBuffTimer = 0f;

    /** Accumulated distance toward the next stack. */
    public float distanceAccum = 0f;

    /** Last known X position for distance delta calculation. */
    public float lastX = Float.NaN;

    /** Last known Y position for distance delta calculation. */
    public float lastY = Float.NaN;

    // ---- RangePerk / SpeedPerk ----

    /** Current buff scale 0-1, set each tick based on distance or speed ratio. */
    public float currentScale = 0f;

    /** Resets all fields to their default values. */
    public void reset() {
        currentStacks = 0;
        hitProgress = 0;
        isActivated = false;
        idleTimer = 0f;
        pendingShot = false;
        buffActive = false;
        buffTimer = 0f;
        stacksOnFire = 0;
        shotBuffTimer = 0f;
        distanceAccum = 0f;
        lastX = Float.NaN;
        lastY = Float.NaN;
        currentScale = 0f;
        // smoothProgress and smoothActivated lerp back naturally — don't snap
    }
}
