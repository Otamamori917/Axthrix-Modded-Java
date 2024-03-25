package axthrix.content;

import arc.graphics.Color;
import arc.struct.Seq;
import axthrix.world.types.entities.comp.StealthUnit;
import axthrix.world.types.ai.DynFlyingAI;
import axthrix.world.types.unittypes.CnSUnitType;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.bullet.*;
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
            asta,adira,allura,andrea,athena,
        //Support Tank  |Wealth|
            danu,dorit,duarte,dhanya,dhanashri,
        //Specialist aircraft |Fame|
            efim,estes,elmena,evdoxia,estanislao
    //Ikatusa |undetermined|

    //Core Units |3 units|
            ;
    public static void load(){

        efim = new CnSUnitType("efim") {{
            localizedName = "[purple]Efim";
            description = """
                          [purple]A Stealthy Adversary, Efim Cloaks up and evades enemy defences.
                          Efim Drops a small burst of 8 bombs.
                          
                          Efim due to is modules and compact size is very fragile, But its armor is reinforced.
                          """;
            itemCapacity = 0;
            factions.add(AxFactions.raodon);
            cloaks = true;
            vulnerabilityTime = 520;
            outlines = false;
            health = 80;
            armor = 20;
            hitSize = 18;

            speed = 2.5f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;

            circleTarget = true;
            lowAltitude = true;
            faceTarget = false;
            flying = true;
            engineSize = 0;
            constructor = StealthUnit::new;
            aiController = DynFlyingAI::new;

            engineColor = Color.valueOf("2d0827");
            engineLayer = 0.1f;
            engineSize = 0;
            engines = Seq.with(new UnitEngine(0,-3f,2.5f,-90));

            targetFlags = new BlockFlag[]{BlockFlag.unitAssembler,BlockFlag.core, null};


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
    }
}