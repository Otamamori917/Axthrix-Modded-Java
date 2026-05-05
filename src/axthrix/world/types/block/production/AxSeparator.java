package axthrix.world.types.block.production;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.world.Block;
import mindustry.world.blocks.production.Separator;

import static mindustry.Vars.state;

public class AxSeparator extends Separator {

    public Seq<AxFaction> faction = new Seq<>();
    public AxSeparator(String name)
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
}
