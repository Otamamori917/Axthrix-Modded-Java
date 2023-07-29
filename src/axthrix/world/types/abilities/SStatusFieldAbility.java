package axthrix.world.types.abilities;

import arc.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;

public class SStatusFieldAbility extends Ability{
    public StatusEffect effect;
    public float duration = 60, reload = 100, range = 20;
    public boolean onShoot = false, atNotShoot = false;
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
        return Core.bundle.format("ability.sstatusfield", effect.emoji());
    }

    @Override
    public void update(Unit unit){
        timer += Time.delta;

        if(timer >= reload){
            if(!atNotShoot && unit.isShooting || !onShoot && !unit.isShooting){
                Units.nearby(unit.team, unit.x, unit.y, range, other -> {
                    other.apply(effect, duration);
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