package axthrix.world.types.block.traversal;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.AxFaction;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.util.AxStats;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.Duct;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;

public class AxDuct extends Duct {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public boolean waterBlock;

    public Seq<AxFaction> faction = new Seq<>();

    public AxDuct(String name) {
        super(name);
        solid = false;
        destructible = true;
        update = true;
        hasShadow = false;
    }

    @Override
    public void init(){
        super.init();
        if(waterBlock){
            floating = true;
            placeableLiquid = true;
        }
    }

    @Override
    public boolean canBreak(Tile tile) {
        if (waterBlock) {
            return LayerManager.isPlayerSubmerged();
        }

        return !LayerManager.isPlayerSubmerged();
    }


    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile == null) return false;

        if (waterBlock) {

            return LayerManager.isPlayerSubmerged() &&
                    (tile.floor() == AxthrixEnvironment.tharaxianDeep || tile.floor() == Blocks.deepwater);
        }


        if (LayerManager.isPlayerSubmerged()) return false;

        return super.canPlaceOn(tile, team, rotation);
    }





    public class AxDuctBuild extends DuctBuild {

        @Override
        public void draw() {
            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();
            boolean onLiquid = tile.floor().isLiquid;

            // 1. Unified Transition Weight (0 = Normal, 1 = Shadow)
            float shadowWeight = 0f;
            if (waterBlock) {
                shadowWeight = 1f - intensity;
            } else if (onLiquid) {
                shadowWeight = intensity;
            }

            // 2. Prepare Shared Visuals (Jiggle & Color)
            float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
            float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

            // Base state color and alpha
            Draw.color(Color.white, Color.black, shadowWeight);
            Draw.alpha(Mathf.lerp(1f, 0.25f, shadowWeight));

            // Native Underwater Tinting
            if (waterBlock && playerSub) {
                Color floorCol = tile.floor().mapColor;
                Tmp.c1.set(Draw.getColor()).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
                Draw.color(Tmp.c1);
            }

            // Save state for sub-calls
            Color currentColor = Tmp.c3.set(Draw.getColor());
            float currentAlpha = Draw.getColorAlpha();

            // 3. Drawing logic
            float rotation = rotdeg();
            int r = this.rotation;

            // Draw blended connections (tiling)
            for(int i = 0; i < 4; i++){
                if((blending & (1 << i)) != 0){
                    int dir = r - i;
                    float rot = i == 0 ? rotation : (dir) * 90;
                    drawAt(x + Geometry.d4x(dir) * tilesize * 0.75f + ox,
                            y + Geometry.d4y(dir) * tilesize * 0.75f + oy,
                            0, rot, i != 0 ? SliceMode.bottom : SliceMode.top,
                            currentColor, currentAlpha, shadowWeight);
                }
            }

            // Draw item
            if(current != null){
                Draw.z(Layer.blockUnder + 0.1f);

                // Item tinting: blend item color with the current transition state
                Draw.color(current.color);
                Draw.color(Draw.getColor().mul(currentColor));
                Draw.alpha(currentAlpha);

                Tmp.v1.set(Geometry.d4x(recDir) * tilesize / 2f, Geometry.d4y(recDir) * tilesize / 2f)
                        .lerp(Geometry.d4x(r) * tilesize / 2f, Geometry.d4y(r) * tilesize / 2f,
                                Mathf.clamp((progress + 1f) / (2f - 1f / speed)));

                Draw.rect(current.fullIcon, x + Tmp.v1.x + ox, y + Tmp.v1.y + oy, itemSize, itemSize);
            }

            // Draw main body
            Draw.scl(xscl, yscl);
            drawAt(x + ox, y + oy, blendbits, rotation, SliceMode.none, currentColor, currentAlpha, shadowWeight);
            Draw.reset();
        }

        // Updated drawAt to accept transition parameters
        protected void drawAt(float x, float y, int bits, float rotation, SliceMode slice, Color stateCol, float stateAlpha, float shadowWeight){
            Draw.z(Layer.blockUnder);
            Draw.color(stateCol);
            Draw.alpha(stateAlpha);
            Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);

            // Transparent layer (Fades out as it becomes a shadow)
            float glassAlpha = 1f - shadowWeight;
            if(glassAlpha > 0.01f){
                Draw.z(Layer.blockUnder + 0.2f);
                Draw.color(transparentColor);
                Draw.color(Draw.getColor().mul(stateCol));
                Draw.alpha(stateAlpha * glassAlpha);
                Draw.rect(sliced(botRegions[bits], slice), x, y, rotation);
            }

            // Top casing
            Draw.z(Layer.blockUnder + 0.3f);
            Draw.color(stateCol);
            Draw.alpha(stateAlpha);
            Draw.rect(sliced(topRegions[bits], slice), x, y, rotation);
        }



        @Override
        public void drawLight() {
            if (waterBlock == LayerManager.isPlayerSubmerged()) {
                super.drawLight();
            }
        }

    }


    @Override
    public void setStats() {
        super.setStats();
        if (faction.any()) {
            stats.add(AxStats.faction, Core.bundle.get("team." + faction.peek().name));
        }
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        fullIcon = Core.atlas.find(name + "-full", fullIcon);
        uiIcon = Core.atlas.find(name + "-ui", fullIcon);
    }
}
