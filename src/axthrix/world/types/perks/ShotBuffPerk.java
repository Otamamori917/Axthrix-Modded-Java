package axthrix.world.types.perks;

import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

public class ShotBuffPerk extends Perk {

    /**
     * If true, all stacks consumed when the turret/unit fires.
     * If false, stacks persist until a miss or decay timer clears them.
     */
    public boolean resetOnShot = true;

    public ShotBuffPerk() {
        name = "ShotBuff";
        consumesOnActivate = false;
        decaysOnMiss = true;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY) {
        // buffPending tracked via s.isActivated for resetOnShot mode
    }

    @Override
    public void onReset(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        s.isActivated = false;
    }

    @Override
    public void update(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        super.update(unit, turret, s);
        if(unit != null && !resetOnShot && s.currentStacks > 0) {
            applyStackingBuffsToUnit(unit, s);
        }
    }

    public float getShotDamageMultiplier(PerkStateData s) {
        if(resetOnShot) return s.isActivated ? getTurretDamageMultiplier(s) : 1f;
        return getTurretDamageMultiplier(s);
    }

    public float getShotReloadMultiplier(PerkStateData s) {
        if(resetOnShot) return s.isActivated ? getTurretReloadMultiplier(s) : 1f;
        return getTurretReloadMultiplier(s);
    }

    public void consumeShot(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        if(!resetOnShot) return;
        s.isActivated = false;
        s.currentStacks = 0;
        s.hitProgress = 0;
    }

    @Override
    public Perk copy() {
        ShotBuffPerk copy = new ShotBuffPerk();
        copyBaseTo(copy);
        copy.resetOnShot = resetOnShot;
        return copy;
    }
}