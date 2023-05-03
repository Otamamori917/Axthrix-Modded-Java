package ajmain.content;

import ajmain.content.types.turretypes.*;
import mindustry.content.*;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.*;

import static mindustry.type.ItemStack.*;

public class BlocksA {
    public static Block

    //turrets
    acceleratedTurret;

    public static void load(){
        acceleratedTurret = new AcceleratedTurret("accelerated-turret"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            acceleratedDelay = 240f;
            acceleratedBonus = 4f;
            acceleratedSteps = 2f;
            buildCostMultiplier = 0.1f;
            size = 4;
            scaledHealth = 420f;
            reload = 30f;
            range = 180f;
            maxAmmo = 400;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            ammo(
                    Items.titanium, new BasicBulletType(4f, 100){{
                        width = 16f;
                        height = 21f;
                        hitSize = 5f;
                        lifetime = 100f;
                    }}
            );
            inaccuracy = 4f;
        }};
    }
}