package axthrix.world.util;

import axthrix.world.types.statuseffects.StackStatusEffect;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import mindustry.type.StatusEffect;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class StackWorldState {
    public static void load() {
        SaveVersion.addCustomChunk("stackEffects", new SaveFileReader.CustomChunk() {
            @Override
            public void write(DataOutput stream) throws IOException {
                int count = Vars.content.statusEffects().count(se -> se instanceof StackStatusEffect);
                stream.writeInt(count);
                Vars.content.statusEffects().each(se -> {
                    if (se instanceof StackStatusEffect st){
                        try {
                            stream.writeInt(st.id);
                            stream.writeInt(st.unitCharges.size());
                            st.unitCharges.forEach((unit,c) -> {
                                try {
                                    if (unit == null) return;
                                    stream.writeInt(unit.id);
                                    stream.writeInt(c);
                                    if (st.unitTeam.containsKey(unit))
                                        stream.writeInt(st.unitTeam.get(unit).id);
                                    else
                                        stream.writeInt(-1);
                                    stream.writeFloat(st.unitTime.get(unit));
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
                    int statusEffId = stream.readInt();
                    int unitsize = stream.readInt();
                    StatusEffect status = Vars.content.statusEffects().find(se -> se.id == statusEffId);
                    if (status instanceof StackStatusEffect st) {
                        st.unitTeam.clear();
                        st.unitTime.clear();
                        st.unitCharges.clear();
                        for(;unitsize > 0;unitsize--) {
                            int unitId = stream.readInt();
                            int unitSt = stream.readInt();
                            int unitT = stream.readInt();
                            float unitTime = stream.readFloat();
                            Unit unit = Groups.unit.find(u->u.id == unitId);
                            if (unit == null) continue;
                            st.unitCharges.put(unit,unitSt);
                            st.unitTeam.put(unit, Team.get(unitT));
                            st.unitTime.put(unit,unitTime);
                        }
                    }
                }
            }
        });
    }
}
