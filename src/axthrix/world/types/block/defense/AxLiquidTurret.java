package axthrix.world.types.block.defense;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.LiquidTurret;

import static mindustry.Vars.state;

public class AxLiquidTurret extends LiquidTurret {

    public Seq<AxFaction> faction = new Seq<>();
    public boolean blackListFactions = false;
    public AxLiquidTurret(String name) {
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
