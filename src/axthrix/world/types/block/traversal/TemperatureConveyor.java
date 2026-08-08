package axthrix.world.types.block.traversal;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class TemperatureConveyor extends AxBlock implements Autotiler {
    public float transferRate = 2f;
    public float maxTempStorage = 20f;

    public TextureRegion[] topRegions = new TextureRegion[5];
    public TextureRegion[] botRegions = new TextureRegion[5];

    public TemperatureConveyor(String name){
        super(name);
        group = BlockGroup.transportation;
        update = true;
        solid = false;
        rotate = true;
        noSideBlend = true;
        conveyorPlacement = true;
        noUpdateDisabled = true;
        underBullets = true;

        effectResistanceHeat = 0.5f;
        effectResistanceCold = 0.5f;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.output, table -> {
            String rate = TempUnit.formatDelta(transferRate);
            table.add("Transfer: " + rate + StatUnit.perSecond.localized());
        });
        stats.add(Stat.heatCapacity, table -> {
            String coldCap = TempUnit.format(-maxTempStorage);
            String heatCap = TempUnit.format(maxTempStorage);
            table.add("[cyan]" + coldCap + "[]  -  [red]" + heatCap + "[]");
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("temperature", (TemperatureConveyorBuild entity) -> new TemperatureBar("Temp", entity));
    }

    @Override
    public void load() {
        super.load();
        for (int i = 0; i < 5; i++) {
            topRegions[i] = Core.atlas.find(name + "-top-" + i);
            botRegions[i] = Core.atlas.find(name + "-bottom-" + i, Core.atlas.find("duct-bottom-" + i));
        }
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        int[] bits = getTiling(plan, list);
        if(bits == null) return;

        Draw.scl(bits[1], bits[2]);
        Draw.alpha(0.5f);
        Draw.rect(botRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.color();
        Draw.rect(topRegions[bits[0]], plan.drawx(), plan.drawy(), plan.rotation * 90);
        Draw.scl();
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
        return otherblock instanceof TemperatureConveyor || otherblock instanceof TemperatureRouter || otherblock instanceof TemperatureJunction;
    }
    public class TemperatureConveyorBuild extends Building {
        public int blendbits, xscl, yscl, blending;
        public @Nullable Building next;

        @Override
        public void updateTile(){
            float myHeat = TemperatureLogic.getHeatBuilding(this);
            float myCold = TemperatureLogic.getColdBuilding(this);
            float frameTransferLimit = transferRate * Time.delta / 60f;

            next = front();
            if(next != null){
                float targetMaxStorage = TemperatureLogic.getMaxTempStorage(next.block);
                float targetRemaining = 0f;

                if(next instanceof TemperatureJunction.TemperatureJunctionBuild jb) {
                    int targetAxis = (this.rotation % 2 == 0) ? 0 : 1;

                    if (myHeat > 0.001f) {
                        targetRemaining = jb.channels[targetAxis] < 0 ? 0f : targetMaxStorage - jb.channels[targetAxis];
                    } else if (myCold > 0.001f) {
                        targetRemaining = jb.channels[targetAxis] > 0 ? 0f : targetMaxStorage - Math.abs(jb.channels[targetAxis]);
                    }

                    if (targetRemaining > 0.001f) {
                        float transfer = Math.min(myHeat > 0.001f ? myHeat : myCold, Math.min(frameTransferLimit, targetRemaining));
                        if (myHeat > 0.001f) {
                            jb.channels[targetAxis] += transfer;
                            TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                            myHeat -= transfer;
                        } else {
                            jb.channels[targetAxis] -= transfer;
                            TemperatureLogic.applyTemperatureBuilding(this, transfer);
                            myCold -= transfer;
                        }
                    }
                } else {
                    if(myHeat > 0.001f){
                        targetRemaining = targetMaxStorage - TemperatureLogic.getHeatBuilding(next);
                        if (targetRemaining > 0.001f) {
                            float transfer = Math.min(myHeat, Math.min(frameTransferLimit, targetRemaining));
                            TemperatureLogic.applyTemperatureBuilding(next, transfer);
                            TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                            myHeat -= transfer;
                        }
                    }
                    if(myCold > 0.001f){
                        targetRemaining = targetMaxStorage - TemperatureLogic.getColdBuilding(next);
                        if (targetRemaining > 0.001f) {
                            float transfer = Math.min(myCold, Math.min(frameTransferLimit, targetRemaining));
                            TemperatureLogic.applyTemperatureBuilding(next, -transfer);
                            TemperatureLogic.applyTemperatureBuilding(this, transfer);
                            myCold -= transfer;
                        }
                    }
                }
            }

            Building back = back();
            if(back != null && !(back.block instanceof TemperatureConveyor) && !(back.block instanceof TemperatureRouter) && !(back.block instanceof TemperatureJunction)){
                float spaceLeftHeat = maxTempStorage - myHeat;
                float spaceLeftCold = maxTempStorage - myCold;
                float backHeat = TemperatureLogic.getHeatBuilding(back);
                float backCold = TemperatureLogic.getColdBuilding(back);

                if(backHeat > 0.001f && spaceLeftHeat > 0.001f){
                    float transfer = Math.min(backHeat, Math.min(frameTransferLimit, spaceLeftHeat));
                    TemperatureLogic.applyTemperatureBuilding(this, transfer);
                    TemperatureLogic.applyTemperatureBuilding(back, -transfer);
                    myHeat += transfer;
                }
                if(backCold > 0.001f && spaceLeftCold > 0.001f){
                    float transfer = Math.min(backCold, Math.min(frameTransferLimit, spaceLeftCold));
                    TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                    TemperatureLogic.applyTemperatureBuilding(back, transfer);
                    myCold += transfer;
                }
            }

            for(int i = 0; i < 2; i++){
                int sideRotation = (rotation + (i == 0 ? 1 : -1) * 2) % 4;
                if(sideRotation < 0) sideRotation += 4;

                Building side = nearby(sideRotation);
                if(side != null && !(side.block instanceof TemperatureConveyor) && !(side.block instanceof TemperatureRouter) && !(side.block instanceof TemperatureJunction)){
                    float spaceLeftHeat = maxTempStorage - myHeat;
                    float spaceLeftCold = maxTempStorage - myCold;
                    float sideHeat = TemperatureLogic.getHeatBuilding(side);
                    float sideCold = TemperatureLogic.getColdBuilding(side);

                    if(sideHeat > 0.001f && spaceLeftHeat > 0.001f){
                        float transfer = Math.min(sideHeat, Math.min(frameTransferLimit, spaceLeftHeat));
                        TemperatureLogic.applyTemperatureBuilding(this, transfer);
                        TemperatureLogic.applyTemperatureBuilding(side, -transfer);
                        myHeat += transfer;
                    }
                    if(sideCold > 0.001f && spaceLeftCold > 0.001f){
                        float transfer = Math.min(sideCold, Math.min(frameTransferLimit, spaceLeftCold));
                        TemperatureLogic.applyTemperatureBuilding(this, -transfer);
                        TemperatureLogic.applyTemperatureBuilding(side, transfer);
                        myCold += transfer;
                    }
                }
            }
        }
        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            int[] bits = buildBlending(tile, rotation, null, true);
            blendbits = bits[0];
            xscl = bits[1];
            yscl = bits[2];
            blending = bits[4];
            next = front();
        }

        @Override
        public void draw(){
            float rotationDeg = rotdeg();
            int r = this.rotation;

            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = r - i;
                    float rot = i == 0 ? rotationDeg : (dir)*90;
                    drawAt(x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
                }
            }

            Draw.scl(xscl, yscl);
            drawAt(x, y, blendbits, rotationDeg, SliceMode.none);
            Draw.scl();

            float heat = TemperatureLogic.getHeatBuilding(this);
            float cold = TemperatureLogic.getColdBuilding(this);
            if(heat > 0.01f || cold > 0.01f){
                Draw.z(Layer.blockUnder + 0.3f);
                Draw.color(heat > cold ? Pal.turretHeat : Pal.lancerLaser);
                float offset = (Time.time % 60f) / 60f * 8f;
                Lines.stroke(2f);
                Lines.lineAngle(x + Geometry.d4x(this.rotation) * offset, y + Geometry.d4y(this.rotation) * offset, this.rotation * 90f, 4f);
                Draw.reset();
            }
        }

        protected void drawAt(float x, float y, int bits, float rotation, SliceMode slice){
            Draw.z(Layer.blockUnder);
            Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);

            Draw.z(Layer.blockUnder + 0.2f);
            Draw.color(new Color(0.4f, 0.4f, 0.4f, 0.1f));
            Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);
            Draw.color();
            Draw.rect(sliced(topRegions[bits], slice), x, y, rotation);
        }
    }
}
