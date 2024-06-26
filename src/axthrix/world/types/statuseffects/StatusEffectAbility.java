package axthrix.world.types.statuseffects;

import arc.Events;
import arc.struct.Seq;
import mindustry.entities.abilities.Ability;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.game.EventType;
import mindustry.gen.Unit;
import mindustry.world.meta.Stat;

import java.util.HashMap;

public class StatusEffectAbility extends AxStatusEffect {
    public Ability ability;
    public boolean scaleAbility = false;
    HashMap<Unit,Ability> uAbilitys = new HashMap<>();
    public StatusEffectAbility(String name) {
        super(name);
        permanent = true;
        Events.run(EventType.Trigger.update,()->{
            Seq<Unit> delUnits = new Seq<>();
            uAbilitys.keySet().forEach((u) -> {
                if (!u.isValid() || !u.hasEffect(this))
                    delUnits.add(u);
            });
            delUnits.each(u -> {
                uAbilitys.remove(u);
            });
        });
    }

    @Override
    public void setStats(){
        super.setStats();
        if (ability != null) {
            stats.add(Stat.abilities, ability.localized());
        }
    }

    @Override
    public void start(Unit unit) {
        super.start(unit);
        Ability cpy = ability.copy();
        if (scaleAbility) {
            if (ability instanceof ShieldArcAbility a) {
                ShieldArcAbility c = (ShieldArcAbility) cpy;
                c.max = (a.max/100f)*unit.maxHealth;
                c.x = a.x*unit.hitSize/8;
                c.y = a.y*unit.hitSize/8;
                c.radius = a.radius*unit.hitSize/8;
                c.init(unit.type);
            }
        }
        uAbilitys.put(unit,cpy);
    }

    @Override
    public void update(Unit unit, float time){
        super.update(unit,time);
        if (ability != null && uAbilitys.containsKey(unit))
            uAbilitys.get(unit).update(unit);
    }

    @Override
    public void draw(Unit unit) {
        super.draw(unit);
        if (ability != null && uAbilitys.containsKey(unit))
            uAbilitys.get(unit).draw(unit);
    }
}
