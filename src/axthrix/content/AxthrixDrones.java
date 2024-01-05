package axthrix.content;

import arc.graphics.Color;
import axthrix.world.types.ai.AgressiveFlyingAi;
import axthrix.world.types.bulletypes.SonicBulletType;
import axthrix.world.types.unittypes.AmmoLifeTimeUnitType;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.FireBulletType;
import mindustry.entities.bullet.ShrapnelBulletType;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.*;
import mindustry.graphics.Pal;
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
            health = 200;
            armor = 2;
            faceTarget = true;
            hitSize = 2*2;
            engineColor = Color.orange;
            itemCapacity = 0;
            speed = 40f / 7.5f;
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
                reload = 15;
                inaccuracy = 10;
                immunities.add(StatusEffects.melting);
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
                    fragBullet = new BulletType(4.2f, 8f){{
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
            health = 200;
            armor = 2;
            faceTarget = true;
            hitSize = 2*2;
            engineColor = Color.blue;
            itemCapacity = 0;
            speed = 40f / 7.5f;
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
                    width = 24f;
                    hittable = false;
                    keepVelocity = false;
                    collidesAir = false;
                    despawnEffect = Fx.none;
                    status = StatusEffects.freezing;
                    damage = 4;
                    fromColor = Color.blue;
                    toColor = Pal.techBlue;
                }};
            }});
        }};
        wattGround = new AmmoLifeTimeUnitType("e-w")
        {{
            localizedName = "[green]Grn[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new AgressiveFlyingAi(true);
            health = 200;
            armor = 2;
            faceTarget = true;
            hitSize = 2*2;
            engineColor = Color.green;
            itemCapacity = 0;
            speed = 40f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = false;
            range = 12*8;
            engineSize = 4;
            engineOffset = 4;
            weapons.add(new Weapon(){{
                shootSound = Sounds.shockBlast;
                x = 0;
                y = 0;
                shootY = 6;
                mirror = false;
                reload = 240;
                inaccuracy = 50;
                shoot.shots = 60;
                shoot.shotDelay = 1;
                immunities.add(AxthrixStatus.nanodiverge);
                bullet = new BasicBulletType(2f, 0.5f){{
                    homingRange = 40f;
                    homingPower = 4f;
                    homingDelay = 5f;
                    width = 0.5f;
                    height = 0.5f;
                    lifetime = 40;
                    healPercent = 1;
                    collidesTeam = true;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.heal;
                    trailLength = 4;
                    trailWidth = 0.5f;
                    status = AxthrixStatus.nanodiverge;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});
        }};
        wattAir = new AmmoLifeTimeUnitType("a-w")
        {{
            localizedName = "[white]Eir[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new AgressiveFlyingAi(true);
            health = 200;
            armor = 2;
            faceTarget = true;
            hitSize = 2*2;
            engineColor = Color.white;
            itemCapacity = 0;
            speed = 40f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = false;
            range = 12*8;
            engineSize = 2;
            weapons.add(new Weapon(){{
                shootSound = Sounds.shockBlast;
                x = 0;
                y = 0;
                shootY = 6;
                mirror = false;
                reload = 40;
                inaccuracy = 5;
                shoot.shots = 3;
                shoot.shotDelay = 3;
                immunities.add(AxthrixStatus.vibration);
                bullet = new SonicBulletType(){{
                    damage = 2;
                    width = 8;
                    height = 4;
                }};
            }});
        }};
    }
}