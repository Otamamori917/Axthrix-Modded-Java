package axthrix.world.types.block.temperature;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.ui.Styles;
import mindustry.world.meta.BuildVisibility;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperatureVoidBlock extends AxBlock {

    public TemperatureVoidBlock(String name){
        super(name);
        update = true;
        solid = true;
        rotate = true;
        configurable = true;
        saveConfig = true;
        buildVisibility = BuildVisibility.sandboxOnly;
        rotateDraw = false;

        config(Float.class, (TemperatureVoidBuild b, Float f) -> b.targetRange = Mathf.clamp(f, 8, 800));
        config(Integer.class, (TemperatureVoidBuild b, Integer i) -> b.drainMode = Mathf.clamp(i, 0, 2));
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
        drawTouchBorders(x * Vars.tilesize + offset, y * Vars.tilesize + offset, rotation, 1, Color.black);
    }
    public class TemperatureVoidBuild extends Building {
        public float targetRange = 50;
        public int drainMode = 1;

        @Override
        public void drawSelect(){
            super.drawSelect();
            if(drainMode == 0){
                Drawf.dashCircle(x, y, targetRange, Color.black);
            } else {
                drawTouchBorders(x, y, rotation, drainMode, Color.black);
            }
        }

        @Override
        public void updateTile(){
            if(drainMode == 0){
                Groups.unit.intersect(x - targetRange, y - targetRange, targetRange * 2f, targetRange * 2f,
                        (Unit u) -> TemperatureLogic.setTemperatureUnit(u, 0f));
                Vars.indexer.allBuildings(x, y, targetRange, other -> {
                    if(other != this) TemperatureLogic.setTemperatureBuilding(other, 0f);
                });
            } else if(drainMode == 1){
                Building target = front();
                if(target != null) TemperatureLogic.setTemperatureBuilding(target, 0f);
            } else if(drainMode == 2){
                for(int i = 0; i < 4; i++){
                    Building target = nearby(i);
                    if(target != null) TemperatureLogic.setTemperatureBuilding(target, 0f);
                }
            }
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

                ft.add(contentTable).growX().row();
                rebuildContent(contentTable);
            });

        }

        private void rebuildContent(Table t) {
            if(drainMode == 0){
                t.label(() -> Stat.range.localized()+" "+(int)targetRange/8+" "+StatUnit.blocks.localized()).center().row();
                t.slider(8, 800f, 8f, targetRange, this::configure).width(220f).growX().padTop(4f);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(targetRange);
            write.b((byte)drainMode);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            targetRange = read.f();
            drainMode = read.b();
        }
    }
}
