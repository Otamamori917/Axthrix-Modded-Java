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

public class TemperatureRouter extends AxBlock {
    public float transferRate = 2f;
    public float maxTempStorage = 20f;

    public TemperatureRouter(String name) {
        super(name);
        update = true;
        solid = true;
        noUpdateDisabled = true;
        underBullets = true;
        effectResistanceHeat = 0.5f;
        effectResistanceCold = 0.5f;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.output, table -> {
            String rate = TempUnit.formatDelta(transferRate);
            table.add("Split Transfer: " + rate + StatUnit.perSecond.localized());
        });
        stats.add(Stat.heatCapacity, table -> {
            String coldCap = TempUnit.format(-maxTempStorage);
            String heatCap = TempUnit.format(maxTempStorage);
            table.add("[cyan]" + coldCap + "[]  -  [red]" + heatCap + "[]");
        });
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("temperature", (TemperatureRouterBuild entity) -> new TemperatureBar("Temp", entity));
    }

    public class TemperatureRouterBuild extends AxBlockBuild {

        @Override
        public void updateTile() {
            float myHeat = TemperatureLogic.getHeatBuilding(this);
            float myCold = TemperatureLogic.getColdBuilding(this);

            if (myHeat <= 0.001f && myCold <= 0.001f) return;

            float frameTransferLimit = transferRate * Time.delta / 60f;

            for (int i = 0; i < 4; i++) {
                Building other = nearby(i);
                if (other == null) continue;

                if (other.block instanceof TemperatureConveyor || other.block instanceof TemperatureRouter || other.block instanceof TemperatureJunction) {
                    float targetMaxStorage = TemperatureLogic.getMaxTempStorage(other.block);

                    if (myHeat > 0.001f && !(other.block instanceof TemperatureRouter)) {
                        float targetRemaining = targetMaxStorage - TemperatureLogic.getHeatBuilding(other);
                        if (targetRemaining > 0.001f) {
                            float transfer = Math.min(myHeat, Math.min(frameTransferLimit / 3f, targetRemaining));
                            TemperatureLogic.applyTemperatureBuilding(other, transfer);
                            TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                            myHeat -= transfer;
                        }
                    }
                    if (myCold > 0.001f && !(other.block instanceof TemperatureRouter)) {
                        float targetRemaining = targetMaxStorage - TemperatureLogic.getColdBuilding(other);
                        if (targetRemaining > 0.001f) {
                            float transfer = Math.min(myCold, Math.min(frameTransferLimit / 3f, targetRemaining));
                            TemperatureLogic.applyTemperatureBuilding(other, -transfer);
                            TemperatureLogic.applyTemperatureBuilding(this, transfer);
                            myCold -= transfer;
                        }
                    }
                } else {
                    float spaceLeftHeat = maxTempStorage - myHeat;
                    float spaceLeftCold = maxTempStorage - myCold;
                    float otherHeat = TemperatureLogic.getHeatBuilding(other);
                    float otherCold = TemperatureLogic.getColdBuilding(other);

                    if (otherHeat > 0.001f && spaceLeftHeat > 0.001f) {
                        float transfer = Math.min(otherHeat, Math.min(frameTransferLimit, spaceLeftHeat));
                        TemperatureLogic.applyTemperatureBuilding(this, transfer);
                        TemperatureLogic.applyTemperatureBuilding(other, -transfer);
                        myHeat += transfer;
                    }
                    if (otherCold > 0.001f && spaceLeftCold > 0.001f) {
                        float transfer = Math.min(otherCold, Math.min(frameTransferLimit, spaceLeftCold));
                        TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                        TemperatureLogic.applyTemperatureBuilding(other, transfer);
                        myCold += transfer;
                    }
                }
            }
        }

        @Override
        public void draw() {
            super.draw();
            float heat = TemperatureLogic.getHeatBuilding(this);
            float cold = TemperatureLogic.getColdBuilding(this);

            if (heat > 0.01f || cold > 0.01f) {
                Draw.z(30f);
                Draw.color(heat > cold ? Pal.turretHeat : Pal.lancerLaser);
                Lines.stroke(2f);
                Lines.square(x, y, 3f, Time.time * 1.5f);
                Draw.reset();
            }
        }
    }
}
