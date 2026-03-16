package axthrix.world.types.unittypes;

import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.abilities.DroneSpawnAbility;
import mindustry.gen.*;

import java.util.HashMap;

public class DroneUnitType extends AmmoLifeTimeUnitType {

    public HashMap<Unit, Unit> tetherUnit = new HashMap<>();
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

        if(tetherUnit.get(unit) == null || !tetherUnit.get(unit).isValid() || tetherUnit.get(unit).team != unit.team){
            Call.unitDespawn(unit.self());
        }

        // Shield drones block piercing
        if(isShield){
            // Check for nearby bullets that might pierce
            mindustry.gen.Groups.bullet.intersect(
                    unit.x - unit.hitSize,
                    unit.y - unit.hitSize,
                    unit.hitSize * 2,
                    unit.hitSize * 2,
                    bullet -> {
                        if(bullet.team != unit.team &&
                                bullet.type.pierce &&
                                bullet.within(unit, unit.hitSize)){
                            // This bullet just hit us - remove it to prevent pierce
                            bullet.type.fragOnAbsorb = false;
                            bullet.absorb();
                        }
                    }
            );
        }
    }
}
