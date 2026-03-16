package axthrix.content.units;

import arc.graphics.Color;
import axthrix.content.AxthrixStatus;
import axthrix.world.types.abilities.DamageDistributionAbility;
import axthrix.world.types.abilities.PassiveRegenerationAbility;
import axthrix.world.types.abilities.SacrificeProtocolAbility;
import axthrix.world.types.bulletypes.SonicBulletType;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.*;
import axthrix.world.types.ai.*;
import mindustry.graphics.Pal;
import mindustry.type.Weapon;
import mindustry.type.weapons.BuildWeapon;

public class AxthrixDrones {
    public static DroneUnitType
    //shield drones
    builderDrone,barriShield,blockShield,paliShield,paraShield,impedShield,
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
        builderDrone = new DroneUnitType("helper-drone")
        {{

            ammoCapacity = 250;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
            health = 120;
            armor = 2;
            isShield = true;
            faceTarget = true;
            hitSize = 4*2;
            itemCapacity = 0;
            speed = 40f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = true;
            engineOffset = 0;
            engineSize = 1f;
            buildSpeed = 1.5f;
            buildBeamOffset = 8f;
            weapons.add(
                    new BuildWeapon("aj-builder")
            );
            abilities.add(
                    new ShieldArcAbility(){{
                        region = "aj-sheild-small";
                        radius = 55f;
                        y = -25f;
                        angle = 180f;
                        regen = 0.6f;
                        cooldown = 200f;
                        max = 100f;
                        width = 8f;
                        whenShooting = false;
                    }},
                    new PassiveRegenerationAbility(){{
                        healAmount = 1f;
                        healDelay = 360f;
                        detectionRange = 280f;
                    }});
        }};
        barriShield = new DroneUnitType("barri-shield")
        {{

            ammoCapacity = 250;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
            health = 120;
            armor = 2;
            isShield = true;
            faceTarget = true;
            hitSize = 4*2;
            itemCapacity = 0;
            speed = 40f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = true;
            engineOffset = 0;
            engineSize = 1f;
            abilities.add(
            new DamageDistributionAbility(){{
                range = 40f;
                sharePercent = 0.3f;
                linkWidth = 0.2f;
            }},
           new PassiveRegenerationAbility(){{
               healAmount = 2f;
               healDelay = 360f;
               detectionRange = 200f;
           }});
        }};
        paliShield = new DroneUnitType("pali-shield")
        {{

            ammoCapacity = 250;

            flying = alwaysShootWhenMoving = drawAmmo = true;
            playerControllable = useUnitCap = false;
            constructor = UnitEntity::create;
            controller = u -> new DroneAI();
            health = 80;
            armor = 2;
            isShield = true;
            faceTarget = true;
            hitSize = 4*2;
            itemCapacity = 0;
            speed = 40f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = true;
            engineSize = 2;
            abilities.add(new DamageDistributionAbility(){{
                range = 40f;
                sharePercent = 0.9f; // 50% shared
                linkWidth = 0.2f;
            }},
            new PassiveRegenerationAbility(){{
                healAmount = 2f; // Heal per tick
                healDelay = 180f; // 3 seconds out of combat
                detectionRange = 120f;
            }}
            );
        }};
        wattFlame = new DroneUnitType("f-w")
        {{
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
            abilities.add(new SacrificeProtocolAbility(){{
                healthThreshold = 0.2f; // <20% health
                kamikazeDamage = 200f;
            }});
            weapons.add(new Weapon(){{
                shootSound = Sounds.shootFlame;
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
                shootSound = Sounds.shootFuse;
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
                shootSound = Sounds.shootAfflict;
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
                shootSound = Sounds.shootAvert;
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