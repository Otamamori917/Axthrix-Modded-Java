package axthrix.world.types.block.drill;

import arc.struct.Seq;
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
    public boolean partOfPlayerFaction()
    {
        if (blackListFactions)
            return faction.count(f -> f.partOf(Vars.player.team())) == 0;
        return faction.size == 0 || faction.count(f -> f.partOf(Vars.player.team())) > 0;
    }

    @Override
    public boolean isVisible(){
        return state.rules.editor || (partOfPlayerFaction() && !isHidden() && (!state.rules.hideBannedBlocks || !state.rules.isBanned(this)));
    }

    @Override
    public boolean isPlaceable(){
        return Vars.net.server() || (!state.rules.isBanned(this) || state.rules.editor) && supportsEnv(state.rules.env);
    }
}
