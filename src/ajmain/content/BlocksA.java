package ajmain.content;

import ajmain.content.types.turretypes.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;

import static mindustry.type.ItemStack.*;

public class BlocksA {
    public static Block

    //turrets
    kramola, razdor, smuta;

    public static void load(){
        kramola = new AcceleratedTurret("kramola"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            acceleratedDelay = 120f;
            acceleratedBonus = 2f;
            acceleratedSteps = 2f;
            buildCostMultiplier = 0.1f;
            size = 1;
            scaledHealth = 420f;
            reload = 10f;
            range = 180f;
            maxAmmo = 200;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            shoot.shots = 4;
            shoot.shotDelay = 20;
            shoot = new ShootHelix(){{
                mag = 1f;
                scl = 2f;
            }};
            ammo(
                    Items.titanium, new BasicBulletType(4f, 100){{
                        homingPower = 4f;
                        homingRange = 50;
                        homingDelay = 20f;
                        width = 2f;
                        height = 5f;
                        hitSize = 1f;
                        lifetime = 100f;
                    }}
            );
            inaccuracy = 0f;
        }};
    }
}