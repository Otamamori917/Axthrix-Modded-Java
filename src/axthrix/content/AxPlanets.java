package axthrix.content;

import arc.graphics.Color;
import mindustry.content.Planets;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.type.Planet;

public class AxPlanets {
    public static Planet Tharaxia;

    public static void load() {
        Tharaxia = new Planet("Tharaxia", Planets.sun, 1f, 2) {{
            generator = new ThraxiaPlanetGenerator();
            localizedName = "Tharaxia";
            lightColor = Color.valueOf("aaaaaa");
            alwaysUnlocked = true;
            tidalLock = false;
            accessible = true;
            meshLoader = () -> new HexMesh(this, 5);
            // Deep purple atmosphere to match the ocean
            atmosphereColor = Color.valueOf("1e0f3d");
            startSector = 15;
            totalRadius = 50f;
            clipRadius = 4;
            atmosphereRadIn = -0.01f;
            atmosphereRadOut = 0.5f;
            // Dark purple-tinted clouds
            landCloudColor = Color.valueOf("2d1a52");
            bloom = true;
            iconColor = Color.valueOf("1e0f3d");
            alwaysUnlocked = true;
            ruleSetter = r -> {};
        }};
    }
}