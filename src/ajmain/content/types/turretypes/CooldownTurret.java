/*package ajmain.content.types.turretypes;

import mindustry.graphics.*;
import mindustry.ui.*;
import arc.*;

import mindustry.world.blocks.defense.turrets.*;

public class CooldownTurret extends ItemTurret{
    public float cooldownTreshold = 10, cooldownPeirod = 240
    public float heatsinkX, heatsinkY, heatsinkMoveY;

    
    public CooldownTurret(String name){
        super(name);
    }

  @Override
    public void setBars(){
        super.setBars();
        addBar("Firerate", (CooldownTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.aj-firerate-bonus"),
            () -> Pal.heal,
            () -> entity.cooldownCount / cooldownTreshold));
    }

    public class CooldownTurretBuild extends ItemTurretBuild{
        public float cooldownCounter;
        public int cooldownCount;

        @Override
        public void updateTile(){
            super.updateTile();

            if(isShooting()){
                cooldownCounter += edelta();
                if(cooldownCount < cooldownTreshold && cooldownCounter >= 60){
                    accelBoost += (CooldownBonus - 1);
                    cooldownCount++;
                    cooldownCounter %= 60;
                }
            }else{
                cooldownCount = 0;
                cooldownCounter = 0;
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
}*/