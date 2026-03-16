package axthrix.world.types.block.effect;

import arc.Core;
import arc.func.Func;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Strings;
import arc.util.Time;
import axthrix.world.util.AxStats;
import mindustry.core.UI;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;

public class GeneratorCoreBlock extends AxCore{
    public float powerProduction = 60 / 60f;
    public float costRate = 60f; // Ticks per item consumed
    public ObjectMap<Item, Float> itemBoosts = new ObjectMap<>();

    public GeneratorCoreBlock(String name){
        super(name);
        hasPower = true;
        conductivePower = true;
        outputsPower = true;
        consumesPower = false;
    }
    /// item boosters
    public void addBoost(Item item, float boostMultiplier){
        itemBoosts.put(item, boostMultiplier);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.basePowerGeneration, powerProduction * 60f, StatUnit.powerSecond);

        if(itemBoosts.size > 0){
            stats.add(Stat.booster, par -> {
                par.row();

                par.table(Styles.grayPanel, table -> {
                    table.defaults().left().padLeft(2.5f).padTop(1.5f);

                    table.add("[lightgray]"+Core.bundle.format("stat.aj-consumes")+": [white]" + Strings.autoFixed(60f / costRate, 2) + " " + StatUnit.perSecond.localized())
                            .style(Styles.outlineLabel);
                    table.row();
                    for(var entry : itemBoosts){
                        Item item = entry.key;
                        float boost = entry.value;

                        // Display item with icon
                        table.image(item.uiIcon).size(32f).padRight(8f);
                        table.add(item.localizedName).color(item.color).padRight(4f);
                        table.row();

                        // Display boost percentage
                        table.add("[stat]+" + Strings.autoFixed(boost * 100f, 1) + "% [lightgray]" +
                                Core.bundle.get("stat.boosteffect")).style(Styles.outlineLabel);
                        table.row();
                        table.add("");
                        table.row();

                    }

                }).margin(5).pad(5f);

                par.row();

            });
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("poweroutput", (GeneratorCoreBlockBuild entity) -> {
            return new Bar(
                    () -> {
                        float currentPower = entity.getCurrentPowerProduction();
                        return Core.bundle.format("bar.poweroutput", Strings.fixed(currentPower * 60f, 1));
                    },
                    () -> Pal.powerBar,
                    () -> 1f
            );
        });

        addBar("power", makePowerBalance());
    }

    public static Func<Building, Bar> makePowerBalance(){
        return entity -> new Bar(() ->
                Core.bundle.format("bar.powerbalance",
                        ((entity.power.graph.getPowerBalance() >= 0 ? "+" : "") + UI.formatAmount((long)(entity.power.graph.getPowerBalance() * 60 + 0.0001f)))),
                () -> Pal.powerBar,
                () -> Mathf.clamp(entity.power.graph.getLastPowerProduced() / entity.power.graph.getLastPowerNeeded())
        );
    }

    public class GeneratorCoreBlockBuild extends CoreBuild{
        public float consumeTimer = 0f;
        public Item currentBoostItem = null;

        @Override
        public void updateTile(){
            super.updateTile();

            // Update consumption timer
            consumeTimer += Time.delta;

            if(consumeTimer >= costRate && currentBoostItem != null){
                // Consume one item
                if(items.has(currentBoostItem)){
                    items.remove(currentBoostItem, 1);
                }
                consumeTimer = 0f;
            }
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            if(!allowUpdate()){
                enabled = false;
            }
        }

        public float getCurrentPowerProduction(){
            if(!enabled) return 0f;

            // Find highest boost from items in core
            float highestBoost = 0f;
            Item bestItem = null;

            for(var entry : itemBoosts){
                Item item = entry.key;
                float boost = entry.value;

                // Check if core has this item
                if(items.has(item) && boost > highestBoost){
                    highestBoost = boost;
                    bestItem = item;
                }
            }

            // Store the current boost item for consumption
            currentBoostItem = bestItem;

            // Apply boost: base power * (1 + boost)
            return powerProduction * (1f + highestBoost);
        }

        @Override
        public float getPowerProduction(){
            return getCurrentPowerProduction();
        }
    }
}