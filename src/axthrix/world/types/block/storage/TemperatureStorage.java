package axthrix.world.types.block.storage;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.meta.Stat;

public class TemperatureStorage extends Block {
    public float maxTempStorage = 500f;

    // Temperature resistances
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 0.1f; // Very resistant to effects
    public float effectResistanceCold = 0.1f;

    public TemperatureStorage(String name){
        super(name);
        update = true;
        solid = true;
        configurable = false;
        hasPower = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        // Use the Lambda (table -> {}) to make it update when settings change
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

        addBar("temperature", (TemperatureStorageBuild entity) -> new TemperatureBar("Temp", entity));

    }

    public class TemperatureStorageBuild extends Building {
        public boolean isPowered = false;

        @Override
        public void updateTile(){
            if(power.status > 0f){
                TemperatureLogic.refreshLastHitTime(pos());
            }
        }

        @Override
        public void draw(){
            super.draw();

            // Draw storage indicator
            float heat = TemperatureLogic.getHeatBuilding(this);
            float cold = TemperatureLogic.getColdBuilding(this);

            if(heat > 0){
                Draw.color(Pal.turretHeat);
                Draw.alpha(Mathf.clamp(heat / maxTempStorage));
                Fill.rect(x - size * 2, y, 4f, size * 8f);
            }

            if(cold > 0){
                Draw.color(Pal.lancerLaser);
                Draw.alpha(Mathf.clamp(cold / maxTempStorage));
                Fill.rect(x + size * 2, y, 4f, size * 8f);
            }

            Draw.reset();
        }

        @Override
        public void buildConfiguration(Table table){
            super.buildConfiguration(table);
        }
    }
}