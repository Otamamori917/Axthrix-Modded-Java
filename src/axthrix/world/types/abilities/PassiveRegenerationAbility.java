package axthrix.world.types.abilities;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class PassiveRegenerationAbility extends Ability {
    public float healAmount = 2f; // Heal per tick
    public float healDelay = 180f; // 3 seconds out of combat
    public float detectionRange = 120f; // Range to check for enemies

    protected float timer = 0f;

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-passive-regeneration");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.healing.localized() + ": [white]" + healAmount +" "+ StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-out-of-combat")+": [white]" + (int)(healDelay / 60f) +" "+ StatUnit.seconds.localized());
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-detect") + Stat.shootRange.localized() + ": [white]" + (int)(detectionRange / 8f) +" "+ StatUnit.blocks.localized());
        t.row();
    }

    @Override
    public void update(Unit unit){

        boolean enemiesNearby = Units.closestEnemy(unit.team, unit.x, unit.y, detectionRange, u -> true) != null;

        if(enemiesNearby){
            timer = 0f;
        }else{
            timer += Time.delta;

            if(timer >= healDelay && unit.damaged()){
                unit.heal(healAmount);
            }
        }
    }
}
