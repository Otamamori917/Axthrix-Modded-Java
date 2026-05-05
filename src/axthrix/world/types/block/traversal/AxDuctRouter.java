package axthrix.world.types.block.traversal;

import arc.Core;
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
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.DuctRouter;

public class AxDuctRouter extends DuctRouter {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public boolean waterBlock;

    public Seq<AxFaction> faction = new Seq<>();

    public AxDuctRouter(String name) {
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





    public class AxDuctRouterBuild extends DuctRouterBuild {

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

            // 2. Prepare Drawing State
            float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
            float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

            // LERP: Base Color (White -> Black) and Alpha (1.0 -> 0.25)
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

            // 3. Draw Base Region
            Draw.rect(region, x + ox, y + oy);

            // 4. Draw Top/Center
            // We only draw the "center" (item color) if it's NOT a shadow.
            // As it turns into a shadow, we fade it out and reveal the topRegion silhouette instead.
            float centerAlpha = 1f - shadowWeight;

            if (sortItem != null) {
                // Draw the pure-color center only when visible (not shadow)
                if (centerAlpha > 0.01f) {
                    Draw.color(sortItem.color);
                    // Tint item color by current state
                    Draw.color(Draw.getColor().mul(currentColor));
                    Draw.alpha(currentAlpha * centerAlpha);
                    Draw.rect("center", x + ox, y + oy);
                }

                // When becoming a shadow, the center disappears, but we don't want a "hole,"
                // so we can draw the topRegion as the silhouette.
                if (shadowWeight > 0.5f) {
                    Draw.color(currentColor);
                    Draw.alpha(currentAlpha * shadowWeight);
                    Draw.rect(topRegion, x + ox, y + oy, rotdeg());
                }
            } else {
                // No item: Just draw the standard top rotation
                Draw.rect(topRegion, x + ox, y + oy, rotdeg());
            }

            Draw.reset();
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
