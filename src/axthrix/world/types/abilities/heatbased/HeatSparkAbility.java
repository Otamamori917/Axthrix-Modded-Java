package axthrix.world.types.abilities.heatbased;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;


public class HeatSparkAbility extends PassiveHeatAbility {
    public float damageThreshold = 50f;
    public float heatDamage = 15f;
    public float heatRadius = 40f;
    public float temperaturePerHit = 8f;
    public float selfHeatMultiplier = 0.01f; // 1% per degree of own heat

    // Damage scaling
    public float targetHeatDamageBonus = 0.02f; // 2% per degree of target heat

    // Building damage
    public boolean damageBuildings = true;
    public float buildingDamageMultiplier = 0.5f;

    private ObjectMap<Integer, Float> lastHealthCheck = new ObjectMap<>();

    public HeatSparkAbility(){
        singleTickHeatLoss = 0.15f;
        bytickHeatLoss = 0;
        idleHeatGain = 5;
    }

    @Override
    public void update(Unit unit){
        super.update(unit); // Handle heat generation

        if(!lastHealthCheck.containsKey(unit.id)){
            lastHealthCheck.put(unit.id, unit.health);
        }

        float lastHealth = lastHealthCheck.get(unit.id);

        if(lastHealth - unit.health >= damageThreshold){
            removeInstantHeat(unit);

            // Scale damage by unit's own heat
            float scaledDamage = getScaledDamage(unit, heatDamage, selfHeatMultiplier);

            // Discharge heat to units
            Units.nearbyEnemies(unit.team, unit.x, unit.y, heatRadius, target -> {
                float targetHeat = TemperatureLogic.getHeatUnit(target);
                float targetEffectResist = TemperatureLogic.getEffectResistanceHeatUnit(target);

                float finalDamage = applyTargetHeatBonus(targetHeat, targetEffectResist, scaledDamage, targetHeatDamageBonus);

                target.damage(finalDamage);
                TemperatureLogic.applyTemperatureUnit(target, temperaturePerHit);
            });

            // Discharge heat to buildings
            if(damageBuildings){
                Seq<Building> targets = new Seq<>();
                Units.nearbyBuildings(unit.x, unit.y, heatRadius, building -> {
                    if(building.team != unit.team && building.block.destructible){
                        targets.add(building);
                    }
                });

                for(Building building : targets){
                    float buildingHeat = TemperatureLogic.getHeatBuilding(building);
                    float buildingEffectResist = TemperatureLogic.getEffectResistanceHeatBuilding(building);

                    float finalDamage = applyTargetHeatBonus(buildingHeat, buildingEffectResist, scaledDamage, targetHeatDamageBonus);

                    building.damage(finalDamage * buildingDamageMultiplier);
                    TemperatureLogic.applyTemperatureBuilding(building, temperaturePerHit);
                }
            }
            Fx.explosion.at(unit.x, unit.y, heatRadius);
            lastHealthCheck.put(unit.id, unit.health);
        }
        if(unit.health > lastHealth){
            lastHealthCheck.put(unit.id, unit.health);
        }
    }

    @Override
    public String localized(){
        return "Heat Spark";
    }
}