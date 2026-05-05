package axthrix.world.types.block.production;

import axthrix.world.types.block.storage.TemperatureStorage;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.world.meta.Stat;

public class TemperatureCrafter extends AxGenericCrafter {
    public float minTemperature = 30f;
    public float maxTemperature = 100f;

    public boolean requiresHeat = false;
    public boolean requiresCold = false;

    // Temperature resistances
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public float maxTempStorage = 100f;

    public TemperatureCrafter(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

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

        addBar("temperature", (TemperatureCrafterBuild entity) -> new TemperatureBar("Temp", entity));

    }

    public class TemperatureCrafterBuild extends GenericCrafterBuild {
        public float efficiency = 0f;

        @Override
        public void updateTile(){
            float heatTemp = TemperatureLogic.getHeatBuilding(this);
            float coldTemp = TemperatureLogic.getColdBuilding(this);

            if(requiresHeat && requiresCold){
                // Both required - check both separately
                float heatEfficiency = heatTemp >= minTemperature ?
                        Math.min(heatTemp / maxTemperature, 1f) : 0f;
                float coldEfficiency = coldTemp >= minTemperature ?
                        Math.min(coldTemp / maxTemperature, 1f) : 0f;

                efficiency = Math.min(heatEfficiency, coldEfficiency);

            }else if(requiresHeat){
                efficiency = heatTemp >= minTemperature ?
                        Math.min(heatTemp / maxTemperature, 1f) : 0f;

            }else if(requiresCold){
                efficiency = coldTemp >= minTemperature ?
                        Math.min(coldTemp / maxTemperature, 1f) : 0f;
            }

            if(efficiency > 0){
                super.updateTile();
            }else{
                warmup = 0f;
            }
        }

        @Override
        public float efficiencyScale(){
            return super.efficiencyScale() * efficiency;
        }

        @Override
        public float warmup(){
            return efficiency;
        }
    }
}