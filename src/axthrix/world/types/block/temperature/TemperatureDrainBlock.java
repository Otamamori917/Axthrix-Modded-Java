package axthrix.world.types.block.temperature;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
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

public class TemperatureDrainBlock extends AxBlock {
    public float drainRate = 5f;
    public boolean drainHeat = true;
    public boolean swappable = true;
    public boolean drainUnits = false;


    /// 0 = Radial Circle Area, 1 = Touch Front Only, 2 = Touch All Sides
    public int drainMode = 0;
    /// Only utilized if drainMode == 0
    public float range = 60f;

    public TemperatureDrainBlock(String name){
        super(name);
        hasPower = true;
        update = true;
        solid = true;
        rotate = true;
        rotateDraw = false;

        if(swappable){
            configurable = true;
            config(Boolean.class, (TemperatureDrainBuild b, Boolean v) -> b.drainHeat = v);
        }
    }

    private void drawTouchBorders(float bx, float by, int rotation, Color col) {
        float ext = (size * Vars.tilesize);
        int dashes = Math.round((size * Vars.tilesize)*4);

        if (drainMode == 1) {
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
        } else if (drainMode == 2) {
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
        Color col = drainHeat ? Color.red : Color.cyan;

        if (drainMode == 0) {
            Drawf.dashCircle(x * Vars.tilesize + offset, y * Vars.tilesize + offset, range, col);
        } else {
            drawTouchBorders(x * Vars.tilesize + offset, y * Vars.tilesize + offset, rotation, col);
        }
    }
    public class TemperatureDrainBuild extends Building {
        public boolean drainHeat = TemperatureDrainBlock.this.drainHeat;

        @Override
        public void drawSelect(){
            super.drawSelect();
            Color col = drainHeat ? Color.red : Color.cyan;
            if (drainMode == 0) {
                Drawf.dashCircle(x, y, range, col);
            } else {
                drawTouchBorders(x, y, rotation, col);
            }
        }

        @Override
        public void updateTile(){
            if(efficiency <= 0f) return;

            float amount = drainRate * (Time.delta / 60f) * efficiency;

            if (drainMode == 0) {
                if(drainUnits){
                    Groups.unit.intersect(x - range, y - range, range * 2f, range * 2f,
                            (Unit u) -> TemperatureLogic.drainTemperatureTowardZero(u, drainHeat, amount));
                }
                Vars.indexer.allBuildings(x, y, range, other -> {
                    if(other != this) TemperatureLogic.drainTemperatureTowardZero(other, drainHeat, amount);
                });
            } else if (drainMode == 1) {
                Building target = front();
                if(target != null) {
                    TemperatureLogic.drainTemperatureTowardZero(target, drainHeat, amount);
                }
            } else if (drainMode == 2) {
                for(int i = 0; i < 4; i++) {
                    Building target = nearby(i);
                    if(target != null) {
                        TemperatureLogic.drainTemperatureTowardZero(target, drainHeat, amount);
                    }
                }
            }
        }
        @Override
        public void buildConfiguration(Table table){
            if(swappable){
                table.button(drainHeat ? Core.bundle.get("button.aj-drain-heat") : Core.bundle.get("button.aj-drain-cold"), () -> {
                    configure(!drainHeat);
                }).size(280f, 44f).update(b -> b.setText(drainHeat ? Core.bundle.get("button.aj-drain-heat") : Core.bundle.get("button.aj-drain-cold")));
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(drainHeat);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            drainHeat = read.bool();
        }
    }
}
