package axthrix.world.util.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import axthrix.world.types.abilities.AfterBurnAbility;
import axthrix.world.types.abilities.StaticEMPability;
import axthrix.world.types.perks.*;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.weapontypes.PerkWeapon;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.*;

public class CustomUnitInfoBar {
    public Table container;

    public void build(){
        if(container != null) return;

        container = new Table(Tex.buttonEdge3);
        container.top().left();
        container.visible(() -> player.unit() != null && ui.hudfrag.shown);

        ui.hudGroup.addChild(container);

        container.update(() -> {
            Unit unit = player.unit();
            if(unit == null || !ui.hudfrag.shown){
                container.clearChildren();
                container.visible = false;
                return;
            }

            rebuildInfo(unit);

            container.setPosition(
                    4f,
                    Core.graphics.getHeight() - container.getHeight() - 120f
            );

            container.pack();
        });
    }

    private void rebuildInfo(Unit unit){
        container.clearChildren();
        container.background(Tex.buttonEdge3);
        container.margin(6f);

        boolean hasAnyInfo = false;

        // 1. Core Temperature Engine Integration
        float temp = 0f;
        String typeLabelKey = "";
        Building selected = control.input.config.getSelected();

        if(unit.isPlayer()){
            temp = TemperatureLogic.getTemperatureUnit(unit);
            typeLabelKey = "ui.ax-info.unit";
        }else if(selected instanceof Turret.TurretBuild tb){
            temp = TemperatureLogic.getTemperatureBuilding(tb);
            typeLabelKey = "ui.ax-info.turret";
        }

        if(temp != 0f){
            float resistance = 1f;
            if(unit.type instanceof AxUnitType axType){
                resistance = temp > 0 ? axType.effectResistanceHeat : axType.effectResistanceCold;
            }

            addTemperatureInfo(container, temp, typeLabelKey, resistance);
            hasAnyInfo = true;
        }

        // 2. Custom Active Abilities Iterator
        if(unit.abilities != null){
            for(Ability ab : unit.abilities){
                if(ab instanceof AfterBurnAbility afterburn){
                    addAfterBurnInfo(container, afterburn);
                    hasAnyInfo = true;
                } else if(ab instanceof StaticEMPability emp){
                    addEMPInfo(container, unit, emp);
                    hasAnyInfo = true;
                }
            }
        }

        // 3. Custom Attached Modular Perks Mapping System
        if(unit.mounts != null){
            for(WeaponMount mount : unit.mounts){
                if(mount.weapon instanceof PerkWeapon pw && pw.perks != null){
                    for(Perk perk : pw.perks){
                        PerkStateData s = pw.getState(unit, perk);
                        addPerkInfo(container, perk, s);
                        hasAnyInfo = true;
                    }
                }
            }
        }

        container.visible = hasAnyInfo;
    }

    private void addTemperatureInfo(Table container, float temperature, String typeLabelKey, float resistance){
        container.table(t -> {
            Color statusColor = temperature > 0 ? Color.red : Color.cyan;
            t.image(Icon.waves).size(16f).color(statusColor).padRight(6f);

            t.label(() -> {
                String typeLabel = Core.bundle.get(typeLabelKey);
                String temperatureStr = TempUnit.format(temperature);
                String prefix = temperature > 0 ? "[red]+" : "[cyan]";

                // Format baseline temperature: "{Type} Temp: +XX°C"
                String out = Core.bundle.format("ui.ax-info.temp-base", typeLabel, prefix + temperatureStr);

                // 0.0f = Pure Immune (Hides the debuff suffix completely)
                if(resistance <= 0f){
                    return out;
                }

                float modifierFactor = resistance;

                if(temperature > 0){
                    float finalDmgDebuff = temperature * modifierFactor;
                    String formattedVal = Strings.fixed(finalDmgDebuff, 0);
                    out += " " + Core.bundle.format("ui.ax-info.temp-heat-debuff", formattedVal);
                } else {
                    float finalColdDebuff = (-temperature) * modifierFactor;
                    String formattedVal = Strings.fixed(finalColdDebuff, 0);
                    out += " " + Core.bundle.format("ui.ax-info.temp-cold-debuff", formattedVal);
                }

                return out;
            }).style(Styles.outlineLabel).minWidth(120f);
        }).left().padBottom(6f);
        container.row();
    }

