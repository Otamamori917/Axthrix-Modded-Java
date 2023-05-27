package axthrix.types.turretypes;

import arc.*;
import arc.math.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;

public class AcceleratedTurret extends ItemTurret{
    public float acceleratedDelay = 120, acceleratedBonus = 1.5f;
    public int acceleratedSteps = 1;
    public float burnoutDelay = 240, cooldownDelay = 120;
    public boolean burnsOut = true;

    public AcceleratedTurret(String name){
        super(name);
    }
    

    @Override
    public void setBars(){
        super.setBars();
        addBar("aj-phases", (AcceleratedTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.aj-phases", Strings.autoFixed(entity.accelBoost * 100f, 2)),
            () -> entity.accelCount > acceleratedSteps ? Pal.remove : Pal.techBlue,
            entity::boostf
        ));
    }

    public class AcceleratedTurretBuild extends ItemTurretBuild{
        public float accelBoost, accelCounter;
        public int accelCount;

        @Override
        public void updateTile(){
            super.updateTile();

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
        protected void updateReload(){
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += delta() * multiplier * accelBoost * baseReloadSpeed();

            reloadCounter = Math.min(reloadCounter, reload);
        }
        
        public float boostf(){
            if(accelCount > acceleratedSteps) return 1 - (accelCounter / cooldownDelay);
            return Mathf.clamp((float)accelCount / acceleratedSteps);
        }
    }
}