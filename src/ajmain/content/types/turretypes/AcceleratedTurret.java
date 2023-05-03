package ajmain.content.types.turretypes;

import mindustry.world.blocks.defense.turrets.*;

public class AcceleratedTurret extends ItemTurret{
    public float  acceleratedDelay = 120, acceleratedBonus = 1.5f, acceleratedSteps = 1;
    
    public AcceleratedTurret(String name){
        super(name);
    }

    public class AcceleratedTurretBuild extends ItemTurretBuild{
        public float accelTimer, accelBoost, accelCount, accelCounter, Boost;

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

            if(isShooting()){
                accelCounter += edelta();
                if(accelCount < acceleratedSteps && accelCounter >= accelTimer){
                    Boost += (accelBoost - 1);
                    accelCount++;
                    accelCounter %= accelTimer;
                }
            }else{
                accelCount = 0;
                accelCounter = 0;
                }
            }

        @Override
        protected void updateReload(){
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += delta() * multiplier * Boost * baseReloadSpeed();

            reloadCounter = Math.min(reloadCounter, reload);
        }
    }
}