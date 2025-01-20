package axthrix.content.units;

import arc.graphics.Color;
import arc.struct.Seq;
import axthrix.content.AxFactions;
import axthrix.content.AxthrixSounds;
import axthrix.content.AxthrixStatus;
import axthrix.world.types.entities.comp.StealthUnit;
import axthrix.world.types.ai.DynFlyingAI;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.unittypes.CnSUnitType;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.abilities.EnergyFieldAbility;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.MechUnit;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.ammo.ItemAmmoType;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.tilesize;

public class RaodonUnits {
    public static UnitType
    //Raodon |3 trees|
        //Assault Walker |Power|
            asta,adira,allura,bolverkr,ahriman,
        //Support Tank  |Wealth|
            danu,odon,dorit,shriyan,beelzebub,
        //Specialist aircraft |Fame|
            efim,estes,zephyr,erebus,clymene


    //Core Units |3 units|
            ;
    public static void load(){

        asta = new AxUnitType("asta"){{
            ammoType = new ItemAmmoType(Items.silicon);
            speed = 0.55f;
            hitSize = 6f;
            health = 340;
            armor = 2f;
            canBoost = true;
            boostMultiplier = 2.5f;
            constructor = MechUnit::create;
            factions.add(AxFactions.raodon);
            weapons.add(new Weapon(name+"-pow"){{
                y = x = 0;
                shootY = 3;
                shootX = 4;
                recoil = 4;
                reload = 60;
                rotate = false;
                alternate = false;
                top = false;
                shootSound = Sounds.shootAltLong;
                bullet = new BasicBulletType(9.0F, 30.0F){{
                    shootSound = Sounds.shootBig;
                    keepVelocity = false;
                    pierce = true;
                    pierceCap = 10;
                    width = 7.0F;
                    height = 14.0F;
                    lifetime = 14.0F;
                    shootEffect = Fx.shootBig;
                    fragVelocityMin = 0.4F;
                    hitEffect = Fx.blastExplosion;
                    splashDamage = 26.0F;
                    splashDamageRadius = 13.0F;
                    frontColor = Color.valueOf("83575a");
                    backColor = Color.valueOf("30121e");
                    recoil = 2;
                }};
            }});
        }};
        adira = new AxUnitType("adira"){{
            ammoType = new ItemAmmoType(Items.silicon);
            speed = 0.55f;
            hitSize = 6f;
            health = 340;
            armor = 2f;
            canBoost = true;
            boostMultiplier = 2.5f;
            constructor = MechUnit::create;
            factions.add(AxFactions.raodon);
            weapons.add(new Weapon(name+"-slash"){{
                y = x = 0;
                shootY = 3;
                shootX = 4;
                recoil = -6;
                reload = 15;
                rotate = false;
                top = false;
                soundPitchMin = 0.42f;
                soundPitchMax = 1.74f;
                shootSound = AxthrixSounds.Swings;
                bullet = new ShrapnelBulletType(){{
                    fromColor = Color.valueOf("30121e");
                    toColor = Color.valueOf("83575a");
                    hitColor = Color.valueOf("46292d");
                    shootEffect = Fx.none;
                    smokeEffect = Fx.none;
                    width = 0;
                    recoil = -0.35f;
                    maxRange = 32;
                    length = 30;
                    lifetime = 12;
                    serrations = 6;
                    serrationWidth = 11;
                    serrationSpacing = 3;
                    serrationSpaceOffset = 9;
                    serrationLenScl = 6;
                    serrationFadeOffset = 0;
                    hitEffect = Fx.mine;
                    damage = 120;
                    knockback = 0;
                    hittable = false;
                    reflectable = false;
                    absorbable = false;
                    hitLarge = false;
                }};
            }});
        }};
        efim = new CnSUnitType("efim",520) {{
            itemCapacity = 0;
            factions.add(AxFactions.raodon);
            health = 45;
            armor = 10;
            hitSize = 8;

            speed = 2.5f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;

            circleTarget = true;
            lowAltitude = true;
            faceTarget = false;
            flying = true;
            constructor = StealthUnit::new;
            aiController = DynFlyingAI::new;
            engineSize = 0;
            engines = Seq.with(new UnitEngine(0,-3f,2.5f,-90));

            targetFlags = new BlockFlag[]{BlockFlag.repair,BlockFlag.unitAssembler, null};


            weapons.add(new Weapon(){{
                x = y = 0f;
                shootY = -5;
                mirror = false;
                reload = 180f;
                minShootVelocity = 0.01f;
                shootSound = Sounds.none;
                shoot.shots = 8;
                shoot.shotDelay = 4;
                bullet = new BasicBulletType(){{
                    sprite = "large-bomb";
                    width = height = 60/4f;
                    maxRange = 30f;
                    ignoreRotation = true;
                    shootEffect = smokeEffect = Fx.none;
                    backColor = Color.valueOf("2d0827");
                    frontColor = Color.white;
                    hitSound = Sounds.explosion;
                    shootCone = 180f;
                    ejectEffect = Fx.none;

                    collidesAir = false;

                    lifetime = 45f;
                    despawnEffect = Fx.none;
                    hitEffect = Fx.explosion;
                    keepVelocity = false;
                    spin = 2f;
                    shrinkX = shrinkY = 0.7f;
                    speed = 0f;
                    collides = false;
                    splashDamage = 55f;
                    splashDamageRadius = 20;
                }};
            }});
        }};
        estes = new CnSUnitType("estes",600) {{
            itemCapacity = 0;
            factions.add(AxFactions.raodon);
            health = 115;
            armor = 11;
            hitSize = 18;

            speed = 2.45f;
            accel = 0.035f;
            drag = 0.017f;
            rotateSpeed = 5.5f;

            circleTarget = true;
            lowAltitude = true;
            faceTarget = false;
            flying = true;
            constructor = StealthUnit::new;
            aiController = DynFlyingAI::new;
            engineSize = 0;
            engines = Seq.with(new UnitEngine(0,-3f,2.5f,-90));

            targetFlags = new BlockFlag[]{BlockFlag.battery,BlockFlag.generator, null};

            weapons.add(new Weapon(){{
                x = y = 0f;
                shootY = 0;
                mirror = false;
                reload = 240f;
                minShootVelocity = 0.01f;
                baseRotation = 180;
                shootSound = Sounds.none;
                shoot.shots = 4;
                shoot.shotDelay = 5;
                bullet = new BasicBulletType(){{
                    sprite = "large-bomb";
                    width = height = 60/4f;
                    maxRange = 30f;
                    ignoreRotation = true;
                    shootEffect = smokeEffect = Fx.none;
                    backColor = Color.valueOf("2d0827");
                    frontColor = Color.white;
                    hitSound = Sounds.none;
                    shootCone = 180f;
                    ejectEffect = hitEffect = despawnEffect = Fx.none;

                    collidesAir = false;

                    lifetime = 15f;
                    keepVelocity = false;
                    spin = 2.5f;
                    shrinkX = shrinkY = 0.7f;
                    speed = 0f;
                    collides = false;
                    splashDamage = 0f;
                    fragBullets = 6;
                    fragRandomSpread = 0;
                    fragSpread = 60;
                    fragBullet = bullet = new BasicBulletType(){{
                        sprite = "large-bomb";
                        width = height = 60/8f;
                        maxRange = 30f;
                        ignoreRotation = true;
                        shootEffect = smokeEffect = Fx.none;
                        backColor = Color.valueOf("2d0827");
                        frontColor = Color.white;
                        hitSound = Sounds.explosion;
                        shootCone = 180f;
                        ejectEffect = Fx.none;

                        collidesAir = false;
                        lifetime = 45f;
                        despawnEffect = Fx.none;
                        hitEffect = Fx.explosion;
                        keepVelocity = false;
                        spin = 4f;
                        shrinkX = shrinkY = 0.7f;
                        speed = 3f;
                        drag = 0.1f;
                        collides = false;
                        splashDamage = 100f;
                        scaledSplashDamage = true;
                        splashDamageRadius = 25;
                    }};
                }};
            }});
        }};
    }
}