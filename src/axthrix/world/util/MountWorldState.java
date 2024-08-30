package axthrix.world.util;

import axthrix.world.types.statuseffects.StackStatusEffect;
import axthrix.world.types.unittypes.MountUnitType;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import mindustry.type.Liquid;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MountWorldState {
    public static void load() {
        SaveVersion.addCustomChunk("mountUnits", new SaveFileReader.CustomChunk() {
            @Override
            public void write(DataOutput stream) throws IOException {
                int count = Vars.content.units().count(mu -> mu instanceof MountUnitType);
                stream.writeInt(count);
                Vars.content.units().each(mu -> {
                    if (mu instanceof MountUnitType mut){
                        try {
                            stream.writeInt(mut.id);
                            stream.writeInt(mut.powTick.size());
                            mut.powTick.forEach((unit,c) -> {
                                try {
                                    if (unit == null) return;
                                    stream.writeInt(unit.id);
                                    stream.writeFloat(c);
                                    if (mut.liquidType.containsKey(unit) && mut.liquidType.get(unit) != null)
                                        stream.writeInt(mut.liquidType.get(unit).id);
                                    else
                                        stream.writeInt(-1);
                                    stream.writeInt(mut.liquidAmount.get(unit));
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
                    int mountUnitId = stream.readInt();
                    int unitsize = stream.readInt();
                    UnitType unittype = Vars.content.units().find(mu -> mu.id == mountUnitId);
                    if (unittype instanceof MountUnitType mut) {
                        mut.liquidAmount.clear();
                        mut.liquidType.clear();
                        mut.powTick.clear();
                        for(;unitsize > 0;unitsize--) {
                            int unitId = stream.readInt();
                            float unitT = stream.readFloat();
                            int unitLid = stream.readInt();
                            int unitLAmt = stream.readInt();
                            Unit unit = Groups.unit.find(u->u.id == unitId);
                            if (unit == null) continue;
                            mut.powTick.put(unit,unitT);
                            mut.liquidType.put(unit, Vars.content.liquid(unitLid));
                            mut.liquidAmount.put(unit,unitLAmt);
                        }
                    }
                }
            }
        });
    }
}
