package axthrix.world.types.block.production;

import axthrix.world.types.bulletypes.TemperatureBulletType;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.meta.Stat;

public class TemperatureCrafter extends GenericCrafter {
    public float minTemperature = 30f; // Minimum temp to operate
    public float maxTemperature = 100f; // Temperature for max efficiency
    public boolean requiresHeat = true; // true = needs heat, false = needs cold

    // Temperature resistances
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public TemperatureCrafter(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.input, (requiresHeat ? "[red]Heat: " : "[cyan]Cold: ") + minTemperature + "° - " + maxTemperature + "°");
    }

    public class TemperatureCrafterBuild extends GenericCrafterBuild {
        public float efficiency = 0f;

        @Override
        public void updateTile(){
            float temp = TemperatureBulletType.getTemperatureBuilding(this);
            float effectiveTemp = requiresHeat ? temp : -temp;

            // Calculate efficiency based on temperature
            if(effectiveTemp >= minTemperature){
                efficiency = Math.min(effectiveTemp / maxTemperature, 1f);
            }else{
                efficiency = 0f;
            }

            // Only craft if we have temperature
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