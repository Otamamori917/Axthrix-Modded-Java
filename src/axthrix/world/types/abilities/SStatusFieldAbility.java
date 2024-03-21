package axthrix.world.types.abilities;

import arc.*;
import arc.math.*;
import arc.scene.ui.layout.Table;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class SStatusFieldAbility extends Ability{
    public StatusEffect effect;
    public float duration = 60, reload = 100, range = 20;
    public boolean onShoot = false, atNotShoot = false, applyToSelf = true;
    public Effect applyEffect = Fx.none;
    public Effect activeEffect = Fx.overdriveWave;
    public float effectX, effectY;
    public boolean parentizeEffects, effectSizeParam = true;

    protected float timer;

    SStatusFieldAbility(){}

    public SStatusFieldAbility(StatusEffect effect, float duration, float reload, float range){
        this.duration = duration;
        this.reload = reload;
        this.range = range;
        this.effect = effect;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-sstatusfield");
    }
    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.reload.localized() + ": [white]" + Strings.autoFixed(60f / reload, 2) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(range / tilesize, 2) + " " + StatUnit.blocks.localized());
        t.row();
        t.add(effect.emoji() + " " + effect.localizedName);
    }
    @Override
    public void update(Unit unit){
        timer += Time.delta;

        if(timer >= reload){
            if(!atNotShoot && unit.isShooting || !onShoot && !unit.isShooting){
                Units.nearby(unit.team, unit.x, unit.y, range, other -> {
                    other.apply(effect, duration);
                    if(!applyToSelf){
                        unit.unapply(effect);
                    }
                    applyEffect.at(other, parentizeEffects);
                });  
                float x = unit.x + Angles.trnsx(unit.rotation, effectY, effectX), y = unit.y + Angles.trnsy(unit.rotation, effectY, effectX);
                activeEffect.at(x, y, effectSizeParam ? range : unit.rotation, parentizeEffects ? unit : null);      
            }    
            timer %= reload;    

            timer = 0f;
        }
    }
}
