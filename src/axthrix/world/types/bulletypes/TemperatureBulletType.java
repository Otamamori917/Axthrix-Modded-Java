package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.content.FX.AxthrixFfx;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;
import mindustry.type.unit.ErekirUnitType;
import mindustry.world.Block;

public class TemperatureBulletType extends BulletType {

    // ========================================
    // BULLET PROPERTIES
    // ========================================
    public float temperaturePerHit = 10f;

    // ========================================
    // DEBUG
    // ========================================
    public static boolean debugLogging = false;

    // ========================================
    // STATIC TRACKING MAPS
    // ========================================
    // Temperature tracking
    protected static ObjectMap<Integer, Float> unitTemperature = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingTemperature = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> lastHitTime = new ObjectMap<>();

    // Stat modifications
    protected static ObjectMap<Integer, Float> originalArmor = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> coldResistanceReduction = new ObjectMap<>();

    // Unit accumulation resistance (reduces incoming temperature)
    protected static ObjectMap<Integer, Float> unitAccumulationResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> unitAccumulationResistanceCold = new ObjectMap<>();

    // Unit effect resistance (reduces stat penalties)
    protected static ObjectMap<Integer, Float> unitEffectResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> unitEffectResistanceCold = new ObjectMap<>();

    // Building accumulation resistance (reduces incoming temperature)
    protected static ObjectMap<Integer, Float> buildingAccumulationResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingAccumulationResistanceCold = new ObjectMap<>();

    // Building effect resistance (reduces stat penalties)
    protected static ObjectMap<Integer, Float> buildingEffectResistanceHeat = new ObjectMap<>();
    protected static ObjectMap<Integer, Float> buildingEffectResistanceCold = new ObjectMap<>();

    // ========================================
    // BLOCK CLASS DEFAULT RESISTANCES
    // ========================================
    public static Seq<Block> Compatable = new Seq<>();

    // ========================================
    // CONSTRUCTORS
    // ========================================
    public TemperatureBulletType(){
        super();
    }

    public TemperatureBulletType(float speed, float damage){
        super(speed, damage);
    }

