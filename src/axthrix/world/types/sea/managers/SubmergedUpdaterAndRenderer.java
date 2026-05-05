package axthrix.world.types.sea.managers;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.Element;
import arc.struct.Seq;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.AxLayers;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.sea.block.SeaTurret;
import axthrix.world.types.sea.block.SubmergedOre;
import axthrix.world.types.sea.unit.SubmarineUnitType;
import mindustry.*;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Shaders;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.environment.Floor;

import static mindustry.Vars.*;

public class SubmergedUpdaterAndRenderer {

    public static void init() {
        ///  draws the visuals of underwater
        Events.run(EventType.Trigger.draw, () -> {
            if (LayerManager.shaderIntensity <= 0.005f) return;
            float intensity = LayerManager.shaderIntensity;
            float fade = Mathf.curve(intensity, 0.05f, 1f);

            int x1 = (int)((Core.camera.position.x - Core.camera.width / 2f) / 8) - 1;
            int y1 = (int)((Core.camera.position.y - Core.camera.height / 2f) / 8) - 1;
            int x2 = (int)((Core.camera.position.x + Core.camera.width / 2f) / 8) + 1;
            int y2 = (int)((Core.camera.position.y + Core.camera.height / 2f) / 8) + 1;


            ///  sand
            if(AxthrixLoader.drawUnderwaterSand){
                Draw.z(Layer.floor + 0.00001f);
                for (int x = x1; x <= x2; x++) {
                    for (int y = y1; y <= y2; y++) {
                        Tile tile = world.tile(x, y);
                        if (tile == null || !tile.floor().isLiquid) continue;

                        Floor f = (Floor)AxthrixEnvironment.crimsonSandFloor;
                        if (tile.floor() == Blocks.deepwater || tile.floor() == AxthrixEnvironment.tharaxianDeep) {
                            f = (Floor)AxthrixEnvironment.crimsonSandDeepFloor;
                        }

                        TextureRegion region = f.variantRegions[Mathf.randomSeed(tile.pos(), 0, f.variants - 1)];

                        Tmp.c1.set(Color.white).lerp(tile.getFloorColor(), fade);
                        Draw.color(Tmp.c1, fade);
                        Draw.rect(region, x * 8, y * 8);
                    }
                }
                Draw.reset();
            }


            ///  wavy underwater shader
            Draw.draw(AxLayers.underWaterLayer, () -> {

                Draw.color(Color.valueOf("0f0520"), 0.45f * fade);
                Fill.rect(Core.camera.position.x, Core.camera.position.y, Core.camera.width, Core.camera.height);

                if(renderer.animateWater){
                    Blending.additive.apply();
                    Draw.color(Color.white, fade);
                    Draw.blit(Shaders.caustics);
                    Blending.normal.apply();
                }
                Draw.reset();
            });

            ///  floor shade
            if(AxthrixLoader.drawUnderwaterVoid){
                Draw.z(Layer.fogOfWar);
                drawVoidInternal(fade, x1, y1, x2, y2);
            }

        });

        /// draw underwater static/env elements (there's literally no other way to do it bruh)
        Events.run(EventType.Trigger.draw, () -> {
            if (LayerManager.shaderIntensity <= 0.001f && !LayerManager.isPlayerSubmerged()) return;
            float intensity = LayerManager.shaderIntensity;
            int minX = Mathf.floor(Core.camera.position.x / tilesize - Core.graphics.getWidth() / tilesize / 2f) - 2;
            int minY = Mathf.floor(Core.camera.position.y / tilesize - Core.graphics.getHeight() / tilesize / 2f) - 2;
            int maxX = Mathf.ceil(Core.camera.position.x / tilesize + Core.graphics.getWidth() / tilesize / 2f) + 2;
            int maxY = Mathf.ceil(Core.camera.position.y / tilesize + Core.graphics.getHeight() / tilesize / 2f) + 2;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    Tile tile = world.tile(x, y);
                    if (tile != null && tile.overlay() instanceof SubmergedOre ore) {
                        Draw.z(Layer.floor + 0.01f);
                        Color floorCol = tile.floor().mapColor;
                        Tmp.c1.set(Color.white).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
                        Draw.color(Tmp.c1);
                        Draw.alpha(intensity);
                        int variant = Mathf.randomSeed(tile.pos(), 0, Math.max(0, ore.variantRegions.length - 1));
                        Draw.rect(ore.variantRegions[variant], tile.worldx(), tile.worldy());
                    }
                }
            }
            Draw.reset();
        });

        /// updater
        Events.on(EventType.WorldLoadEvent.class, e -> {
            UnderwaterZone.scan();
            LayerManager.clear();
        });

        Events.run(EventType.Trigger.update, () -> {
            /// Underwater layer shader fade updater
            if (!Vars.headless) {
                LayerManager.update();
            }

            /// Collision updater
            LayerManager.submergedUnits.each(unit -> {
                if(unit.isValid()){
                    boolean isSubmerged = LayerManager.submergedUnits.contains(unit);
                    Tile tile = unit.tileOn();
                    if (tile == null) return;

                    if (isSubmerged) {
                        if (tile.floor() != AxthrixEnvironment.tharaxianDeep && tile.floor() != Blocks.deepwater) {
                            float dx = unit.x - tile.worldx(), dy = unit.y - tile.worldy();
                            float len = Mathf.len(dx, dy);
                            if (len > 0) {
                                dx /= len; dy /= len;
                                float dot = unit.vel.x * dx + unit.vel.y * dy;
                                if (dot < 0) unit.vel.sub(dx * dot, dy * dot);
                                unit.vel.add(dx * 0.4f, dy * 0.4f);
                                unit.x += dx * 0.2f; unit.y += dy * 0.2f;
                            }
                        }
                    } else {
                        if (!tile.floor().isLiquid) {
                            float dx = unit.x - tile.worldx(), dy = unit.y - tile.worldy();
                            float len = Mathf.len(dx, dy);
                            if (len > 0) {
                                dx /= len; dy /= len;
                                float dot = unit.vel.x * dx + unit.vel.y * dy;
                                if (dot < 0) unit.vel.sub(dx * dot, dy * dot);
                                unit.vel.add(dx * 0.4f, dy * 0.4f);
                                unit.x += dx * 0.2f; unit.y += dy * 0.2f;
                            }
                        }
                    }
                }
            });

            /// submarine mobile button
            if (Vars.ui == null || Vars.player.unit() == null) return;
            Element existing = Vars.ui.hudGroup.find("submarine-dive-button");
            if (existing == null) {
                Vars.ui.hudGroup.fill(t -> {
                    t.name = "submarine-dive-button";
                    t.bottom().left();


                    t.update(() -> {
                        float offsetY = 0f;
                        float offsetX = 0f;

                        //Time Control
                        if(Vars.mods.getMod("time-control") != null && Vars.mods.getMod("time-control").enabled()){
                            offsetY += 80f;
                        }

                        //Testing Utilities
                        if(Vars.mods.getMod("test-utils") != null && Vars.mods.getMod("test-utils").enabled()){
                            offsetY += 5f;
                            offsetX += 10f;
                        }

                        t.marginBottom(offsetY);
                        t.marginLeft(offsetX);
                    });

                    // Visibility: (Is Sub) AND (Is Mobile OR Desktop Setting Enabled)
                    t.visible(() -> Vars.player.unit() != null &&
                            Vars.player.unit().type instanceof SubmarineUnitType &&
                            (Vars.mobile || AxthrixLoader.showMobileDiveButton));

                    t.button(b -> {
                        b.label(() -> LayerManager.isPlayerSubmerged() ? "SURFACE" : "DIVE").pad(10);
                    }, Styles.flatBordert, () -> {
                        Unit unit = Vars.player.unit();
                        Tile currentTile = unit.tileOn();

                        if (!LayerManager.isPlayerSubmerged()) {
                            if (currentTile == null || currentTile.floor().name.contains("shallow") || LayerManager.isShallow(currentTile)) {
                                Vars.ui.showInfoToast("Water too shallow to dive!", 2f);
                                return;
                            }
                        }

                        boolean isSub = LayerManager.submergedUnits.contains(unit);
                        LayerManager.setSubmerged(unit, !isSub);
                        Fx.bubble.at(unit.x, unit.y);
                    }).size(140f, 50f);
                });
            }
        });

    }
    /// void shade with certain turret implementations
    private static void drawVoidInternal(float fade, int x1, int y1, int x2, int y2) {
        Seq<SeaTurret.SeaTurretBuild> scanners = new Seq<>();

        indexer.eachBlock(null, Core.camera.position.x, Core.camera.position.y, Core.camera.width,
                b -> b instanceof SeaTurret.SeaTurretBuild st && ((SeaTurret)st.block).seeOutsideLayer,
                b -> scanners.add((SeaTurret.SeaTurretBuild)b)
        );

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                Tile tile = world.tile(x, y);
                if (tile == null || tile.floor().isLiquid) continue;

                float worldX = x * 8, worldY = y * 8;
                float minAlpha = 1f;

                for (var st : scanners) {
                    float turretRange = ((Turret)st.block).range;
                    float dst = Mathf.dst(st.x, st.y, worldX, worldY);
                    if (dst < turretRange) {
                        float alpha = Mathf.clamp(0.25f + (dst / turretRange) * 0.75f);
                        if (alpha < minAlpha) minAlpha = alpha;
                    }
                }

                if (minAlpha > 0) {
                    Draw.color(Color.black, minAlpha * fade);
                    Fill.rect(worldX, worldY, 8, 8);
                }
            }
        }
        Draw.reset();
    }
}
