package axthrix.world.types.abilities;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

public class SwarmIntelligenceAbility extends Ability {
    // Combat stats
    public float damageBonus = 0.02f;
    public float reloadBonus = 0.04f;
    public float rangeBonus = 4f;

    // Movement stats
    public float speedBonus = 0.05f;

    // Survival stats
    public float healthRegenBonus = 0f;
    public float armorBonus = 2f;
    public float shieldRegenBonus = 0f;

    // Utility stats
    public float buildSpeedBonus = 0.1f;
    public float payloadCapacityBonus = 0f;

    public HashMap<Unit, float[]> baseRanges = new HashMap<>();
    public HashMap<Unit, Float> baseArmor = new HashMap<>();
    public HashMap<Unit, Float> basePayloadCapacity = new HashMap<>();

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-swarm-intelligence");
    }

    @Override
    public void addStats(Table t){
        // Only show stats that are > 0
        if(damageBonus > 0){
            t.add("[lightgray]" + Stat.damage.localized() + ": [white]+" + (int)(damageBonus * 100f) + "% [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(reloadBonus > 0){
            t.add("[lightgray]" + Stat.reload.localized() + ": [white]+" + (int)(reloadBonus * 100f) + "% [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(rangeBonus > 0){
            t.add("[lightgray]" + Stat.range.localized() + ": [white]+" + Strings.autoFixed(rangeBonus / 8f, 1) + " [lightgray]"+ StatUnit.blocks.localized() +" "+ Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(speedBonus > 0){
            t.add("[lightgray]" + Stat.speed.localized() + ": [white]+" + (int)(speedBonus * 100f) + "% [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(healthRegenBonus > 0){
            t.add("[lightgray]" + Stat.healing.localized() + ": [white]+" + Strings.autoFixed(healthRegenBonus * 60f, 1) + " [lightgray]"+ StatUnit.perSecond.localized() +" "+ Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(armorBonus > 0){
            t.add("[lightgray]"+Stat.armor.localized()+" [white]+" + Strings.autoFixed(armorBonus, 1) + " [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(shieldRegenBonus > 0){
            t.add("[lightgray]"+Stat.regenerationRate.localized()+": [white]+" + Strings.autoFixed(shieldRegenBonus * 60f, 1) + " [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(buildSpeedBonus > 0){
            t.add("[lightgray]" + Stat.buildSpeed.localized() + ": [white]+" + (int)(buildSpeedBonus * 100f) + "% [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
        if(payloadCapacityBonus > 0){
            t.add("[lightgray]"+Stat.payloadCapacity.localized()+": [white]+" + (int)(payloadCapacityBonus * 100f) + "% [lightgray]" + Core.bundle.format("stat.aj-per-drone"));
            t.row();
        }
    }

    @Override
    public void update(Unit unit){

        if (!baseRanges.containsKey(unit)){
            baseRanges.put(unit, null);
        }
        if (!baseArmor.containsKey(unit)){
            baseArmor.put(unit, -1f);
        }
        if (!basePayloadCapacity.containsKey(unit)){
            basePayloadCapacity.put(unit, -1f);
        }


        if(baseRanges.get(unit) == null && unit.mounts != null && unit.mounts.length > 0){
            baseRanges.replace(unit, new float[unit.mounts.length]);
            for(int i = 0; i < unit.mounts.length; i++){
                if(unit.mounts[i].weapon != null){
                    baseRanges.get(unit)[i] = unit.mounts[i].weapon.range();
                }
            }
        }
        if(baseArmor.get(unit) < 0) baseArmor.replace(unit,unit.armor);
        if(basePayloadCapacity.get(unit) < 0) basePayloadCapacity.replace(unit,unit.type.payloadCapacity);


        int droneCount = 0;
        for(var ability : unit.abilities){
            if(ability instanceof DroneSpawnAbility droneAbility){
                Unit drone = droneAbility.aliveUnit.get(unit);
                if(drone != null && drone.isValid()){
                    droneCount++;
                }
            }
        }

        if(droneCount > 0){

            if(damageBonus > 0){
                unit.damageMultiplier = 1f + (damageBonus * droneCount);
            }
            if(reloadBonus > 0){
                unit.reloadMultiplier = 1f + (reloadBonus * droneCount);
            }


            if(speedBonus > 0){
                unit.speedMultiplier = 1f + (speedBonus * droneCount);
            }


            if(healthRegenBonus > 0 && unit.damaged()){
                unit.heal(healthRegenBonus);
            }
            if(armorBonus > 0){
                unit.armor = baseArmor.get(unit) + (armorBonus * droneCount);
            }
            if(shieldRegenBonus > 0 && unit.shield < unit.maxHealth){
                unit.shield = Math.min(unit.shield + shieldRegenBonus, unit.maxHealth);
            }


            if(buildSpeedBonus > 0){
                unit.buildSpeedMultiplier = 1f + (buildSpeedBonus * droneCount);
            }
            if(payloadCapacityBonus > 0){
                float capacityMult = 1f + (payloadCapacityBonus * droneCount);
                unit.type.payloadCapacity = basePayloadCapacity.get(unit) * capacityMult;
            }


            if(rangeBonus > 0 && unit.mounts != null && baseRanges != null){
                float rangeAdd = rangeBonus * droneCount;
                for(int i = 0; i < unit.mounts.length; i++){
                    if(unit.mounts[i].weapon != null && i < baseRanges.get(unit).length){
                        Weapon weapon = unit.mounts[i].weapon;
                        weapon.bullet.lifetime = (baseRanges.get(unit)[i] + rangeAdd) / weapon.bullet.speed;
                    }
                }
            }

        }else{

            unit.damageMultiplier = 1f;
            unit.reloadMultiplier = 1f;
            unit.speedMultiplier = 1f;
            unit.drag = unit.type.drag;
            unit.armor = baseArmor.get(unit);
            unit.buildSpeedMultiplier = 1f;
            unit.type.payloadCapacity = basePayloadCapacity.get(unit);

            if(unit.mounts != null && baseRanges != null){
                for(int i = 0; i < unit.mounts.length; i++){
                    if(unit.mounts[i].weapon != null && i < baseRanges.get(unit).length){
                        Weapon weapon = unit.mounts[i].weapon;
                        weapon.bullet.lifetime = baseRanges.get(unit)[i] / weapon.bullet.speed;
                    }
                }
            }
        }
    }
}