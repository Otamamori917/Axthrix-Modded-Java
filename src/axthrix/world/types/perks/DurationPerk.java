package axthrix.world.types.perks;

import arc.util.Time;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

/**
 * Activates a buff for a set duration when max stacks is reached.
 * Buffs aren't stacked buff set equals buff given
 * @| Supports all TriggerModes:
 */
public class DurationPerk extends Perk {

    /**
     * How long the buff lasts after activation, in ticks.
     * Range: > 0.
     */
    public float duration = 300f;

    // ---- Runtime state ----

    /** Whether the buff is currently active. */
    public boolean buffActive = false;

    /** Remaining ticks of the active buff. */
    public float buffTimer = 0f;

    public DurationPerk() {
        name = "Duration";
        consumesOnActivate = true;
        decaysOnMiss = false;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {
        buffActive = true;
        buffTimer = duration;
    }

    @Override
    public void onReset(Unit unit, Turret.TurretBuild turret) {
        buffActive = false;
        buffTimer = 0f;
    }

    @Override
    public void update(Unit unit, Turret.TurretBuild turret) {
        super.update(unit, turret);

        if(buffActive) {
            buffTimer -= Time.delta;
            if(buffTimer <= 0f) {
                buffActive = false;
                buffTimer = 0f;
            } else {
                // Apply flat buff at full value (t = 1.0) while active
                if(unit != null) applyFlatBuffsToUnit(unit, 1f);
            }
        }
    }

    /**
     * Returns 1f if buff is active, 0f otherwise.
     * Used by PerkTurretTypeBuild to know whether to apply turret-side buffs.
     */
    public float getBuffScale() {
        return buffActive ? 1f : 0f;
    }

    /**
     * Returns buff progress 0-1 based on remaining time (1 = just activated, 0 = expired).
     * Useful for visual effects.
     */
    public float getBuffProgress() {
        if(!buffActive || duration <= 0f) return 0f;
        return buffTimer / duration;
    }

    @Override
    public float getProgress() {
        // Show buff timer progress while active, stack progress otherwise
        if(buffActive) return getBuffProgress();
        return super.getProgress();
    }

    @Override
    public Perk copy() {
        DurationPerk copy = new DurationPerk();
        copyBaseTo(copy);
        copy.duration = duration;
        return copy;
    }
}