    // ========================================
    // BULLET HIT EVENTS
    // ========================================
    @Override
    public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float health){
        if(entity instanceof Unit target){
            if(debugLogging) Log.info("[TemperatureBullet] Hit unit: @ (ID: @), applying @ temp",
                    target.type.name, target.id, temperaturePerHit);
            applyTemperatureUnit(target, temperaturePerHit);
        }else if(entity instanceof Building target){
            if(debugLogging) Log.info("[TemperatureBullet] Hit building: @ (pos: @), applying @ temp",
                    target.block.name, target.pos(), temperaturePerHit);
            applyTemperatureBuilding(target, temperaturePerHit);
        }

        super.hitEntity(b, entity, health);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct){
        super.hitTile(b, build, x, y, initialHealth, direct);

        if(build != null){
            if(debugLogging) Log.info("[TemperatureBullet] Hit tile: @ (pos: @), applying @ temp",
                    build.block.name, build.pos(), temperaturePerHit);
            applyTemperatureBuilding(build, temperaturePerHit);
        }
    }

    // ========================================
    // TEMPERATURE APPLICATION
    // ========================================
    public static void applyTemperatureUnit(Unit unit, float amount){
        int id = unit.id;

        // Store resistance values if not already stored
        if(!unitAccumulationResistanceHeat.containsKey(id)){
            if(unit.type instanceof AxUnitType axType){
                unitAccumulationResistanceHeat.put(id, axType.accumulationResistanceHeat);
                unitAccumulationResistanceCold.put(id, axType.accumulationResistanceCold);
                unitEffectResistanceHeat.put(id, axType.effectResistanceHeat);
                unitEffectResistanceCold.put(id, axType.effectResistanceCold);
            }else if(unit.type instanceof ErekirUnitType){ // Erekir immunity to heat, slightly weak to cold
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

        // Apply accumulation resistance
        float accumulationResist = amount > 0 ?
                unitAccumulationResistanceHeat.get(id, 1f) :
                unitAccumulationResistanceCold.get(id, 1f);
        float resistedAmount = amount * accumulationResist;

        // Apply temperature
        float currentTemp = unitTemperature.get(id, 0f);
        float newTemp = Mathf.clamp(currentTemp + resistedAmount, -100f, 100f);

        if(debugLogging) {
            Log.info("[TempSystem] Unit @ (ID: @): Temp @ -> @ (applied: @, accumulation resist: @x)",
                    unit.type.name, id, currentTemp, newTemp, amount, accumulationResist);
        }

        unitTemperature.put(id, newTemp);
        lastHitTime.put(id, Time.time);
    }

    public static void applyTemperatureBuilding(Building building, float amount){
        int pos = building.pos();

        // Store resistance values if not already stored
        if(!buildingAccumulationResistanceHeat.containsKey(pos)){
            if(building.block instanceof AxBlock axBlock){
                // Use AxBlock resistance values
                buildingAccumulationResistanceHeat.put(pos, axBlock.accumulationResistanceHeat);
                buildingAccumulationResistanceCold.put(pos, axBlock.accumulationResistanceCold);
                buildingEffectResistanceHeat.put(pos, axBlock.effectResistanceHeat);
                buildingEffectResistanceCold.put(pos, axBlock.effectResistanceCold);
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
                } catch(Exception e){
                    if(debugLogging) {
                        Log.info("[Error] Building @ (pos: @), error: @) all defaulted to 1",
                                building.block.name, pos, e);
                    }
                    buildingAccumulationResistanceHeat.put(pos, 1f);
                    buildingAccumulationResistanceCold.put(pos, 1f);
                    buildingEffectResistanceHeat.put(pos, 1f);
                    buildingEffectResistanceCold.put(pos, 1f);
                }

            }
        }

        // Apply accumulation resistance
        float accumulationResist = amount > 0 ?
                buildingAccumulationResistanceHeat.get(pos, 1f) :
                buildingAccumulationResistanceCold.get(pos, 1f);
        float resistedAmount = amount * accumulationResist;

        float currentTemp = buildingTemperature.get(pos, 0f);
        float newTemp = Mathf.clamp(currentTemp + resistedAmount, -100f, 100f);

        if(debugLogging) {
            Log.info("[TempSystem] Building @ (pos: @): Temp @ -> @ (applied: @, accumulation resist: @x)",
                    building.block.name, pos, currentTemp, newTemp, amount, accumulationResist);
        }

        buildingTemperature.put(pos, newTemp);
        lastHitTime.put(pos, Time.time);
    }

    // ========================================
    // TEMPERATURE GETTERS
    // ========================================
    public static float getTemperatureUnit(Unit unit){
        return unitTemperature.get(unit.id, 0f);
    }

    public static float getTemperatureBuilding(Building building){
        return buildingTemperature.get(building.pos(), 0f);
    }

    public static float getHeatUnit(Unit unit){
        float temp = getTemperatureUnit(unit);
        return temp > 0 ? temp : 0f;
    }

    public static float getColdUnit(Unit unit){
        float temp = getTemperatureUnit(unit);
        return temp < 0 ? -temp : 0f;
    }

    // ========================================
    // DAMAGE CALCULATION
    // ========================================
    public static float getTemperatureDamageMultiplier(mindustry.gen.Entityc entity){
        float temp = 0f;

        if(entity instanceof Unit u){
            temp = getTemperatureUnit(u);
        }else if(entity instanceof Building b){
            temp = getTemperatureBuilding(b);
        }

        if(temp > 0){ // Heat increases damage
            float mult = 1f + (temp * 0.01f);
            if(debugLogging) {
                Log.info("[TempSystem] Damage multiplier: @x (temp: @)", mult, temp);
            }
            return mult;
        }

        return 1f;
    }

    public static float getEffectiveHealthMultiplier(Unit unit){
        float baseMult = 1f;

        // Get base health multiplier from status effects
        if(unit.statusBits() != null){
            for(StatusEffect status : Vars.content.statusEffects()){
                if(unit.hasEffect(status)){
                    baseMult *= status.healthMultiplier;
                }
            }
        }

        // Apply cold reduction if exists
        float reduction = coldResistanceReduction.get(unit.id, 0f);
        float effectiveMult = Math.max(1f, baseMult - reduction);

        if(debugLogging && reduction > 0){
            Log.info("[TempSystem] Unit @ (ID: @): Effective health mult: @ (base: @, reduction: @)",
                    unit.type.name, unit.id, effectiveMult, baseMult, reduction);
        }

        return effectiveMult;
    }

    // ========================================
    // MAIN UPDATE SYSTEM
    // ========================================
    public static void updateTemperatureSystem(){
        if(mindustry.Vars.state.isPaused()) return;

        // Update all units
        unitTemperature.each((id, temp) -> {
            Unit u = Groups.unit.getByID(id);
            if(u == null || u.dead){
                cleanupUnit(id, temp);
            }else{
                updateUnitTemperature(u, id, temp);
                applyUnitTemperatureEffects(u, temp);
                drawTemperatureEffects(u.x, u.y, u.hitSize, temp);
            }
        });

        // Update all buildings
        buildingTemperature.each((pos, temp) -> {
            Building b = mindustry.Vars.world.build(pos);
            if(b == null || b.dead){
                cleanupBuilding(pos, temp);
            }else{
                updateBuildingTemperature(b, pos, temp);
                applyBuildingTemperatureEffects(b, temp);
                drawTemperatureEffects(b.x, b.y, b.block.size * 8f, temp);
            }
        });
    }

    // ========================================
    // TEMPERATURE COOLDOWN
    // ========================================
    protected static void updateUnitTemperature(Unit unit, int id, float temp){
        float lastHit = lastHitTime.get(id, 0f);
        float timeSinceHit = Time.time - lastHit;

        if(timeSinceHit > 180f && temp != 0){ // Cooldown delay
            float oldTemp = temp;
            temp = temp > 0 ? Math.max(0f, temp - 0.25f) : Math.min(0f, temp + 0.25f);

            if(debugLogging && oldTemp != temp){
                Log.info("[TempSystem] Unit @ (ID: @): Cooldown @ -> @ (time: @)",
                        unit.type.name, id, oldTemp, temp, timeSinceHit);
            }

            unitTemperature.put(id, temp);
        }
    }

    protected static void updateBuildingTemperature(Building building, int pos, float temp){
        float lastHit = lastHitTime.get(pos, 0f);
        float timeSinceHit = Time.time - lastHit;

        if(timeSinceHit > 180f && temp != 0){
            float oldTemp = temp;
            temp = temp > 0 ? Math.max(0f, temp - 0.25f) : Math.min(0f, temp + 0.25f);

            if(debugLogging && oldTemp != temp){
                Log.info("[TempSystem] Building @ (pos: @): Cooldown @ -> @ (time: @)",
                        building.block.name, pos, oldTemp, temp, timeSinceHit);
            }

            buildingTemperature.put(pos, temp);
        }
    }

    // ========================================
    // TEMPERATURE EFFECTS APPLICATION
    // ========================================
    protected static void applyUnitTemperatureEffects(Unit unit, float temperature){
        int id = unit.id;

        if(temperature < 0){ // COLD - reduce armor and resistance
            float coldPercent = -temperature;

            // Apply effect resistance (reduces penalty)
            float effectResist = unitEffectResistanceCold.get(id, 1f);
            float effectiveColdPercent = coldPercent * effectResist;

            // Armor reduction
            if(!originalArmor.containsKey(id)){
                originalArmor.put(id, unit.armor);
                if(debugLogging) {
                    Log.info("[TempSystem] Unit @ (ID: @): Stored original armor: @",
                            unit.type.name, id, unit.armor);
                }
            }

            float armorMult = Math.max(0f, 1f - (effectiveColdPercent * 0.01f));
            float oldArmor = unit.armor;
            unit.armor = originalArmor.get(id) * armorMult;

            if(debugLogging && Math.abs(oldArmor - unit.armor) > 0.01f){
                Log.info("[TempSystem] Unit @ (ID: @): Armor @ -> @ (cold: @%, effective: @%, effect resist: @x)",
                        unit.type.name, id, oldArmor, unit.armor, coldPercent, effectiveColdPercent, effectResist);
            }

            // Resistance reduction
            float totalHealthMult = 1f;
            if(unit.statusBits() != null){
                for(StatusEffect status : Vars.content.statusEffects()){
                    if(unit.hasEffect(status) && status.healthMultiplier > 1f){
                        totalHealthMult *= status.healthMultiplier;
                        if(debugLogging){
                            Log.info("[TempSystem] Unit @ (ID: @): Has status @ (healthMult: @x)",
                                    unit.type.name, id, status.name, status.healthMultiplier);
                        }
                    }
                }
            }

            if(totalHealthMult > 1f){
                float resistanceReduction = effectiveColdPercent / 100f;
                float reducedMult = Mathf.lerp(totalHealthMult, 1f, resistanceReduction);
                float multDifference = totalHealthMult - reducedMult;

                if(debugLogging){
                    Log.info("[TempSystem] Unit @ (ID: @): Resistance - Base: @x, Reduced: @x, Diff: @ (cold: @%, effective: @%)",
                            unit.type.name, id, totalHealthMult, reducedMult, multDifference, coldPercent, effectiveColdPercent);
                }

                coldResistanceReduction.put(id, multDifference);
            }else{
                coldResistanceReduction.remove(id);
            }

        }else if(temperature == 0){ // NORMAL - restore all stats
            // Restore armor
            if(originalArmor.containsKey(id)){
                float restoredArmor = originalArmor.get(id);
                if(debugLogging) {
                    Log.info("[TempSystem] Unit @ (ID: @): Restoring armor @ -> @",
                            unit.type.name, id, unit.armor, restoredArmor);
                }
                unit.armor = restoredArmor;
                originalArmor.remove(id);
            }

            // Remove resistance reduction
            if(coldResistanceReduction.containsKey(id)){
                if(debugLogging) {
                    Log.info("[TempSystem] Unit @ (ID: @): Removing resistance reduction", unit.type.name, id);
                }
                coldResistanceReduction.remove(id);
            }
        }
    }

    protected static void applyBuildingTemperatureEffects(Building building, float temperature){
        if(temperature >= 0) return; // Only cold affects buildings

        float coldPercent = -temperature;
        int pos = building.pos();

        // Apply effect resistance (reduces shatter threshold penalty)
        float effectResist = buildingEffectResistanceCold.get(pos, 1f);
        float effectiveColdPercent = coldPercent * effectResist;

        float shatterThreshold = 0.25f * (effectiveColdPercent / 100f);

        if(building.healthf() < shatterThreshold){
            if(debugLogging) {
                Log.info("[TempSystem] Building @ (pos: @): SHATTERED! HP: @%, Threshold: @% (cold: @%, effective: @%, effect resist: @x)",
                        building.block.name, pos, building.healthf() * 100f,
                        shatterThreshold * 100f, coldPercent, effectiveColdPercent, effectResist);
            }

            AxthrixFfx.crystalShatter(building.block, Color.cyan).at(building.x, building.y);
            Fx.freezing.at(building.x, building.y);
            building.kill();
        }
    }

    // ========================================
    // VISUAL EFFECTS
    // ========================================
    protected static void drawTemperatureEffects(float x, float y, float size, float temperature){
        if(temperature == 0) return;

        Draw.z(Layer.flyingUnit + 1);

        float intensity = Math.abs(temperature) / 100f;
        float alpha = intensity * 0.6f;

        if(temperature > 0){ // HEAT
            // Red overlay
            Draw.color(Color.red);
            Draw.alpha(alpha);
            Fill.circle(x, y, size * 0.8f);

            // Pulsing glow
            Draw.color(Color.red, Color.orange, Mathf.absin(Time.time, 3f, 1f));
            Draw.alpha(alpha * 0.4f);
            Fill.circle(x, y, size * 1.2f * (1f + Mathf.absin(Time.time, 2f, 0.1f)));

            // Heat particles
            if(!mindustry.Vars.state.isPaused() && Mathf.chance(intensity * 0.3f)){
                float angle = Mathf.random(360f);
                float dist = Mathf.random(size);
                Fx.fire.at(
                        x + Mathf.cosDeg(angle) * dist,
                        y + Mathf.sinDeg(angle) * dist,
                        angle, Color.orange
                );
            }

        }else{ // COLD
            // Cyan overlay
            Draw.color(Color.cyan);
            Draw.alpha(alpha);
            Fill.circle(x, y, size * 0.8f);

            // Frost glow
            Draw.color(Color.cyan, Color.white, Mathf.absin(Time.time, 3f, 1f));
            Draw.alpha(alpha * 0.4f);
            Fill.circle(x, y, size * 1.15f);

            // Frost particles
            if(!mindustry.Vars.state.isPaused() && Mathf.chance(intensity * 0.2f)){
                float angle = Mathf.random(360f);
                float dist = Mathf.random(size);
                Fx.freezing.at(
                        x + Mathf.cosDeg(angle) * dist,
                        y + Mathf.sinDeg(angle) * dist
                );
            }
        }

        Draw.reset();
    }

    // ========================================
    // CLEANUP
    // ========================================
    protected static void cleanupUnit(int id, float temp){
        if(debugLogging) Log.info("[TempSystem] Cleaning up dead unit ID: @ (temp was: @)", id, temp);
        unitTemperature.remove(id);
        lastHitTime.remove(id);
        originalArmor.remove(id);
        coldResistanceReduction.remove(id);
        unitAccumulationResistanceHeat.remove(id);
        unitAccumulationResistanceCold.remove(id);
        unitEffectResistanceHeat.remove(id);
        unitEffectResistanceCold.remove(id);
    }

    protected static void cleanupBuilding(int pos, float temp){
        if(debugLogging) Log.info("[TempSystem] Cleaning up dead building pos: @ (temp was: @)", pos, temp);
        buildingTemperature.remove(pos);
        lastHitTime.remove(pos);
        buildingAccumulationResistanceHeat.remove(pos);
        buildingAccumulationResistanceCold.remove(pos);
        buildingEffectResistanceHeat.remove(pos);
        buildingEffectResistanceCold.remove(pos);
    }
}