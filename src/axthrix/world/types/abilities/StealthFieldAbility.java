package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.util.Strings;
import arc.util.Time;
import axthrix.world.types.unittypes.CopterUnitType;
import mindustry.entities.abilities.*;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import arc.scene.ui.layout.Table;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;


public class StealthFieldAbility extends Ability{
    public float vulnerabilityTime = 260f;

    protected float timer;
    protected boolean cloaked;

    public StealthFieldAbility(){
    }
    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-stealth-field");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]Activation Delay: [white]" + Strings.autoFixed(vulnerabilityTime / 60f, 2) + " " + StatUnit.seconds.localized());
        t.row();
    }

    public void update(CopterUnitType unity, Unit unit) {
        timer += Time.delta;

        if(timer >= vulnerabilityTime){
            Draw.mixcol(Pal.shadow, 0.6f * (1f - timer/vulnerabilityTime));
            Draw.alpha(Math.max(0.1f, (1f - timer/vulnerabilityTime)));
            cloaked = true;
            unity.targetable = false;
        }
        if(unit.isShooting()){
            cloaked = false;
            unity.targetable = true;
            unity.applyColor(unit);
            timer = 0;
        }
    }
}