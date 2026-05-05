package axthrix.world.util.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import axthrix.world.types.abilities.AfterBurnAbility;
import axthrix.world.types.abilities.StaticEMPability;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.ui.Styles;

import static mindustry.Vars.*;

public class CustomUnitInfoBar {
    public Table container;

    public void build(){
        container = new Table(Tex.buttonEdge3);
        container.top().left();
        container.visible(() -> player.unit() != null && ui.hudfrag.shown); // Hide when UI is hidden

        ui.hudGroup.addChild(container);

        container.update(() -> {
            Unit unit = player.unit();
            if(unit == null || !ui.hudfrag.shown){ // Check UI visibility
                container.clearChildren();
                return;
            }

            rebuildInfo(unit);

            // Position: top-left, down a bit
            container.setPosition(
                    4f, // Left edge with small padding
                    Core.graphics.getHeight() - container.getHeight() - 120f // Top, down 120px
            );

            container.pack();
        });
    }

    private void rebuildInfo(Unit unit){
        container.clearChildren();
        container.background(Tex.buttonEdge3);
        container.margin(4f);

        boolean hasAnyInfo = false;

        // Check for AfterBurn ability
        if(unit.abilities != null){
            for(Ability ab : unit.abilities){
                if(ab instanceof AfterBurnAbility afterburn){
                    addAfterBurnInfo(container, afterburn);
                    hasAnyInfo = true;
                }

                if(ab instanceof StaticEMPability emp){
                    addEMPInfo(container, unit, emp);
                    hasAnyInfo = true;
                }
            }
        }

        // Hide container if no info to show
        container.visible = hasAnyInfo;
    }

    private void addAfterBurnInfo(Table container, AfterBurnAbility afterburn){
        container.table(t -> {
            // Icon
            t.image(Icon.defense).size(16f).color(afterburn.colors[4]).padRight(4f);

            // Fuel text
            t.label(() -> {
                float fuel = afterburn.fuel;
                float max = afterburn.fuelCap;
                int colorIndex = Math.min(4, (int)(fuel / (max / 5f)));
                Color color = afterburn.colors[colorIndex];
                return "[#" + color.toString() + "]" +
                        Strings.fixed(fuel, 0) + "[white]/[]" + Strings.fixed(max, 0);
            }).style(Styles.outlineLabel).minWidth(70f);
        }).left().padBottom(2f);
        container.row();
    }

    private void addEMPInfo(Table container, Unit unit, StaticEMPability emp){
        container.table(t -> {
            // Icon
            t.image(Icon.power).size(16f).color(emp.color).padRight(4f);

            // Charge text
            t.label(() -> {
                Integer charge = emp.currentCharge.get(unit);
                if(charge == null) charge = 0;
                int max = emp.maxPower;
                Color color = charge >= emp.minimumPowerToDischarge ?
                        emp.color.cpy().lerp(Color.white, 0.3f) :
                        emp.color.cpy().lerp(Color.gray, 0.5f);
                return " [#" + color.toString() + "]" +
                        charge + "[white]/[]" + max;
            }).style(Styles.outlineLabel).minWidth(70f);
        }).left().padBottom(2f);
        container.row();
    }
}