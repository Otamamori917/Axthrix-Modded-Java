package axthrix.content.blocks;

import arc.graphics.Color;
import axthrix.content.AxItems;
import axthrix.world.types.block.ObeliskBlock;
import axthrix.world.types.block.env.TemperatureFloor;
import axthrix.world.types.sea.block.SubmergedOre;
import mindustry.content.StatusEffects;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.blocks.environment.OreBlock;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.BuildVisibility;

import static mindustry.type.ItemStack.with;

public class AxthrixEnvironment {
    public static Block
            // Crimson sand tiles (sprited)
            crimsonSandFloor, crimsonSandDeepFloor, crimsonSandDarkSlope, crimsonSandLightSlope, nest,

    // Temperature floors
    lavaFloor, iceFloor,

    // Tharaxia terrain floors
    darkBasalt, tharaxianStone, deepSlate,
            ferricStreak, tharaxianMoss, voidBloom,

    // Tharaxia ocean floors
    tharaxianShallows, tharaxianDeep,

    //Tharaxia ocean ores
    testOceanOre,

    // Tharaxia walls
    darkBasaltWall, tharaxianStoneWall, deepSlateWall,

    // Lore
    obelisk;

    public static void load() {

        // ---- LORE ----

        obelisk = new ObeliskBlock("obelisk") {{
            requirements(Category.effect, BuildVisibility.sandboxOnly, with());
            alwaysUnlocked = true;
        }};

        // ---- CRIMSON SAND (sprited) ----

        crimsonSandFloor = new Floor("crimson-sand-floor") {{
            supportsOverlay = true;
            variants = 3;
        }};

        crimsonSandDeepFloor = new Floor("crimson-sand-deep-floor") {{
            supportsOverlay = true;
            variants = 3;
        }};

        crimsonSandDarkSlope = new Floor("crimson-sand-slope-dark") {{
            supportsOverlay = true;
            variants = 3;
        }};

        crimsonSandLightSlope = new Floor("crimson-sand-slope-light") {{
            supportsOverlay = true;
            variants = 3;
        }};

        nest = new Floor("nest") {{
            needsSurface = true;
        }};

        // ---- TEMPERATURE FLOORS ----

        lavaFloor = new TemperatureFloor("lava") {{
            temperaturePerSecond = 10f;
            damageTaken = 2f;
            status = StatusEffects.melting;
            statusDuration = 240f;
            speedMultiplier = 0.19f;
            variants = 0;
            attributes.set(Attribute.heat, 0.85f);
            emitLight = true;
            lightRadius = 40f;
            lightColor = Color.orange.cpy().a(0.38f);
            obstructsLight = true;
            forceDrawLight = true;
        }};

        iceFloor = new TemperatureFloor("ice") {{
            temperaturePerSecond = -10f;
            dragMultiplier = 0.35f;
            speedMultiplier = 0.9f;
            attributes.set(Attribute.water, 0.4f);
            albedo = 0.65f;
        }};

        // ---- THARAXIA TERRAIN FLOORS ----

        // Dark Basalt — main land tile
        // Mid: #2a2a35 | Dark: #1a1a22 | Light: #3d3d4a
        darkBasalt = new Floor("tharaxia-dark-basalt") {{
            variants = 3;
            supportsOverlay = true;
            attributes.set(Attribute.water, 0f);
        }};

        // Tharaxian Stone — mid terrain
        // Mid: #363640 | Dark: #222228 | Light: #4a4a56
        tharaxianStone = new Floor("tharaxia-stone") {{
            variants = 3;
            supportsOverlay = true;
        }};

        // Deep Slate — high terrain / peaks
        // Mid: #1e1e2a | Dark: #111118 | Light: #2d2d3d
        deepSlate = new Floor("tharaxia-deep-slate") {{
            variants = 2;
            supportsOverlay = true;
        }};

        // Ferric Streak — orange vein patches
        // Mid: #7a3d18 | Dark: #4d2610 | Light: #9e5228
        ferricStreak = new Floor("tharaxia-ferric-streak") {{
            variants = 2;
            supportsOverlay = true;
        }};

        // Tharaxian Moss — dark green wildlife patches
        // Mid: #1a3320 | Dark: #0f1f14 | Light: #284d30
        tharaxianMoss = new Floor("tharaxia-moss") {{
            variants = 2;
            supportsOverlay = true;
            attributes.set(Attribute.water, 0.1f);
        }};

        // Void Bloom — deep purple wildlife patches
        // Mid: #2d1545 | Dark: #1a0a2a | Light: #3d2058
        voidBloom = new Floor("tharaxia-void-bloom") {{
            variants = 2;
            supportsOverlay = true;
        }};

        // ---- THARAXIA OCEAN FLOORS ----

        // Tharaxian Shallows — shallow ocean
        // Mid: #1e0f3d | Dark: #12082a | Light: #2d1a52
        tharaxianShallows = new Floor("tharaxia-shallows") {{
            variants = 3;
            isLiquid = true;
            drownTime = 150f;
            speedMultiplier = 0.6f;
            attributes.set(Attribute.water, 1f);
            liquidDrop = mindustry.content.Liquids.water;
            supportsOverlay = false;
            cacheLayer = mindustry.graphics.CacheLayer.water;
        }};

        // Tharaxian Deep — deep ocean
        // Mid: #0f0520 | Dark: #080312 | Light: #1a0a35
        tharaxianDeep = new Floor("tharaxia-deep") {{
            variants = 3;
            isLiquid = true;
            drownTime = 80f;
            speedMultiplier = 0.3f;
            attributes.set(Attribute.water, 1f);
            liquidDrop = mindustry.content.Liquids.water;
            supportsOverlay = false;
            cacheLayer = mindustry.graphics.CacheLayer.water;
            status = StatusEffects.wet;
            statusDuration = 120f;
        }};

        // Ocean Ores

        testOceanOre = new SubmergedOre("sea-ore", AxItems.sulfur){{
            variants = 3;
        }};

        // ---- THARAXIA WALLS ----

        darkBasaltWall = new StaticWall("tharaxia-dark-basalt-wall") {{
            variants = 2;
        }};

        tharaxianStoneWall = new StaticWall("tharaxia-stone-wall") {{
            variants = 2;
        }};

        deepSlateWall = new StaticWall("tharaxia-deep-slate-wall") {{
            variants = 2;
        }};
    }
}