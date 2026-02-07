package axthrix.world.types.block.drill;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.world.blocks.production.Drill;
import axthrix.world.types.AxFaction;

import static mindustry.Vars.state;

public class AxDrill extends Drill {

    public Seq<AxFaction> faction = new Seq<>();
    public boolean blackListFactions = false;

    public AxDrill(String name) {
        super(name);
    }

    @Override
    public void setStats() {
        super.setStats();

        if(faction.any()){
            stats.add(AxStats.faction, Core.bundle.get("team." +  faction.peek().name));
        }
    }
}
