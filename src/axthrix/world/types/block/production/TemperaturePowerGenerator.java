package axthrix.world.types.block.production;

import axthrix.world.types.bulletypes.TemperatureBulletType;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperaturePowerGenerator extends PowerGenerator {
    public float minTemperature = 50f; // Minimum temp to operate
    public float maxTemperature = 100f; // Maximum temp for max power
    public boolean requiresHeat = true; // true = needs heat, false = needs cold

    // Temperature resistances
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public TemperaturePowerGenerator(String name){
        super(name);
        // Don't consume power, we generate it
        consumesPower = false;
        outputsPower = true;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.basePowerGeneration); // Remove default stat
        stats.add(Stat.basePowerGeneration, powerProduction * 60f, StatUnit.powerSecond);
        stats.add(Stat.input, (requiresHeat ? "[red]Heat: " : "[cyan]Cold: ") + minTemperature + "° - " + maxTemperature + "°");
    }

    public class TemperaturePowerGeneratorBuild extends GeneratorBuild {
        public float currentPower = 0f;

        @Override
        public void updateTile(){
            float temp = TemperatureBulletType.getTemperatureBuilding(this);

            // Check if temperature meets requirements
            float effectiveTemp = requiresHeat ? temp : -temp;

            if(effectiveTemp >= minTemperature){
                // Calculate power based on temperature
                float tempPercent = Math.min(effectiveTemp, maxTemperature) / maxTemperature;
                currentPower = powerProduction * tempPercent;
                productionEfficiency = tempPercent;
            }else{
                currentPower = 0f;
                productionEfficiency = 0f;
            }
        }

        @Override
        public float getPowerProduction(){
            return currentPower;
        }
    }
}