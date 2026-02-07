package axthrix.world.types.block.production;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;

import static mindustry.Vars.state;

public class AxGenericCrafter extends GenericCrafter {

    public Seq<AxFaction> faction = new Seq<>();
    public AxGenericCrafter(String name)
    {
        super(name);
        solid = true;
        destructible = true;
    }
    @Override
    public void setStats() {
        super.setStats();

        if(faction.any()){
            stats.add(AxStats.faction, Core.bundle.get("team." +  faction.peek().name));
        }

    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        fullIcon = Core.atlas.find(name + "-full",fullIcon);
        uiIcon = Core.atlas.find(name + "-ui",fullIcon);
    }
}
