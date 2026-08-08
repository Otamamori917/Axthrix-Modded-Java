package axthrix.world.types.block.storage;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.meta.Stat;

public class TemperatureStorage extends AxBlock {
    public float maxTempStorage = 500f;


    public TemperatureStorage(String name){
        super(name);
        update = true;
        solid = true;
        configurable = false;
        hasPower = true;

        effectResistanceHeat = 0.1f;
        effectResistanceCold = 0.1f;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.heatCapacity, table -> {
            String coldCap = TempUnit.format(-maxTempStorage);
            String heatCap = TempUnit.format(maxTempStorage);
            table.add("[cyan]" + coldCap + "[]  -  [red]" + heatCap + "[]");
        });
    }


    @Override
    public void setBars(){
        super.setBars();

        addBar("temperature", (TemperatureStorageBuild entity) -> new TemperatureBar("Temp", entity));

    }

    public class TemperatureStorageBuild extends Building {

        @Override
        public void updateTile(){
            if(power.status > 0f){
                TemperatureLogic.refreshLastHitTime(pos());
            }
        }

        @Override
        public void draw(){
            super.draw();

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