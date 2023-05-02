package ajmain.content.turretypes;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

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

        protected void updateReload(){
            float accelBoost = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += delta() * accelBoost * baseReloadSpeed();

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload);
        }
    }
}
