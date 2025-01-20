package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.*;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.power.PowerNode;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

public class StaticEMPability extends Ability {
    public float range,baseDamage = 15;
    public int maxPower = 100, minimumPowerToDischarge = 50;
    public boolean emp = true,alwaysShowRange = false;
    public float empDuration = 30,empPower = 0.2f;
    public float passiveCharge = 0.05f, poweredCharge = 0.8f;
    public float x = 0f,y = 0f;

    public Color color;
    public Effect damageEffect = Fx.chainLightning;
    public HashMap<Unit, Integer> currentCharge = new HashMap<>();
    public HashMap<Unit, Float> chargingFloat = new HashMap<>();
    public HashMap<Unit, Boolean> redirected = new HashMap<>();

    public float layer = Layer.bullet + 4f;

    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar(
                () ->  Core.bundle.format("bar.aj-static-power" , Strings.autoFixed(currentCharge.get(unit), maxPower)),
                () ->  (currentCharge.get(unit)>minimumPowerToDischarge) ? color.cpy().add(Color.darkGray) :color.cpy().add(Color.black),
                () ->  (float)currentCharge.get(unit) / maxPower
        )).row();
    }

    public StaticEMPability(){

    }

    public void addStats(Table t) {
        t.add("[white]" +AxStats.staticShock.localized());
        t.row();
        t.add("[#"+color.toString()+"]---------------------------------------------");
        t.row();
        t.add("[lightgray]" + AxStats.chargeTimePas.localized() + ": [white]" + Strings.autoFixed(((1/passiveCharge)*maxPower)/60, 2) + " " + StatUnit.seconds.localized());
        t.row();
        t.add("[lightgray]" + AxStats.chargeTimePow.localized() + ": [white]" + Strings.autoFixed(((1/(passiveCharge+poweredCharge))*maxPower)/60, 2) + " " + StatUnit.seconds.localized());
        t.row();
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Strings.autoFixed((minimumPowerToDischarge*baseDamage), 2)+ "~" +Strings.autoFixed(maxPower*baseDamage, 2));
        t.row();
        t.add("[lightgray]" + Stat.powerCapacity.localized() + ": [white]" + Strings.autoFixed(maxPower, 2)+ " " +StatUnit.powerUnits.localized());
        t.row();
        t.add("[lightgray]" + AxStats.minimumPowerToDischarge.localized() + ": [white]" + Strings.autoFixed(minimumPowerToDischarge, 2)+ " " +StatUnit.powerUnits.localized());
        t.row();

        if(emp){
            t.add("[lightgray]" + AxStats.emp.localized());
            t.row();
            t.table( u -> {
                u.add("[lightgray]   " + AxStats.empPower.localized() + ": [red]-" + Strings.autoFixed(100*empPower, 2) + StatUnit.percent.localized()).left();
                u.row();
                u.add("[lightgray]   " + AxStats.empDuration.localized() + ": [white]" + Strings.autoFixed(empDuration/60, 2) + " " + StatUnit.seconds.localized()).left();
                u.row();
            });
        }
    }

    @Override
    public void update(Unit unit) {

        if (!currentCharge.containsKey(unit)){
            currentCharge.put(unit, 0);
        }
        if (!chargingFloat.containsKey(unit)){
            chargingFloat.put(unit, 0f);
        }
        if (!redirected.containsKey(unit)){
            redirected.put(unit, false);
        }
        Units.nearbyBuildings(unit.x, unit.y, range, b -> {
            if (b.team() == unit.team() && b.block() instanceof PowerNode) {
                if(b.power != null && b.power.graph.getLastPowerProduced() > 0.0F) {
                    if (!(currentCharge.get(unit) >= maxPower)) {
                        chargingFloat.replace(unit, chargingFloat.get(unit) + poweredCharge);
                    }

                }
            }
        });
        if (!(currentCharge.get(unit) >= maxPower)) {
            chargingFloat.replace(unit,chargingFloat.get(unit)+passiveCharge);
        }
        if(chargingFloat.get(unit) > 1){
            currentCharge.replace(unit,currentCharge.get(unit) + 1);
            chargingFloat.replace(unit,chargingFloat.get(unit) - 1);
        }
        if(chargingFloat.get(unit) > 2){
            chargingFloat.replace(unit,2f);
        }


        if (currentCharge.get(unit) > maxPower) {
            currentCharge.replace(unit,maxPower);
        }
        Units.nearbyBuildings(unit.x, unit.y, range, b -> {

            if(unit.isShooting() && currentCharge.get(unit) >= minimumPowerToDischarge){
                if (b.team() != unit.team() && b.power != null && b.power.graph.getLastPowerProduced() > 0.0F) {
                    Units.nearbyBuildings(b.x, b.y, 12, p -> {
                        if(p.absorbLasers()){
                            redirected.replace(unit,true);
                            if(emp){
                                p.applySlowdown(empPower, empDuration);
                            }
                            AxthrixFfx.staticShock((float)currentCharge.get(unit)/6).at(p.x, p.y, unit.angleTo(p), color);
                            for (int i = 0; i < (currentCharge.get(unit)/6); i++) {
                                Tmp.v1.trns(unit.rotation - 90.0F, x, y).add(b.x, b.y);
                                float rx = Tmp.v1.x;
                                float ry = Tmp.v1.y;
                                damageEffect.at(rx, ry, 0.0F, color, p);
                            }
                            for (int i = 0; i < currentCharge.get(unit)/2; i++) {
                                currentCharge.replace(unit,currentCharge.get(unit)-2);
                            }
                            p.damage(unit.team, ((baseDamage*currentCharge.get(unit)) * Vars.state.rules.unitDamage(unit.team))/2);
                        }else{
                            redirected.replace(unit,false);
                        }
                    });
                    //Log.info("DISCHARGE | Damage:"+(currentCharge.get(unit)*baseDamage)*Vars.state.rules.unitDamage(unit.team));
                    if(emp){
                        b.applySlowdown(empPower, empDuration);
                    }
                    AxthrixFfx.staticShock((float)currentCharge.get(unit)/4).at(b.x, b.y, unit.angleTo(b), color);
                    for (int i = 0; i < (currentCharge.get(unit)/4); i++) {
                        Tmp.v1.trns(unit.rotation - 90.0F, x, y).add(unit.x, unit.y);
                        float rx = Tmp.v1.x;
                        float ry = Tmp.v1.y;
                        damageEffect.at(rx, ry, 0.0F, color, b);
                    }
                    for (int i = 0; i < currentCharge.get(unit); i++) {
                        currentCharge.replace(unit,currentCharge.get(unit)-1);
                    }
                    b.damage(unit.team,redirected.get(unit) ? (baseDamage*currentCharge.get(unit)) * Vars.state.rules.unitDamage(unit.team) : ((baseDamage*currentCharge.get(unit)) * Vars.state.rules.unitDamage(unit.team))/8);
                }
            }
        });

    }
    @Override
    public void draw(Unit unit) {
        Draw.z(layer);
        Tmp.v1.trns(unit.rotation - 90, x, y).add(unit.x, unit.y);
        float rx = Tmp.v1.x, ry = Tmp.v1.y;

        if(alwaysShowRange || unit.isPlayer()){
            Draw.color(color);
            Drawf.circles(rx,ry, range);
        }
    }

    public String localized() {
        return Core.bundle.format("ability.aj-static-energy");
    }
}
