package axthrix.world.types.block.traversal;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.util.Time;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperatureJunction extends AxBlock {
    public float transferRate = 2.5f;
    public float maxTempStorage = 20f;

    public TemperatureJunction(String name) {
        super(name);
        update = true;
        solid = true;
        noUpdateDisabled = true;
        effectResistanceHeat = 0.5f;
        effectResistanceCold = 0.5f;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, table -> {
            String rate = TempUnit.formatDelta(transferRate);
            table.add("Junction Flow: " + rate + StatUnit.perSecond.localized());
        });
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("temperature", (TemperatureJunctionBuild entity) -> new TemperatureBar("Temp", entity));
    }
    public class TemperatureJunctionBuild extends AxBlockBuild {
        public float[] channels = new float[2];

        @Override
        public void updateTile() {
            float frameLimit = transferRate * Time.delta / 60f;

            for (int i = 0; i < 4; i++) {
                Building source = nearby(i);
                if (source == null) continue;

                int axis = (i % 2 == 0) ? 0 : 1;
                Building output = nearby((i + 2) % 4);

                if (!(source.block instanceof TemperatureConveyor) && !(source.block instanceof TemperatureRouter) && !(source.block instanceof TemperatureJunction)) {
                    float sHeat = TemperatureLogic.getHeatBuilding(source);
                    float sCold = TemperatureLogic.getColdBuilding(source);

                    if (sHeat > 0.001f && channels[axis] >= 0) {
                        float space = maxTempStorage - channels[axis];
                        float transfer = Math.min(sHeat, Math.min(frameLimit, space));
                        channels[axis] += transfer;
                        TemperatureLogic.applyTemperatureBuilding(source, -transfer);
                    }
                    if (sCold > 0.001f && channels[axis] <= 0) {
                        float space = maxTempStorage - Math.abs(channels[axis]);
                        float transfer = Math.min(sCold, Math.min(frameLimit, space));
                        channels[axis] -= transfer;
                        TemperatureLogic.applyTemperatureBuilding(source, transfer);
                    }
                }

                if (output != null && Math.abs(channels[axis]) > 0.001f) {
                    float targetMaxStorage = TemperatureLogic.getMaxTempStorage(output.block);

                    if (channels[axis] > 0) {
                        float targetRemaining = targetMaxStorage - TemperatureLogic.getHeatBuilding(output);
                        if (targetRemaining > 0.001f) {
                            float transfer = Math.min(channels[axis], Math.min(frameLimit, targetRemaining));
                            TemperatureLogic.applyTemperatureBuilding(output, transfer);
                            channels[axis] -= transfer;
                        }
                    } else {
                        float targetRemaining = targetMaxStorage - TemperatureLogic.getColdBuilding(output);
                        if (targetRemaining > 0.001f) {
                            float transfer = Math.min(Math.abs(channels[axis]), Math.min(frameLimit, targetRemaining));
                            TemperatureLogic.applyTemperatureBuilding(output, -transfer);
                            channels[axis] += transfer;
                        }
                    }
                }
            }

            float totalHeat = Math.max(0, channels[0]) + Math.max(0, channels[1]);
            float totalCold = Math.max(0, -channels[0]) + Math.max(0, -channels[1]);

            TemperatureLogic.applyTemperatureBuilding(this, totalHeat - TemperatureLogic.getHeatBuilding(this));
            TemperatureLogic.applyTemperatureBuilding(this, -(totalCold - TemperatureLogic.getColdBuilding(this)));
        }
        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            for (int i = 0; i < 4; i++) {
                Building b = nearby(i);
                if (b != null) b.noSleep();
            }
        }

        @Override
        public void draw() {
            super.draw();
            float hState = channels[0];
            float vState = channels[1];

            if (Math.abs(vState) > 0.01f || Math.abs(hState) > 0.01f) {
                Draw.z(30f);
                Lines.stroke(2f);

                if (Math.abs(vState) > 0.01f) {
                    Draw.color(vState > 0 ? Pal.turretHeat : Pal.lancerLaser);
                    Lines.line(x, y - 4f, x, y + 4f);
                }
                if (Math.abs(hState) > 0.01f) {
                    Draw.color(hState > 0 ? Pal.turretHeat : Pal.lancerLaser);
                    Lines.line(x - 4f, y, x + 4f, y);
                }
                Draw.reset();
            }
        }
    }
}
