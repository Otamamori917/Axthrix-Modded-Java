package axthrix.world.types.bulletypes;

import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;

public class TemperatureBulletType extends BulletType {
    public float temperaturePerHit = 10f;

    public TemperatureBulletType(){
        super();
    }

    public TemperatureBulletType(float speed, float damage){
        super(speed, damage);
    }

    @Override
    public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float health){
        if(entity instanceof Unit target){
            TemperatureLogic.applyTemperatureUnit(target, temperaturePerHit);
        }else if(entity instanceof Building target){
            TemperatureLogic.applyTemperatureBuilding(target, temperaturePerHit);
        }

        super.hitEntity(b, entity, health);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);

        if(build != null){
            TemperatureLogic.applyTemperatureBuilding(build, temperaturePerHit);
        }
    }
}