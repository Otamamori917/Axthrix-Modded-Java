package axthrix.world.types.statuseffects;

import axthrix.content.AxthrixStatus;
import axthrix.world.types.statuseffects.AxStatusEffect;
import mindustry.content.StatusEffects;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class StatusEffectTrigger extends AxStatusEffect {
    public boolean activationRepair = false;
    public StatusEffect activationStatusFx = AxthrixStatus.bFx;
    /*by percentage  1.25/75%  2/50%  3.33/30%  4/25%  5/20%  6.66/15%  10/10%*/
    public float activationThreshold = 2, ActivationRepairAmount = 2,

                 activationResistanceTime = 60;

    public StatusEffectTrigger(String name) {super(name);}

    @Override
    public void update(Unit unit, float time) {
        if (unit.health < unit.maxHealth() / activationThreshold) {
            unit.apply(activationStatusFx);
            if (activationRepair) {
                unit.health = unit.maxHealth() / ActivationRepairAmount;
            }
            unit.add();
            unit.dead = false;
            unit.unapply(this);
            unit.apply(StatusEffects.invincible ,activationResistanceTime);
        }
    }
}
