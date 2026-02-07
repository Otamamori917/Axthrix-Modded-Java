package axthrix.content.blocks;

import arc.Core;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;

public class AxthrixEnvironment {
    public static Block

        //tiles
        crimsonSandFloor,crimsonSandDeepFloor,crimsonSandDarkSlope,crimsonSandLightSlope,nest;

    public static void load() {

        crimsonSandFloor = new Floor("crimson-sand-floor"){{
            supportsOverlay = true;
            localizedName = "Crimson Sand";
            variants = 3;
        }};
        crimsonSandDeepFloor = new Floor("crimson-sand-deep-floor"){{
            supportsOverlay = true;
            localizedName = "Deep Crimson Sand";
            variants = 3;
        }};
        crimsonSandDarkSlope = new Floor("crimson-sand-slope-dark"){{
            supportsOverlay = true;
            localizedName = "Crimson Sand Slope";
            variants = 3;
        }};
        crimsonSandLightSlope = new Floor("crimson-sand-slope-light"){{
            supportsOverlay = true;
            localizedName = "Crimson Sand Slope";
            variants = 3;
        }};

        nest = new Floor("nest") {{
            needsSurface = true;
        }};
    }
}
