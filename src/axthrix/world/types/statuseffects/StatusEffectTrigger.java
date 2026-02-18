package axthrix.world.types.statuseffects;

import axthrix.content.AxthrixStatus;
import axthrix.world.types.statuseffects.AxStatusEffect;
import mindustry.content.StatusEffects;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class StatusEffectTrigger extends AxStatusEffect {
    public boolean activationRepair = false, Reap = false;
    public StatusEffect activationStatusFx = AxthrixStatus.bFx;
    ///by percentage  1.25/75%  2/50%  3.33/30%  4/25%  5/20%  6.66/15%  10/10%
    public float activationThreshold = 2, ActivationRepairAmount = 2,

                 activationResistanceTime = 60;

    public StatusEffectTrigger(String name) {super(name);}

    @Override
    public void update(Unit unit, StatusEntry entry) {
        if (unit.health < unit.maxHealth() / activationThreshold) {
            unit.apply(activationStatusFx);
            if(Reap){
                unit.kill();
            }else{
                if (activationRepair) {
                    unit.health = unit.maxHealth() / ActivationRepairAmount;
                }
                unit.add();
                unit.dead = false;
                unit.apply(StatusEffects.invincible ,activationResistanceTime);
            }
            unit.unapply(this);
        }
    }
}

