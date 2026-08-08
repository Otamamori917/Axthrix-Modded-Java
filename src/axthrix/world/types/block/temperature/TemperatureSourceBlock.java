package axthrix.world.types.block.temperature;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperatureSourceBlock extends AxBlock {

    public TemperatureSourceBlock(String name){
        super(name);
        update = true;
        solid = true;
        rotate = true;
        configurable = true;
        saveConfig = true;
        buildVisibility = BuildVisibility.sandboxOnly;
        rotateDraw = false;

        config(Float.class, (TemperatureSourceBuild b, Float f) -> b.targetTemp = Mathf.clamp(f, -1000f, 1000f));
        config(Integer.class, (TemperatureSourceBuild b, Integer i) -> {
            if(i >= 8){
                b.targetRange = i;
            } else {
                b.drainMode = i;
            }
        });
    }

    private void drawTouchBorders(float bx, float by, int rotation, int mode, Color col) {
        float ext = (size * Vars.tilesize);
        int dashes = Math.round((size * Vars.tilesize) * 4);

        if (mode == 1) {
            Lines.stroke(3f, Color.gray);
            if (rotation == 0) Lines.dashLine(bx + ext, by - ext, bx + ext, by + ext, dashes);
            else if (rotation == 1) Lines.dashLine(bx - ext, by + ext, bx + ext, by + ext, dashes);
            else if (rotation == 2) Lines.dashLine(bx - ext, by - ext, bx - ext, by + ext, dashes);
            else if (rotation == 3) Lines.dashLine(bx - ext, by - ext, bx + ext, by - ext, dashes);
            Lines.stroke(1f, col);
            if (rotation == 0) Lines.dashLine(bx + ext, by - ext, bx + ext, by + ext, dashes);
            else if (rotation == 1) Lines.dashLine(bx - ext, by + ext, bx + ext, by + ext, dashes);
            else if (rotation == 2) Lines.dashLine(bx - ext, by - ext, bx - ext, by + ext, dashes);
            else if (rotation == 3) Lines.dashLine(bx - ext, by - ext, bx + ext, by - ext, dashes);
        } else if (mode == 2) {
            Lines.stroke(3f, Color.gray);
            Lines.dashLine(bx - ext, by - ext, bx + ext, by - ext, dashes);
            Lines.dashLine(bx + ext, by - ext, bx + ext, by + ext, dashes);
            Lines.dashLine(bx + ext, by + ext, bx - ext, by + ext, dashes);
            Lines.dashLine(bx - ext, by + ext, bx - ext, by - ext, dashes);
            Lines.stroke(1f, col);
            Lines.dashLine(bx - ext, by - ext, bx + ext, by - ext, dashes);
            Lines.dashLine(bx + ext, by - ext, bx + ext, by + ext, dashes);
            Lines.dashLine(bx + ext, by + ext, bx - ext, by + ext, dashes);
            Lines.dashLine(bx - ext, by + ext, bx - ext, by - ext, dashes);
        }
        Draw.color();
        Lines.stroke(1f);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);
        drawTouchBorders(x * Vars.tilesize + offset, y * Vars.tilesize + offset, rotation, 1, Pal.accent);
    }
    public class TemperatureSourceBuild extends Building {
        public float targetTemp = 50f;
        public float targetRange = 50;
        public int drainMode = 1;

        @Override
        public void drawSelect(){
            super.drawSelect();
            Color col = targetTemp > 0 ? Color.red : Color.cyan;
            if(drainMode == 0){
                Drawf.dashCircle(x, y, targetRange, col);
            } else {
                drawTouchBorders(x, y, rotation, drainMode, col);
            }
        }

        @Override
        public void updateTile(){
            if(drainMode == 0){
                Groups.unit.intersect(x - targetRange, y - targetRange, targetRange * 2f, targetRange * 2f,
                        (Unit u) -> TemperatureLogic.setTemperatureUnit(u, targetTemp));
                Vars.indexer.allBuildings(x, y, targetRange, other -> {
                    if(other != this) TemperatureLogic.setTemperatureBuilding(other, targetTemp);
                });
            } else if(drainMode == 1){
                Building target = front();
                if(target != null) TemperatureLogic.setTemperatureBuilding(target, targetTemp);
            } else if(drainMode == 2){
                for(int i = 0; i < 4; i++){
                    Building target = nearby(i);
                    if(target != null) TemperatureLogic.setTemperatureBuilding(target, targetTemp);
                }
            }
        }

        @Override
        public void draw(){
            Draw.z(Layer.blockOver);
            if(targetTemp > 0){
                Draw.color(Tmp.c1.set(Color.white).lerp(Color.red, Mathf.clamp(targetTemp / 150f)));
            }else{
                Draw.color(Tmp.c1.set(Color.white).lerp(Color.cyan, Mathf.clamp(-targetTemp / 150f)));
            }
            Fill.square(x, y, 2);
            Draw.color();
            super.draw();
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.grayPanel, ft -> {
                ft.label(() -> Core.bundle.get("ui.aj-drain-mode") + "-" +
                        Core.bundle.get(drainMode == 0 ? "ui.aj-mode-radial" : drainMode == 1 ? "ui.aj-mode-front" : "ui.aj-mode-all")).center().row();

                Table contentTable = new Table();

                ft.slider(0, 2, 1, drainMode, i -> {
                    configure((int)i);
                    contentTable.clear();
                    rebuildContent(contentTable);
                }).width(220f).growX().padTop(4f).row();

                ft.label(() -> Core.bundle.get("ui.aj-temp") + (targetTemp > 0 ? Core.bundle.get("ui.aj-heat")+" " : Core.bundle.get("ui.aj-cold")+" ") + (int)targetTemp).center().row();
                ft.slider(-1000f, 1000f, 1f, targetTemp, this::configure).width(220f).growX().padTop(4f).row();

                ft.add(contentTable).growX().row();
                rebuildContent(contentTable);
            });
        }

        private void rebuildContent(Table t) {
            if(drainMode == 0){
                t.label(() -> Stat.range.localized()+" "+(int)targetRange/8+" "+StatUnit.blocks.localized()).center().row();
                t.slider(8f, 800f, 8f, targetRange, f -> configure((int)f)).width(220f).growX().padTop(4f);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(targetTemp);
            write.f(targetRange);
            write.b((byte)drainMode);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            targetTemp = read.f();
            targetRange = read.f();
            drainMode = read.b();
        }
    }
}
