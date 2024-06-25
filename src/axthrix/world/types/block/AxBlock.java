package axthrix.world.types.block;

import arc.Core;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.world.Block;


import static mindustry.Vars.state;

public class AxBlock extends Block {

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

    public boolean partOfPlayerFaction()
    {
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
