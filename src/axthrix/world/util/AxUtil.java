package axthrix.world.util;

import mindustry.entities.bullet.*;
import mindustry.type.*;

public class AxUtil {
    public static float GetRange(float speed,float DesiredRange)
    {
        return (DesiredRange*8)/speed;
    }
    public static String GetName(String added)
    {
        return "ax-"+added;
    }
    public static float GetDamage(float DPS, float fireRate){
        return DPS/fireRate;
    }

    public static float bulletDamage(BulletType b, float lifetime){
        if(b.spawnUnit != null){ //Missile unit damage
            if(b.spawnUnit.weapons.isEmpty()) return 0f;
            Weapon uW = b.spawnUnit.weapons.first();
            return bulletDamage(uW.bullet, uW.bullet.lifetime) * uW.shoot.shots;
        }else{
            float damage = b.damage + b.splashDamage; //Base Damage
            damage += b.lightningDamage * b.lightning * b.lightningLength; //Lightning Damage

            if(b.fragBullet != null){ //Frag Bullet Damage
                damage += bulletDamage(b.fragBullet, b.fragBullet.lifetime) * b.fragBullets;
            }

            if(b.intervalBullet != null){ //Interval Bullet Damage
                int amount = (int)(lifetime / b.bulletInterval * b.intervalBullets);
                damage += bulletDamage(b.intervalBullet, b.intervalBullet.lifetime) * amount;
            }

            if(b instanceof ContinuousBulletType cB){ //Continuous Damage
                return damage * lifetime / cB.damageInterval;
            }else{
                return damage;
            }
        }
    }
}
