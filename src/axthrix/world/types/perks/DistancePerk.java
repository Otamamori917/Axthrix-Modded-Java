package axthrix.world.types.perks;

import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

public class DistancePerk extends Perk {

    /** How many ticks the per-shot buff lasts after shooting. */
    public float shotBuffDuration = 1f;

    public DistancePerk() {
        name = "Distance";
        triggerMode = TriggerMode.DISTANCE;
        consumesOnActivate = false;
        decaysOnMiss = false;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY) {}

    @Override
    public void update(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        super.update(unit, turret, s);

        if(s.shotBuffTimer > 0f) {
            s.shotBuffTimer -= arc.util.Time.delta;
            if(unit != null && s.stacksOnFire > 0) {
                if(reloadBuff > 0f) unit.reloadMultiplier *= Math.max(0.1f, 1f - reloadBuff * s.stacksOnFire);
                if(damageBuff > 0f) unit.damageMultiplier += damageBuff * s.stacksOnFire;
                if(resistanceBuff > 0f) unit.armor += resistanceBuff * s.stacksOnFire;
                if(speedBuff > 0f) unit.speedMultiplier += speedBuff * s.stacksOnFire;
            }
            if(s.shotBuffTimer <= 0f) s.stacksOnFire = 0;
        }
    }

    public void onShoot(Unit unit, Turret.TurretBuild turret, PerkStateData s) {
        if(s.currentStacks <= 0) return;
        s.stacksOnFire = s.currentStacks;
        s.currentStacks = 0;
        s.hitProgress = 0;
        s.isActivated = false;
        s.shotBuffTimer = shotBuffDuration;
    }

    public float getConsumedDamageMultiplier(PerkStateData s) {
        if(s.stacksOnFire <= 0) return 1f;
        return 1f + damageBuff * s.stacksOnFire;
    }

    public float getConsumedReloadMultiplier(PerkStateData s) {
        if(s.stacksOnFire <= 0) return 1f;
        return Math.max(0.1f, 1f - reloadBuff * s.stacksOnFire);
    }

    public float getConsumedRangeBonus(PerkStateData s) {
        if(s.stacksOnFire <= 0) return 0f;
        return rangeBuff * s.stacksOnFire;
    }

    @Override
    public Perk copy() {
        DistancePerk copy = new DistancePerk();
        copyBaseTo(copy);
        copy.shotBuffDuration = shotBuffDuration;
        return copy;
    }
}