package axthrix.world.util.logics;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import axthrix.AxthrixLoader;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.block.env.TemperatureFloor;
import axthrix.world.types.unittypes.AxUnitType;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.gen.Building;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.type.unit.ErekirUnitType;
import mindustry.world.Tile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TemperatureLogic {


    /// DEBUG

    public static boolean debugLogging = false;


    /// STATIC TRACKING MAPS

    /// Temperature tracking - combined (heat - cold, cancels out)
    protected static ObjectMap<Integer, Float> unitTemperature = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingTemperature = new ObjectMap<>();

    /// Separate heat/cold tracking (for blocks that need both)
    protected static ObjectMap<Integer, Float> buildingHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingCold = new ObjectMap<>();

    ///last hit times for heat and cold and combined
    protected static ObjectMap<Integer, Float> lastHitTime = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> lastHitTimeHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> lastHitTimeCold = new ObjectMap<>();

    /// Stat modifications
    protected static ObjectMap<Integer, Float> originalArmor = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> coldResistanceReduction = new ObjectMap<>();

    /// Unit accumulation resistance
    protected static ObjectMap<Integer, Float> unitAccumulationResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> unitAccumulationResistanceCold = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> unitEffectResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> unitEffectResistanceCold = new ObjectMap<>();

    /// Building accumulation resistance
    protected static ObjectMap<Integer, Float> buildingAccumulationResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingAccumulationResistanceCold = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingEffectResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingEffectResistanceCold = new ObjectMap<>();

    /// idk bruh
    protected static ObjectMap<Integer, TileCont> blockTilesList = new ObjectMap<>();

    /// Effect particle counters
    protected static int heatParticleCounter = 0;
    protected static int coldParticleCounter = 0;
    protected static int dualTempParticleCounter = 0;


    /// MULTI-TILE TEMPERATURE APPLICATION

    public static void applyTemperatureFromFloors(Building building) {
        int pos = building.pos();
        if (!blockTilesList.containsKey(pos)) {
            TileCont cont = new TileCont();
            int size = building.block.size;

            cont.isEven = (size % 2 == 0);

            int offset = cont.isEven ? 1 : 0;

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int tx = (building.tileX() + x - size / 2) + offset;
                    int ty = (building.tileY() + y - size / 2) + offset;
                    Tile tile = Vars.world.tile(tx, ty);
                    if (tile != null) cont.tiles.put(tile.pos(), tile);
                }
            }
            blockTilesList.put(pos, cont);
        }

        TileCont cached = blockTilesList.get(pos);
        float totalHeat = 0f, totalCold = 0f;
        int heatCount = 0, coldCount = 0;

        for (Tile tile : cached.tiles.values()) {
            if (tile.floor() instanceof TemperatureFloor tempFloor) {
                float temp = tempFloor.temperaturePerSecond * Time.delta / 60f;

                if (temp > 0) { totalHeat += temp; heatCount++; }
                else if (temp < 0) { totalCold += -temp; coldCount++; }
            }
        }

        if (heatCount > 0) applyTemperatureBuilding(building, totalHeat / heatCount);
        if (coldCount > 0) applyTemperatureBuilding(building, -(totalCold / coldCount));
    }



    /// TEMPERATURE APPLICATION

    public static void applyTemperatureUnit(Unit unit, float amount){
        int id = unit.id;

        // Store resistance values if not already stored
        loadUnitResistances(unit);

        float accumulationResist = amount > 0 ?
                unitAccumulationResistanceHeat.get(id, 1f) :
                unitAccumulationResistanceCold.get(id, 1f);
        float resistedAmount = amount * accumulationResist;

        float currentTemp = unitTemperature.get(id, 0f);
        float newTemp = Mathf.clamp(currentTemp + resistedAmount, -100f, 100f);

        if(debugLogging) {
            Log.info("[TempSystem] Unit @ (ID: @): Temp @ -> @ (applied: @, resist: @x)",
                    unit.type.name, id, currentTemp, newTemp, amount, accumulationResist);
        }

        unitTemperature.put(id, newTemp);
        lastHitTime.put(id, Time.time);
    }

    public static void applyTemperatureBuilding(Building building, float amount){
        int pos = building.pos();

        // Store resistance values if not already stored
        loadBuildingResistances(building);

        float accumulationResist = amount > 0 ?
                buildingAccumulationResistanceHeat.get(pos, 1f) :
                buildingAccumulationResistanceCold.get(pos, 1f);
        float resistedAmount = amount * accumulationResist;

        // Check if building needs both temps
        boolean needsBoth = false;
        try {
            var blockClass = building.block.getClass();
            var heatField = blockClass.getField("requiresHeat");
            var coldField = blockClass.getField("requiresCold");
            boolean reqHeat = heatField.getBoolean(building.block);
            boolean reqCold = coldField.getBoolean(building.block);
            needsBoth = reqHeat && reqCold;

            if(debugLogging) {
                Log.info("[GrabVariable] Requires for @ - Heat: @, Cold: @, NeedsBoth: @",
                        building.block.name, reqHeat, reqCold, needsBoth);
            }
        } catch(Exception e){
            // No requirements, use normal combined temp
        }

        // Check for custom max storage limits
        float maxTemp = 100f;
        try {
            var blockClass = building.block.getClass();
            var maxTempField = blockClass.getField("maxTempStorage");
            maxTemp = maxTempField.getFloat(building.block);

            if(debugLogging) {
                Log.info("[GrabVariable] Storage limits for @ - Max Temp: @",
                        building.block.name, maxTemp);
            }
        } catch(Exception e){
            // No custom storage, use default 100
        }

        if(needsBoth){
            // Separate heat/cold maps (no cancellation)
            if(amount > 0){ // HEAT
                float currentHeat = buildingHeat.get(pos, 0f);
                float newHeat = Mathf.clamp(currentHeat + resistedAmount, 0f, maxTemp);

                if(debugLogging) {
                    Log.info("[TempSystem] Building @ (pos: @): Heat @ -> @ (applied: @, resist: @x, max: @) [SEPARATE]",
                            building.block.name, pos, currentHeat, newHeat, amount, accumulationResist, maxTemp);
                }

                buildingHeat.put(pos, newHeat);
                lastHitTimeHeat.put(pos, Time.time);
            }else{ // COLD
                float currentCold = buildingCold.get(pos, 0f);
                float newCold = Mathf.clamp(currentCold - resistedAmount, 0f, maxTemp);

                if(debugLogging) {
                    Log.info("[TempSystem] Building @ (pos: @): Cold @ -> @ (applied: @, resist: @x, max: @) [SEPARATE]",
                            building.block.name, pos, currentCold, newCold, amount, accumulationResist, maxTemp);
                }

                buildingCold.put(pos, newCold);
                lastHitTimeCold.put(pos, Time.time);
            }
        }else{
            // Combined temperature (heat cancels cold)
            float currentTemp = buildingTemperature.get(pos, 0f);
            float newTemp = Mathf.clamp(currentTemp + resistedAmount, -maxTemp, maxTemp);

            if(debugLogging) {
                Log.info("[TempSystem] Building @ (pos: @): Temp @ -> @ (applied: @, resist: @x) [COMBINED]",
                        building.block.name, pos, currentTemp, newTemp, amount, accumulationResist);
            }

            buildingTemperature.put(pos, newTemp);
            lastHitTime.put(pos, Time.time);
        }
    }
    /// LOAD RESISTANCES
    public static void loadUnitResistances(Unit unit){
        int id = unit.id;

        if(!unitAccumulationResistanceHeat.containsKey(id)){
            if(unit.type instanceof AxUnitType axType){
                unitAccumulationResistanceHeat.put(id, axType.accumulationResistanceHeat);
                unitAccumulationResistanceCold.put(id, axType.accumulationResistanceCold);
                unitEffectResistanceHeat.put(id, axType.effectResistanceHeat);
                unitEffectResistanceCold.put(id, axType.effectResistanceCold);
            }else if(unit.type instanceof ErekirUnitType){
                unitAccumulationResistanceHeat.put(id, 0f);
                unitAccumulationResistanceCold.put(id, 1.2f);
                unitEffectResistanceHeat.put(id, 0f);
                unitEffectResistanceCold.put(id, 1.2f);
            }else{
                unitAccumulationResistanceHeat.put(id, 1f);
                unitAccumulationResistanceCold.put(id, 1f);
                unitEffectResistanceHeat.put(id, 1f);
                unitEffectResistanceCold.put(id, 1f);
            }
        }
    }

    public static void loadBuildingResistances(Building building){
        int pos = building.pos();
        if(!buildingAccumulationResistanceHeat.containsKey(pos)){
            if(building.block instanceof AxBlock axBlock){
                buildingAccumulationResistanceHeat.put(pos, axBlock.accumulationResistanceHeat);
                buildingAccumulationResistanceCold.put(pos, axBlock.accumulationResistanceCold);
                buildingEffectResistanceHeat.put(pos, axBlock.effectResistanceHeat);
                buildingEffectResistanceCold.put(pos, axBlock.effectResistanceCold);

                if(debugLogging) {
                    Log.info("[GrabVariable] AxBlock @ - acc heat: @, acc cold: @, eff heat: @, eff cold: @",
                            building.block.name, axBlock.accumulationResistanceHeat, axBlock.accumulationResistanceCold,
                            axBlock.effectResistanceHeat, axBlock.effectResistanceCold);
                }
            }else{
                try {
                    var blockClass = building.block.getClass();
                    var accHeatField = blockClass.getField("accumulationResistanceHeat");
                    var accColdField = blockClass.getField("accumulationResistanceCold");
                    var effHeatField = blockClass.getField("effectResistanceHeat");
                    var effColdField = blockClass.getField("effectResistanceCold");

                    buildingAccumulationResistanceHeat.put(pos, accHeatField.getFloat(building.block));
                    buildingAccumulationResistanceCold.put(pos, accColdField.getFloat(building.block));
                    buildingEffectResistanceHeat.put(pos, effHeatField.getFloat(building.block));
                    buildingEffectResistanceCold.put(pos, effColdField.getFloat(building.block));

                    if(debugLogging) {
                        Log.info("[GrabVariable] Success for @ (class: @) - acc heat: @, acc cold: @, eff heat: @, eff cold: @",
                                building.block.name, blockClass.getSimpleName(),
                                accHeatField.getFloat(building.block), accColdField.getFloat(building.block),
                                effHeatField.getFloat(building.block), effColdField.getFloat(building.block));
                    }
                } catch(Exception e){
                    if(debugLogging) {
                        Log.info("[GrabVariable] Failed for @ (class: @) - Error: @",
                                building.block.name, building.block.getClass().getSimpleName(), e.getMessage());
                    }
                    buildingAccumulationResistanceHeat.put(pos, 1f);
                    buildingAccumulationResistanceCold.put(pos, 1f);
                    buildingEffectResistanceHeat.put(pos, 1f);
                    buildingEffectResistanceCold.put(pos, 1f);
                }
            }
        }
    }


    /// TEMPERATURE GRABBERS

    public static float getTemperatureUnit(Unit unit){
        return unitTemperature.get(unit.id, 0f);
    }

    public static float getTemperatureBuilding(Building building){
        return buildingTemperature.get(building.pos(), 0f);
    }

    public static float getEffectResistanceHeatUnit(Unit unit){
        if (unitEffectResistanceHeat.get(unit.id) == null){
            loadUnitResistances(unit);
        }
        return unitEffectResistanceHeat.get(unit.id);
    }

    public static float getEffectResistanceHeatBuilding(Building build){
        if (buildingEffectResistanceHeat.get(build.pos()) == null){
            loadBuildingResistances(build);
        }
        return buildingEffectResistanceHeat.get(build.pos());
    }

    public static float getHeatUnit(Unit unit){
        float temp = getTemperatureUnit(unit);
        return temp > 0 ? temp : 0f;
    }

    public static float getColdUnit(Unit unit){
        float temp = getTemperatureUnit(unit);
        return temp < 0 ? -temp : 0f;
    }

    public static float getHeatBuilding(Building building){
        float separateHeat = buildingHeat.get(building.pos(), -1f);
        if(separateHeat >= 0){
            return separateHeat;
        }

        float temp = getTemperatureBuilding(building);
        return temp > 0 ? temp : 0f;
    }

    public static float getColdBuilding(Building building){
        float separateCold = buildingCold.get(building.pos(), -1f);
        if(separateCold >= 0){
            return separateCold;
        }

        float temp = getTemperatureBuilding(building);
        return temp < 0 ? -temp : 0f;
    }

    /// UTILITY METHODS

    public static void refreshLastHitTime(int pos){
        // Refresh both separate timers if they exist
        if(lastHitTimeHeat.containsKey(pos)){
            lastHitTimeHeat.put(pos, Time.time);
        }
        if(lastHitTimeCold.containsKey(pos)){
            lastHitTimeCold.put(pos, Time.time);
        }
        // Refresh combined timer
        if(lastHitTime.containsKey(pos)){
            lastHitTime.put(pos, Time.time);
        }
    }
    public static class TileCont {
        public boolean isEven; // Move your "evenness" check result here
        protected ObjectMap<Integer, Tile> tiles = new ObjectMap<>();
    }



    /// DAMAGE CALCULATION

    public static float getTemperatureDamageMultiplier(Entityc entity){
        float temp = 0f;

        if(entity instanceof Unit u){
            temp = getTemperatureUnit(u);
        }else if(entity instanceof Building b){
            temp = getTemperatureBuilding(b);
            if(buildingHeat.containsKey(b.pos())){
                temp = buildingHeat.get(b.pos(), 0f);
            }
        }

        if(temp > 0){
            return 1f + (temp * 0.01f);
        }

        return 1f;
    }

    public static float getEffectiveHealthMultiplier(Unit unit){
        float baseMult = 1f;

        if(unit.statusBits() != null){
            for(StatusEffect status : Vars.content.statusEffects()){
                if(unit.hasEffect(status)){
                    baseMult *= status.healthMultiplier;
                }
            }
        }

        float reduction = coldResistanceReduction.get(unit.id, 0f);
        return Math.max(1f, baseMult - reduction);
    }


    /// MAIN UPDATE SYSTEM

    public static void updateTemperatureSystem(){
        if(Vars.state.isPaused()) return;

        // Reset particle counters
        heatParticleCounter = 0;
        coldParticleCounter = 0;
        dualTempParticleCounter = 0;

        // Update all units
        Seq<Integer> deadUnits = new Seq<>();
        unitTemperature.each((id, temp) -> {
            Unit u = Groups.unit.getByID(id);
            if(u == null || u.dead){
                deadUnits.add(id);
            }else{
                updateUnitTemperature(u, id, temp);
                applyUnitTemperatureEffects(u, temp);
                drawTemperatureEffects(u.x, u.y, u.hitSize, temp, false);
            }
        });

        for(int id : deadUnits){
            cleanupUnit(id);
        }

        // Update buildings (combined temp)
        Seq<Integer> deadBuildings = new Seq<>();
        buildingTemperature.each((pos, temp) -> {
            if(pos == null){
                deadBuildings.add(pos);
                return;
            }

            Building b = Vars.world.build(pos);
            if(b == null || b.dead){
                deadBuildings.add(pos);
            }else{
                updateBuildingTemperature(b, pos, temp);
                applyBuildingTemperatureEffects(b, temp);
                drawTemperatureEffects(b.x, b.y, b.block.size * 8f, temp, false);
            }
        });

        for(Integer pos : deadBuildings){
            if(pos != null){
                cleanupBuilding(pos);
            }
        }

        // Update buildings with separate heat/cold
        Seq<Integer> deadBuildingsSeparate = new Seq<>();
        buildingHeat.each((pos, heat) -> {
            if(pos == null){
                deadBuildingsSeparate.add(pos);
                return;
            }

            Building b = Vars.world.build(pos);
            if(b == null || b.dead){
                deadBuildingsSeparate.add(pos);
            }else{
                updateBuildingHeatCold(b, pos, heat, buildingCold.get(pos, 0f));
                // Draw dual-temp effects
                float combinedTemp = heat + buildingCold.get(pos, 0f); // Both positive for intensity
                if(combinedTemp > 0){
                    drawTemperatureEffects(b.x, b.y, b.block.size * 8f, combinedTemp, true);
                }
            }
        });

        for(Integer pos : deadBuildingsSeparate){
            if(pos != null){
                buildingHeat.remove(pos);
                buildingCold.remove(pos);
                lastHitTimeHeat.remove(pos);
                lastHitTimeCold.remove(pos);
            }
        }
    }


    /// TEMPERATURE COOLDOWN

    protected static void updateUnitTemperature(Unit unit, int id, float temp){
        float lastHit = lastHitTime.get(id, 0f);
        float timeSinceHit = Time.time - lastHit;

        if(timeSinceHit > 300f && temp != 0){
            float oldTemp = temp;
            temp = temp > 0 ? Math.max(0f, temp - 0.1f) : Math.min(0f, temp + 0.1f);

            if(debugLogging && oldTemp != temp){
                Log.info("[TempSystem] Unit @ (ID: @): Cooldown @ -> @",
                        unit.type.name, id, oldTemp, temp);
            }

            unitTemperature.put(id, temp);
        }
    }

    protected static void updateBuildingTemperature(Building building, int pos, float temp){
        float lastHit = lastHitTime.get(pos, 0f);
        float timeSinceHit = Time.time - lastHit;

        if(timeSinceHit > 300f && temp != 0){
            float oldTemp = temp;
            temp = temp > 0 ? Math.max(0f, temp - 0.1f) : Math.min(0f, temp + 0.1f);

            if(debugLogging && oldTemp != temp){
                Log.info("[TempSystem] Building @ (pos: @): Cooldown @ -> @",
                        building.block.name, pos, oldTemp, temp);
            }

            buildingTemperature.put(pos, temp);
        }
    }

    protected static void updateBuildingHeatCold(Building building, int pos, float heat, float cold){
        float lastHitHeat = lastHitTimeHeat.get(pos, 0f);
        float lastHitCold = lastHitTimeCold.get(pos, 0f);
        float timeSinceHitHeat = Time.time - lastHitHeat;
        float timeSinceHitCold = Time.time - lastHitCold;

        // Independent cooldowns
        if(timeSinceHitHeat > 300f && heat > 0){
            float newHeat = Math.max(0f, heat - 0.1f);
            if(debugLogging && heat != newHeat){
                Log.info("[TempSystem] Building @ (pos: @): Heat cooldown @ -> @",
                        building.block.name, pos, heat, newHeat);
            }
            buildingHeat.put(pos, newHeat);
        }

        if(timeSinceHitCold > 300f && cold > 0){
            float newCold = Math.max(0f, cold - 0.1f);
            if(debugLogging && cold != newCold){
                Log.info("[TempSystem] Building @ (pos: @): Cold cooldown @ -> @",
                        building.block.name, pos, cold, newCold);
            }
            buildingCold.put(pos, newCold);
        }
    }


    /// TEMPERATURE EFFECTS APPLICATION

    protected static void applyUnitTemperatureEffects(Unit unit, float temperature){
        int id = unit.id;

        if(temperature < 0){ // COLD
            float coldPercent = -temperature;
            float effectResist = unitEffectResistanceCold.get(id, 1f);
            float effectiveColdPercent = coldPercent * effectResist;

            if(!originalArmor.containsKey(id)){
                originalArmor.put(id, unit.armor);
            }

            float armorMult = Math.max(0f, 1f - (effectiveColdPercent * 0.01f));
            unit.armor = originalArmor.get(id) * armorMult;

            float totalHealthMult = 1f;
            if(unit.statusBits() != null){
                for(StatusEffect status : Vars.content.statusEffects()){
                    if(unit.hasEffect(status) && status.healthMultiplier > 1f){
                        totalHealthMult *= status.healthMultiplier;
                    }
                }
            }

            if(totalHealthMult > 1f){
                float resistanceReduction = effectiveColdPercent / 100f;
                float reducedMult = Mathf.lerp(totalHealthMult, 1f, resistanceReduction);
                coldResistanceReduction.put(id, totalHealthMult - reducedMult);
            }else{
                coldResistanceReduction.remove(id);
            }

        }else if(temperature == 0){
            if(originalArmor.containsKey(id)){
                unit.armor = originalArmor.get(id);
                originalArmor.remove(id);
            }
            coldResistanceReduction.remove(id);
        }
    }

    protected static void applyBuildingTemperatureEffects(Building building, float temperature){
        if(temperature >= 0) return;

        float coldPercent = -temperature;
        int pos = building.pos();

        float effectResist = buildingEffectResistanceCold.get(pos, 1f);
        float effectiveColdPercent = coldPercent * effectResist;

        float shatterThreshold = 0.25f * (effectiveColdPercent / 100f);

        if(building.healthf() < shatterThreshold){
            if(debugLogging) {
                Log.info("[TempSystem] Building @ SHATTERED! HP: @%, Threshold: @%",
                        building.block.name, building.healthf() * 100f, shatterThreshold * 100f);
            }

            AxthrixFfx.crystalShatter(building.block, Color.cyan).at(building.x, building.y);
            Fx.freezing.at(building.x, building.y);
            building.kill();
        }
    }


    /// VISUAL EFFECTS

    protected static void drawTemperatureEffects(float x, float y, float size, float temperature, boolean isDualTemp){
        if(temperature == 0) return;

        Draw.z(Layer.flyingUnit + 1);

        float intensity = Math.abs(temperature) / 100f;
        float alpha = intensity * 0.6f;

        if(isDualTemp){
            // Dual-temp building - show smoke effect
            Draw.color(Color.gray, Color.white, Mathf.absin(Time.time, 3f, 0.5f));
            Draw.alpha(alpha);
            Fill.circle(x, y, size * 0.8f);

            // Reduced particle spawning based on counter
            if(!Vars.state.isPaused() && Mathf.chance(intensity * 0.1f) && dualTempParticleCounter % 5 == 0){
                float angle = Mathf.random(360f);
                float dist = Mathf.random(size);
                Fx.fireSmoke.at(
                        x + Mathf.cosDeg(angle) * dist,
                        y + Mathf.sinDeg(angle) * dist,
                        angle
                );
            }
            dualTempParticleCounter++;

        }else if(temperature > 0){ // HEAT
            Draw.color(Color.red);
            Draw.alpha(alpha);
            Fill.circle(x, y, size * 0.8f);

            Draw.color(Color.red, Color.orange, Mathf.absin(Time.time, 3f, 1f));
            Draw.alpha(alpha * 0.4f);
            Fill.circle(x, y, size * 1.2f * (1f + Mathf.absin(Time.time, 2f, 0.1f)));

            // Reduced particle spawning based on counter
            if(!Vars.state.isPaused() && Mathf.chance(intensity * 0.3f) && heatParticleCounter % 3 == 0){
                float angle = Mathf.random(360f);
                float dist = Mathf.random(size);
                Fx.fire.at(
                        x + Mathf.cosDeg(angle) * dist,
                        y + Mathf.sinDeg(angle) * dist,
                        angle, Color.orange
                );
            }
            heatParticleCounter++;

        }else{ // COLD
            Draw.color(Color.cyan);
            Draw.alpha(alpha);
            Fill.circle(x, y, size * 0.8f);

            Draw.color(Color.cyan, Color.white, Mathf.absin(Time.time, 3f, 1f));
            Draw.alpha(alpha * 0.4f);
            Fill.circle(x, y, size * 1.15f);

            // Reduced particle spawning based on counter
            if(!Vars.state.isPaused() && Mathf.chance(intensity * 0.2f) && coldParticleCounter % 4 == 0){
                float angle = Mathf.random(360f);
                float dist = Mathf.random(size);
                Fx.freezing.at(
                        x + Mathf.cosDeg(angle) * dist,
                        y + Mathf.sinDeg(angle) * dist
                );
            }
            coldParticleCounter++;
        }

        Draw.reset();
    }

    /// SAVE/LOAD SYSTEM

    public static void writeTemperatureData(DataOutput stream) throws IOException {
        // Write unit temperature data
        stream.writeInt(unitTemperature.size);
        unitTemperature.each((id, temp) -> {
            try {
                stream.writeInt(id);
                stream.writeFloat(temp);

                // Write resistances
                stream.writeFloat(unitAccumulationResistanceHeat.get(id, 1f));
                stream.writeFloat(unitAccumulationResistanceCold.get(id, 1f));
                stream.writeFloat(unitEffectResistanceHeat.get(id, 1f));
                stream.writeFloat(unitEffectResistanceCold.get(id, 1f));

                // Write stat modifications
                stream.writeFloat(originalArmor.get(id, 0f));
                stream.writeFloat(coldResistanceReduction.get(id, 0f));

                // Write last hit time
                stream.writeFloat(lastHitTime.get(id, 0f));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Write building combined temperature data
        stream.writeInt(buildingTemperature.size);
        buildingTemperature.each((pos, temp) -> {
            try {
                stream.writeInt(pos);
                stream.writeFloat(temp);

                // Write resistances
                stream.writeFloat(buildingAccumulationResistanceHeat.get(pos, 1f));
                stream.writeFloat(buildingAccumulationResistanceCold.get(pos, 1f));
                stream.writeFloat(buildingEffectResistanceHeat.get(pos, 1f));
                stream.writeFloat(buildingEffectResistanceCold.get(pos, 1f));

                // Write last hit time
                stream.writeFloat(lastHitTime.get(pos, 0f));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Write building separate heat/cold data
        stream.writeInt(buildingHeat.size);
        buildingHeat.each((pos, heat) -> {
            try {
                stream.writeInt(pos);
                stream.writeFloat(heat);
                stream.writeFloat(buildingCold.get(pos, 0f));

                // Write resistances (if not already written in combined)
                if(!buildingTemperature.containsKey(pos)){
                    stream.writeFloat(buildingAccumulationResistanceHeat.get(pos, 1f));
                    stream.writeFloat(buildingAccumulationResistanceCold.get(pos, 1f));
                    stream.writeFloat(buildingEffectResistanceHeat.get(pos, 1f));
                    stream.writeFloat(buildingEffectResistanceCold.get(pos, 1f));
                }

                // Write separate last hit times
                stream.writeFloat(lastHitTimeHeat.get(pos, 0f));
                stream.writeFloat(lastHitTimeCold.get(pos, 0f));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        if(debugLogging) {
            Log.info("[TempSystem] Saved @ units, @ combined buildings, @ dual-temp buildings",
                    unitTemperature.size, buildingTemperature.size, buildingHeat.size);
        }
    }

    public static void readTemperatureData(DataInput stream) throws IOException {
        // Clear all existing data
        clearAllTemperatureData();

        // Read unit temperature data
        int unitCount = stream.readInt();
        for(int i = 0; i < unitCount; i++){
            int id = stream.readInt();
            float temp = stream.readFloat();

            // Read resistances
            float accHeat = stream.readFloat();
            float accCold = stream.readFloat();
            float effHeat = stream.readFloat();
            float effCold = stream.readFloat();

            // Read stat modifications
            float origArmor = stream.readFloat();
            float coldResReduce = stream.readFloat();

            // Read last hit time
            float lastHit = stream.readFloat();

            // Verify unit still exists
            Unit unit = Groups.unit.getByID(id);
            if(unit != null){
                unitTemperature.put(id, temp);
                unitAccumulationResistanceHeat.put(id, accHeat);
                unitAccumulationResistanceCold.put(id, accCold);
                unitEffectResistanceHeat.put(id, effHeat);
                unitEffectResistanceCold.put(id, effCold);

                if(origArmor > 0) originalArmor.put(id, origArmor);
                if(coldResReduce > 0) coldResistanceReduction.put(id, coldResReduce);

                lastHitTime.put(id, lastHit);
            }
        }

        // Read building combined temperature data
        int buildingCount = stream.readInt();
        for(int i = 0; i < buildingCount; i++){
            int pos = stream.readInt();
            float temp = stream.readFloat();

            // Read resistances
            float accHeat = stream.readFloat();
            float accCold = stream.readFloat();
            float effHeat = stream.readFloat();
            float effCold = stream.readFloat();

            // Read last hit time
            float lastHit = stream.readFloat();

            // Verify building still exists
            Building building = mindustry.Vars.world.build(pos);
            if(building != null){
                buildingTemperature.put(pos, temp);
                buildingAccumulationResistanceHeat.put(pos, accHeat);
                buildingAccumulationResistanceCold.put(pos, accCold);
                buildingEffectResistanceHeat.put(pos, effHeat);
                buildingEffectResistanceCold.put(pos, effCold);

                lastHitTime.put(pos, lastHit);
            }
        }

        // Read building separate heat/cold data
        int dualTempCount = stream.readInt();
        for(int i = 0; i < dualTempCount; i++){
            int pos = stream.readInt();
            float heat = stream.readFloat();
            float cold = stream.readFloat();

            // Read resistances if not in combined
            boolean hasCombined = buildingTemperature.containsKey(pos);
            float accHeat = hasCombined ? buildingAccumulationResistanceHeat.get(pos, 1f) : stream.readFloat();
            float accCold = hasCombined ? buildingAccumulationResistanceCold.get(pos, 1f) : stream.readFloat();
            float effHeat = hasCombined ? buildingEffectResistanceHeat.get(pos, 1f) : stream.readFloat();
            float effCold = hasCombined ? buildingEffectResistanceCold.get(pos, 1f) : stream.readFloat();

            // Read separate last hit times
            float lastHitH = stream.readFloat();
            float lastHitC = stream.readFloat();

            // Verify building still exists
            Building building = mindustry.Vars.world.build(pos);
            if(building != null){
                buildingHeat.put(pos, heat);
                buildingCold.put(pos, cold);

                if(!hasCombined){
                    buildingAccumulationResistanceHeat.put(pos, accHeat);
                    buildingAccumulationResistanceCold.put(pos, accCold);
                    buildingEffectResistanceHeat.put(pos, effHeat);
                    buildingEffectResistanceCold.put(pos, effCold);
                }

                lastHitTimeHeat.put(pos, lastHitH);
                lastHitTimeCold.put(pos, lastHitC);
            }
        }

        if(debugLogging) {
            Log.info("[TempSystem] Loaded @ units, @ combined buildings, @ dual-temp buildings",
                    unitCount, buildingCount, dualTempCount);
        }
    }

    protected static void clearAllTemperatureData(){
        unitTemperature.clear();
        buildingTemperature.clear();
        buildingHeat.clear();
        buildingCold.clear();
        lastHitTime.clear();
        lastHitTimeHeat.clear();
        lastHitTimeCold.clear();
        originalArmor.clear();
        coldResistanceReduction.clear();
        unitAccumulationResistanceHeat.clear();
        unitAccumulationResistanceCold.clear();
        unitEffectResistanceHeat.clear();
        unitEffectResistanceCold.clear();
        buildingAccumulationResistanceHeat.clear();
        buildingAccumulationResistanceCold.clear();
        buildingEffectResistanceHeat.clear();
        buildingEffectResistanceCold.clear();
    }

    /// CLEANUP

    protected static void cleanupUnit(int id){
        if(debugLogging) Log.info("[TempSystem] Cleaning up dead unit ID: @", id);
        unitTemperature.remove(id);
        lastHitTime.remove(id);
        originalArmor.remove(id);
        coldResistanceReduction.remove(id);
        unitAccumulationResistanceHeat.remove(id);
        unitAccumulationResistanceCold.remove(id);
        unitEffectResistanceHeat.remove(id);
        unitEffectResistanceCold.remove(id);
    }

    protected static void cleanupBuilding(int pos){
        if(debugLogging) Log.info("[TempSystem] Cleaning up dead building pos: @", pos);
        buildingTemperature.remove(pos);
        buildingHeat.remove(pos);
        buildingCold.remove(pos);
        lastHitTime.remove(pos);
        lastHitTimeHeat.remove(pos);
        lastHitTimeCold.remove(pos);
        buildingAccumulationResistanceHeat.remove(pos);
        buildingAccumulationResistanceCold.remove(pos);
        buildingEffectResistanceHeat.remove(pos);
        buildingEffectResistanceCold.remove(pos);
        blockTilesList.remove(pos);
    }
}
