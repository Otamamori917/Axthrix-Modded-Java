package axthrix.world.types.unittypes;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.types.ai.TemporaryDroneAI;
import axthrix.world.types.perks.*;
import axthrix.world.types.weapontypes.PerkWeapon;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.AxStats;
import axthrix.world.util.PerkStats;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.Vars;
import mindustry.game.Gamemode;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.ui.Bar;
import mindustry.world.meta.Env;

import static mindustry.Vars.iconMed;

public class AxUnitType extends UnitType {

    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;

    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;

    public Seq<AxFaction> factions = new Seq<>();

    public AxUnitType(String name) {
        super(name);
        outlineColor = Color.valueOf("#181a1b");
        envDisabled = Env.none;
    }

    @Override
    public void loadIcon() {
        super.loadIcon();
        fullIcon = Core.atlas.find(name + "-preview", fullIcon);
        uiIcon = Core.atlas.find(name + "-ui", fullIcon);
    }

    @Override
    public void setStats() {
        super.setStats();

        if (factions.any()) {
            stats.add(AxStats.faction, Core.bundle.get("team." + factions.peek().name));
        }

        for (Weapon w : weapons) {
            if (w instanceof PerkWeapon pw && !pw.perks.isEmpty()) {
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
    public void update(Unit unit){
        AxPartParms.axparams.set(0, 0, TemperatureLogic.getTemperatureUnit(unit)/100);
    }

    @Override
    public void display(Unit unit, Table table) {
        table.table(t -> {
            t.left();
            t.add(new Image(uiIcon)).size( iconMed ).scaling(arc.util.Scaling.fit);
            t.labelWrap(localizedName).left().width(190f).padLeft(5);
        }).growX().left();
        table.row();

        table.table(bars -> {
            bars.defaults().growX().height(20f).pad(4);

            // Health bar
            bars.add(new Bar("stat.health", Pal.health, unit::healthf).blink(Color.white));
            bars.row();

            // === PERK BARS ===
            for (Weapon w : weapons) {
                if (w instanceof PerkWeapon pw) {
                    for (Perk perk : pw.perks) {
                        PerkStateData s = pw.getState(unit, perk);

                        bars.add(new Bar(
                                () -> getPerkTitle(perk, s),
                                () -> getPerkColor(perk, s),
                                () -> getPerkFraction(perk, s)
                        )).row();
                    }
                }
            }

            // Let abilities and other things add their bars too
            for (mindustry.entities.abilities.Ability ability : unit.abilities) {
                ability.displayBars(unit, bars);
            }

        }).growX();

        table.row();
    }

    // Helper methods
    private String getPerkTitle(Perk perk, PerkStateData s) {
        if (perk instanceof DurationPerk dp) {
            return perk.name + " [accent]" + (int)(dp.getBuffProgress(s) * 100) + "%[]";
        }
        if (perk instanceof BulletPerk) {
            return perk.name + (s.pendingShot ? " [green]READY[]" : " " + s.currentStacks + "/" + perk.maxStacks);
        }
        if (perk instanceof RangePerk || perk instanceof SpeedPerk) {
            return perk.name + " [accent]" + (int)(s.smoothProgress * 100) + "%[]";
        }
        return perk.name + " " + s.currentStacks + "/" + perk.maxStacks;
    }

    private Color getPerkColor(Perk perk, PerkStateData s) {
        boolean active = s.smoothActivated > 0.4f ||
                (perk instanceof DurationPerk dp && dp.getBuffScale(s) > 0f) ||
                (perk instanceof BulletPerk && s.pendingShot);
        return active ? Pal.remove : (perk.color != null ? perk.color : Pal.accent);
    }

    private float getPerkFraction(Perk perk, PerkStateData s) {
        if (perk instanceof DurationPerk dp) return dp.getBuffProgress(s);
        if (perk instanceof RangePerk || perk instanceof SpeedPerk) return s.smoothProgress;
        if (perk instanceof BulletPerk) return s.smoothProgress;
        return perk.maxStacks > 0 ? (float) s.currentStacks / perk.maxStacks : 0f;
    }

    @Override
    public boolean unlockedNow() {
        return Vars.state.rules.mode() == Gamemode.sandbox ||
                Vars.state.isEditor() ||
                Vars.net.server() ||
                (factions.size == 0 || factions.count(f -> f.partOf(Vars.player.team())) > 0) &&
                        super.unlockedNow();
    }
}