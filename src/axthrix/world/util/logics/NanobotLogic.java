package axthrix.world.util.logics;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.Seq;
import arc.util.*;
import axthrix.world.types.bulletypes.NanobotBulletType;
import mindustry.*;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.StatusEffect;
import axthrix.world.types.AxLayers;
import axthrix.world.types.abilities.NanobotAbility;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.util.AxShaders;

import java.util.HashMap;

import static mindustry.Vars.*;

public class NanobotLogic {

    private static final HashMap<Object, Seq<Nanobot>> ownerNanobots = new HashMap<>();
    private static final HashMap<Object, Float> healEffectTimers = new HashMap<>();
    private static final HashMap<Object, Float> damageEffectTimers = new HashMap<>();
    private static final float effectInterval = 10f;

    public static class NanobotParams {
        public float x, y;
        public float damage = 2f;
        public float healAmount = 3f;
        public float healPercent = 1f;
        public float range = 90f;
        public float tickRate = 5f;
        public float buildingDamageMultiplier = 0.25f;

        public float bulletSpeedBonus = 1.5f;
        public float bulletSlowdown = 0.6f;

        public float efficiencyBoost = 1.5f;
        public float stackPenalty = 0.8f;

        public StatusEffect status;
        public float statusDuration = 60f * 3f;

        public Effect healEffect = Fx.heal;
        public Effect damageEffect = Fx.hitLaserBlast;

        public Color color = Color.valueOf("84f491");
        public int nanobotCount = 40;
        public float nanobotSize = 2f;
        public float nanobotOrbitSpeed = 2f;
        public float nanobotMoveSpeed = 0.05f;

        public float fadeMultiplier = 1f;
        public float spreadMultiplier = 1f;

        public mindustry.game.Team team;
    }

    public static void initNanobots(Object owner, float x, float y, int count) {
        if (!ownerNanobots.containsKey(owner)) {
            Seq<Nanobot> nanobots = new Seq<>();
            for (int i = 0; i < count; i++) {
                nanobots.add(new Nanobot(x, y));
            }
            ownerNanobots.put(owner, nanobots);
            healEffectTimers.put(owner, 0f);
            damageEffectTimers.put(owner, 0f);
        }
    }

    public static void removeNanobots(Object owner) {
        ownerNanobots.remove(owner);
        healEffectTimers.remove(owner);
        damageEffectTimers.remove(owner);
    }

    public static Seq<Nanobot> getNanobots(Object owner) {
        return ownerNanobots.get(owner);
    }

    public static void cleanupInvalid() {
        ownerNanobots.entrySet().removeIf(entry -> {
            Object owner = entry.getKey();
            if (owner instanceof Unit u) return !u.isValid();
            if (owner instanceof Building b) return !b.isValid();
            return false;
        });
        healEffectTimers.entrySet().removeIf(entry -> !ownerNanobots.containsKey(entry.getKey()));
        damageEffectTimers.entrySet().removeIf(entry -> !ownerNanobots.containsKey(entry.getKey()));
    }

    public static void drawNanobots(Object owner, NanobotParams params) {
        Seq<Nanobot> nanobots = ownerNanobots.get(owner);
        if (nanobots == null) return;

        boolean useShader = Vars.renderer.animateShields;

        Draw.draw(AxLayers.nanobotLayer, () -> {
            if (useShader) {
                Draw.shader(AxShaders.nanoBots);
                AxShaders.nanoBots.time = Time.time;
            }

            for (Nanobot nano : nanobots) {
                nano.update(params.x, params.y, params.range, params.nanobotOrbitSpeed, params.nanobotMoveSpeed, params.spreadMultiplier);

                float alpha = nano.alpha * params.fadeMultiplier;
                float scale = nano.scale * params.fadeMultiplier;

                Tmp.c1.set(params.color).lerp(Color.gray, 1f - params.fadeMultiplier);

                // Ghost trail — always drawn when nanobot has a target
                if (nano.targetTimer > 0) {
                    float[] trailAlphas = {0.4f, 0.25f, 0.1f};
                    float[] trailScales = {0.7f, 0.5f, 0.3f};

                    for (int i = 0; i < 3; i++) {
                        float px = nano.prevX[i];
                        float py = nano.prevY[i];
                        if (px == 0 && py == 0) continue;

                        Draw.color(Tmp.c1, alpha * trailAlphas[i]);
                        Fill.circle(px, py, params.nanobotSize * scale * trailScales[i]);
                    }

                    // Pulse ring — only when boosting a block, drawn in orange
                    if (nano.boostTarget) {
                        float ringProgress = 1f - (nano.targetTimer / 30f);
                        float ringRadius = params.nanobotSize * 4f * ringProgress;
                        float ringAlpha = (1f - ringProgress) * alpha;

                        Draw.color(Color.orange, ringAlpha);
                        Lines.stroke(0.8f);
                        Lines.circle(nano.x, nano.y, ringRadius);
                    }
                }

                // Main nanobot dot
                Draw.color(Tmp.c1, alpha);
                Fill.circle(nano.x, nano.y, params.nanobotSize * scale);

                Draw.color(Color.white, alpha * 0.7f);
                Fill.circle(nano.x, nano.y, params.nanobotSize * scale * 0.5f);
            }

            if (useShader) Draw.shader();
            Draw.reset();
        });
    }

