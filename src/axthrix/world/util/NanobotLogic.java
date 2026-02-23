package axthrix.world.util;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

import static mindustry.Vars.*;

public class NanobotLogic {

    // Static map for nanobots - supports both units and buildings
    private static final HashMap<Object, Seq<Nanobot>> ownerNanobots = new HashMap<>();

    // Shared parameters
    public static class NanobotParams {
        public float x, y; // Position
        public float damage = 2f;
        public float healAmount = 3f;
        public float healPercent = 1f;
        public float range = 90f;
        public float tickRate = 5f;
        public float buildingDamageMultiplier = 0.25f;

        public float bulletSpeedBonus = 1.5f;
        public float bulletSlowdown = 0.6f;

        public float efficiencyBoost = 1.5f;
        public float stackPenalty = 0.5f;

        public StatusEffect status;
        public float statusDuration = 60f * 3f;

        public Effect healEffect = Fx.heal;
        public Effect damageEffect = Fx.hitLaserBlast;

        public Color color = Color.valueOf("84f491");
        public int nanobotCount = 40;
        public float nanobotSize = 2f;
        public float nanobotSpeed = 2f;

        public mindustry.game.Team team;
        public boolean hasNanobot = false; // For anti-stacking check
    }

    public static void initNanobots(Object owner, float x, float y, int count){
        if(!ownerNanobots.containsKey(owner)){
            Seq<Nanobot> nanobots = new Seq<>();
            for(int i = 0; i < count; i++){
                nanobots.add(new Nanobot(x, y));
            }
            ownerNanobots.put(owner, nanobots);
        }
    }

    public static void removeNanobots(Object owner){
        ownerNanobots.remove(owner);
    }

    public static Seq<Nanobot> getNanobots(Object owner){
        return ownerNanobots.get(owner);
    }

    public static void cleanupInvalid(){
        ownerNanobots.entrySet().removeIf(entry -> {
            Object owner = entry.getKey();
            if(owner instanceof Unit u) return !u.isValid();
            if(owner instanceof Building b) return !b.isValid();
            return true;
        });
    }

    public static void drawNanobots(Object owner, NanobotParams params){
        Seq<Nanobot> nanobots = ownerNanobots.get(owner);
        if(nanobots == null) return;

        Draw.z(Layer.flyingUnit + 1);

        for(Nanobot nano : nanobots){
            nano.update(params.x, params.y, params.range, params.nanobotSpeed);

            Draw.color(params.color, nano.alpha);
            Fill.circle(nano.x, nano.y, params.nanobotSize * nano.scale);

            Draw.color(Color.white, nano.alpha * 0.7f);
            Fill.circle(nano.x, nano.y, params.nanobotSize * nano.scale * 0.5f);

            if(nano.targetX != 0 || nano.targetY != 0){
                Draw.color(params.color, nano.alpha * 0.3f);
                Lines.stroke(1f);
                Lines.line(nano.x, nano.y,
                        Mathf.lerp(nano.x, nano.orbitX, 0.3f),
                        Mathf.lerp(nano.y, nano.orbitY, 0.3f));
            }
        }

        Draw.reset();
    }

    public static void updateNanobots(Object owner, NanobotParams params, float timer, boolean useAmmo, Runnable consumeAmmo){
        Seq<Nanobot> nanobots = ownerNanobots.get(owner);
        if(nanobots == null) return;

        // Apply effects every tickRate
        if(timer >= params.tickRate && (!useAmmo || consumeAmmo == null)){

            // Heal/damage units
            Units.nearby(null, params.x, params.y, params.range, other -> {
                if(other.team == params.team){
                    if(other.damaged()){
                        float healMultiplier = params.hasNanobot ? params.stackPenalty : 1f;
                        float totalHeal = (params.healAmount + (other.maxHealth * params.healPercent / 100f)) * healMultiplier;
                        other.heal(totalHeal);
                        params.healEffect.at(other.x, other.y, 0f, params.color);
                        assignNanobots(owner, other.x, other.y, true);
                    }
                }else{
                    other.damage(params.damage * state.rules.unitDamage(params.team));
                    other.apply(params.status, params.statusDuration);
                    params.damageEffect.at(other.x, other.y, 0f, params.color);
                    assignNanobots(owner, other.x, other.y, false);
                }
            });

            // Heal/damage buildings
            indexer.eachBlock(null, params.x, params.y, params.range, other -> true, building -> {
                if(building.team == params.team){
                    if(building.damaged()){
                        float totalHeal = params.healAmount + (building.maxHealth * params.healPercent / 100f);
                        building.heal(totalHeal);
                        Fx.healBlockFull.at(building.x, building.y, 0f, params.color, building.block);
                        assignNanobots(owner, building.x, building.y, true);
                    }
                }else{
                    building.damage(params.damage * params.buildingDamageMultiplier * state.rules.unitDamage(params.team));
                    params.damageEffect.at(building.x, building.y, 0f, params.color);
                    assignNanobots(owner, building.x, building.y, false);
                }
            });

            // Affect bullets
            Groups.bullet.intersect(params.x - params.range, params.y - params.range, params.range * 2, params.range * 2, bullet -> {
                if(bullet.within(params.x, params.y, params.range)){
                    if(bullet.team == params.team){
                        bullet.vel.scl(params.bulletSpeedBonus);
                        if(Mathf.chance(0.1f)){
                            assignNanobots(owner, bullet.x, bullet.y, true);
                        }
                    }else{
                        bullet.vel.scl(params.bulletSlowdown);
                        if(Mathf.chance(0.1f)){
                            assignNanobots(owner, bullet.x, bullet.y, false);
                            Fx.hitLaserBlast.at(bullet.x, bullet.y, 0f, params.color);
                        }
                    }
                }
            });

            if(consumeAmmo != null){
                consumeAmmo.run();
            }
        }
    }

