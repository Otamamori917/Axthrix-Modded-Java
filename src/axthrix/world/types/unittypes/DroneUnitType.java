package axthrix.world.types.unittypes;

import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.abilities.DroneSpawnAbility;
import mindustry.gen.*;

import java.util.HashMap;

public class DroneUnitType extends AmmoLifeTimeUnitType {


    public HashMap<Unit, Unit> tetherUnit = new HashMap<>();
    public int tetherUnitID = -1;
    public DroneUnitType(String name){
        super(name);
    }

    @Override
    public void update(Unit unit){
        if (!tetherUnit.containsKey(unit)){
            tetherUnit.put(unit,null);
        }
        if (tetherUnitID != -1) {
            tetherUnit.replace(unit, Groups.unit.getByID(tetherUnitID));
            tetherUnitID = -1;
        }
        if(tetherUnit.get(unit) == null || !tetherUnit.get(unit).isValid() || tetherUnit.get(unit).team != unit.team){
            Call.unitDespawn(unit.self());
            if(tetherUnit.get(unit) != null) {
                if(tetherUnit.get(unit).type.abilities.peek() instanceof DroneSpawnAbility abl){
                    abl.unitAlive.replace(tetherUnit.get(unit),false);
                }
            }
        }
    }
}
