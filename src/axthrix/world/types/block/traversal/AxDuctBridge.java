package axthrix.world.types.block.traversal;


import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
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
import mindustry.core.Renderer;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.DuctBridge;

import static mindustry.Vars.tilesize;

public class AxDuctBridge extends DuctBridge {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public boolean waterBlock;

    public Seq<AxFaction> faction = new Seq<>();

    public AxDuctBridge(String name) {
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





    public class AxDuctBridgeBuild extends DuctBridgeBuild {

        @Override
        public void draw() {
            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();
            boolean onLiquid = tile.floor().isLiquid;

            // 1. Unified Transition Logic (0 = Block, 1 = Shadow)
            float shadowWeight = 0f;
            if (waterBlock) {
                shadowWeight = 1f - intensity;
            } else if (onLiquid) {
                shadowWeight = intensity;
            }

            // 2. Prepare Shared Visuals
            float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
            float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

            // LERP Color & Alpha
            Draw.color(Color.white, Color.black, shadowWeight);
            Draw.alpha(Mathf.lerp(1f, 0.25f, shadowWeight));

            // Native Underwater Tinting
            if (waterBlock && playerSub) {
                Color floorCol = tile.floor().mapColor;
                Tmp.c1.set(Draw.getColor()).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
                Draw.color(Tmp.c1);
            }

            Color currentColor = Tmp.c3.set(Draw.getColor());
            float currentAlpha = Draw.getColorAlpha();

            // 3. Draw The Bridge Logic
            if (shadowWeight < 0.2f) {
                // Mostly normal block - use standard super.draw() with jiggle offset
                float oldX = x, oldY = y;
                x += ox; y += oy;
                super.draw();
                x = oldX; y = oldY;
            } else {
                // Transitioning or Shadow mode
                // Base and Direction regions
                Draw.rect(block.region, x + ox, y + oy);
                Draw.rect(dirRegion, x + ox, y + oy, rotdeg());

                var link = findLink();
                if (link != null) {
                    float x1 = x, y1 = y, x2 = link.x, y2 = link.y;
                    float opacity = Renderer.bridgeOpacity;
                    float angle = Angles.angle(x1, y1, x2, y2);
                    float cx = (x1 + x2) / 2f + ox;
                    float cy = (y1 + y2) / 2f + oy;
                    float len = Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) - size * tilesize;

                    // Apply opacity scaled by current transition alpha
                    Draw.alpha(opacity * currentAlpha);

                    // Draw Bridge Span
                    Draw.rect(bridgeRegion, cx, cy, len, bridgeRegion.height * bridgeRegion.scl(), angle);
                    if (bridgeBotRegion.found()) {
                        Draw.rect(bridgeBotRegion, cx, cy, len, bridgeBotRegion.height * bridgeBotRegion.scl(), angle);
                    }

                    // Draw Flow Arrows (only if not too shadowy)
                    if (shadowWeight < 0.9f) {
                        for (float i = 6f; i <= len + size * tilesize - 5f; i += 5f) {
                            Draw.rect(arrowRegion,
                                    x1 + Geometry.d4x(rotation) * i + ox,
                                    y1 + Geometry.d4y(rotation) * i + oy,
                                    angle);
                        }
                    }
                }
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