    /** Returns true if the unit has a NanobotAbility or fires Nanobot bullets. Used to reduce incoming healing from those instances. */
    public static boolean hasNanobot(Unit unit) {
        // Check for NanobotAbility
        for (Ability ability : unit.abilities) {
            if (ability instanceof NanobotAbility) return true;
        }
        // Check if unit can fire a flying nanobot bullet
        for (var weapon : unit.type.weapons) {
            if (weapon.bullet instanceof NanobotBulletType) return true;
        }
        return false;
    }

    public static void updateNanobots(Object owner, NanobotParams params, float timer, boolean useAmmo, Runnable consumeAmmo) {
        Seq<Nanobot> nanobots = ownerNanobots.get(owner);
        if (nanobots == null) return;

        // Tick effect cooldown timers
        healEffectTimers.merge(owner, Time.delta, Float::sum);
        damageEffectTimers.merge(owner, Time.delta, Float::sum);

        if (timer >= params.tickRate && (!useAmmo || consumeAmmo == null)) {

            Units.nearby(null, params.x, params.y, params.range, other -> {
                if (other.team == params.team) {
                    if (other.damaged()) {
                        float healMultiplier = hasNanobot(other) || other.type instanceof DroneUnitType ? params.stackPenalty : 1f;
                        float totalHeal = (params.healAmount + (other.maxHealth * params.healPercent / 100f)) * healMultiplier;
                        other.heal(totalHeal);

                        if (healEffectTimers.getOrDefault(owner, 0f) >= effectInterval) {
                            params.healEffect.at(other.x, other.y, 0f, params.color);
                            healEffectTimers.put(owner, 0f);
                        }
                        if (!(other.type instanceof DroneUnitType) || owner != other){
                            assignNanobots(owner, other.x, other.y, true, false,3);
                        }

                    }
                } else {
                    other.damage(params.damage * state.rules.unitDamage(params.team));
                    other.apply(params.status, params.statusDuration);

                    if (damageEffectTimers.getOrDefault(owner, 0f) >= effectInterval) {
                        params.damageEffect.at(other.x, other.y, 0f, params.color);
                        damageEffectTimers.put(owner, 0f);
                    }

                    assignNanobots(owner, other.x, other.y, false, false,10);
                }
            });

            indexer.eachBlock(null, params.x, params.y, params.range, other -> true, building -> {
                if (building.team == params.team) {
                    if (building.damaged()) {
                        float totalHeal = params.healAmount + (building.maxHealth * params.healPercent / 100f);
                        building.heal(totalHeal);

                        if (healEffectTimers.getOrDefault(owner, 0f) >= effectInterval) {
                            Fx.healBlockFull.at(building.x, building.y, 0f, params.color, building.block);
                            healEffectTimers.put(owner, 0f);
                        }

                        if (owner != building){
                            assignNanobots(owner, building.x, building.y, true, false,3);
                        }

                    }
                } else {
                    building.damage(params.damage * params.buildingDamageMultiplier * state.rules.unitDamage(params.team));

                    if (damageEffectTimers.getOrDefault(owner, 0f) >= effectInterval) {
                        params.damageEffect.at(building.x, building.y, 0f, params.color);
                        damageEffectTimers.put(owner, 0f);
                    }

                    assignNanobots(owner, building.x, building.y, false, false,10);
                }
            });

            Groups.bullet.intersect(params.x - params.range, params.y - params.range, params.range * 2, params.range * 2, bullet -> {
                if (bullet.within(params.x, params.y, params.range)) {
                    if (bullet.team == params.team) {
                        if (params.bulletSpeedBonus != 1f) {
                            bullet.vel.scl(params.bulletSpeedBonus);
                            if (Mathf.chance(0.1f)) {
                                assignNanobots(owner, bullet.x, bullet.y, true, false,2);
                            }
                        }
                    } else {
                        if (params.bulletSlowdown != 1f) {
                            bullet.vel.scl(params.bulletSlowdown);
                            if (Mathf.chance(0.1f)) {
                                if (damageEffectTimers.getOrDefault(owner, 0f) >= effectInterval) {
                                    Fx.hitLaserBlast.at(bullet.x, bullet.y, 0f, params.color);
                                    damageEffectTimers.put(owner, 0f);
                                }
                                assignNanobots(owner, bullet.x, bullet.y, false, false,2);
                            }
                        }
                    }
                }
            });

            if (consumeAmmo != null) {
                consumeAmmo.run();
            }
        }
    }

