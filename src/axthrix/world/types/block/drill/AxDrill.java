package axthrix.world.types.block.drill;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.AxFaction;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.util.AxStats;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;

public class AxDrill extends Drill {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public boolean waterBlock;

    public Seq<AxFaction> faction = new Seq<>();

    public AxDrill(String name) {
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





    public class AxDrillBuild extends DrillBuild {

        @Override
        public void draw() {
            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();
            boolean onLiquid = tile.floor().isLiquid;

            // 1. Core Logic: How much "Shadow" should this block be right now?
            // This value now slides smoothly from 0.0 (Block) to 1.0 (Shadow)
            float shadowWeight = 0f;
            if(waterBlock){
                // Water blocks are shadows when NOT submerged.
                // As playerSub goes to 1 (submerging), shadowWeight goes to 0 (becoming block)
                shadowWeight = 1f - intensity;
            } else if(onLiquid){
                // Land blocks on liquid are shadows ONLY when submerged.
                // As playerSub goes to 1 (submerging), shadowWeight goes to 1 (becoming shadow)
                shadowWeight = intensity;
            }

            // 2. Prepare Drawing State & Transition Logic
            // Apply jiggle based on the current shadowWeight
            float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
            float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

            // LERP Color: White (Normal) -> Black (Shadow)
            Draw.color(Color.white, Color.black, shadowWeight);
            // LERP Alpha: 1.0 (Normal) -> 0.25 (Shadow)
            Draw.alpha(Mathf.lerp(1f, 0.25f, shadowWeight));

            // Special case: If it's a water block and we ARE submerged, apply the floor tint
            if(waterBlock && playerSub){
                Color floorCol = tile.floor().mapColor;
                // reuse Tmp to avoid garbage collection
                Tmp.c1.set(Draw.getColor()).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
                Draw.color(Tmp.c1);
            }

            // Capture final state for sub-components
            Color currentColor = Tmp.c3.set(Draw.getColor());
            float currentAlpha = Draw.getColorAlpha();

            // 3. Drill Drawing Logic
            float s = 0.3f;
            float ts = 0.6f;

            // Base Layer
            Draw.rect(region, x + ox, y + oy);

            // Cracks (Smoothly fade with alpha)
            if(currentAlpha > 0.7f){
                Draw.z(Layer.blockCracks);
                drawDefaultCracks();
                Draw.z(Layer.blockAfterCracks);
            }

            // Heat Rim (Fade it out entirely as it becomes a shadow)
            if(drawRim){
                Draw.color(heatColor);
                // We multiply by (1 - shadowWeight) so the glow disappears as it turns into a shadow
                Draw.alpha(warmup * ts * (1f - s + Mathf.absin(Time.time, 3f, s)) * (1f - shadowWeight));
                Draw.blend(Blending.additive);
                Draw.rect(rimRegion, x + ox, y + oy);
                Draw.blend();
                Draw.color(currentColor);
                Draw.alpha(currentAlpha);
            }

            // Rotator Layer
            if(drawSpinSprite){
                Drawf.spinSprite(rotatorRegion, x + ox, y + oy, timeDrilled * rotateSpeed);
            } else {
                Draw.rect(rotatorRegion, x + ox, y + oy, timeDrilled * rotateSpeed);
            }

            // Top Layer
            Draw.rect(topRegion, x + ox, y + oy);

            // Mined Item
            if(dominantItem != null && drawMineItem){
                Draw.color(dominantItem.color);
                Draw.color(Draw.getColor().mul(currentColor));
                Draw.alpha(currentAlpha);
                Draw.rect(itemRegion, x + ox, y + oy);
            }

            Draw.reset();
        }




        @Override
        public void updateTile() {
            super.updateTile();


            float range = (block.size * 4f) + 4f;

            mindustry.gen.Groups.unit.intersect(x - range, y - range, range * 2, range * 2, unit -> {



                if (waterBlock == LayerManager.submergedUnits.contains(unit)){
                    if(!unit.isFlying()) applyBlockBounce(unit);
                }
            });
        }

        private void applyBlockBounce(Unit unit) {
            float dx = unit.x - x;
            float dy = unit.y - y;
            float len = Mathf.len(dx, dy);
            float minLen = (block.size * 4f) + (unit.hitSize / 2f);

            if (len < minLen) {
                if (len == 0) { dx = 0.1f; len = 0.1f; }
                dx /= len;
                dy /= len;


                float push = (minLen - len) + 0.2f;
                unit.vel.add(dx * push * 0.1f, dy * push * 0.1f);
                unit.x += dx * push;
                unit.y += dy * push;
            }
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
}
