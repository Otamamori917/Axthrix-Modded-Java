package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyBind;
import arc.math.Interp;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.graphics.Drawf;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

import static mindustry.input.Binding.boost;

public class AfterBurnAbility extends Ability {
    public float lightStroke = 40.0F;
    public float oscScl = 1.2F;
    public float oscMag = 0.02F;
    public int divisions = 25;
    public boolean drawFlare = true;
    public Color flareColor = Color.valueOf("FF8B00");
    public Color lightColor = Color.valueOf("FF8B00").a(0.55F);
    public float flareWidth = 0.5F;
    public float flareInnerScl = 0.5F;
    public float flareLength = 6F;
    public float flareInnerLenScl = 0.5F;
    public float flareLayer = 99.9999F;
    public float flareRotSpeed = 1.2F;
    public boolean rotateFlare = false;
    public float[] lengthWidthPans;
    public Color[] colors;
    public boolean mirror = true;
    public float width = 1F;
    public float length = 6f;
    public float setRot = 45;
    public float setX = 5;
    public float setY = 2;
    public KeyBind PlayerKeybind = boost;
    public HashMap<Unit, Float> warmup = new HashMap<>();
    protected float fuel;
    public float fuelUsedPerTick =1.24f;
    public float fuelRegenPerTick = 0.4f;
    public int fuelCap = 600;
    public float boostForce = 5f;
    public float forceDirection = 0;

    public AfterBurnAbility(){
        lengthWidthPans = new float[]{1.12F, 1.3F, 0.32F, 1.0F, 1.0F, 0.3F, 0.8F, 0.9F, 0.2F, 0.5F, 0.8F, 0.15F, 0.25F, 0.7F, 0.1F};
        colors = new Color[]{Color.valueOf("0200C6").a(0.55F),Color.valueOf("412395").a(0.7F),Color.valueOf("814663").a(0.8F),Color.valueOf("C06832"),Color.valueOf("FF8B00")};
        lightColor = colors[1].cpy().a(1.0F);
    }

    public void addStats(Table t) {
        t.add(Core.bundle.format("stat.afterburner-description", PlayerKeybind.value.key, 2));
        t.row();
        t.add("[#"+colors[4].cpy().a(1.0F).toString()+"]---------------------------------------------");
        t.row();
        t.add("[lightgray]" + AxStats.refuelRate.localized() + ": [white]" + Strings.autoFixed((fuelRegenPerTick*60), 2) + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + AxStats.maxBoostTime.localized() + ": [white]" + Strings.autoFixed(((1/fuelUsedPerTick)*fuelCap)/60, 2) + " " + StatUnit.seconds.localized());
        t.row();
        t.add("[lightgray]" + AxStats.Fuel.localized() + ": [white]" + Strings.autoFixed(fuelCap, 2)+ " " +StatUnit.liquidUnits.localized());
        t.row();
        t.add("[lightgray]" + AxStats.unitsOfForce.localized() + ": [white]" + Strings.autoFixed((boostForce), 2) + StatUnit.perSecond.localized());
        t.row();
    }

    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar(
                () ->  Core.bundle.format("bar.aj-afterburner-fuel" , Strings.autoFixed((int)fuel, fuelCap)),
                () ->  (int)fuel/(fuelCap/5) == 5 ? colors[4].cpy() : colors[(int)fuel/(fuelCap/5)].cpy(),
                () ->  fuel / fuelCap
        )).row();
    }
    public boolean CheckIfPlayer(Unit unit){
        if (unit.isPlayer()){
            return Core.input.keyDown(PlayerKeybind);
        }
        return unit.within(unit.aimX(), unit.aimY(), 120);
    }


    public void update(Unit unit) {
        if(!Vars.state.isPaused()){
            if(fuel > 1 && CheckIfPlayer(unit)){
                fuel -= fuelUsedPerTick;
                unit.vel.add(Tmp.v1.trns(unit.rotation+forceDirection, (boostForce/60)));
            }
            if(!CheckIfPlayer(unit) && !(fuel >= fuelCap)){
                Log.info("fuel : "+fuel);
                fuel += fuelRegenPerTick;
            }
        }
    }


    public void draw(Unit unit) {
        int len = mirror ? 2 : 1;

        if (!warmup.containsKey(unit)){
            warmup.put(unit,0f);
        }else{
            if(!Vars.state.isPaused()){
                warmup.replace(unit,Mathf.lerpDelta(warmup.get(unit),fuel > 1 && CheckIfPlayer(unit) ? 1.0F : 0.0F, 0.1f));
            }
        }

        for(int s = 0; s < len; ++s) {

            float sign = (float) ((s == 0 ? -1 : 1));
            Tmp.v1.trns(unit.rotation - 180, setY * -1, setX * sign).add(unit.x, unit.y);
            float rx = Tmp.v1.x;
            float ry = Tmp.v1.y;
            float rot = setRot * sign + unit.rotation() - 180;
            Draw.xscl *= sign;

            float sin = Mathf.sin(Time.time, oscScl, oscMag);

            for(int i = 0; i < colors.length; ++i) {
                Draw.color(colors[i].write(Tmp.c1).mul(0.9F).mul(1.0F + Mathf.absin(Time.time, 1.0F, 0.1F)));
                Drawf.flame(rx, ry, divisions, rot,(length * warmup.get(unit)) * lengthWidthPans[i * 3] * (1.0F - sin), width * lengthWidthPans[i * 3 + 1] * (1.0F + sin), lengthWidthPans[i * 3 + 2]);
            }

            if (drawFlare) {
                Draw.color(flareColor);
                Draw.z(flareLayer);
                float angle = Time.time * flareRotSpeed + (rotateFlare ? unit.rotation() : 0.0F);

                for(int i = 0; i < 4; ++i) {
                    Drawf.tri(rx, ry, flareWidth, flareLength * (warmup.get(unit) + sin), (float)(i * 90 + 45) + angle);
                }

                Draw.color();

                for(int i = 0; i < 4; ++i) {
                    Drawf.tri(rx, ry, flareWidth * flareInnerScl, flareLength * flareInnerLenScl * (warmup.get(unit) + sin), (float)(i * 90 + 45) + angle);
                }
            }

            Tmp.v1.trns(rot,(length * warmup.get(unit)) * 1.1F);
            Drawf.light(rx, ry, rx + Tmp.v1.x, ry + Tmp.v1.y, lightStroke, lightColor, 0.7F);
            Draw.reset();
        }
    }
}
