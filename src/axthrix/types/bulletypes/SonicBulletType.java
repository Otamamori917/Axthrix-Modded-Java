package axthrix.types.bulletypes;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.entities.abilities.*;
import axthrix.content.AxthrixStatus.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.content.*;

/** sonic themed bullet*/
public class SonicBulletType extends BasicBulletType{

    public SonicBulletType(float damage){
        super(damage);
        bulletSprite = "sonic";
        pierceBuilding = true;
        pierce = true;
        laserAbsorb = false;
        backColor = Pal.darkishGray;
        frontColor = Pal.lightishGray;
        shrinkY = -0.2f;
        shrinkX = -0.6f;
        width = 6f;
        height = 3f;
        hitSound = Sounds.shield;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        hitEffect = Fx.none;
        smokeEffect = Fx.none;
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
        this(damage);
    }

    public SonicBulletType(){
        this(200f);
    }
}