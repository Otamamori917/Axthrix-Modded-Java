package axthrix.world.types.sea.block;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.AxFaction;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.util.AxStats;
import mindustry.content.Blocks;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.part.RegionPart;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.draw.DrawTurret;

public class SeaTurret extends Turret {
    /** Whether this turret exists in the submerged layer. */
    public boolean waterBlock;
    /** If true, this turret can target units on both the surface AND underwater. */
    public boolean seeOutsideLayer = false;
    /** Factions this block belongs to. */
    public Seq<AxFaction> faction = new Seq<>();

    public SeaTurret(String name) {
        super(name);
        update = true;
        solid = false;
        hasShadow = false;
    }

    @Override
    public void init() {
        super.init();
        if (waterBlock) {
            floating = true;
            placeableLiquid = true;
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        if (faction.any()) {
            stats.add(AxStats.faction, Core.bundle.get("team." + faction.peek().name));
        }

        stats.add(mindustry.world.meta.Stat.abilities, waterBlock ? "[blue]Submerged Only[]" : "[accent]Surface Only[]");
        if (seeOutsideLayer) {
            stats.add(mindustry.world.meta.Stat.shootRange, "Can target multiple layers");
        }
    }

    @Override
    public boolean canBreak(Tile tile) {
        return waterBlock == LayerManager.isPlayerSubmerged();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile == null) return false;
        if (waterBlock) {
            return LayerManager.isPlayerSubmerged() &&
                    (tile.floor() == AxthrixEnvironment.tharaxianDeep || tile.floor() == Blocks.deepwater);
        }
        return !LayerManager.isPlayerSubmerged() && super.canPlaceOn(tile, team, rotation);
    }

    public class SeaTurretBuild extends TurretBuild {
        @Override
        public void draw() {
            if(AxthrixLoader.drawEnchancedShadows){
                super.draw();
                return;
            }

            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();
            boolean onLiquid = tile.floor().isLiquid;

            // 1. Identify States (Unified shadow logic)
            boolean isShadow = (waterBlock && !playerSub) || (!waterBlock && playerSub && onLiquid);
            boolean isNativeWater = waterBlock && playerSub;
            float shadowWeight = isShadow ? (playerSub ? intensity : (1f - intensity)) : 0f;

            if (isShadow) {
                float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
                float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

                Draw.color(Color.black);
                // We split the alpha across 5 draws so they add up to roughly 0.25
                float blurAlpha = (0.25f * shadowWeight) / 3f;
                float offset = 0.8f; // How "blurry" it is. Lower = sharper, Higher = softer.

                if (drawer instanceof DrawTurret dt) {
                    // 1. Draw the "Soft Edges" (the blur)
                    Draw.alpha(blurAlpha);
                    // Draw shifted copies
                    Draw.rect(dt.preview, x + recoilOffset.x + ox + offset, y + recoilOffset.y + oy + offset, drawrot());
                    Draw.rect(dt.preview, x + recoilOffset.x + ox - offset, y + recoilOffset.y + oy - offset, drawrot());

                    // 2. Draw the "Core" (the center)
                    Draw.alpha(0.25f * shadowWeight);
                    Draw.rect(dt.base, x + ox, y + oy);
                    Draw.alpha(0.15f * shadowWeight);
                    Draw.rect(dt.preview, x + recoilOffset.x + ox, y + recoilOffset.y + oy, drawrot());
                }
                Draw.reset();
            }
            else if (isNativeWater) {
                // STATE: NATIVE UNDERWATER (Hue shifted floor tint)
                Color floorCol = tile.floor().mapColor;
                Tmp.c1.set(Color.white).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);

                Draw.color(Tmp.c1);
                super.draw(); // Standard drawing with tint
                Draw.reset();

            } else {
                // STATE: NORMAL (Surface/Land)
                super.draw();
            }
        }



        @Override
        public void updateTile() {
            super.updateTile();

            float range = (block.size * 4f) + 4f;
            Groups.unit.intersect(x - range, y - range, range * 2, range * 2, unit -> {
                if (waterBlock == LayerManager.isSubmerged(unit)) {
                    if (!unit.isFlying()) applyBlockBounce(unit);
                }
            });
        }

        private void applyBlockBounce(Unit unit) {
            float dx = unit.x - x, dy = unit.y - y;
            float len = Mathf.len(dx, dy);
            float minLen = (block.size * 4f) + (unit.hitSize / 2f);

            if (len < minLen) {
                if (len == 0) { dx = 0.1f; len = 0.1f; }
                dx /= len; dy /= len;
                float push = (minLen - len) + 0.2f;
                unit.vel.add(dx * push * 0.1f, dy * push * 0.1f);
                unit.x += dx * push; unit.y += dy * push;
            }
        }


        @Override
        protected void findTarget() {
            target = null;
            final float[] closest = {range + 1f}; // Track the closest distance

            Units.nearbyEnemies(this.team, x, y, range, other -> {
                float distance = dst(other);

                if (SeaDetection(other)) {
                    float priorityDist = other.type.coreUnitDock ? -1f : distance;

                    if (priorityDist < closest[0]) {
                        target = other;
                        closest[0] = priorityDist;
                    }
                }
            });

            if (target == null) {
                Units.nearbyBuildings(x, y, range, other -> {
                    float distance = dst(other);

                    if (other.team != this.team && SeaDetection(other)) {
                        boolean isCore = other instanceof CoreBlock.CoreBuild;
                        float priorityDist = isCore ? -1f : distance;

                        if (priorityDist < closest[0]) {
                            target = other;
                            closest[0] = priorityDist;
                        }
                    }
                });
            }
        }


        public boolean SeaDetection(Posc target){
            if (waterBlock == LayerManager.isSubmerged(target)){
                return true;
            }
            return seeOutsideLayer;
        }


        @Override
        protected boolean validateTarget() {
            boolean valid = !invalidateTarget(target, canHeal() ? Team.derelict : team, x, y, range) || isControlled() || logicControlled();
            if (seeOutsideLayer) return valid;

            if (valid) {
                return LayerManager.isSubmerged(target) == waterBlock;
            }
            return valid;
        }

        public boolean invalidateTarget(Posc target, Team team, float x, float y, float range){

            return target == null ||
                    (range != Float.MAX_VALUE && !target.within(x, y, range + (target instanceof Sized hb ? hb.hitSize()/2f : 0f))) ||
                    (target instanceof Teamc t && t.team() == team) ||
                    (target instanceof Healthc h && !h.isValid()) ||
                    (target instanceof Unit u && !canSee(team,u));
        }

        public boolean canSee(Team team, Unit unit){
            return LayerManager.isSubmerged(unit) || unit.targetable(team);
        }
    }
}
