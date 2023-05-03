package ajmain.content.turretypes;

import mindustry.world.blocks.defense.turrets.*;

public class AcceleratedTurret extends ItemTurret{
    public float  acceleratedDelay = 120, acceleratedBonus = 1.5f;
    
    public AcceleratedTurret(String name){
        super(name);
    }

    public class AcceleratedTurretBuild extends ItemTurretBuild{
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
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            accelBoost/2 = Stage
            reloadCounter += delta() * multiplier * Stage * baseReloadSpeed();
            
            reloadCounter += delta() * multiplier * accelBoost * baseReloadSpeed();

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload);
        }
    }
}