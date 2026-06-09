package axthrix.world.types.perks;

import arc.util.Time;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

public class DurationPerk extends Perk {

    /** How long the buff lasts after activation, in ticks. Range: > 0. */
    public float duration = 300f;

    public DurationPerk() {
        name = "Duration";
        consumesOnActivate = true;
        decaysOnMiss = false;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY) {
        s.buffActive = true;
        s.buffTimer = duration;
    }

    @Override
    public void onReset(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        s.buffActive = false;
        s.buffTimer = 0f;
    }

    @Override
    public void update(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        super.update(unit, turret, s);

        if(s.buffActive) {
            s.buffTimer -= Time.delta;
            if(s.buffTimer <= 0f) {
                s.buffActive = false;
                s.buffTimer = 0f;
            } else if(unit != null) {
                applyFlatBuffsToUnit(unit, 1f);
            }
        }
    }

    public float getBuffScale(PerkStateData s) {
        return s.buffActive ? 1f : 0f;
    }

    public float getBuffProgress(PerkStateData s) {
        if(!s.buffActive || duration <= 0f) return 0f;
        return s.buffTimer / duration;
    }

    @Override
    public float getProgress(PerkStateData s) {
        if(s.buffActive) return getBuffProgress(s);
        return super.getProgress(s);
    }

    @Override
    public Perk copy() {
        DurationPerk copy = new DurationPerk();
        copyBaseTo(copy);
        copy.duration = duration;
        return copy;
    }
}