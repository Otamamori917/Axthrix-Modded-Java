package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyBind;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import mindustry.Vars;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.ui.Bar;
import mindustry.world.meta.StatUnit;

import arc.graphics.g2d.Draw;

import static mindustry.input.Binding.boost;

public class AfterBurnAbility extends Ability {
    public float lightStroke = 40f;
    public float oscScl = 1.2f;
    public float oscMag = 0.02f;
    public int divisions = 25;
    public boolean drawFlare = true;
    public Color flareColor = Color.valueOf("FF8B00");
    public Color lightColor = Color.valueOf("FF8B00").a(0.55f);
    public float flareWidth = 0.5f;
    public float flareInnerScl = 0.5f;
    public float flareLength = 6f;
    public float flareInnerLenScl = 0.5f;
    public float flareLayer = 99.9999f;
    public float flareRotSpeed = 1.2f;
    public boolean rotateFlare = false;
    public float[] lengthWidthPans;
    public Color[] colors;
    public boolean mirror = true;
    public float width = 1f;
    public float length = 6f;
    public float setRot = 45;
    public float setX = 5;
    public float setY = 2;

    public ObjectMap<Unit, Float> warmup = new ObjectMap<>();
    public float fuelUsedPerTick = 0.8f;
    public float fuelRegenPerTick = 0.6f;
    public int fuelCap = 200;
    public float boostForce = 10f;
    public float forceDirection = 0;

    public float fuel;

    /** UI hold-to-boost (mobile / showMobileButtons). */
    public static ObjectMap<Integer, Boolean> uiBoostHeld = new ObjectMap<>();

    public static void setBoostHeld(Unit unit, boolean held) {
        if (unit == null) return;
        if (held) uiBoostHeld.put(unit.id, true);
        else uiBoostHeld.remove(unit.id);
    }

    public AfterBurnAbility() {
        lengthWidthPans = new float[]{
                1.12f, 1.3f, 0.32f,
                1f, 1f, 0.3f,
                0.8f, 0.9f, 0.2f,
                0.5f, 0.8f, 0.15f,
                0.25f, 0.7f, 0.1f
        };
        colors = new Color[]{
                Color.valueOf("0200C6").a(0.55f),
                Color.valueOf("412395").a(0.7f),
                Color.valueOf("814663").a(0.8f),
                Color.valueOf("C06832"),
                Color.valueOf("FF8B00")
        };
        lightColor = colors[1].cpy().a(1f);
    }

    @Override
    public String localized() {
        return Core.bundle.format("ability.aj-afterburner");
    }

    @Override
    public void addStats(Table t) {
        t.add(Core.bundle.format("stat.afterburner-description", AxthrixLoader.bindActivate.value, 2));
        t.row();
        t.add("[#" + colors[4].cpy().a(1f) + "]────────────────────────────────────");
        t.row();
        t.add("[lightgray]" + Core.bundle.format("stat.aj-refuel-rate") + ": [white]"
                + Strings.autoFixed(fuelRegenPerTick * 60, 2) + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Core.bundle.format("stat.aj-max-boost-time") + ": [white]"
                + Strings.autoFixed(((1f / fuelUsedPerTick) * fuelCap) / 60, 2) + " " + StatUnit.seconds.localized());
        t.row();
        t.add("[lightgray]" + Core.bundle.format("stat.aj-fuel") + ": [white]"
                + Strings.autoFixed(fuelCap, 2) + " " + StatUnit.liquidUnits.localized());
        t.row();
        t.add("[lightgray]" + Core.bundle.format("stat.aj-units-of-force") + ": [white]"
                + Strings.autoFixed(boostForce, 2) + StatUnit.perSecond.localized());
        t.row();
    }

    @Override
    public void displayBars(Unit unit, Table bars) {
        bars.add(new Bar(
                () -> Core.bundle.format("bar.aj-afterburner-fuel", fuel),
                () -> (int) fuel / (fuelCap / 5) == 5 ? colors[4].cpy() : colors[(int) fuel / (fuelCap / 5)].cpy(),
                () -> fuel / fuelCap
        )).row();
    }

    public boolean CheckIfPlayer(Unit unit) {
        if (unit.isPlayer()) {
            if (uiBoostHeld.get(unit.id, false)) return true;
            return AxthrixLoader.activateDown();
        }
        return unit.within(unit.aimX(), unit.aimY(), 120);
    }

    @Override
    public void update(Unit unit) {
        if (!unit.isPlayer() || !AxthrixLoader.showMobileButtons) {
            uiBoostHeld.remove(unit.id);
        }
        if (!Vars.state.isPaused()) {
            if (fuel > 1 && CheckIfPlayer(unit)) {
                fuel -= fuelUsedPerTick;
                Tmp.v1.trns(unit.rotation + forceDirection, boostForce / 60f);
                unit.vel.x += Tmp.v1.x;
                unit.vel.y += Tmp.v1.y;
            }
            if (!CheckIfPlayer(unit) && fuel < fuelCap) {
                fuel += fuelRegenPerTick;
            }
        }
    }

    @Override
    public void draw(Unit unit) {
        int len = mirror ? 2 : 1;

        if (!warmup.containsKey(unit)) {
            warmup.put(unit, 0f);
        } else if (!Vars.state.isPaused()) {
            warmup.put(unit, Mathf.lerpDelta(warmup.get(unit), fuel > 1 && CheckIfPlayer(unit) ? 1f : 0f, 0.1f));
        }

        for (int s = 0; s < len; s++) {
            float sign = (s == 0 ? -1 : 1);
            Tmp.v1.trns(unit.rotation - 180, setY * -1, setX * sign).add(unit.x, unit.y);
            float rx = Tmp.v1.x;
            float ry = Tmp.v1.y;
            float rot = setRot * sign + unit.rotation() - 180;
            Draw.xscl *= sign;

            float sin = Mathf.sin(Time.time, oscScl, oscMag);
            float wu = warmup.get(unit, 0f);

            for (int i = 0; i < colors.length; i++) {
                Draw.color(colors[i].write(Tmp.c1).mul(0.9f).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
                Drawf.flame(rx, ry, divisions, rot,
                        (length * wu) * lengthWidthPans[i * 3] * (1f - sin),
                        width * lengthWidthPans[i * 3 + 1] * (1f + sin),
                        lengthWidthPans[i * 3 + 2]);
            }

            if (drawFlare) {
                Draw.color(flareColor);
                Draw.z(flareLayer);
                float angle = Time.time * flareRotSpeed + (rotateFlare ? unit.rotation() : 0f);
                for (int i = 0; i < 4; i++) {
                    Drawf.tri(rx, ry, flareWidth, flareLength * (wu + sin), i * 90 + 45 + angle);
                }
                Draw.color();
                for (int i = 0; i < 4; i++) {
                    Drawf.tri(rx, ry, flareWidth * flareInnerScl, flareLength * flareInnerLenScl * (wu + sin), i * 90 + 45 + angle);
                }
            }

            Tmp.v1.trns(rot, (length * wu) * 1.1f);
            Drawf.light(rx, ry, rx + Tmp.v1.x, ry + Tmp.v1.y, lightStroke, lightColor, 0.7f);
            Draw.reset();
        }
    }
}