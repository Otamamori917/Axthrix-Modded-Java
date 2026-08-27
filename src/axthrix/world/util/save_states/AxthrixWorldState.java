package axthrix.world.util.save_states;

import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.abilities.ShieldPlateAbility;
import axthrix.world.types.statuseffects.StackStatusEffect;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.types.unittypes.MountUnitType;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.io.SaveFileReader;
import mindustry.io.SaveVersion;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class AxthrixWorldState {
    public static void load() {
        //mount units
        SaveVersion.addCustomChunk("mountUnits", new SaveFileReader.CustomChunk() {
            @Override
            public void write(DataOutput stream) throws IOException {
                MountUnitType.writeSaveData(stream);
            }

            @Override
            public void read(DataInput stream) throws IOException {
                MountUnitType.readSaveData(stream);
            }
        });


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

        SaveVersion.addCustomChunk("axthrixTemperature", new SaveVersion.CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException {
                TemperatureLogic.writeTemperatureData(stream);
            }

            @Override
            public void read(DataInput stream) throws IOException {
                TemperatureLogic.readTemperatureData(stream);
            }
        });

        SaveVersion.addCustomChunk("axthrixDrones", new SaveVersion.CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException {
                int droneTypeCount = 0;
                for(var unit : Vars.content.units()){
                    if(unit instanceof DroneUnitType){
                        droneTypeCount++;
                    }
                }

                stream.writeInt(droneTypeCount);

                for(var unit : Vars.content.units()){
                    if(unit instanceof DroneUnitType droneType){
                        stream.writeInt(droneType.id);

                        stream.writeInt(droneType.tetherUnit.size());
                        for(var entry : droneType.tetherUnit.entrySet()){
                            Unit drone = entry.getKey();
                            Unit tether = entry.getValue();
                            if(drone != null){
                                stream.writeInt(drone.id);
                                stream.writeInt(tether != null ? tether.id : -1);
                            }
                        }

                        stream.writeInt(droneType.droneSlot.size());
                        for(var entry : droneType.droneSlot.entrySet()){
                            Unit drone = entry.getKey();
                            Integer slot = entry.getValue();
                            if(drone != null){
                                stream.writeInt(drone.id);
                                stream.writeInt(slot);
                            }
                        }

                            Log.info("[DroneSave] Saved DroneUnitType @ (ID: @) - @ tethers, @ slots",
                                    droneType.name, droneType.id,
                                    droneType.tetherUnit.size(), droneType.droneSlot.size());

                    }
                }

                int abilityDataCount = 0;

                for(var unit : Groups.unit){
                    if(unit.abilities != null){
                        for(var ability : unit.abilities){
                            if(ability instanceof DroneSpawnAbility spawn && spawn.aliveUnit.containsKey(unit)){
                                abilityDataCount++;
                            }
                        }
                    }
                }

                stream.writeInt(abilityDataCount);

                for(var unit : Groups.unit){
                    if(unit.abilities != null){
                        for(int i = 0; i < unit.abilities.length; i++){
                            var ability = unit.abilities[i];
                            if(ability instanceof DroneSpawnAbility spawn){
                                if(spawn.aliveUnit.containsKey(unit)){
                                    stream.writeInt(unit.id);
                                    stream.writeInt(i);
                                    stream.writeInt(spawn.droneSlot);

                                    Unit aliveUnit = spawn.aliveUnit.get(unit);
                                    stream.writeInt(aliveUnit != null ? aliveUnit.id : -1);

                                    stream.writeFloat(spawn.warmup.get(unit));
                                    stream.writeFloat(spawn.timer);


                                        Log.info("[DroneSave] Saved ability for unit @ (ability @) - alive drone: @",
                                                unit.id, i, aliveUnit != null ? aliveUnit.id : "none");
                                }
                            }
                        }
                    }
                }

                Log.info("[Axthrix] Saved @ drone types, @ ability instances", droneTypeCount, abilityDataCount);
            }

            @Override
            public void read(DataInput stream) throws IOException {

                int droneTypeCount = stream.readInt();


                for(int i = 0; i < droneTypeCount; i++){
                    int typeId = stream.readInt();
                    var unitType = Vars.content.unit(typeId);

                    if(unitType instanceof DroneUnitType droneType){

                        droneType.tetherUnit.clear();
                        droneType.droneSlot.clear();

                        int tetherCount = stream.readInt();
                        for(int j = 0; j < tetherCount; j++){
                            int droneId = stream.readInt();
                            int tetherId = stream.readInt();

                            Unit drone = Groups.unit.getByID(droneId);
                            Unit tether = tetherId != -1 ? Groups.unit.getByID(tetherId) : null;

                            if(drone != null){
                                droneType.tetherUnit.put(drone, tether);
                            }
                        }


                        int slotCount = stream.readInt();
                        for(int j = 0; j < slotCount; j++){
                            int droneId = stream.readInt();
                            int slot = stream.readInt();

                            Unit drone = Groups.unit.getByID(droneId);

                            if(drone != null){
                                droneType.droneSlot.put(drone, slot);
                            }
                        }

                            Log.info("[DroneSave] Loaded DroneUnitType @ (ID: @) - @ tethers, @ slots",
                                    droneType.name, droneType.id,
                                    droneType.tetherUnit.size(), droneType.droneSlot.size());

                    }
                }


                int abilityDataCount = stream.readInt();


                for(int i = 0; i < abilityDataCount; i++){
                    int unitId = stream.readInt();
                    int abilityIndex = stream.readInt();
                    int droneSlot = stream.readInt();
                    int aliveUnitId = stream.readInt();
                    float warmup = stream.readFloat();
                    float timer = stream.readFloat();

                    Unit unit = Groups.unit.getByID(unitId);

                    if(unit != null && unit.abilities != null && abilityIndex < unit.abilities.length){
                        var ability = unit.abilities[abilityIndex];

                        if(ability instanceof DroneSpawnAbility spawn){
                            Unit aliveUnit = aliveUnitId != -1 ? Groups.unit.getByID(aliveUnitId) : null;

                            spawn.aliveUnit.put(unit, aliveUnit);
                            spawn.warmup.put(unit, warmup);
                            spawn.timer = timer;
                            spawn.droneSlot = droneSlot;

                                Log.info("[DroneSave] Loaded ability for unit @ (ability @) - alive drone: @, warmup: @, timer: @",
                                        unitId, abilityIndex, aliveUnitId, warmup, timer);

                        }
                    }
                }

                Log.info("[Axthrix] Loaded @ drone types, @ ability instances", droneTypeCount, abilityDataCount);
            }
        });
        SaveVersion.addCustomChunk("plateShields", new SaveVersion.CustomChunk(){
            @Override
            public void write(DataOutput stream) throws IOException {
                ShieldPlateAbility.writeSaveData(stream);
            }

            @Override
            public void read(DataInput stream) throws IOException {
                ShieldPlateAbility.readSaveData(stream);
            }
        });
    }
}