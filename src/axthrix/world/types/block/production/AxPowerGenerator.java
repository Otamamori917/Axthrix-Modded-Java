package axthrix.world.types.block.production;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.world.blocks.power.PowerGenerator;

public class AxPowerGenerator extends PowerGenerator {

    public Seq<AxFaction> faction = new Seq<>();
    public AxPowerGenerator(String name)
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
