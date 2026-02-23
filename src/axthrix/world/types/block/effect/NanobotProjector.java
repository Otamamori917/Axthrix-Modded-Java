package axthrix.world.types.block.effect;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.*;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.AxDrawf;
import axthrix.world.util.AxStatValues;
import axthrix.world.util.AxStats;
import axthrix.world.util.NanobotLogic;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static axthrix.content.AxthrixStatus.nanodiverge;
import static mindustry.Vars.*;
import static mindustry.Vars.tilesize;
import static mindustry.entities.part.DrawPart.params;

public class NanobotProjector extends AxBlock {

    public float damage = 10f;
    public float healAmount = 2f;
    public float healPercent = 1f;
    public float range = 90f;
    public float baseTickRate = 30f; // Base tick rate without liquid
    public float buildingDamageMultiplier = 0.25f;

    public float bulletSpeedBonus = 1.10f;
    public float bulletSlowdown = 0.9f;

    public float efficiencyBoost = 1.5f;
    public float stackPenalty = 0.5f;

    public StatusEffect status = nanodiverge;
    public float statusDuration = 60f * 2f;

    public Color color = Pal.heal;
    public Color color2 = Pal.accent;
    public int nanobotCount = 20;
    public float nanobotSize = 1f;
    public float nanobotSpeed = 4f;

    public float liquidBoost = 2f;

    public NanobotProjector(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        hasPower = true;
        hasLiquids = true;
        outputsLiquid = false;
        configurable = false;
        canOverdrive = false;
    }

