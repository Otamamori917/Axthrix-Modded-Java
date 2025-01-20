package axthrix.content.units;

import arc.graphics.Color;
import axthrix.content.AxthrixStatus;
import axthrix.world.types.bulletypes.SonicBulletType;
import axthrix.world.types.unittypes.AmmoLifeTimeUnitType;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.*;
import axthrix.world.types.ai.*;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.ammo.PowerAmmoType;

public class AxthrixDrones {
    public static UnitType
    //naji tree sentry
    ivy,iris,clove,azalea,bramble;
    public static DroneUnitType
    //shield drones
    paliShield,
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
        ivy = new AmmoLifeTimeUnitType("ivy")
        {{
            localizedName = "[green]Ivy";
            ammoCapacity = 400;
            engineColor = Color.valueOf("4ea572");
            aiController = SentriAI::new;
            constructor = UnitEntity::create;

            speed = accel = 0f;
            drag = 0.1f;
            flying = true;
            isEnemy = true;
            useUnitCap = false;
            ammoType = new PowerAmmoType(10);
            itemCapacity = 0;
            health = 200;
            engineSize = -1;
            Weapon gunL = new Weapon(name + "-gun-r"){{
                rotate = top = false;
                mirror = false;
                alternate = true;
                otherSide = 1;

                x = 4.75f / 4f;
                y = 11f / 4f;
                shootX = -0.5f / 4f;
                shootY = 3f / 4f;

                reload = 6f;
                recoil = 1.5f / 4f;
                ejectEffect = Fx.casing1;
                bullet = new LaserBoltBulletType(2f, 12.5f){{
                    shootSound = Sounds.pulseBlast;
                    soundPitchMax = soundPitchMin = 2;
                    lifetime = 60f;
                    width = 1f;
                    height = 3.5f;
                    backColor = Color.valueOf("4ea572");
                    frontColor = Color.white;
                }};
            }};
            Weapon gunR = gunL.copy();
            gunR.name = name + "-gun-l";
            gunR.x *= -1;
            gunR.shootX *= -1;
            gunR.flipSprite = true;
            gunR.otherSide = 0;
            parts.add(new RegionPart("-anchor"){{
                mirror = false;
                under = true;
                layerOffset = 1f;
                outlineLayerOffset = 1f;
            }});

            weapons.add(gunL, gunR);


        }};
        paliShield = new DroneUnitType("pali-shield")
        {{
            localizedName = "Shield U1";
            ammoCapacity = 250;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
            health = 40;
            armor = 2;
            //isShield = true;
            faceTarget = true;
            hitSize = 2*2;
            itemCapacity = 0;
            speed = 40f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = true;
            engineSize = 2;
            abilities.add(new ShieldArcAbility(){{
                radius = 45f;
                angle = 90f;
                y = -24f;
                regen = 0.6f;
                cooldown = 200f;
                max = 100f;
                width = 10f;
                whenShooting = false;
            }});
        }};
        wattFlame = new DroneUnitType("f-w")
        {{
            localizedName = "[orange]Sol[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
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
        wattIce = new DroneUnitType("i-w")
        {{
            localizedName = "[blue]Cyo[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
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
        wattGround = new DroneUnitType("e-w")
        {{
            localizedName = "[green]Grn[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
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
        wattAir = new DroneUnitType("a-w")
        {{
            localizedName = "[white]Eir[gray]|[]W";
            ammoCapacity = 500;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
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
                bullet = new SonicBulletType(){{
                    damage = 10;
                    width = 8;
                    height = 4;
                }};
            }});
        }};
    }
}