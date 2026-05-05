package axthrix.world.types.block.production;

import arc.util.Time;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperatureGenerator extends AxGenericCrafter {
    /// positive for heat negative for cyro
    public float temperatureOutput = 5f;
    public float maxTempStorage = 100f;

    // Temperature resistances
    public float accumulationResistanceHeat = 0.3f;
    public float accumulationResistanceCold = 0.3f;
    public float effectResistanceHeat = 0.3f;
    public float effectResistanceCold = 0.3f;

    public TemperatureGenerator(String name){
        super(name);
        outputsPower = false;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.basePowerGeneration); // Remove power stat

        stats.add(Stat.output, table -> {
            // 1. Determine Visuals (Red for Heat, Cyan for Cold)
            // If positive, add a "+" sign. If negative, the number itself will carry the "-" sign.
            String prefix = (temperatureOutput > 0) ? "[red]+" : "[cyan]";

            // 2. Calculate the Value
            // We use formatDelta because this is a RATE, not a thermometer reading.
            String value = TempUnit.formatDelta(temperatureOutput);

            // 3. Display: "[red]+10°F/sec" or "[cyan]-10°F/sec"
            table.add(prefix + value + StatUnit.perSecond.localized());
        });

        stats.add(Stat.heatCapacity, table -> {

            // 1. Convert the Cold Limit (Negative internal value)
            String coldCap = TempUnit.format(-maxTempStorage);

            // 2. Convert the Heat Limit (Positive internal value)
            String heatCap = TempUnit.format(maxTempStorage);

            // 3. Display as a Range: "[cyan]-148°F[]  to  [red]212°F[]"
            table.add("[cyan]" + coldCap + "[]  -  [red]" + heatCap + "[]");
        });
    }


    @Override
    public void setBars(){
        super.setBars();

        addBar("temperature", (TemperatureGeneratorBuild entity) -> new TemperatureBar("Temp", entity));

    }
    public class TemperatureGeneratorBuild extends GenericCrafterBuild {

        @Override
        public void updateTile(){
            super.updateTile(); // Handle all consumption automatically

            // Only produce temperature if we're efficient (have resources)
            if(efficiency > 0){
                float deltaTemp = temperatureOutput *
                        efficiency * Time.delta / 60f;
                TemperatureLogic.applyTemperatureBuilding(this, deltaTemp);
            }
        }
    }
}