package axthrix.world.types.block.defense;

import axthrix.world.types.block.storage.TemperatureStorage;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.world.meta.Stat;

public class TemperatureTurret extends AxPowerTurret {
    public float minTemperature = 30f;
    public float maxTemperature = 100f;

    public boolean requiresHeat = false;
    public boolean requiresCold = false;

    public TemperatureTurret(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.input, table -> {
            String heatMin = TempUnit.format(minTemperature);
            String heatMax = TempUnit.format(maxTemperature);
            String heatText = heatMin + " - " + heatMax;

            String coldMin = TempUnit.format(-minTemperature);
            String coldMax = TempUnit.format(-maxTemperature);
            String coldText = coldMin + " - " + coldMax;

            if(requiresHeat && requiresCold){
                table.add("[red]Heat: " + heatText + "[]\n[cyan]Cold: " + coldText + "[]");
            }
            else if(requiresHeat){
                table.add("[red]Heat: " + heatText + "[]");
            }
            else if(requiresCold){
                table.add("[cyan]Cold: " + coldText + "[]");
            }
        });

        stats.add(Stat.heatCapacity, table -> {
            String prefix = "";
            if(requiresHeat && requiresCold){
                prefix = "[cyan]" + TempUnit.format(-maxTempStorage) + "[]  -  [red]" + TempUnit.format(maxTempStorage) + "[]";
            }
            else if(requiresHeat){
                prefix = "[red]" + TempUnit.format(maxTempStorage);
            }
            else if(requiresCold){
                prefix = "[cyan]" + TempUnit.format(-maxTempStorage);
            }

            table.add(prefix);
        });
    }


    @Override
    public void setBars(){
        super.setBars();

        addBar("temperature", (TemperatureTurretBuild entity) -> new TemperatureBar("Temp", entity));

    }

    public class TemperatureTurretBuild extends AxPowerTurretBuild {

        protected float getTempEfficiency(){
            float heatTemp = TemperatureLogic.getHeatBuilding(this);
            float coldTemp = TemperatureLogic.getColdBuilding(this);

            if(requiresHeat && requiresCold){
                float heatEfficiency = heatTemp >= minTemperature ? Math.min(heatTemp / maxTemperature, 1f) : 0f;
                float coldEfficiency = coldTemp >= minTemperature ? Math.min(coldTemp / maxTemperature, 1f) : 0f;
                return Math.min(heatEfficiency, coldEfficiency);
            }else if(requiresHeat){
                return heatTemp >= minTemperature ? Math.min(heatTemp / maxTemperature, 1f) : 0f;
            }else if(requiresCold){
                return coldTemp >= minTemperature ? Math.min(coldTemp / maxTemperature, 1f) : 0f;
            }else{
                return 1f;
            }
        }

        @Override
        public void updateTile(){
            if(getTempEfficiency() <= 0f) return;
            super.updateTile();
        }

        @Override
        protected void updateCooling(){
            if(getTempEfficiency() > 0f){
                super.updateCooling();
            }else{
                heat = 0f;
                reloadCounter = 0f;
            }
        }

        @Override
        public void draw(){
            super.draw();
            AxPartParms.axparams.set(0, 0, getTempEfficiency());
        }
    }
}