    public static void updateBoost(Object owner, NanobotParams params){
        indexer.eachBlock(null, params.x, params.y, params.range, other -> other.team == params.team, building -> {
            if(building.block.canOverdrive){
                building.applyBoost(params.efficiencyBoost, params.tickRate+1);
                if(Mathf.chance(0.05f)){
                    assignNanobots(owner, building.x, building.y, true);
                }
            }
        });
    }

    public static void assignNanobots(Object owner, float targetX, float targetY, boolean healing){
        Seq<Nanobot> nanobots = ownerNanobots.get(owner);
        if(nanobots == null) return;

        int assigned = 0;
        int toAssign = Mathf.random(3, 6);

        for(Nanobot nano : nanobots){
            if(assigned >= toAssign) break;
            if(nano.targetX == 0 && nano.targetY == 0){
                nano.targetX = targetX;
                nano.targetY = targetY;
                nano.healing = healing;
                nano.targetTimer = 30f;
                assigned++;
            }
        }
    }

    public static class Nanobot {
        public float x, y;
        public float orbitX, orbitY;
        public float targetX, targetY;
        public float angle;
        public float orbitRadius;
        public float verticalAngle;
        public float alpha = 1f;
        public float scale = 1f;
        public boolean healing = false;
        public float targetTimer = 0f;

        public Nanobot(float spawnX, float spawnY){
            x = spawnX;
            y = spawnY;
            angle = Mathf.random(360f);
            verticalAngle = Mathf.random(360f);
            float randomFactor = Mathf.pow(Mathf.random(), 1f/3f);
            orbitRadius = Mathf.random(5f, 95f) * randomFactor;
        }

        public void update(float centerX, float centerY, float maxRange, float speed){
            if(Vars.state.isPaused()) return;

            angle += speed * Time.delta;
            verticalAngle += speed * Time.delta * 0.7f;

            if(Mathf.chance(0.02f)){
                float newRadius = Mathf.random(5f, maxRange * 0.95f);
                float randomFactor = Mathf.pow(Mathf.random(), 1f/3f);
                orbitRadius = Mathf.lerp(orbitRadius, newRadius * randomFactor, 0.1f);
            }

            float horizontalRadius = orbitRadius * Mathf.cos(verticalAngle * Mathf.degreesToRadians);
            Tmp.v1.trns(angle, horizontalRadius);

            orbitX = centerX + Tmp.v1.x;
            orbitY = centerY + Tmp.v1.y;

            if(targetTimer > 0){
                targetTimer -= Time.delta;
                x = Mathf.lerp(x, targetX, 0.3f);
                y = Mathf.lerp(y, targetY, 0.3f);
                scale = 1.2f;
                alpha = 1f;

                if(targetTimer <= 0){
                    targetX = 0;
                    targetY = 0;
                }
            }else{
                x = Mathf.lerp(x, orbitX, 0.05f);
                y = Mathf.lerp(y, orbitY, 0.05f);
                float depthFactor = (Mathf.sin(verticalAngle * Mathf.degreesToRadians) + 1f) / 2f;
                scale = 0.7f + depthFactor * 0.5f;
                alpha = 0.5f + depthFactor * 0.5f;
            }
        }
    }
}