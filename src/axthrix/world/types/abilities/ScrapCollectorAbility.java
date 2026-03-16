package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

public class ScrapCollectorAbility extends Ability {
    public float collectionRange = 80f;
    public Item itemType;
    public float armorMultiplier = 1f; // Amount = enemy armor * multiplier

    public ScrapCollectorAbility(){}

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-scrap-collector");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.range.localized() + ": [white]" + (int)(collectionRange / 8f) +" "+ StatUnit.blocks.localized());
        t.row();
        // Put label and item on same row without breaking
        t.table(row -> {
            row.add("[lightgray]"+ Core.bundle.format("stat.aj-collects"));
            row.add(StatValues.displayItem(itemType, 0, true)).padLeft(4f);
        }).left();
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-amount")+" [white]"+Core.bundle.format("stat.aj-armor")+" × " + Strings.autoFixed(armorMultiplier, 1));
        t.row();
    }

    @Override
    public void init(UnitType type){
        Events.on(EventType.UnitDestroyEvent.class, event -> {
            mindustry.gen.Groups.unit.each(unit -> {
                if(unit.abilities != null){
                    for(var ability : unit.abilities){
                        if(ability instanceof ScrapCollectorAbility collector){
                            collector.onUnitDeath(unit, event.unit);
                        }
                    }
                }
            });
        });
    }

    protected void onUnitDeath(Unit collector, Unit killed){
        if(killed.team != collector.team && collector.within(killed, collectionRange)){
            if(collector.stack.amount == 0){
                collector.stack.item = itemType;
            } else if (collector.stack.item != itemType) {
                return;
            }
            int amount = (int)((killed.armor + 1) * armorMultiplier);
            collector.stack.amount += amount;
        }
    }
}