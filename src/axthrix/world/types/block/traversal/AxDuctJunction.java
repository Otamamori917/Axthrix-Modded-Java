package axthrix.world.types.block.traversal;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.AxFaction;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.util.AxStats;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.distribution.DuctJunction;

import static mindustry.Vars.itemSize;
import static mindustry.Vars.tilesize;

public class AxDuctJunction extends DuctJunction {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public boolean waterBlock;

    public Seq<AxFaction> faction = new Seq<>();

    public static java.lang.reflect.Field itemDataField, timesField;

    static{
        try{
            // We reach into the vanilla class to force access to the hidden data
            itemDataField = mindustry.world.blocks.distribution.DuctJunction.DuctJunctionBuild.class.getDeclaredField("itemdata");
            itemDataField.setAccessible(true);
            timesField = mindustry.world.blocks.distribution.DuctJunction.DuctJunctionBuild.class.getDeclaredField("times");
            timesField.setAccessible(true);
        }catch(Exception e){
            Log.err("failed to grab", e);
        }
    }

    public AxDuctJunction(String name) {
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





    public class AxDuctJunctionBuild extends DuctJunctionBuild {

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

            // 3. Draw Bottom
            Draw.rect(bottomRegion, x + ox, y + oy);

            // 4. Moving Items (Apply state tint + shadow logic)
            try {
                Item[] items = (Item[]) itemDataField.get(this);
                float[] t = (float[]) timesField.get(this);

                for (int i = 0; i < 4; i++) {
                    if (items[i] != null) {
                        float progress = (Mathf.clamp((t[i] + 1f) / (2f - 1f / speed)) - 0.5f) * 2f;

                        // Items become silhouettes in shadow mode
                        Draw.color(items[i].color);
                        Draw.color(Draw.getColor().mul(currentColor));
                        Draw.alpha(currentAlpha);

                        Draw.rect(items[i].fullIcon,
                                x + Geometry.d4x(i) * tilesize / 2f * progress + ox,
                                y + Geometry.d4y(i) * tilesize / 2f * progress + oy,
                                itemSize, itemSize);
                    }
                }
            } catch (Exception ignored) {}

            // Restore state for the top parts
            Draw.color(currentColor);
            Draw.alpha(currentAlpha);

            // 5. Transparent Middle (Fades out in shadow mode)
            float middleAlpha = 1f - shadowWeight;
            if (middleAlpha > 0.01f) {
                Draw.color(transparentColor);
                Draw.color(Draw.getColor().mul(currentColor));
                Draw.alpha(currentAlpha * middleAlpha);
                Draw.rect(bottomRegion, x + ox, y + oy);
            }

            // 6. Draw Top
            Draw.color(currentColor);
            Draw.alpha(currentAlpha);
            Draw.rect(topRegion, x + ox, y + oy);

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
