package axthrix.content.blocks;

import arc.graphics.Color;
import axthrix.world.types.block.TemperatureFloor;
import mindustry.content.Liquids;
import mindustry.content.StatusEffects;
import mindustry.graphics.CacheLayer;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.Attribute;

public class AxthrixEnvironment {
    public static Block

        //tiles
        crimsonSandFloor,crimsonSandDeepFloor,crimsonSandDarkSlope,crimsonSandLightSlope,nest,lavaFloor,iceFloor;

    public static void load() {

        crimsonSandFloor = new Floor("crimson-sand-floor"){{
            supportsOverlay = true;
            variants = 3;
        }};
        crimsonSandDeepFloor = new Floor("crimson-sand-deep-floor"){{
            supportsOverlay = true;

            variants = 3;
        }};
        crimsonSandDarkSlope = new Floor("crimson-sand-slope-dark"){{
            supportsOverlay = true;
            variants = 3;
        }};
        crimsonSandLightSlope = new Floor("crimson-sand-slope-light"){{
            supportsOverlay = true;
            variants = 3;
        }};

        nest = new Floor("nest") {{
            needsSurface = true;
        }};
        lavaFloor = new TemperatureFloor("lava"){{
            temperaturePerSecond = 10f; // Heat
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
        iceFloor = new TemperatureFloor("ice"){{
            temperaturePerSecond = -10f; // Cold (negative)
            dragMultiplier = 0.35f;
            speedMultiplier = 0.9f;
            attributes.set(Attribute.water, 0.4f);
            albedo = 0.65f;
        }};
    }
}
