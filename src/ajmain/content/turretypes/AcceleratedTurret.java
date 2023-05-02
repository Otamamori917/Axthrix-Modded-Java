package ajmain.content.turretypes;

import mindustry.world.blocks.defense.turrets.*;

public class AcceleratedTurret extends ItemTurret{
    public final int boostTimer = timers++;
    public float  acceleratedDelay = 120, acceleratedBonus = 1.5f;
    
    public AcceleratedTurret(String name){
        super(name);
    }

    public class AcceleratedTurretBuild extends ItemTurretBuild{
        public float accelBoost;

        @Override
        public void updateTile(){
            super.updateTile();

            if(isShooting()){
                if(timer.get(boostTimer, acceleratedDelay)) accelBoost = acceleratedBonus;
            }else{
                accelBoost = 1;
            }
        }

        @Override
        protected void updateReload(){
            float accelBoost = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += delta() * accelBoost * baseReloadSpeed();

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload);
        }
    }
}