package ajmain.content.types.turretypes;

import mindustry.world.blocks.defense.turrets.*;

public class AcceleratedTurret extends ItemTurret{
    public float acceleratedDelay = 120, acceleratedBonus = 1.5f, acceleratedSteps = 1;
    
    public AcceleratedTurret(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();

        if(accelBoost > 0){
            addBar("FireRate", (TurretBuild entity) ->
            new Bar(() ->
            Core.bundle.format("bar.firerate", (int)accelBoost),
            () -> pal.heal));
        }
    }

    public class AcceleratedTurretBuild extends ItemTurretBuild{
        public float accelBoost, accelCount, accelCounter;

        @Override
        public void updateTile(){
            super.updateTile();

            if(isShooting()){
                accelCounter += edelta();
                if(accelCount < acceleratedSteps && accelCounter >= acceleratedDelay){
                    accelBoost += (acceleratedBonus - 1);
                    accelCount++;
                    accelCounter %= acceleratedDelay;
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
    }
}