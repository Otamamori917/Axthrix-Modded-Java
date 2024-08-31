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
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (ut.abilities.first() instanceof DroneSpawnAbility dsa && ut instanceof AxUnitType aut){
                        try {
                            stream.writeInt(aut.id);
                            stream.writeInt(dsa.unitAlive.size());
                            dsa.unitAlive.forEach((unit,c) -> {
                                try {
                                    if (unit == null) return;
                                    stream.writeInt(unit.id);
                                    stream.writeBoolean(c);
                                    if (dsa.aliveUnit.containsKey(unit) && dsa.aliveUnit.get(unit) != null)
                                        stream.writeInt(dsa.aliveUnit.get(unit).id);
                                    else
                                        stream.writeInt(-1);
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
                    int dunitsize = stream.readInt();
                    UnitType dunittype = Vars.content.units().find(mu -> mu.id == droneId);
                    if (dunittype instanceof DroneUnitType dut) {
                        dut.tetherUnit.clear();
                        for(;dunitsize > 0;dunitsize--) {
                            int unitId = stream.readInt();
                            int unitDid = stream.readInt();
                            Unit unit = Groups.unit.find(u->u.id == unitId);
                            if (unit == null) continue;
                            dut.tetherUnit.put(unit, Groups.unit.find(u->u.id == unitDid));
                        }
                    }
                    int mountUnitId = stream.readInt();
                    int sunitsize = stream.readInt();
                    UnitType sunittype = Vars.content.units().find(mu -> mu.id == mountUnitId);
                    if (sunittype.abilities.first() instanceof DroneSpawnAbility dsa) {
                        dsa.unitAlive.clear();
                        dsa.aliveUnit.clear();
                        for(;sunitsize > 0;sunitsize--) {
                            int unitId = stream.readInt();
                            boolean unitDa = stream.readBoolean();
                            int unitDid = stream.readInt();
                            Unit unit = Groups.unit.find(u->u.id == unitId);
                            if (unit == null) continue;
                            dsa.unitAlive.put(unit,unitDa);
                            dsa.aliveUnit.put(unit, Groups.unit.find(u->u.id == unitDid));
                        }
                    }
                }
            }
        });
    }
}
