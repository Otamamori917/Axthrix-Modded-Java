package axthrix.world.util;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class AxStats {
    public static Stat
    maxDamage,maxDamageMultiplier,maxHealthMultiplier,maxSpeedMultiplier,maxReloadSpeedMultiplier, maxBuildSpeedMultiplier,maxCharges,maxHealing,dragMultiplier,maxDragMultiplier,newTeam,
    grinderTier,pierceReduction,shield,maxShield,timeToCloak,recipes,producer,produce,used,maxFireRateBonus,timeForMaxBonus,overheat,timeToCool
            ;
    public static void load()
    {
        dragMultiplier = new Stat("dragMultiplier",StatCat.function);
        maxDamage = new Stat("maxDamage",StatCat.function);
        maxDamageMultiplier = new Stat("maxDamageMultiplier",StatCat.function);
        maxHealthMultiplier = new Stat("MaxHealthMultiplier",StatCat.function);
        maxSpeedMultiplier = new Stat("maxSpeedMultiplier",StatCat.function);
        maxReloadSpeedMultiplier = new Stat("maxReloadMultiplier",StatCat.function);
        maxBuildSpeedMultiplier = new Stat("maxBuildSpeedMultiplier",StatCat.function);
        maxDragMultiplier = new Stat("maxDragMultiplier",StatCat.function);
        maxHealing = new Stat("maxHealing",StatCat.function);
        maxCharges = new Stat("maxCharges",StatCat.function);
        newTeam = new Stat("newTeam",StatCat.function);
        grinderTier = new Stat("grinderTier",StatCat.crafting);
        pierceReduction = new Stat("pierceReduction",StatCat.general);
        shield = new Stat("shield",StatCat.function);
        maxShield = new Stat("maxShield",StatCat.function);
        timeToCloak = new Stat("timeToCloak",StatCat.function);
        maxFireRateBonus = new Stat("maxBonus",StatCat.function);
        timeForMaxBonus = new Stat("timeMax",StatCat.function);
        overheat = new Stat("timeOverheat",StatCat.function);
        timeToCool = new Stat("timeCool",StatCat.function);
        recipes = new Stat("recipes", StatCat.crafting);
        producer = new Stat("producer", StatCat.crafting);
        produce = new Stat("produce", StatCat.crafting);
        used = new Stat("used", StatCat.crafting);
    }
}
