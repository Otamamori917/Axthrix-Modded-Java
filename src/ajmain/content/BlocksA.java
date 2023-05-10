package ajmain.content;

import ajmain.content.types.turretypes.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.graphics.*;

import static mindustry.type.ItemStack.*;

public class BlocksA {
    public static Block

    //turrets
    kramola, razdor, smuta;

    public static void load(){
        kramola = new AcceleratedTurret("kramola"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 120f;
            acceleratedBonus = 1.5f;
            acceleratedSteps = 3;
            burnoutDelay = 600f;
            cooldownDelay = 300f;

            buildCostMultiplier = 0.1f;
            size = 2;
            scaledHealth = 420f;
            reload = 10f;
            range = 360f;
            maxAmmo = 200;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            shoot = new HelixAPattern(){{
                mag = 1f;
                scl = 2f;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 15f;
                    homingPower = 4f;
                    homingRange = 50;
                    homingDelay = 20f;
                    width = 2f;
                    height = 4f;
                    hitSize = 2f;
                    lifetime = 100f;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.techBlue;
                    trailLength = 5;
                    trailWidth = 1f;
                }}
            );
            inaccuracy = 2f;
        }};




        razdor = new AcceleratedTurret("razdor"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 120f;
            acceleratedBonus = 1.5f;
            acceleratedSteps = 3;
            burnoutDelay = 700f;
            cooldownDelay = 400f;

            buildCostMultiplier = 0.1f;
            size = 3;
            scaledHealth = 420f;
            reload = 10f;
            range = 360f;
            maxAmmo = 300;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            shoot = new HelixAPattern(){{
                mag = 1.5f;
                scl = 2.5f;
                shots = 3;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 30f;
                    homingPower = 4f;
                    homingRange = 50;
                    homingDelay = 20f;
                    width = 3f;
                    height = 6f;
                    hitSize = 3f;
                    lifetime = 100f;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.techBlue;
                    trailLength = 5;
                    trailWidth = 1f;
                }}
            );
            inaccuracy = 2f;
        }};


        smuta = new AcceleratedTurret("smuta"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 120f;
            acceleratedBonus = 1.5f;
            acceleratedSteps = 3;
            burnoutDelay = 800f;
            cooldownDelay = 500f;

            buildCostMultiplier = 0.1f;
            size = 4;
            scaledHealth = 420f;
            reload = 10f;
            range = 360f;
            maxAmmo = 400;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            shoot = new HelixAPattern(){{
                mag = 1.75f;
                scl = 2.75f;
                shots = 4;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 60f;
                    homingPower = 4f;
                    homingRange = 50;
                    homingDelay = 20f;
                    width = 4f;
                    height = 8f;
                    hitSize = 4f;
                    lifetime = 100f;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.techBlue;
                    trailLength = 5;
                    trailWidth = 1f;
                }}
            );
            inaccuracy = 2f;
        }};
    }
}