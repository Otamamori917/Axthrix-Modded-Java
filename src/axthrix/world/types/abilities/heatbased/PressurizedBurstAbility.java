package axthrix.world.types.abilities.heatbased;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Unit;

public class PressurizedBurstAbility extends PassiveHeatAbility {
    public float chargeTime = 120f;
    public float burstRadius = 100f;
    public float burstDamage = 40f;
    public float burstTemperature = 25f;
    public float knockback = 8f;
    public float selfHeatMultiplier = 0.015f; // 1.5% per degree

    // Damage scaling
    public float targetHeatDamageBonus = 0.02f;

    // Building damage
    public boolean damageBuildings = true;
    public float buildingDamageMultiplier = 0.8f;

    private ObjectMap<Integer, Float> chargeProgress = new ObjectMap<>();
    private ObjectMap<Integer, Vec2> lastPosition = new ObjectMap<>();

    public PressurizedBurstAbility(){
        singleTickHeatLoss = 0.05f;
        bytickHeatLoss = 0;

        idleHeatGain = 4;
    }

    @Override
    public void update(Unit unit){
        super.update(unit); // Handle heat generation

        if(!chargeProgress.containsKey(unit.id)){
            chargeProgress.put(unit.id, 0f);
            lastPosition.put(unit.id, new Vec2(unit.x, unit.y));
        }

        Vec2 lastPos = lastPosition.get(unit.id);
        boolean moving = unit.vel.len() > 0.1f || !unit.within(lastPos.x, lastPos.y, 2f);

        if(moving){
            float charge = chargeProgress.get(unit.id);
            charge += Time.delta;

            if(charge >= chargeTime){
                removeInstantHeat(unit);

                float scaledDamage = getScaledDamage(unit, burstDamage, selfHeatMultiplier);

                Units.nearbyEnemies(unit.team, unit.x, unit.y, burstRadius, target -> {
                    float targetHeat = TemperatureLogic.getHeatUnit(target);
                    float targetEffectResist = TemperatureLogic.getEffectResistanceHeatUnit(target);

                    float finalDamage = applyTargetHeatBonus(targetHeat, targetEffectResist, scaledDamage, targetHeatDamageBonus);

                    target.damage(finalDamage);
                    TemperatureLogic.applyTemperatureUnit(target, burstTemperature);

                    float angle = Angles.angle(unit.x, unit.y, target.x, target.y);
                    Tmp.v1.trns(angle, knockback);
                    target.vel.add(Tmp.v1);
                });

                if(damageBuildings){
                    Seq<Building> targets = new Seq<>();
                    Units.nearbyBuildings(unit.x, unit.y, burstRadius, building -> {
                        if(building.team != unit.team && building.block.destructible){
                            targets.add(building);
                        }
                    });

                    for(Building building : targets){
                        float buildingHeat = TemperatureLogic.getHeatBuilding(building);
                        float buildingEffectResist = TemperatureLogic.getEffectResistanceHeatBuilding(building);

                        float finalDamage = applyTargetHeatBonus(buildingHeat, buildingEffectResist, scaledDamage, targetHeatDamageBonus);

                        building.damage(finalDamage * buildingDamageMultiplier);
                        TemperatureLogic.applyTemperatureBuilding(building, burstTemperature);
                    }
                }

                Fx.massiveExplosion.at(unit.x, unit.y, burstRadius);
                Effect.shake(3f, 3f, unit);
                charge = 0f;
            }

            chargeProgress.put(unit.id, charge);
        }

        lastPosition.get(unit.id).set(unit.x, unit.y);
    }

    @Override
    public void draw(Unit unit){
        if(chargeProgress.containsKey(unit.id)){
            float progress = chargeProgress.get(unit.id) / chargeTime;
            Draw.z(29f);
            Draw.color(Color.orange, Color.red, progress);
            if(progress > 0){
                Draw.alpha(0.3f + progress * 0.4f);
                Lines.stroke(2f + progress * 2f);
                Lines.circle(unit.x, unit.y, unit.hitSize + progress * 10f);
                Draw.reset();
            }
        }
    }

    @Override
    public String localized(){
        return "Pressurized Burst";
    }
}