    private void addAfterBurnInfo(Table container, AfterBurnAbility afterburn){
        container.table(t -> {
            t.image(Icon.defense).size(16f).color(afterburn.colors[4]).padRight(6f);
            t.label(() -> {
                float fuel = afterburn.fuel;
                float max = afterburn.fuelCap;
                int colorIndex = Math.min(4, (int)(fuel / (max / 5f)));
                Color color = afterburn.colors[colorIndex];

                String valStr = "[#" + color.toString() + "]" + Strings.fixed(fuel, 0) + "[white]/[]" + Strings.fixed(max, 0);
                return Core.bundle.format("ui.ax-info.fuel", valStr);
            }).style(Styles.outlineLabel).minWidth(70f);
        }).left().padBottom(6f);
        container.row();
    }

    private void addEMPInfo(Table container, Unit unit, StaticEMPability emp){
        container.table(t -> {
            t.image(Icon.power).size(16f).color(emp.color).padRight(6f);
            t.label(() -> {
                Integer charge = emp.currentCharge.get(unit);
                if(charge == null) charge = 0;
                int max = emp.maxPower;
                Color color = charge >= emp.minimumPowerToDischarge ?
                        emp.color.cpy().lerp(Color.white, 0.3f) :
                        emp.color.cpy().lerp(Color.gray, 0.5f);

                String valStr = "[#" + color.toString() + "]" + charge + "[white]/[]" + max;
                return Core.bundle.format("ui.ax-info.emp", valStr);
            }).style(Styles.outlineLabel).minWidth(70f);
        }).left().padBottom(6f);
        container.row();
    }

    private void addPerkInfo(Table container, Perk perk, PerkStateData s){
        container.table(t -> {
            t.image(Icon.layers).size(16f).color(getPerkColor(perk, s)).padRight(6f);

            t.label(() -> {
                String title = perk.name;
                Color col = getPerkColor(perk, s);
                String hexColor = "[#" + col.toString() + "]";

                if(perk instanceof DurationPerk dp){
                    return hexColor + title + " " + Core.bundle.format("ui.ax-info.perk-duration", (int)(dp.getBuffProgress(s) * 100));
                }
                if(perk instanceof BulletPerk){
                    String state = s.pendingShot ? Core.bundle.get("ui.ax-info.perk-ready") : " " + s.currentStacks + "/" + perk.maxStacks;
                    return hexColor + title + state;
                }
                if(perk instanceof RangePerk || perk instanceof SpeedPerk){
                    return hexColor + title + " " + Core.bundle.format("ui.ax-info.perk-percent", (int)(s.smoothProgress * 100));
                }
                if(perk instanceof ShotBuffPerk){
                    return hexColor + title + " " + Core.bundle.format("ui.ax-info.perk-shotbuff", s.currentStacks, s.hitProgress, perk.hitsPerStack);
                }
                return hexColor + title + " " + s.currentStacks + "/" + perk.maxStacks;
            }).style(Styles.outlineLabel).minWidth(100f);
        }).left().padBottom(6f);
        container.row();
    }

    private Color getPerkColor(Perk perk, PerkStateData s) {
        boolean active = s.smoothActivated > 0.4f ||
                (perk instanceof DurationPerk dp && dp.getBuffScale(s) > 0f) ||
                (perk instanceof BulletPerk && s.pendingShot);
        return active ? Pal.remove : (perk.color != null ? perk.color : Pal.accent);
    }
}
