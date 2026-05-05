package axthrix.world.util.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Tmp;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.turrets.Turret;

import static mindustry.Vars.*;

public class TemperatureGaugeUI {
    public Table container;

    public void build(){
        container = new Table(Tex.buttonEdge3);
        container.top().left();
        container.visible(() -> {
            if(!ui.hudfrag.shown || player.unit() == null) return false;

            // Check if controlling unit or turret
            Unit u = player.unit();
            if(u != null && !u.isPlayer()) return false; // Only show for player

            float temp = 0f;
            if(u != null){
                temp = TemperatureLogic.getTemperatureUnit(u);
            }else if(control.input.config.getSelected() instanceof Turret.TurretBuild tb){
                temp = TemperatureLogic.getTemperatureBuilding(tb);
            }

            return temp != 0f; // Only show if has temperature
        });

        ui.hudGroup.addChild(container);

        container.update(() -> {
            container.clearChildren();
            container.background(Tex.buttonEdge3);
            container.margin(4f);

            Unit unit = player.unit();
            Building selected = control.input.config.getSelected();

            float temp = 0f;
            String label = "";

            if(unit != null && unit.isPlayer()){
                temp = TemperatureLogic.getTemperatureUnit(unit);
                label = "Unit";
            }else if(selected instanceof Turret.TurretBuild tb){
                temp = TemperatureLogic.getTemperatureBuilding(tb);
                label = "Turret";
            }

            if(temp != 0f){
                buildGauge(temp, label);
            }

            // Position: top-left, below other UI
            container.setPosition(
                    4f,
                    Core.graphics.getHeight() - container.getHeight() - 200f
            );

            container.pack();
        });
    }

    private void buildGauge(float temperature, String label){
        container.table(t -> {
            t.background(Tex.buttonEdge3);

            // Label
            t.add(label + " Temperature").style(Styles.outlineLabel).padBottom(4f);
            t.row();

            // Gradient bar with indicator
            float barWidth = 200f;
            float barHeight = 20f;

            t.table(barTable -> {
                barTable.image().size(barWidth, barHeight).update(img -> {
                    float x = img.x;
                    float y = img.y;

                    Draw.z(Layer.overlayUI);

                    // Draw gradient background (blue -> red)
                    int segments = 100;
                    for(int i = 0; i < segments; i++){
                        float percent = i / (float)segments;
                        Color segColor = Tmp.c1.set(Color.cyan).lerp(Color.red, percent);
                        Draw.color(segColor);
                        Fill.rect(
                                x + i * (barWidth/segments) + (barWidth/(segments*2f)),
                                y + barHeight/2f,
                                barWidth/segments,
                                barHeight
                        );
                    }

                    // Indicator line (maps -100 to 100 → 0 to barWidth)
                    float linePos = (temperature + 100f) / 200f;
                    float lineX = x + linePos * barWidth;

                    // Flash at max temperature
                    boolean atMax = Math.abs(temperature) >= 100f;
                    Color lineColor = temperature > 0 ? Color.red : Color.cyan;

                    if(atMax){
                        // Rapid flash
                        float flash = Mathf.absin(arc.util.Time.time, 0.5f, 1f);
                        lineColor = Tmp.c2.set(lineColor).lerp(Color.white, flash);
                    }

                    // Draw indicator line
                    Draw.color(lineColor);
                    Lines.stroke(3f);
                    Lines.line(lineX, y, lineX, y + barHeight);

                    // Draw outline
                    Draw.color(Color.white);
                    Lines.stroke(2f);
                    Lines.rect(x, y, barWidth, barHeight);

                    Draw.reset();
                });
            }).center();
            t.row();

            String temperatureStr = TempUnit.format(temperature);

            // Temperature value
            String tempText = temperature > 0 ?
                    "[red]+" + temperatureStr:
                    "[cyan]" + temperatureStr;

            t.add(tempText).style(Styles.outlineLabel).padTop(4f);
            t.row();

            // Effect description
            if(temperature > 0){
                t.add("[lightgray]+" + Strings.fixed(temperature, 0) + "% [red]damage taken")
                        .style(Styles.outlineLabel).padTop(2f);
            }else{
                float coldPercent = -temperature;
                t.add("[lightgray]-" + Strings.fixed(coldPercent, 0) + "% [cyan]armor & resistance")
                        .style(Styles.outlineLabel).padTop(2f);
            }
        }).pad(8f);
    }
}