package axthrix.world.types.abilities;

import arc.Core;
import arc.scene.ui.layout.*;
import arc.util.*;
import axthrix.content.AxthrixStatus;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.Ability;
import mindustry.gen.*;
import mindustry.type.StatusEffect;
import mindustry.world.meta.*;

import static mindustry.Vars.tilesize;

public class ChainHealAbility extends Ability {
    public StatusEffect effect = StatusEffects.overdrive;
    public float duration = 80, amount = 1, reload = 100, range = 60;
    public Effect healEffect = Fx.heal;
    public Effect activeEffect = Fx.healWaveDynamic;
    public boolean parentizeEffects = false, applyToSelf = false;

    protected float timer;
    protected boolean wasHealed = false;

    ChainHealAbility(){}

    public ChainHealAbility(StatusEffect effect, float amount, float reload, float range){
        this.amount = amount;
        this.reload = reload;
        this.range = range;
        this.effect = effect;
    }
    public String localized(){
        return Core.bundle.format("ability.aj-chain-heal");
    }
    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.repairSpeed.localized() + ": [white]" + Strings.autoFixed(amount * 60f / reload, 2) + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.shootRange.localized() + ": [white]" +  Strings.autoFixed(range / tilesize, 2) + " " + StatUnit.blocks.localized());
    }

    @Override
    public void update(Unit unit){
        timer += Time.delta;

        if(timer >= reload){
            wasHealed = false;

            Units.nearby(unit.team, unit.x, unit.y, range, other -> {
                if(other.damaged()){
                    healEffect.at(other, parentizeEffects);
                    wasHealed = true;
                    other.apply(effect, duration);
                    if(!applyToSelf){
                        unit.unapply(effect);
                    }
                }
                other.heal(amount);
            });

            if(wasHealed){
                activeEffect.at(unit, range);
            }

            timer = 0f;
        }
    }
}