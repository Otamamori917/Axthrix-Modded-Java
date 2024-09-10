package axthrix.world.util;

import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.types.unittypes.MountUnitType;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import mindustry.type.UnitType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DroneWorldState {
    public static void load() {
        SaveVersion.addCustomChunk("dronesUnits", new SaveFileReader.CustomChunk() {
            @Override
            public void write(DataOutput stream) throws IOException {
                int count = Vars.content.units().count(ut -> ut instanceof DroneUnitType) + Vars.content.units().count(ut -> ut.abilities.first() instanceof DroneSpawnAbility);
                stream.writeInt(count);
                Vars.content.units().each(ut -> {
                    if (ut instanceof DroneUnitType dut){
                        try {
                            stream.writeInt(dut.id);
                            stream.writeInt(dut.tetherUnit.size());
                            dut.tetherUnit.forEach((unit,c) -> {
                                try {
                                    if (unit == null) return;
                                    stream.writeInt(unit.id);
                                    stream.writeInt(c.id);
                                    stream.writeFloat(dut.delay.get(unit));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            @Override
            public void read(DataInput stream) throws IOException {
                int count = stream.readInt();
                for(;count > 0;count--) {
                    int droneId = stream.readInt();
                    int unitsize = stream.readInt();
                    UnitType unittype = Vars.content.units().find(mu -> mu.id == droneId);
                    if (unittype instanceof DroneUnitType dut /*&& unittype.abilities.first() instanceof DroneSpawnAbility abl*/) {
                        dut.tetherUnit.clear();
                        dut.delay.clear();
                        //abl.aliveUnit.clear();
                        for(;unitsize > 0;unitsize--) {
                            int unitId = stream.readInt();
                            int unitDid = stream.readInt();
                            Float unitDtic = stream.readFloat();
                            Unit unit = Groups.unit.find(u->u.id == unitId);
                            if (unit == null) continue;
                            dut.tetherUnit.put(unit, Groups.unit.find(u->u.id == unitDid));
                            dut.delay.put(unit,unitDtic);
                            //abl.aliveUnit.put(Groups.unit.find(u->u.id == unitDid),unit);
                        }
                    }
                }
            }
        });
    }
}
