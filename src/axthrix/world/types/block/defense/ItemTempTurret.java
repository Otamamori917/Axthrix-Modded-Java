package axthrix.world.types.block.defense;

import arc.math.Mathf;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.entities.bullet.BulletType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class ItemTempTurret extends AxItemTurret{

    public float temperatureOutput = 3;
    /// the magnitude at which the turret cannot fire anymore and temp needs to be removed
    public float lockOutThreshold = 80;

    public ItemTempTurret(String name) {
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.output, table -> {
            String prefix = (temperatureOutput > 0) ? "[red]+" : "[cyan]";

            String value = TempUnit.formatDelta(temperatureOutput * (60f / reload));

            table.add(prefix + value + StatUnit.perSecond.localized());
        });

        if(lockOutThreshold != 0){
            stats.add(new Stat("aj-lockout"), table -> {
                String prefix = (lockOutThreshold > 0) ? "[red]+" : "[cyan]";

                String value = TempUnit.format(lockOutThreshold);

                table.add(prefix + value);
            });
        }


        stats.add(Stat.heatCapacity, table -> {
            String prefix;
            if(temperatureOutput > 0){
                prefix = "[red]" + TempUnit.format(maxTempStorage);
            }else{
                prefix = "[cyan]" + TempUnit.format(-maxTempStorage);
            }


            table.add(prefix);
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("temperature", (ItemTempTurretBuild entity) -> new TemperatureBar("Temp", entity));
    }

    public class ItemTempTurretBuild extends AxItemTurret.AxItemTurretBuild {


        @Override
        protected void shoot(BulletType type) {
            if(lockOutThreshold != 0){
                float directedTemp = TemperatureLogic.getDirectedTemperature(this, temperatureOutput);
                if(directedTemp >= Math.abs(lockOutThreshold)){
                    return;
                }
            }
            super.shoot(type);
            TemperatureLogic.applyTemperatureBuilding(this, temperatureOutput);
        }

        @Override
        public void draw(){
            super.draw();

            float directedTemp = TemperatureLogic.getDirectedTemperature(this, temperatureOutput);
            AxPartParms.axparams.set(0, 0, Mathf.clamp(directedTemp / maxTempStorage));
        }
    }
}