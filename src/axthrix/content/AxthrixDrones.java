package axthrix.content;

import arc.graphics.Color;
import axthrix.world.types.ai.AgressiveFlyingAi;
import axthrix.world.types.unittypes.AmmoLifeTimeUnitType;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.FireBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.*;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class AxthrixDrones {
    public static UnitType
    //watt
    wattFlame,wattIce,wattGround,wattAir,
    //kilowatt
    kilowattFlame,kilowattIce,kilowattGround,kilowattAir,
    //megawatt
    megawattFlame,megawattIce,megawattGround,megawattAir,
    //gigawatt
    gigawattFlame,gigawattIce,gigawattGround,gigawattAir,
    //terawatt
    terawattFlame,terawattIce,terawattGround,terawattAir,
    //petawatt
    petawattFlame,petawattIce,petawattGround,petawattAir
            ;
    public static void load(){
        wattFlame = new AmmoLifeTimeUnitType("f-w")
        {{
            localizedName = "[orange]Sol[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new AgressiveFlyingAi(true);
            health = 2000;
            armor = 2;
            faceTarget = true;
            hitSize = 2*2;
            engineColor = Color.orange;
            itemCapacity = 0;
            speed = 20f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = false;
            range = 12*8;
            engineSize = 2;
            weapons.add(new Weapon(){{
                shootSound = Sounds.flame;
                x = 0;
                y = 0;
                shootY = 6;
                mirror = false;
                top = false;
                reload = 1;
                inaccuracy = 10;
                immunities.add(StatusEffects.burning);
                bullet = new FireBulletType(){{
                    speed = 6f;
                    radius = 5;
                    velMin = 0.8f;
                    velMax = 6;
                    hittable = false;
                    keepVelocity = false;
                    collidesAir = false;
                    hitEffect = Fx.hitFlameSmall;
                    despawnEffect = Fx.none;

                    fragRandomSpread = 0f;
                    fragSpread = 5f;
                    fragVelocityMin = 1f;
                    fragBullets = 10;
                    fragBullet = new BulletType(4.2f, 3f){{
                        ammoMultiplier = 3f;
                        hitSize = 7f;
                        lifetime = 13f;
                        pierce = true;
                        pierceBuilding = true;
                        pierceCap = 2;
                        statusDuration = 60f * 4;
                        shootEffect = Fx.shootSmallFlame;
                        hitEffect = Fx.hitFlameSmall;
                        despawnEffect = Fx.none;
                        status = StatusEffects.melting;
                        keepVelocity = false;
                        hittable = false;
                    }};
                }};
            }});
        }};
        wattIce = new AmmoLifeTimeUnitType("i-w")
        {{
            localizedName = "[blue]Cyo[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new AgressiveFlyingAi(true);
            health = 2000;
            armor = 2;
            faceTarget = true;
            hitSize = 2*2;
            engineColor = Color.orange;
            itemCapacity = 0;
            speed = 20f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = false;
            range = 12*8;
            engineSize = 2;
            weapons.add(new Weapon(){{
                shootSound = Sounds.shotgun;
                x = 0;
                y = 0;
                shootY = 6;
                mirror = false;
                top = false;
                reload = 30;
                shoot = new ShootSpread(3, 10f);
                immunities.add(StatusEffects.freezing);
                float brange = range + 10f;
                bullet = new ShrapnelBulletType(){{
                    length = brange;
                    width = 17f;
                    hittable = false;
                    keepVelocity = false;
                    collidesAir = false;
                    despawnEffect = Fx.none;
                    status = StatusEffects.freezing;
                    damage = 40;

                    fragRandomSpread = 0f;
                    fragSpread = 5f;
                    fragVelocityMin = 1f;
                    fragBullets = 4;
                    fragBullet = new ShrapnelBulletType(){{
                        length = brange;
                        width = 5f;
                        ammoMultiplier = 3f;
                        hitSize = 7f;
                        lifetime = 13f;
                        damage = 20;
                        pierce = true;
                        pierceBuilding = true;
                        pierceCap = 2;
                        statusDuration = 60f * 4;
                        despawnEffect = Fx.none;
                        status = StatusEffects.freezing;
                        keepVelocity = false;
                        hittable = false;
                    }};
                }};
            }});
        }};
    }
}        