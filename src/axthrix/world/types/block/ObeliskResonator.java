package axthrix.world.types.block;

import mindustry.gen.Building;
import mindustry.world.Block;

public class ObeliskResonator extends Block {
    public ObeliskResonator(String name){
        super(name);
        update = true;
        solid = true;
        size = 1;
        hasPower = true; // Maybe it needs power to work?
    }

    public class ResonatorBuild extends Building {
        // Just a standard building for now
    }
}
