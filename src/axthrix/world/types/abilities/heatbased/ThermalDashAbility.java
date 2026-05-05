package axthrix.world.types.abilities.heatbased;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.input.Binding;

public class ThermalDashAbility extends PassiveHeatAbility {
    public float dashDistance = 2.5f;
    public float dodgeDistance = 1.5f; // Shorter dodge dash
    public float dashSpeed = 30f;
    public float cooldown = 240f;

    public float burstRadius = 80f;
    public float burstDamage = 4000f;
    public float burstTemperature = 20f;
    public int maxChainTargets = 7;

    public float aiDodgeDetectRadius = 100f;
    public float aiAggroDetectRadius = 80f;
    public float aiDodgePriority = 0.7f;

    public float targetHeatDamageBonus = 0.02f;
    public float selfHeatMultiplier = 0.01f;

    public boolean damageBuildings = true;
    public float buildingDamageMultiplier = 0.6f;

    public Effect collideEffect = Fx.lightning;
    public Effect trailEffect;

    public Color dashColor = Color.cyan;

    private static ObjectMap<Integer, Boolean> autoModeMap = new ObjectMap<>();

    private ObjectMap<Integer, Float> cooldownTimer = new ObjectMap<>();
    private ObjectMap<Integer, Float> dashProgress = new ObjectMap<>();
    private ObjectMap<Integer, Float> dashAngle = new ObjectMap<>();
    private ObjectMap<Integer, Boolean> isDodgeDash = new ObjectMap<>(); // Track if this is a dodge

    public ThermalDashAbility(){
        singleTickHeatLoss = 0.15f;
        bytickHeatLoss = 2f;
        idleHeatGain = 4;
        scaleByCurrent = false;
    }

    @Override
    public void update(Unit unit){
        super.update(unit);

        if(!cooldownTimer.containsKey(unit.id)){
            cooldownTimer.put(unit.id, 0f);
            dashProgress.put(unit.id, 0f);
            dashAngle.put(unit.id, 0f);
            isDodgeDash.put(unit.id, false);
        }

        if(!autoModeMap.containsKey(unit.id)){
            autoModeMap.put(unit.id, true);
        }

        float timer = cooldownTimer.get(unit.id);
        float progress = dashProgress.get(unit.id);

        if(timer > 0f){
            cooldownTimer.put(unit.id, timer - Time.delta);
        }

        float heat = TemperatureLogic.getHeatUnit(unit);

        // Currently dashing
        if(progress > 0f && progress < 1f){
            isDealingDamage = true;

            float angle = dashAngle.get(unit.id);
            float moveAmount = dashSpeed * Time.delta;

            Tmp.v1.trns(angle, moveAmount);
            unit.move(Tmp.v1.x, Tmp.v1.y);

            if(Mathf.chance(0.8)){
                if(trailEffect == null){
                    float anglefx = Mathf.random(360f);
                    float dist = Mathf.random(unit.hitSize*4);
                    AxthrixFfx.lightningPart(unit.x, unit.y, Mathf.random(1f, 4f), 29f, 0, dashColor).at(
                            unit.x + Mathf.cosDeg(angle) * dist,
                            (unit.y - 5)+ Mathf.sinDeg(angle) * dist,
                            anglefx
                    );
                } else {
                    trailEffect.at(unit.x, unit.y);
                }
            }

            performThermalBurst(unit);

            // Use dodge distance if this is a dodge dash
            float currentDashDistance = isDodgeDash.get(unit.id) ? dodgeDistance : dashDistance;
            progress += Time.delta / (currentDashDistance / dashSpeed * 60f);
            dashProgress.put(unit.id, Math.min(progress, 1f));

            return;
        } else if(progress >= 1f){
            dashProgress.put(unit.id, 0f);
            isDodgeDash.put(unit.id, false);
        }

        boolean shouldDash = false;
        float targetAngle = 0f;
        boolean isDodge = false;

        if(unit.isPlayer()){
            if(Core.input.keyTap(Binding.control) && !Core.app.isMobile()){
                boolean current = autoModeMap.get(unit.id);
                autoModeMap.put(unit.id, !current);
            }

            if(autoModeMap.get(unit.id)){
                Object[] decision = makeAIDecision(unit);
                shouldDash = (boolean)decision[0];
                targetAngle = (float)decision[1];
                isDodge = (boolean)decision[2];
            } else {
                if(Core.input.keyTap(Binding.boost)){
                    targetAngle = unit.rotation;
                    shouldDash = true;
                    isDodge = false; // Manual dash is always full distance
                }
            }
        } else {
            Object[] decision = makeAIDecision(unit);
            shouldDash = (boolean)decision[0];
            targetAngle = (float)decision[1];
            isDodge = (boolean)decision[2];
        }

        float heatCost = scaleByCurrent ? heat * singleTickHeatLoss : maxSelfHeat * singleTickHeatLoss;

        if(shouldDash && timer <= 0f && heat >= heatCost){
            removeInstantHeat(unit);

            dashAngle.put(unit.id, targetAngle);
            dashProgress.put(unit.id, 0.01f);
            isDodgeDash.put(unit.id, isDodge);
            cooldownTimer.put(unit.id, cooldown);

            Fx.hitLancer.at(unit.x, unit.y, targetAngle, dashColor);
        }
    }

