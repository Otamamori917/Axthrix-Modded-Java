package axthrix.world.types.abilities.heatbased;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;

public class ThermalRadiationAbility extends PassiveHeatAbility {
    public float baseRadius = 60f;
    public float maxRadius = 200f;

    public float baseDamage = 200f;
    public float baseTemperature = 2f;
    public float selfHeatMultiplier = 0.015f; // 1.5% per degree

    public float targetHeatDamageBonus = 0.02f;

    public boolean damageBuildings = true;
    public float buildingDamageMultiplier = 0.5f;

    public Color coldColor = Color.orange;
    public Color hotColor = Color.red;

    public ThermalRadiationAbility(){
        singleTickHeatLoss = 0;
        bytickHeatLoss = 1.5f;

        idleHeatGain = 8;
    }

    @Override
    public void update(Unit unit){
        super.update(unit); // Handle heat generation

        float ownHeat = TemperatureLogic.getHeatUnit(unit);
        float heatPercent = ownHeat / maxSelfHeat;

        float radius = Mathf.lerp(baseRadius, maxRadius, heatPercent);
        float scaledDamage = getScaledDamage(unit, baseDamage, selfHeatMultiplier);
        float damage = scaledDamage * Time.delta / 60f;
        float temp = baseTemperature * (1f + heatPercent * selfHeatMultiplier * 100f) * Time.delta / 60f;

        boolean hasUnits = Units.anyEntities(
                unit.x - radius, unit.y - radius,
                radius * 2f, radius * 2f,
                e -> e.team != unit.team &&
                        e.within(unit, radius) && !e.dead
        );

        Seq<Building> buildingTargets = new Seq<>();
        if(damageBuildings){
            Units.nearbyBuildings(unit.x, unit.y, radius, building -> {
                if(building.team != unit.team && building.block.destructible){
                    buildingTargets.add(building);
                }
            });
        }

        boolean hasEnemies = hasUnits || buildingTargets.size > 0;

        if(hasEnemies){
            isDealingDamage = true;

            Units.nearbyEnemies(unit.team, unit.x, unit.y, radius, target -> {
                float dst = unit.dst(target);
                float falloff = 1f - (dst / radius);

                float targetHeat = TemperatureLogic.getHeatUnit(target);
                float targetEffectResist = TemperatureLogic.getEffectResistanceHeatUnit(target);

                float finalDamage = applyTargetHeatBonus(targetHeat, targetEffectResist, damage, targetHeatDamageBonus);

                target.damage(finalDamage * falloff);
                TemperatureLogic.applyTemperatureUnit(target, temp * falloff);
            });

            for(Building building : buildingTargets){
                float dst = unit.dst(building);
                float falloff = 1f - (dst / radius);

                float buildingHeat = TemperatureLogic.getHeatBuilding(building);
                float buildingEffectResist = TemperatureLogic.getEffectResistanceHeatBuilding(building);

                float finalDamage = applyTargetHeatBonus(buildingHeat, buildingEffectResist, damage, targetHeatDamageBonus);

                building.damage(finalDamage * falloff * buildingDamageMultiplier);
                TemperatureLogic.applyTemperatureBuilding(building, temp * falloff);
            }
        }
    }

    @Override
    public void draw(Unit unit){
        float ownHeat = TemperatureLogic.getHeatUnit(unit);
        float heatPercent = ownHeat / maxSelfHeat;
        float radius = Mathf.lerp(baseRadius, maxRadius, heatPercent);

        Draw.z(29f);

        Draw.color(coldColor, hotColor, heatPercent);
        Draw.alpha(heatPercent * 0.15f);
        Fill.circle(unit.x, unit.y, radius);

        Draw.color(coldColor, hotColor, heatPercent);
        Draw.alpha((1f - heatPercent) * 0.5f);
        Lines.stroke(2f);
        Lines.circle(unit.x, unit.y, radius);

        if(heatPercent > 0.3f){
            int waves = (int)(heatPercent * 3f) + 1;
            for(int i = 0; i < waves; i++){
                float waveTime = (Time.time + i * 30f) % 120f;
                float waveProgress = waveTime / 120f;
                float waveRadius = radius * waveProgress;
                float waveAlpha = (1f - waveProgress) * heatPercent * 0.7f;

                Draw.color(hotColor);
                Draw.alpha(waveAlpha);
                Lines.stroke(3f);
                Lines.circle(unit.x, unit.y, waveRadius);
            }
        }

        Draw.reset();
    }

    @Override
    public String localized(){
        return "Thermal Radiation";
    }
}