package ajmain.content.types.weapontypes;

import mindustry.entities.units.WeaponMount.*;

public class AcceleratedWeaponMount extends WeaponMount{
    public float  acceleratedDelay = 120, acceleratedBonus = 1.5f;
    
    public AcceleratedWeaponMount(String name){
        super(name);
    }

    public class AcceleratedWeaponBuild extends WeaponMountBuild{
        public float accelTimer, accelBoost;

        @Override
        public void updateTile(){
            super.updateTile();

            if(isShooting()){
                accelTimer += edelta();
                if(accelTimer >= acceleratedDelay) accelBoost = acceleratedBonus;
            }else{
                accelBoost = 1;
                accelTimer = 0;
            }
        }

        @Override
        protected void updateReload(){
            float multiplier = hasAmmo() ? peekAmmo().unit.reloadMultiplier : 1f;
            reloadCounter += delta() * multiplier * accelBoost * baseReloadSpeed();

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload);
        }
    }
}