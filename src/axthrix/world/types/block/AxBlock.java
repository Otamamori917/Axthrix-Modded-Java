package axthrix.world.types.block;

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
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.world.Block;
import mindustry.world.Tile;

public class AxBlock extends Block {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public boolean waterBlock;

    public Seq<AxFaction> faction = new Seq<>();

    public AxBlock(String name) {
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





    public abstract class AxBlockBuild extends Building {

        @Override
        public void draw() {
            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();
            boolean onLiquid = tile.floor().isLiquid;

            // 1. Unified Transition Logic (0 = Block, 1 = Shadow)
            float shadowWeight = 0f;
            if (waterBlock) {
                // Water blocks turn into shadows as you surface (intensity -> 0)
                shadowWeight = 1f - intensity;
            } else if (onLiquid) {
                // Land blocks on liquid turn into shadows as you submerge (intensity -> 1)
                shadowWeight = intensity;
            }

            // 2. Prepare Drawing State
            // Jiggle scales with how "shadowy" the block is
            float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
            float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

            // LERP: Base Color (White -> Black) and Alpha (1.0 -> 0.25)
            Draw.color(Color.white, Color.black, shadowWeight);
            Draw.alpha(Mathf.lerp(1f, 0.25f, shadowWeight));

            // Handle Native Underwater Tinting (If it's a water block and we are submerged)
            if (waterBlock && playerSub) {
                Color floorCol = tile.floor().mapColor;
                // Tmp.c1 saves the current hue-shift for the team top later
                Tmp.c1.set(Draw.getColor()).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
                Draw.color(Tmp.c1);
            }

            // Save current color state for components
            Color currentColor = Tmp.c3.set(Draw.getColor());
            float currentAlpha = Draw.getColorAlpha();

            // 3. Main Block Drawing
            if (block.variants != 0 && block.variantRegions != null) {
                Draw.rect(block.variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, block.variantRegions.length - 1))], x + ox, y + oy, drawrot());
            } else {
                Draw.rect(block.region, x + ox, y + oy, drawrot());
            }

            // 4. Team Top (Smoothly fades out as it turns into a shadow)
            if (block.teamRegion.found()) {
                // We multiply team alpha by (1 - shadowWeight) so it disappears as the shadow turns black
                float teamAlpha = currentAlpha * (1f - shadowWeight);

                if (teamAlpha > 0.01f) {
                    Color baseTeam = (block.teamRegions[team.id] == block.teamRegion) ? team.color : Color.white;
                    Draw.color(baseTeam);

                    // Apply the hue-shift if we are native water
                    if (waterBlock && playerSub) {
                        Draw.color(Draw.getColor().mul(currentColor));
                    }

                    Draw.alpha(teamAlpha);
                    Draw.rect(block.teamRegions[team.id], x + ox, y + oy, drawrot());
                }
            }

            Draw.reset();
        }







        @Override
        public boolean checkSolid() {
            return false;
        }

        @Override
        public void updateTile() {
            super.updateTile();


            float range = (block.size * 4f) + 4f;

            mindustry.gen.Groups.unit.intersect(x - range, y - range, range * 2, range * 2, unit -> {



                if (waterBlock == LayerManager.submergedUnits.contains(unit)){
                    if(!unit.isFlying()) CustCollision(unit);
                }
            });
        }

        public void CustCollision(Unit unit) {
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

    @Override
    public void loadIcon(){
        super.loadIcon();
        fullIcon = Core.atlas.find(name + "-full",fullIcon);
        uiIcon = Core.atlas.find(name + "-ui",fullIcon);
    }
}
