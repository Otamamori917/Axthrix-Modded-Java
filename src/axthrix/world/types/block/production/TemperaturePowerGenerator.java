package axthrix.world.types.block.production;

import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperaturePowerGenerator extends AxPowerGenerator {
    public float minTemperature = 50f; // Minimum temp to operate
    public float maxTemperature = 100f; // Maximum temp for max power

    public boolean requiresHeat = false; // Requires heat
    public boolean requiresCold = false; // Requires cold

    // Temperature resistances
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public TemperaturePowerGenerator(String name){
        super(name);
        consumesPower = false;
        outputsPower = true;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.basePowerGeneration);
        stats.add(Stat.basePowerGeneration, powerProduction * 60f, StatUnit.powerSecond);

        stats.add(Stat.input, table -> {
            // 1. Calculate Heat Strings (Positive)
            String heatMin = TempUnit.format(minTemperature);
            String heatMax = TempUnit.format(maxTemperature);
            String heatText = heatMin + " - " + heatMax;

            // 2. Calculate Cold Strings (Negative)
            // We invert min/max because "Max Efficiency" for cold means a LOWER temperature
            String coldMin = TempUnit.format(-minTemperature);
            String coldMax = TempUnit.format(-maxTemperature);
            String coldText = coldMin + " - " + coldMax;

            if(requiresHeat && requiresCold){
                // Split onto two lines or side-by-side for clarity
                // Example: "[red]Heat: 373K[] / [cyan]Cold: 173K[]"
                table.add("[red]Heat: " + heatText + "[]\n[cyan]Cold: " + coldText + "[]");
            }
            else if(requiresHeat){
                table.add("[red]Heat: " + heatText + "[]");
            }
            else if(requiresCold){
                table.add("[cyan]Cold: " + coldText + "[]");
            }
        });
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("temperature", (TemperaturePowerGeneratorBuild entity) -> new TemperatureBar("Temp", entity));

    }

    public class TemperaturePowerGeneratorBuild extends GeneratorBuild {
        public float currentPower = 0f;

        @Override
        public void updateTile(){
            float temp = TemperatureLogic.getTemperatureBuilding(this);
            float heatTemp = TemperatureLogic.getHeatBuilding(this);
            float coldTemp = TemperatureLogic.getColdBuilding(this);

            float efficiency = 0f;

            if(requiresHeat && requiresCold){
                // Both required - check both separately (no cancellation)
                float heatEfficiency = heatTemp >= minTemperature ?
                        Math.min(heatTemp / maxTemperature, 1f) : 0f;
                float coldEfficiency = coldTemp >= minTemperature ?
                        Math.min(coldTemp / maxTemperature, 1f) : 0f;

                // Use minimum of both (both must be satisfied)
                efficiency = Math.min(heatEfficiency, coldEfficiency);

            }else if(requiresHeat){
                // Only heat required
                efficiency = heatTemp >= minTemperature ?
                        Math.min(heatTemp / maxTemperature, 1f) : 0f;

            }else if(requiresCold){
                // Only cold required
                efficiency = coldTemp >= minTemperature ?
                        Math.min(coldTemp / maxTemperature, 1f) : 0f;
            }

            currentPower = powerProduction * efficiency;
            productionEfficiency = efficiency;
        }

        @Override
        public float getPowerProduction(){
            return currentPower;
        }
    }
}