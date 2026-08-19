package axthrix.world.types.bulletypes;

import arc.math.Mathf;
import arc.struct.ObjectIntMap;
import arc.util.Time;
import axthrix.world.types.abilities.ShieldPlateAbility;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;

public class TemperatureBulletType extends BasicBulletType{
    public float temperaturePerHit = 10f;
    public float flashBonusDamage = 35f;
    public float inverseShockMultiplier = 1.50f;

    /** Extra damage = |shooterTemp| * this (0 = off) */
    public float scaleDamageWithShooterTemp = 0f;
    /** Ignore unit shields and force-field absorption */
    public boolean bypassShields = false;

    public TemperatureBulletType() {
        super();
    }

    public TemperatureBulletType(float speed, float damage) {
        super(speed, damage);
    }

    @Override
    public void init() {
        super.init();
        if (bypassShields) {
            absorbable = false;
        }
    }

    protected float shooterTempBonus(Bullet b) {
        if (scaleDamageWithShooterTemp <= 0f || b.owner == null) return 0f;

        float temp = 0f;
        if (b.owner instanceof Unit u) {
            temp = TemperatureLogic.getTemperatureUnit(u);
        } else if (b.owner instanceof Building build) {
            temp = TemperatureLogic.getTemperatureBuilding(build);
            float heat = TemperatureLogic.getHeatBuilding(build);
            float cold = TemperatureLogic.getColdBuilding(build);
            temp = Math.max(heat, cold) > 0f ? (heat >= cold ? heat : -cold) : temp;
        }
        return Math.abs(temp) * scaleDamageWithShooterTemp;
    }

    protected float fullDamage(Bullet b) {
        return b.damage + shooterTempBonus(b);
    }

    protected void dealDamage(Bullet b, Unit target, float dmg) {
        if (bypassShields) {
            float shield = target.shield;
            target.shield = 0f;
            target.damage(dmg);
            target.shield = shield;
        } else {
            target.damage(dmg);
        }
    }


    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        if (entity instanceof Unit target) {
            if (!target.dead && target.isValid()) {
                float currentTemp = TemperatureLogic.getTemperatureUnit(target);
                float dmg = fullDamage(b);

                if ((temperaturePerHit > 0f && currentTemp < -1f) || (temperaturePerHit < 0f && currentTemp > 1f)) {
                    dealDamage(b, target, dmg * inverseShockMultiplier);
                    TemperatureLogic.applyTemperatureUnit(target, -currentTemp);
                    return;
                }

                if (Mathf.equal(Math.abs(currentTemp), 100f, 0.1f)) {
                    dealDamage(b, target, flashBonusDamage);
                }

                TemperatureLogic.applyTemperatureUnit(target, temperaturePerHit);

                if (bypassShields) {
                    dealDamage(b, target, dmg);
                    if (status != null) target.apply(status, statusDuration);
                    return;
                }
            }
        } else if (entity instanceof Building target) {
            if (target.isValid() && !target.dead) {
                TemperatureLogic.applyTemperatureBuilding(target, temperaturePerHit);
            }
        }

        super.hitEntity(b, entity, health);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        super.hitTile(b, build, x, y, initialHealth, direct);

        if (build != null && build.isValid() && !build.dead) {
            TemperatureLogic.applyTemperatureBuilding(build, temperaturePerHit);
        }

        if (splashDamageRadius > 0.1f) {
            Units.nearbyEnemies(b.team, x, y, splashDamageRadius, unit -> {
                if (!unit.dead && unit.isValid()) {
                    float dist = Mathf.dst(x, y, unit.x, unit.y);
                    float factor = 1f - Mathf.clamp(dist / splashDamageRadius);
                    TemperatureLogic.applyTemperatureUnit(unit, temperaturePerHit * factor);
                }
            });

            Units.nearbyBuildings(x, y, splashDamageRadius, otherBuild -> {
                if (otherBuild.isValid() && !otherBuild.dead && otherBuild.team != b.team) {
                    float dist = Mathf.dst(x, y, otherBuild.x, otherBuild.y);
                    float factor = 1f - Mathf.clamp(dist / splashDamageRadius);
                    TemperatureLogic.applyTemperatureBuilding(otherBuild, temperaturePerHit * factor);
                }
            });
        }
    }
}