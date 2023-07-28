/*package axthrix.world.types.weapontypes;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.units.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class AcceleratedWeapon extends Weapon{
    public Float acceleratedDelay = 120f, acceleratedBonus = 1.5f;
    public Int acceleratedSteps = 1;
    public Float burnoutDelay = 240f, cooldownDelay = 120f;
    public Boolean burnsOut = true;

    public AcceleratedWeapon(String name){
        super(name);
    }
    
    public AcceleratedWeapon(){
    }

    {
        reload = 10f;
        predictTarget = true;
        rotate = true;
        useAmmo = false;
        mountType = AcceleratedMount::new;
        recoil = 10f;
    }

    public float accelBoost, accelCounter;
    public int accelCount;

    @Override
    public void update(Unit unit, WeaponMount mount){
         super.update(unit, mount);

        if(accelCount > acceleratedSteps){
            accelCounter += edelta();
            if(accelCounter >= cooldownDelay){
                accelCount = 0;
                accelBoost = 1;
                accelCounter %= cooldownDelay;
            }
        }else if(isShooting()){
            accelCounter += edelta(); 
            if(accelCount < acceleratedSteps && accelCounter >= acceleratedDelay){
                accelBoost += (acceleratedBonus - 1);
                accelCount++;
                accelCounter %= acceleratedDelay;
            }else if(burnsOut && accelCounter >= burnoutDelay){
                accelBoost = 0;
                accelCount++;
                accelCounter %= burnoutDelay;
            }
        }else{
            accelCount = 0;
            accelCounter = 0;
            accelBoost = 1;
        }
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
        reloadCounter += delta() * multiplier * accelBoost * baseReloadSpeed();

        reloadCounter = Math.min(reloadCounter, reload);
    }
        
    public float boostf(){
        if(accelCount > acceleratedSteps) return 1 - (accelCounter / cooldownDelay);
        return Mathf.clamp((float)accelCount / acceleratedSteps);
    }
}*/