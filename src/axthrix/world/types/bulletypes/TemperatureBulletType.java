package axthrix.world.types.bulletypes;

import arc.math.Mathf;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;

public class TemperatureBulletType extends BasicBulletType {
    public float temperaturePerHit = 10f;
    public float flashBonusDamage = 35f;
    public float inverseShockMultiplier = 1.50f;

    public TemperatureBulletType(){
        super();
    }

    public TemperatureBulletType(float speed, float damage){
        super(speed, damage);
    }
    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        if(entity instanceof Unit target){
            if(!target.dead && target.isValid()){
                float currentTemp = TemperatureLogic.getTemperatureUnit(target);

                if((temperaturePerHit > 0f && currentTemp < -1f) || (temperaturePerHit < 0f && currentTemp > 1f)){
                    target.damage(b.damage * inverseShockMultiplier);
                    TemperatureLogic.applyTemperatureUnit(target, -currentTemp);
                    super.hitEntity(b, entity, health);
                    return;
                }

                if(Mathf.equal(Math.abs(currentTemp), 100f, 0.1f)){
                    target.damage(flashBonusDamage);
                }

                TemperatureLogic.applyTemperatureUnit(target, temperaturePerHit);
            }
        }else if(entity instanceof Building target){
            if(target.isValid() && !target.dead){
                TemperatureLogic.applyTemperatureBuilding(target, temperaturePerHit);
            }
        }

        super.hitEntity(b, entity, health);
    }
    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);

        if(build != null && build.isValid() && !build.dead){
            TemperatureLogic.applyTemperatureBuilding(build, temperaturePerHit);
        }

        if(splashDamageRadius > 0.1f){
            Units.nearbyEnemies(b.team, x, y, splashDamageRadius, unit -> {
                if(!unit.dead && unit.isValid()){
                    float dist = Mathf.dst(x, y, unit.x, unit.y);
                    float factor = 1f - Mathf.clamp(dist / splashDamageRadius);
                    TemperatureLogic.applyTemperatureUnit(unit, temperaturePerHit * factor);
                }
            });

            Units.nearbyBuildings(x, y, splashDamageRadius, otherBuild -> {
                if(otherBuild.isValid() && !otherBuild.dead && otherBuild.team != b.team){
                    float dist = Mathf.dst(x, y, otherBuild.x, otherBuild.y);
                    float factor = 1f - Mathf.clamp(dist / splashDamageRadius);
                    TemperatureLogic.applyTemperatureBuilding(otherBuild, temperaturePerHit * factor);
                }
            });
        }
    }
}
