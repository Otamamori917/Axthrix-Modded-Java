package axthrix.world.types.block;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.world.Block;

public class AxBlock extends Block {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;


    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public Seq<AxFaction> faction = new Seq<>();
    public AxBlock(String name)
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
