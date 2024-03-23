package axthrix.content;

import axthrix.world.types.entities.comp.StealthUnit;
import axthrix.world.types.ai.DynFlyingAI;
import axthrix.world.types.unittypes.CnSUnitType;
import mindustry.content.Items;
import mindustry.entities.bullet.*;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.ammo.ItemAmmoType;

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
            cloaks = true;
            vulnerabilityTime = 260;
            float unitRange = 28 * tilesize;
            health = 450;
            hitSize = 18;

            speed = 2.5f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;

            ammoType = new ItemAmmoType(Items.graphite);

            circleTarget = true;
            lowAltitude = true;
            faceTarget = flying = true;
            range = unitRange;

            engineSize = 0;
            constructor = StealthUnit::new;
            aiController = DynFlyingAI::new;


            weapons.add(new Weapon("puw"){{
                rotate = true;
                rotateSpeed = 3;
                shootY = 2f;
                x = 1f;
                y = 0f;
                mirror = false;
                reload = 10;
                top = true;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }});
        }};
    }
}