    public static void updateBoost(Object owner, NanobotParams params,int max) {
        indexer.eachBlock(null, params.x, params.y, params.range, other -> other.team == params.team, building -> {
            if (building.block.canOverdrive) {
                building.applyBoost(params.efficiencyBoost, params.tickRate + 1);
                if (Mathf.chance(0.05f)) {
                    assignNanobots(owner, building.x, building.y, true, true,max);
                }
            }
        });
    }

    /** @param boost whether this nanobot is being sent to boost a building (triggers pulse ring). */
    public static void assignNanobots(Object owner, float targetX, float targetY, boolean healing, boolean boost,int max) {
        Seq<Nanobot> nanobots = ownerNanobots.get(owner);
        if (nanobots == null) return;

        int assigned = 0;
        int toAssign = Mathf.random(0, max);

        for (Nanobot nano : nanobots) {
            if (assigned >= toAssign) break;
            if (nano.targetX == 0 && nano.targetY == 0) {
                nano.targetX = targetX;
                nano.targetY = targetY;
                nano.healing = healing;
                nano.boostTarget = boost;
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
        public boolean boostTarget = false;
        public float targetTimer = 0f;

        // Stores last 3 positions for ghost trail
        public float[] prevX = new float[3];
        public float[] prevY = new float[3];
        private float trailUpdateTimer = 0f;
        private static final float trailUpdateInterval = 2f;

        public Nanobot(float spawnX, float spawnY) {
            x = spawnX;
            y = spawnY;
            angle = Mathf.random(360f);
            verticalAngle = Mathf.random(360f);
            float randomFactor = Mathf.pow(Mathf.random(), 1f / 3f);
            orbitRadius = Mathf.random(5f, 95f) * randomFactor;
        }

        public void update(float centerX, float centerY, float maxRange, float orbitSpeed, float moveSpeed, float spreadMultiplier) {
            if (Vars.state.isPaused()) return;

            // Update trail positions on interval
            trailUpdateTimer += Time.delta;
            if (trailUpdateTimer >= trailUpdateInterval) {
                trailUpdateTimer = 0f;
                // Shift positions back
                prevX[2] = prevX[1];
                prevY[2] = prevY[1];
                prevX[1] = prevX[0];
                prevY[1] = prevY[0];
                prevX[0] = x;
                prevY[0] = y;
            }

            angle += orbitSpeed * Time.delta;
            verticalAngle += orbitSpeed * Time.delta * 0.7f;

            if (Mathf.chance(0.02f)) {
                float newRadius = Mathf.random(5f, maxRange * 0.95f);
                float randomFactor = Mathf.pow(Mathf.random(), 1f / 3f);
                orbitRadius = Mathf.lerp(orbitRadius, newRadius * randomFactor, 0.1f);
            }

            float horizontalRadius = orbitRadius * spreadMultiplier * Mathf.cos(verticalAngle * Mathf.degreesToRadians);
            Tmp.v1.trns(angle, horizontalRadius);

            orbitX = centerX + Tmp.v1.x;
            orbitY = centerY + Tmp.v1.y;

            if (targetTimer > 0) {
                targetTimer -= Time.delta;
                x = Mathf.lerp(x, targetX, moveSpeed * Time.delta);
                y = Mathf.lerp(y, targetY, moveSpeed * Time.delta);
                scale = 1.2f;
                alpha = 1f;

                if (targetTimer <= 0) {
                    targetX = 0;
                    targetY = 0;
                    boostTarget = false;
                    // Clear trail when returning to orbit
                    prevX[0] = prevX[1] = prevX[2] = 0;
                    prevY[0] = prevY[1] = prevY[2] = 0;
                }
            } else {
                x = Mathf.lerp(x, orbitX, moveSpeed * Time.delta);
                y = Mathf.lerp(y, orbitY, moveSpeed * Time.delta);
                float depthFactor = (Mathf.sin(verticalAngle * Mathf.degreesToRadians) + 1f) / 2f;
                scale = 0.7f + depthFactor * 0.5f;
                alpha = 0.5f + depthFactor * 0.5f;
            }
        }
    }
}