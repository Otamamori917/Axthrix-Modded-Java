package axthrix.world.types.unittypes;

import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.ai.TemporaryDroneAI;
import mindustry.gen.*;

import java.util.HashMap;
/** @author Otamamori **/
public class DroneUnitType extends AxUnitType {

    public HashMap<Unit, Unit> tetherUnit = new HashMap<>();
    public HashMap<Unit, Building> tetherBuilding = new HashMap<>();
    public HashMap<Unit, Integer> droneSlot = new HashMap<>();

    public boolean isShield = false;

    public DroneUnitType(String name){
        super(name);
        useUnitCap = false;
    }

    @Override
    public void update(Unit unit){
        if (!tetherUnit.containsKey(unit)){
            tetherUnit.put(unit,null);
        }
        if (!tetherBuilding.containsKey(unit)){   // ← NEW
            tetherBuilding.put(unit, null);
        }

        if(unit.controller() instanceof TemporaryDroneAI){
        } else if(tetherUnit.get(unit) == null || !tetherUnit.get(unit).isValid() || tetherUnit.get(unit).team != unit.team){
            if(tetherBuilding.get(unit) == null || !tetherBuilding.get(unit).isValid() || tetherBuilding.get(unit).team != unit.team){
                Call.unitDespawn(unit.self());
                return;
            }
        }

        if(isShield){
//            mindustry.gen.Groups.bullet.intersect(
//                    unit.x - unit.hitSize,
//                    unit.y - unit.hitSize,
//                    unit.hitSize * 2,
//                    unit.hitSize * 2,
//                    bullet -> {
//                        if(bullet.team != unit.team &&
//                                bullet.type.pierce &&
//                                bullet.within(unit, unit.hitSize)){
//                            bullet.type.fragOnAbsorb = false;
//                            bullet.absorb();
//                        }
//                    }
//            );
        }
    }
}