    /*@Override
    public void setBars(){
        super.setBars();

        addBar("nanobots", (NanobotProjectorBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.aj-nanobots"),
                () -> entity.hasNanobots() ? color : Color.gray,
                () -> entity.hasNanobots() ? 1f : 0f
        ));
    }*/

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.range, range/8, StatUnit.blocks);
        stats.add(Stat.damage, damage * (60f / baseTickRate), StatUnit.perSecond);
        stats.add(Stat.damage, "[lightgray]To Buildings:[] " + (damage * buildingDamageMultiplier * (60f / baseTickRate)), StatUnit.perSecond);
        stats.add(AxStats.healingF, healAmount * (60f / baseTickRate) + " + " +healPercent  * (60f / baseTickRate) + "% " + StatUnit.perSecond.localized(), StatUnit.perSecond);
        stats.add(AxStats.bulletEffect, "[]Ally: +" + (int)(bulletSpeedBonus * 100f) * (60f / baseTickRate) + "% "+ StatUnit.perSecond.localized());
        stats.add(AxStats.bulletEffect, "[]Enemy: -" + (int)((bulletSlowdown) * 100f) * (60f / baseTickRate) + "% "+ StatUnit.perSecond.localized());

        stats.add(AxStats.boosterLiq, (int)(liquidBoost * 100f) + "%");
        stats.add(AxStats.damageB, damage * (60f / (baseTickRate / liquidBoost)), StatUnit.perSecond);
        stats.add(AxStats.damageB, "[lightgray]To Buildings:[] " + (damage * buildingDamageMultiplier * (60f / (baseTickRate / liquidBoost))), StatUnit.perSecond);
        stats.add(AxStats.healingFB, healAmount * (60f / (baseTickRate / liquidBoost)) + " + " +healPercent  * (60f / (baseTickRate / liquidBoost)) + "% " + StatUnit.perSecond.localized(), StatUnit.perSecond);
        stats.add(AxStats.bulletEffectB, "[]Ally: +" + (int)(bulletSpeedBonus * 100f) * (60f / (baseTickRate / liquidBoost)) + "% "+ StatUnit.perSecond.localized());
        stats.add(AxStats.bulletEffectB, "[]Enemy: -" + (int)((bulletSlowdown) * 100f) * (60f / (baseTickRate / liquidBoost)) + "% "+ StatUnit.perSecond.localized());


        for(var cons : consumeBuilder){
            if(cons instanceof ConsumeItems itm){
                for (ItemStack stack : itm.items) {
                    stats.add(AxStats.boosterItm, (int)((efficiencyBoost -1)*100) + "%" + " To Nearby Ally Blocks If " + stack.item.localizedName + " Is Present\n[lightgray]Costs:[]"+stack.amount * (60f / baseTickRate) + StatUnit.perSecond.localized());
                }
            }
        }
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        super.drawPlace(x, y, rotation, valid);

        Drawf.dashCircle(x * tilesize + offset, y * tilesize + offset, range, color);

        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, other -> true, other -> Drawf.selected(other, Tmp.c1.set(color).a(Mathf.absin(4f, 1f))));
        indexer.eachBlock(player.team(), x * tilesize + offset, y * tilesize + offset, range, other -> other.block.canOverdrive, other -> Drawf.selected(other, Tmp.c1.set(color2).a(Mathf.absin(1f, 4f))));

    }

    public class NanobotProjectorBuild extends Building {
        public float timer = 0f;
        public float soundTimer = 0f;
        public float boostTimer = 0f;
        public NanobotLogic.NanobotParams params = new NanobotLogic.NanobotParams();

        @Override
        public void drawSelect(){

            indexer.eachBlock(player.team(),x,y,range, other -> true, building -> {
                if(building.block.canOverdrive){
                    Drawf.selected(building, Tmp.c1.set(color2).a(Mathf.absin(1f, 4f)));
                }
                Drawf.selected(building, Tmp.c1.set(color).a(Mathf.absin(4f, 1f)));
            });

            Drawf.dashCircle(x, y, range, color);
        }

        @Override
        public void updateTile(){
            boolean hasPower = power != null && power.status > 0.01f;
            boolean hasNanobots = hasNanobots();
            boolean hasItems = items != null && items.total() > 0;

            if(!hasPower && hasNanobots){
                NanobotLogic.removeNanobots(this);
                return;
            }

            if(hasPower && !hasNanobots){
                NanobotLogic.initNanobots(this, x, y, nanobotCount);
            }

            if(!hasNanobots) return;

            float currentTickRate = baseTickRate;
            if(liquids.currentAmount() > 0.01f){
                currentTickRate /= liquidBoost;

                float consumeAmount = 0f;
                for(var cons : consumeBuilder){
                    if(cons instanceof ConsumeLiquidBase liq){
                        consumeAmount = liq.amount;
                    }
                }

                liquids.remove(liquids.current(),consumeAmount);
            }

            timer += delta();
            boostTimer += delta();
            soundTimer += delta();

            // Setup parameters
            params.x = x;
            params.y = y;
            params.damage = damage;
            params.healAmount = healAmount;
            params.healPercent = healPercent;
            params.range = range;
            params.tickRate = currentTickRate;
            params.buildingDamageMultiplier = buildingDamageMultiplier;
            params.bulletSpeedBonus = bulletSpeedBonus;
            params.bulletSlowdown = bulletSlowdown;
            params.efficiencyBoost = efficiencyBoost;
            params.stackPenalty = stackPenalty;
            params.status = status;
            params.statusDuration = statusDuration;
            params.color = color;
            params.nanobotCount = nanobotCount;
            params.nanobotSize = nanobotSize;
            params.nanobotSpeed = nanobotSpeed;
            params.team = team;
            params.hasNanobot = false;

            // Do regular nanobot actions (heal, damage, bullets) - no item required
            NanobotLogic.updateNanobots(this, params, timer, false, () -> {
                timer = 0f;
            });

            // Boost blocks only if we have items
            if(hasItems && boostTimer >= baseTickRate){
                boostTimer = 0f;

                NanobotLogic.updateBoost(this,params);

                int consumeAmount = 0;
                for(var cons : consumeBuilder){
                    if(cons instanceof ConsumeItems itm){
                        for (ItemStack stack : itm.items) {
                            consumeAmount = stack.amount;
                        }
                    }
                }

                // Consume item for boost
                items.remove(items.first(), consumeAmount);
            }

            NanobotLogic.cleanupInvalid();
        }

        @Override
        public void draw(){
            super.draw();

            if(!hasNanobots()) return;

            NanobotLogic.drawNanobots(this, params);

            //if(Mathf.equal(renderer.minimap.getHudOpacity(), 1f, 0.1f)){
                Drawf.dashCircle(x, y, range, color);
            //}
        }

        public boolean hasNanobots(){
            return NanobotLogic.getNanobots(this) != null;
        }

        @Override
        public void onRemoved(){
            super.onRemoved();
            NanobotLogic.removeNanobots(this);
        }
    }
}