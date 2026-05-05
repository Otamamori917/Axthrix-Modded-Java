package axthrix.world.types.block.traversal;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.geom.Geometry;
import arc.util.Time;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.block.production.TemperatureGenerator;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class TemperatureConveyor extends AxBlock {
    public float transferRate = 2f; // Temperature transferred per second
    public float sideTransferRate = 1f; // Temperature from sides (for turning)

    // Temperature resistances
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 0.5f;
    public float effectResistanceCold = 0.5f;

    public float maxTempStorage = 20f;

    public TemperatureConveyor(String name){
        super(name);
        update = true;
        solid = false;
        rotate = true;
        noUpdateDisabled = true;
    }

    @Override
    public void setStats(){
        super.setStats();

        // Output Stat (Main Line Transfer)
        stats.add(Stat.output, table -> {
            // formatDelta handles the math for rates (x1.8 for F, x1 for K)
            String rate = TempUnit.formatDelta(transferRate);
            table.add("Transfer: " + rate + StatUnit.perSecond.localized());
        });

        // Input Stat (Side Transfer)
        stats.add(Stat.input, table -> {
            String sideRate = TempUnit.formatDelta(sideTransferRate);
            table.add("Side input: " + sideRate + StatUnit.perSecond.localized());
        });

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

        addBar("temperature", (TemperatureConveyorBuild entity) -> new TemperatureBar("Temp", entity));

    }

    public class TemperatureConveyorBuild extends AxBlockBuild {

        @Override
        public void updateTile(){
            // Get my current temperature
            float myHeat = TemperatureLogic.getHeatBuilding(this);
            float myCold = TemperatureLogic.getColdBuilding(this);

            // Transfer to front first (priority)
            Building front = front();
            if(front != null){
                float transferAmount = transferRate * Time.delta / 60f;

                // Transfer heat
                if(myHeat > 0){
                    float transfer = Math.min(myHeat, transferAmount);
                    TemperatureLogic.applyTemperatureBuilding(front, transfer);
                    // Remove from self
                    TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                    myHeat -= transfer; // Update local value
                }

                // Transfer cold
                if(myCold > 0){
                    float transfer = Math.min(myCold, transferAmount);
                    TemperatureLogic.applyTemperatureBuilding(front, -transfer);
                    // Remove from self
                    TemperatureLogic.applyTemperatureBuilding(this, transfer);
                    myCold -= transfer; // Update local value
                }
            }

            // Pull from back (after transferring forward)
            Building back = back();
            if(back != null){
                float backHeat = TemperatureLogic.getHeatBuilding(back);
                float backCold = TemperatureLogic.getColdBuilding(back);

                float transferAmount = transferRate * Time.delta / 60f;

                // Pull heat from back
                if(backHeat > 0){
                    float transfer = Math.min(backHeat, transferAmount);
                    TemperatureLogic.applyTemperatureBuilding(this, transfer);
                    // Remove from back
                    TemperatureLogic.applyTemperatureBuilding(back, -transfer);
                }

                // Pull cold from back
                if(backCold > 0){
                    float transfer = Math.min(backCold, transferAmount);
                    TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                    // Remove from back
                    TemperatureLogic.applyTemperatureBuilding(back, transfer);
                }
            }

            // Pull from sides (for turning)
            for(int i = 0; i < 2; i++){
                int sideRotation = (rotation + (i == 0 ? 1 : -1) * 2) % 4;
                if(sideRotation < 0) sideRotation += 4; // Handle negative modulo

                Building side = nearby(sideRotation);
                if(side != null){
                    float sideHeat = TemperatureLogic.getHeatBuilding(side);
                    float sideCold = TemperatureLogic.getColdBuilding(side);

                    float sideTransferAmount = sideTransferRate * Time.delta / 60f;

                    // Pull heat from side
                    if(sideHeat > 0){
                        float transfer = Math.min(sideHeat, sideTransferAmount);
                        TemperatureLogic.applyTemperatureBuilding(this, transfer);
                        // Remove from side
                        TemperatureLogic.applyTemperatureBuilding(side, -transfer);
                    }

                    // Pull cold from side
                    if(sideCold > 0){
                        float transfer = Math.min(sideCold, sideTransferAmount);
                        TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                        // Remove from side
                        TemperatureLogic.applyTemperatureBuilding(side, transfer);
                    }
                }
            }
        }

        @Override
        public void draw(){
            super.draw();

            // Draw flow indicator
            float heat = TemperatureLogic.getHeatBuilding(this);
            float cold = TemperatureLogic.getColdBuilding(this);

            if(heat > 0 || cold > 0){
                Draw.z(30f);

                Color color = heat > cold ? Pal.turretHeat : Pal.lancerLaser;
                Draw.color(color);

                // Animated flow arrow
                float offset = (Time.time % 60f) / 60f * 8f;
                int dx = Geometry.d4x(rotation);
                int dy = Geometry.d4y(rotation);

                Lines.stroke(2f);
                Lines.lineAngle(x + dx * offset, y + dy * offset, rotation * 90f, 4f);

                Draw.reset();
            }
        }
    }
}