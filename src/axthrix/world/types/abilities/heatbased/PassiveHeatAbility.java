package axthrix.world.types.abilities.heatbased;

import arc.util.Time;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;

public class PassiveHeatAbility extends Ability {
    public float idleHeatGain = 0.8f; // Heat per second when no enemies
    public float bytickHeatLoss = 1.2f; // Heat per second lost when damaging enemies
    public float singleTickHeatLoss = 0.25f;
    public boolean scaleByCurrent = true;

    public float maxSelfHeat = 100f;

    /// Set to true by child abilities when they're dealing damage for bytickheatloss
    protected boolean isDealingDamage = false;

    @Override
    public void update(Unit unit){
        if(isDealingDamage){
            // Lose heat when attacking
            TemperatureLogic.applyTemperatureUnit(unit, -bytickHeatLoss * Time.delta / 60f);
        } else {
            // Gain heat when idle
            TemperatureLogic.applyTemperatureUnit(unit, idleHeatGain * Time.delta / 60f);
        }

        // Reset flag for next frame
        isDealingDamage = false;
    }
    /**
     * Removes a percentage of heat instantly
     */
    public void removeInstantHeat(Unit unit){
        if(scaleByCurrent){
            float ownHeat = TemperatureLogic.getHeatUnit(unit);
            TemperatureLogic.applyTemperatureUnit(unit, -(ownHeat*singleTickHeatLoss));
        }else{
            TemperatureLogic.applyTemperatureUnit(unit, -(maxSelfHeat*singleTickHeatLoss));
        }
    }

    /**
     * Calculate damage bonus based on unit's own heat
     * @param unit The unit with this ability
     * @param baseDamage Base damage to scale
     * @param selfHeatMultiplier Multiplier per degree of heat (e.g., 0.01 = 1% per degree)
     * @return Scaled damage
     */
    protected float getScaledDamage(Unit unit, float baseDamage, float selfHeatMultiplier){
        float ownHeat = TemperatureLogic.getHeatUnit(unit);
        float heatPercent = ownHeat / maxSelfHeat;
        float multiplier = 1f + (heatPercent * selfHeatMultiplier * 100f);
        return baseDamage * multiplier;
    }

    /**
     * Calculate bonus damage based on target's heat with resistance factored in
     * @param targetHeat Target's heat level
     * @param targetEffectResist Target's effect resistance (0-1+)
     * @param baseDamage Base damage amount
     * @param targetHeatDamageBonus Bonus per degree (e.g., 0.01 = 1% per degree)
     * @return Final damage with heat bonus
     */
    protected float applyTargetHeatBonus(float targetHeat, float targetEffectResist, float baseDamage, float targetHeatDamageBonus){
        // Resistance reduces effectiveness: 0.2 resist = only 20% of bonus applies
        float effectiveBonusRate = targetHeatDamageBonus * (1f - targetEffectResist);
        float heatBonus = 1f + (targetHeat * effectiveBonusRate);
        return baseDamage * heatBonus;
    }

    @Override
    public String localized(){
        return "Passive Heat Generation";
    }
}