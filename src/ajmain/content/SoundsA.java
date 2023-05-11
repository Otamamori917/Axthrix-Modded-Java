package ajmain.content;

import arc.audio.*;
import arc.files.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.world.blocks.power.*;
import assets.SoundsA.*;

import static mindustry.Vars.*;

public class SoundsA{
    public static Sound

    clusterLaunch = new Sound();

    public static void load() {
        if(Vars.headless) return;

        clusterLaunch = Vars.tree.loadSound("cluster-launch");
    }
}    