    private Object[] makeAIDecision(Unit unit){
        boolean shouldDash = false;
        float targetAngle = unit.rotation;
        boolean isDodge = false;

        boolean incomingThreat = Groups.bullet.contains(bullet -> {
            if(bullet.team != unit.team &&
                    unit.within(bullet, aiDodgeDetectRadius) &&
                    bullet.type != null){
                Tmp.v1.set(bullet.vel).scl(30f);
                return unit.within(bullet.x + Tmp.v1.x, bullet.y + Tmp.v1.y, unit.hitSize);
            }
            return false;
        });

        Unit closestEnemy = Units.closestEnemy(unit.team, unit.x, unit.y, aiAggroDetectRadius, u -> true);
        boolean enemyInFront = closestEnemy != null &&
                Angles.within(unit.angleTo(closestEnemy), unit.rotation, 45f);

        if(incomingThreat && Mathf.chance(aiDodgePriority)){
            targetAngle = unit.rotation + (Mathf.randomBoolean() ? 90f : -90f);
            shouldDash = true;
            isDodge = true; // This is a dodge
        } else if(enemyInFront){
            targetAngle = unit.angleTo(closestEnemy);
            shouldDash = true;
        }

        return new Object[]{shouldDash, targetAngle, isDodge};
    }

    private void performThermalBurst(Unit unit){
        Seq<Unit> hitUnits = new Seq<>();
        Seq<Building> hitBuildings = new Seq<>();

        float scaledDamage = getScaledDamage(unit, burstDamage, selfHeatMultiplier);

        Units.nearbyEnemies(unit.team, unit.x, unit.y, burstRadius, target -> {
            if(hitUnits.size < maxChainTargets){
                hitUnits.add(target);
            }
        });

        if(damageBuildings){
            Units.nearbyBuildings(unit.x, unit.y, burstRadius, building -> {
                if(building.team != unit.team &&
                        building.block.destructible &&
                        hitBuildings.size < maxChainTargets){
                    hitBuildings.add(building);
                }
            });
        }

        Unit lastTarget = unit;
        for(Unit target : hitUnits){
            float targetHeat = TemperatureLogic.getHeatUnit(target);
            float targetEffectResist = TemperatureLogic.getEffectResistanceHeatUnit(target);

            float finalDamage = applyTargetHeatBonus(targetHeat, targetEffectResist, scaledDamage, targetHeatDamageBonus);
            target.damage(finalDamage);
            TemperatureLogic.applyTemperatureUnit(target, burstTemperature);

            if(collideEffect != null){
                collideEffect.at(lastTarget.x, lastTarget.y, 0f, new float[]{target.x, target.y});
            }
            lastTarget = target;
        }

        for(Building building : hitBuildings){
            float buildingHeat = TemperatureLogic.getHeatBuilding(building);
            float buildingEffectResist = TemperatureLogic.getEffectResistanceHeatBuilding(building);

            float finalDamage = applyTargetHeatBonus(buildingHeat, buildingEffectResist, scaledDamage, targetHeatDamageBonus);

            building.damage(finalDamage * buildingDamageMultiplier);
            TemperatureLogic.applyTemperatureBuilding(building, burstTemperature);

            if(collideEffect != null){
                collideEffect.at(unit.x, unit.y, 0f, new float[]{building.x, building.y});
            }
        }
    }

    @Override
    public void draw(Unit unit){
        if(!cooldownTimer.containsKey(unit.id)) return;

        float timer = cooldownTimer.get(unit.id);
        float progress = dashProgress.get(unit.id);

        if(timer > 0f && progress == 0f){
            Draw.z(29f);

            float cooldownProgress = 1f - (timer / cooldown);
            Draw.color(dashColor, Color.white, cooldownProgress);
            Draw.alpha(0.5f);

            Fill.arc(unit.x, unit.y - unit.hitSize - 4f, 6f, cooldownProgress, unit.rotation - 90f);

            Draw.reset();
        }

        if(progress > 0f && progress < 1f){
            Draw.z(29f);
            Draw.color(dashColor);
            Draw.alpha(0.6f);
            Fill.circle(unit.x, unit.y, unit.hitSize * 1.3f);
            Draw.reset();
        }
    }

    @Override
    public String localized(){
        return "Thermal Dash";
    }
}