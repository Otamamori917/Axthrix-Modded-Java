package axthrix.world.types.unittypes;

import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.gen.*;

public class DroneUnitType extends AmmoLifeTimeUnitType {
    public transient @Nullable Unit tetherUnit;
    public int tetherUnitID = -1;
    public DroneUnitType(String name){
        super(name);
    }

    @Override
    public void update(Unit unit){
        if (tetherUnitID != -1) {
            tetherUnit = Groups.unit.getByID(tetherUnitID);
            tetherUnitID = -1;
        }
        if(tetherUnit == null || !tetherUnit.isValid() || tetherUnit.team != unit.team){
            Call.unitDespawn(unit.self());
        }
    }

    public void read(Reads read) {
        tetherUnitID = read.i();
    }

    public void write(Writes write) {
        write.i(tetherUnit.id);
    }
}
