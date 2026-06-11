package axthrix.world.types.unittypes;

import arc.Core;
import arc.graphics.Color;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.types.weapontypes.PerkWeapon;
import axthrix.world.util.AxStats;
import axthrix.world.util.PerkStats;
import mindustry.Vars;
import mindustry.game.Gamemode;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.meta.Env;

public class AxUnitType extends UnitType {
    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;


    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public Seq<AxFaction> factions = new Seq<>();
    public AxUnitType(String name)
    {
        super(name);
        outlineColor = Color.valueOf("#181a1b");
        envDisabled = Env.none;
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

        if (factions.any()) {
            stats.add(AxStats.faction, Core.bundle.get("team." + factions.peek().name));
        }

        // Collect perk weapons and emit one stat entry per weapon that has perks.
        for (Weapon w : weapons) {
            if (w instanceof PerkWeapon pw && !pw.perks.isEmpty()) {
                // Label the section with the weapon name if it has one, else generic.
                String label = (pw.name != null && !pw.name.isEmpty())
                        ? pw.name
                        : Core.bundle.get("stat.aj-perk-weapon-unnamed");
                stats.add(AxStats.perkSystem, t -> {
                    t.add("[stat]" + label).left().padBottom(2f).colspan(2).row();
                    PerkStats.addPerkStats(t, pw.perks);
                });
            }
        }
    }

    @Override
    public boolean unlockedNow()
    {
        return Vars.state.rules.mode() == Gamemode.sandbox || Vars.state.isEditor() || Vars.net.server() || (factions.size == 0 || factions.count(f->f.partOf(Vars.player.team())) > 0) && super.unlockedNow();
    }
}
