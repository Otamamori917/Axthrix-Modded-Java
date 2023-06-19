package axthrix.types.bulletypes;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/** sonic themed bullet*/
public class SonicBulletType extends BasicBulletType{

    public SonicBulletType(float damage, String bulletSprite){
        super(damage, bulletSprite);
        pierceBuilding = true;
        pierce = true;
        laserAbsorb - false;
        backColor = Pal.darkishGray;
        frontColor = Pal.lightishGray;
        shrinkY = -0.2f;
        shrinkX = -0.6f;
        width = 6f;
        height = 3f;
        hitSound = Sounds.shield;
        despawnEffect = none;
        shootEffect = none;
        hitEffect = none;
        smokeEffect  none;
        knockback = 4;
        impact = true;
        status = AxthrixStatus.vibration;
        statusDuration = 120;
        keepVelocity = false;
        reflectable = false;
        absorbable = false;
        pierceArmor = true;
        removeAfterPierce = false;
        speed = 5f;
        lifetime = 10f;
    }

    public SonicBulletType(float damage){
        this(damage, "sonic");
    }

    public SonicBulletType(){
        this(200f, "sonic");
    }
}