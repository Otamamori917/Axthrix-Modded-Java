package axthrix.content;

import arc.graphics.Color;
import mindustry.content.Planets;
import mindustry.graphics.Pal;
import mindustry.graphics.g3d.HexMesh;
import mindustry.maps.planet.SerpuloPlanetGenerator;
import mindustry.type.Planet;

import static mindustry.Vars.content;

public class AxPlanets {
    public static Planet
            Tharaxia
            ;
    public static void load()
    {
        //content.planets().each(p->p.hiddenItems.addAll(PvItems.TharaxiaOnlyItems));
        Tharaxia = new Planet("Tharaxia", Planets.sun,1f,2)
        {{
            generator = new SerpuloPlanetGenerator();
            localizedName = "Tharaxia";
            lightColor = Color.valueOf("ffffff");
            alwaysUnlocked = true;
            tidalLock = false;
            accessible = true;
            meshLoader = () -> new HexMesh(this, 5);
            atmosphereColor = Color.valueOf("ffffff");
            startSector = 15;
            totalRadius = 50f;
            clipRadius = 4;
            lightColor = Color.valueOf("aaaaaa");
            atmosphereRadIn = -0.01f;
            atmosphereRadOut = 0.5f;
            landCloudColor = Color.valueOf("ffffff");
            bloom = true;
            //hiddenItems.addAll(content.items()).removeAll(PvItems.TharaxiaItems);
            iconColor = atmosphereColor = Pal.heal;
            alwaysUnlocked = true;
            ruleSetter = r -> {
            };
        }};
    }
}
