package axthrix.world.types.ai;

import mindustry.entities.*;
import mindustry.entities.units.*;

public class SentriAI extends AIController{
    @Override
    public void updateMovement(){
        if(!Units.invalidateTarget(target, unit, unit.range()) && unit.type.faceTarget && unit.type.hasWeapons()){
            unit.lookAt(Predict.intercept(unit, target, unit.type.weapons.first().bullet.speed));
        }
    }

    @Override
    public boolean retarget(){
        return timer.get(timerTarget, target == null ? 10f : 20f);
    }
}
