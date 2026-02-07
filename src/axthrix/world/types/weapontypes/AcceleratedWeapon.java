package axthrix.world.types.weapontypes;

import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

/** @author EyeOfDarkness */
public class AcceleratedWeapon extends Weapon{
    public float accelCooldownTime = 120f;
    public float accelCooldownWaitTime = 60f;
    public float accelPerShot = 1.1f;
    public float minReload = 5f;

    public AcceleratedWeapon(String name){
        super(name);
        mountType = AcceleratedMount::new;
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        AcceleratedMount aMount = (AcceleratedMount)mount;
        //mount.reload -= ((aMount.accel / minReload) * unit.reloadMultiplier * Time.delta) * (reload - minReload);
        float r = ((aMount.accel / reload) * unit.reloadMultiplier * Time.delta) * (reload - minReload);
        if(!alternate || otherSide == -1){
            mount.reload -= r;
        }else{
            WeaponMount other = unit.mounts[otherSide];
            other.reload -= r / 2f;
            mount.reload -= r / 2f;
            if(other instanceof AcceleratedMount aM){
                float accel = unit.isShooting() && unit.canShoot() ? Math.max(aM.accel, aMount.accel) : Math.min(aM.accel, aMount.accel);
                float wTime = unit.isShooting() && unit.canShoot() ? Math.max(aM.waitTime, aMount.waitTime) : Math.min(aM.waitTime, aMount.waitTime);
                aM.accel = accel;
                aM.waitTime = wTime;
                aMount.accel = accel;
                aMount.waitTime = wTime;
            }
        }
        if(aMount.waitTime <= 0f){
            aMount.accel = Math.max(0f, aMount.accel - (minReload / accelCooldownTime) * Time.delta);
        }else{
            aMount.waitTime -= Time.delta;
        }
        super.update(unit, mount);
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation){
        AcceleratedMount aMount = (AcceleratedMount)mount;
        aMount.accel = Mathf.clamp(aMount.accel + accelPerShot, 0f, minReload);
        aMount.waitTime = accelCooldownWaitTime;
        super.shoot(unit, mount, shootX, shootY, rotation);
    }

    public static class AcceleratedMount extends WeaponMount{
        float accel = 0f;
        float waitTime = 0f;

        AcceleratedMount(Weapon weapon){
            super(weapon);
        }
    }
}
