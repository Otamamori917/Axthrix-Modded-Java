package axthrix.world.types.unittypes;

import arc.Core;
import arc.graphics.Color;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.game.Gamemode;
import mindustry.type.UnitType;

public class AxUnitType extends UnitType {
    public Seq<AxFaction> factions = new Seq<>();
    public AxUnitType(String name)
    {
        super(name);
        outlineColor = Color.valueOf("#181a1b");
    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        fullIcon = Core.atlas.find(name + "-preview",fullIcon);
        uiIcon = Core.atlas.find(name + "-ui",fullIcon);
    }

    @Override
    public void setStats() {
        super.setStats();
        if(factions.any()){
            stats.add(AxStats.faction, Core.bundle.get("team." +  factions.peek().name));
        }

    }

    @Override
    public boolean unlockedNow()
    {
        return Vars.state.rules.mode() == Gamemode.sandbox || Vars.state.isEditor() || Vars.net.server() || (factions.size == 0 || factions.count(f->f.partOf(Vars.player.team())) > 0) && super.unlockedNow();
    }
}
