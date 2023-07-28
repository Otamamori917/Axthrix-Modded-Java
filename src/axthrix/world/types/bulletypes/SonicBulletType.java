package axthrix.world.types.bulletypes;

import mindustry.gen.*;
import mindustry.graphics.*;
import axthrix.content.*;
import mindustry.entities.bullet.*;
import mindustry.content.*;

/** sonic themed bullet*/
public class SonicBulletType extends BasicBulletType{

    public SonicBulletType(){
        sprite = "aj-sonic";
        backSprite = "aj-sonic-back";
        hittable = false;
        pierceBuilding = true;
        pierce = true;
        laserAbsorb = false;
        backColor = Pal.darkishGray;
        frontColor = Pal.lightishGray;
        shrinkY = -0.2f;
        shrinkX = -0.6f;
        hitSound = Sounds.shield;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        hitEffect = Fx.none;
        smokeEffect = Fx.none;
        knockback = 4;
        impact = true;
        status = AxthrixStatus.vibration;
        statusDuration = 100;
        keepVelocity = false;
        reflectable = false;
        absorbable = false;
        pierceArmor = true;
        removeAfterPierce = false;
        speed = 8f;
        lifetime = 20f;
    }
}