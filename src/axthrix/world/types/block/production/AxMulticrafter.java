package axthrix.world.types.block.production;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.ArcRuntimeException;
import arc.util.Eachable;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.block.AxBlock;
import axthrix.world.util.AxMulticrafterAnalyzer;
import axthrix.world.util.AxRecipe;
import axthrix.world.util.AxRecipeSelector;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.logic.LAccess;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Bar;
import mindustry.world.blocks.heat.HeatBlock;
import mindustry.world.blocks.heat.HeatConsumer;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;
import multicraft.*;

public class AxMulticrafter extends AxBlock {
    public float itemCapacityMultiplier = 1.0F;
    public float fluidCapacityMultiplier = 1.0F;
    public float powerCapacityMultiplier = 1.0F;
    public Object recipes;
    @Nullable
    public Seq<AxRecipe> resolvedRecipes = null;
    public String menu = "transform";
    @Nullable
    public AxRecipeSelector selector = null;
    public Effect craftEffect;
    public Effect updateEffect;
    public Effect changeRecipeEffect;
    public int[] fluidOutputDirections;
    public float updateEffectChance;
    public float warmupSpeed;
    public boolean ignoreLiquidFullness;
    public boolean dumpExtraFluid;
    public DrawBlock drawer;
    protected boolean isOutputItem;
    protected boolean isConsumeItem;
    protected boolean isConsumeFluid;
    protected boolean isConsumePower;
    protected boolean isOutputHeat;
    protected boolean isConsumeHeat;
    public Color heatColor;
    public float powerCapacity;
    public int defaultRecipeIndex;
    public float overheatScale;
    public float maxEfficiency;
    public float warmupRate;
    protected boolean showNameTooltip;
    @Nullable
    protected static Table hoveredInfo;

    public AxMulticrafter(String name) {
        super(name);
        craftEffect = Fx.none;
        updateEffect = Fx.none;
        changeRecipeEffect = Fx.rotateBlock;
        fluidOutputDirections = new int[]{-1};
        updateEffectChance = 0.04F;
        warmupSpeed = 0.019F;
        ignoreLiquidFullness = false;
        dumpExtraFluid = true;
        drawer = new DrawDefault();
        isOutputItem = false;
        isConsumeItem = false;
        isConsumeFluid = false;
        isConsumePower = false;
        isOutputHeat = false;
        isConsumeHeat = false;
        heatColor = new Color(1.0F, 0.22F, 0.22F, 0.8F);
        powerCapacity = 0.0F;
        defaultRecipeIndex = 0;
        overheatScale = 1.0F;
        maxEfficiency = 4.0F;
        warmupRate = 0.15F;
        showNameTooltip = false;
        update = true;
        solid = true;
        sync = true;
        flags = EnumSet.of(new BlockFlag[]{BlockFlag.factory});
        ambientSound = Sounds.machine;
        configurable = true;
        saveConfig = true;
        ambientSoundVolume = 0.03F;
        config(Integer.class, multicraft.MultiCrafter.MultiCrafterBuild::setCurRecipeIndexFromRemote);
        Log.info("MultiCrafter[" + name + "] loaded.");
    }

    public void init() {
        hasItems = false;
        hasPower = false;
        hasLiquids = false;
        outputsPower = false;
        if (resolvedRecipes == null && recipes != null) {
            resolvedRecipes = AxMulticrafterAnalyzer.analyze(this, recipes);
        }

        if (resolvedRecipes != null && !resolvedRecipes.isEmpty()) {
            if (selector == null) {
                selector = AxRecipeSelector.get(menu);
            }

            decorateRecipes();
            setupBlockByRecipes();
            defaultRecipeIndex = Mathf.clamp(defaultRecipeIndex, 0, resolvedRecipes.size - 1);
            recipes = null;
            setupConsumers();
            super.init();
        } else {
            throw new ArcRuntimeException(MultiCrafterAnalyzer.genName(this) + " has no recipe! It's perhaps because all recipes didn't find items or fluids they need. Check your `last_log.txt` to obtain more information.");
        }
    }

    public void setStats() {
        super.setStats();
        stats.add(Stat.output, (t) -> {
            showNameTooltip = true;
            buildStats(t);
            showNameTooltip = false;
        });
    }

    public void buildStats(Table stat) {
        stat.row();

        for(AxRecipe recipe : resolvedRecipes) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkestGray);
            buildIOEntry(t, recipe, true);
            Table time = new Table();
            float[] duration = new float[]{0.0F};
            float visualCraftTime = recipe.craftTime;
            time.update(() -> {
                duration[0] += Time.delta;
                if (duration[0] > visualCraftTime) {
                    duration[0] = 0.0F;
                }

            });
            String craftTime = recipe.craftTime == 0.0F ? "0" : String.format("%.2f", recipe.craftTime / 60.0F);
            Cell<Bar> barCell = time.add(new Bar(() -> craftTime, () -> Pal.accent, () -> Interp.smooth.apply(duration[0] / visualCraftTime))).height(45.0F);
            if (Vars.mobile) {
                barCell.width(220.0F);
            } else {
                barCell.width(250.0F);
            }

            Cell<Table> timeCell = t.add(time).pad(12.0F);
            if (showNameTooltip) {
                timeCell.tooltip(Stat.productionTime.localized() + ": " + craftTime + " " + StatUnit.seconds.localized());
            }

            buildIOEntry(t, recipe, false);
            stat.add(t).pad(10.0F).grow();
            stat.row();
        }

        stat.row();
        stat.defaults().grow();
    }

    public void buildIOEntry(Table table, AxRecipe recipe, boolean isInput) {
        Table t = new Table();
        if (isInput) {
            t.left();
        } else {
            t.right();
        }

        Table mat = new Table();
        IOEntry entry = isInput ? recipe.input : recipe.output;
        int i = 0;

        for(ItemStack stack : entry.items) {
            Cell<Table> iconCell = mat.add(StatValues.displayItem(stack.item, stack.amount)).pad(2.0F);
            if (showNameTooltip) {
                iconCell.tooltip(stack.item.localizedName);
            }

            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (i != 0 && i % 2 == 0) {
                mat.row();
            }

            ++i;
        }

        ++i;

        for(LiquidStack stack : entry.fluids) {
            Cell<FluidImage> iconCell = mat.add(new FluidImage(stack.liquid.uiIcon, stack.amount * 60.0F)).pad(2.0F);
            if (showNameTooltip) {
                iconCell.tooltip(stack.liquid.localizedName);
            }

            if (isInput) {
                iconCell.left();
            } else {
                iconCell.right();
            }

            if (i != 0 && i % 2 == 0) {
                mat.row();
            }

            ++i;
        }

        Cell<Table> matCell = t.add(mat);
        if (isInput) {
            matCell.left();
        } else {
            matCell.right();
        }

        t.row();
        if (entry.power > 0.0F) {
            Table power = new Table();
            power.image(Icon.power).color(Pal.power);
            Cell<Label> textCell = power.add((isInput ? "-" : "+") + (int)(entry.power * 60.0F));
            if (isInput) {
                textCell.color(Pal.remove);
            } else {
                textCell.color(Pal.powerLight);
            }

            if (isInput) {
                power.left();
            } else {
                power.right();
            }

            Cell<Table> powerCell = t.add(power).grow();
            if (showNameTooltip) {
                powerCell.tooltip(entry.power + " " + StatUnit.powerSecond.localized());
            }

            t.row();
        }

        if (entry.heat > 0.0F) {
            Table heat = new Table();
            heat.image(Icon.terrain).color(heatColor);
            if (isInput) {
                heat.left();
            } else {
                heat.right();
            }

            Cell<Table> heatCell = t.add(heat).grow();
            if (showNameTooltip) {
                heatCell.tooltip(entry.heat + " " + StatUnit.heatUnits.localized());
            }

            t.row();
        }

        Cell<Table> tCell = table.add(t).pad(12.0F).fill();
        tCell.width(120.0F);
    }

    public void setBars() {
        super.setBars();
        addBar("progress", (b) -> {
            Color var10003 = Pal.accent;
            b.getClass();
            return new Bar("bar.loadprogress", var10003, b::progress);
        });
        if (isConsumeHeat || isOutputHeat) {
            addBar("heat", (b) -> {
                if (b instanceof AxMulticrafter.MultiCrafterBuild axb) {
                    Color var10003 = Pal.lightOrange;
                    b.getClass();
                    return new Bar("bar.heat", var10003, axb::heatFrac);
                }
                return null;
            });
        }

    }

    public boolean rotatedOutput(int x, int y) {
        return false;
    }

    public void load() {
        super.load();
        drawer.load(this);
    }

    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
        drawer.drawPlan(this, plan, list);
    }

    public TextureRegion[] icons() {
        return drawer.finalIcons(this);
    }

    public void getRegionsToOutline(Seq<TextureRegion> out) {
        drawer.getRegionsToOutline(this, out);
    }

    public boolean outputsItems() {
        return isOutputItem;
    }

    public void drawOverlay(float x, float y, int rotation) {
        AxRecipe firstRecipe = (AxRecipe)resolvedRecipes.get(defaultRecipeIndex);
        Seq<LiquidStack> fluids = firstRecipe.output.fluids;

        for(int i = 0; i < fluids.size; ++i) {
            int dir = fluidOutputDirections.length > i ? fluidOutputDirections[i] : -1;
            if (dir != -1) {
                Draw.rect(((LiquidStack)fluids.get(i)).liquid.fullIcon, x + (float)Geometry.d4x(dir + rotation) * ((float)(size * 8) / 2.0F + 4.0F), y + (float)Geometry.d4y(dir + rotation) * ((float)(size * 8) / 2.0F + 4.0F), 8.0F, 8.0F);
            }
        }

    }

    protected void decorateRecipes() {
        resolvedRecipes.shrink();

        for(AxRecipe recipe : resolvedRecipes) {
            recipe.shrinkSize();
            recipe.cacheUnique();
        }

    }

    protected void setupBlockByRecipes() {
        int maxItemAmount = 0;
        float maxFluidAmount = 0.0F;
        float maxPower = 0.0F;

        for(AxRecipe recipe : resolvedRecipes) {
            maxItemAmount = Math.max(recipe.maxItemAmount(), maxItemAmount);
            maxFluidAmount = Math.max(recipe.maxFluidAmount(), maxFluidAmount);
            hasItems |= recipe.hasItem();
            hasLiquids |= recipe.hasFluid();
            maxPower = Math.max(recipe.maxPower(), maxPower);
            isOutputItem |= recipe.isOutputItem();
            isConsumeItem |= recipe.isConsumeItem();
            isConsumeFluid |= recipe.isConsumeFluid();
            isConsumeHeat |= recipe.isConsumeHeat();
            isOutputHeat |= recipe.isOutputHeat();
        }

        hasPower = maxPower > 0.0F;
        outputsPower = hasPower;
        itemCapacity = Math.max((int)((float)maxItemAmount * itemCapacityMultiplier), itemCapacity);
        liquidCapacity = Math.max((float)((int)(maxFluidAmount * 60.0F * fluidCapacityMultiplier)), liquidCapacity);
        powerCapacity = Math.max(maxPower * 60.0F * powerCapacityMultiplier, powerCapacity);
        if (isOutputHeat) {
            rotate = true;
            rotateDraw = false;
            canOverdrive = false;
            drawArrow = true;
        }

    }

    protected void setupConsumers() {
        if (isConsumeItem) {
            consume(new ConsumeItemDynamic((build) -> {
                if (build instanceof AxMulticrafter.MultiCrafterBuild axb) {
                    return axb.getCurRecipe().input.items.items;
                }
                return new ItemStack[0];
            }));
        }

        if (isConsumeFluid) {
            consume(new ConsumeFluidDynamic((build) -> {
                if (build instanceof AxMulticrafter.MultiCrafterBuild axb) {
                    return axb.getCurRecipe().input.fluids.items;
                }
                return new LiquidStack[0];
            }));
        }

        if (hasPower) {
            consumePowerBuffered(powerCapacity);
        }

    }

    public class MultiCrafterBuild extends Building implements HeatBlock, HeatConsumer {
        public float[] sideHeat = new float[4];
        public float heat = 0.0F;
        public float craftingTime;
        public float warmup;
        public int curRecipeIndex;

        public MultiCrafterBuild() {
            curRecipeIndex = defaultRecipeIndex;
        }

        public void setCurRecipeIndexFromRemote(int index) {
            int newIndex = Mathf.clamp(index, 0, resolvedRecipes.size - 1);
            if (newIndex != curRecipeIndex) {
                curRecipeIndex = newIndex;
                createEffect(changeRecipeEffect);
                craftingTime = 0.0F;
                if (!Vars.headless) {
                    rebuildHoveredInfo();
                }
            }

        }

        public AxRecipe getCurRecipe() {
            curRecipeIndex = Mathf.clamp(curRecipeIndex, 0, resolvedRecipes.size - 1);
            return (AxRecipe) resolvedRecipes.get(curRecipeIndex);
        }

        public boolean acceptItem(Building source, Item item) {
            return hasItems && getCurRecipe().input.itemsUnique.contains(item) && items.get(item) < getMaximumAccepted(item);
        }

        public boolean acceptLiquid(Building source, Liquid liquid) {
            return hasLiquids && getCurRecipe().input.fluidsUnique.contains(liquid) && liquids.get(liquid) < liquidCapacity;
        }

        public float edelta() {
            AxRecipe cur = getCurRecipe();
            return cur.input.power > 0.0F ? efficiency * Mathf.clamp(getCurPowerStore() / cur.input.power) * delta() : efficiency * delta();
        }

        public void updateTile() {
            AxRecipe cur = getCurRecipe();
            float craftTimeNeed = cur.craftTime;
            if (cur.isConsumeHeat()) {
                heat = calculateHeat(sideHeat);
            }

            if (cur.isOutputHeat()) {
                float heatOutput = cur.output.heat;
                heat = Mathf.approachDelta(heat, heatOutput * efficiency, warmupRate * edelta());
            }

            if (efficiency > 0.0F && (!hasPower || getCurPowerStore() >= cur.input.power)) {
                if (craftTimeNeed > 0.0F) {
                    craftingTime += edelta();
                }

                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                if (hasPower) {
                    float powerChange = (cur.output.power - cur.input.power) * delta();
                    if (!Mathf.zero(powerChange)) {
                        setCurPowerStore(getCurPowerStore() + powerChange);
                    }
                }

                if (cur.isOutputFluid()) {
                    float increment = getProgressIncrease(1.0F);

                    for(LiquidStack output : cur.output.fluids) {
                        Liquid fluid = output.liquid;
                        handleLiquid(this, fluid, Math.min(output.amount * increment, liquidCapacity - liquids.get(fluid)));
                    }
                }

                if (wasVisible && Mathf.chanceDelta((double) updateEffectChance)) {
                    updateEffect.at(x + Mathf.range((float) size * 4.0F), y + (float)Mathf.range(size * 4));
                }
            } else {
                warmup = Mathf.approachDelta(warmup, 0.0F, warmupSpeed);
            }

            if (craftTimeNeed <= 0.0F) {
                if (efficiency > 0.0F) {
                    craft();
                }
            } else if (craftingTime >= craftTimeNeed) {
                craft();
            }

            dumpOutputs();
        }

        public boolean shouldConsume() {
            AxRecipe cur = getCurRecipe();
            if (hasItems) {
                for(ItemStack output : cur.output.items) {
                    if (items.get(output.item) + output.amount > itemCapacity) {
                        return false;
                    }
                }
            }

            if (hasLiquids && cur.isOutputFluid() && !ignoreLiquidFullness) {
                boolean allFull = true;

                for(LiquidStack output : cur.output.fluids) {
                    if (liquids.get(output.liquid) >= liquidCapacity - 0.001F) {
                        if (!dumpExtraFluid) {
                            return false;
                        }
                    } else {
                        allFull = false;
                    }
                }

                if (allFull) {
                    return false;
                }
            }

            return enabled;
        }

        public void craft() {
            consume();
            AxRecipe cur = getCurRecipe();
            if (cur.isOutputItem()) {
                for(ItemStack output : cur.output.items) {
                    for(int i = 0; i < output.amount; ++i) {
                        offload(output.item);
                    }
                }
            }

            if (wasVisible) {
                createCraftEffect();
            }

            if (cur.craftTime > 0.0F) {
                craftingTime %= cur.craftTime;
            } else {
                craftingTime = 0.0F;
            }

        }

        public void createCraftEffect() {
            AxRecipe cur = getCurRecipe();
            Effect curFx = cur.craftEffect;
            Effect fx = curFx != Fx.none ? curFx : craftEffect;
            createEffect(fx);
        }

        public void dumpOutputs() {
            AxRecipe cur = getCurRecipe();
            if (cur.isOutputItem() && timer(timerDump, 5.0F / timeScale)) {
                for(ItemStack output : cur.output.items) {
                    dump(output.item);
                }
            }

            if (cur.isOutputFluid()) {
                Seq<LiquidStack> fluids = cur.output.fluids;

                for(int i = 0; i < fluids.size; ++i) {
                    int dir = fluidOutputDirections.length > i ? fluidOutputDirections[i] : -1;
                    dumpLiquid(((LiquidStack)fluids.get(i)).liquid, 2.0F, dir);
                }
            }

        }

        public float heat() {
            return heat;
        }

        public float heatFrac() {
            AxRecipe cur = getCurRecipe();
            if (isOutputHeat && cur.isOutputHeat()) {
                return heat / cur.output.heat;
            } else {
                return isConsumeHeat && cur.isConsumeHeat() ? heat / cur.input.heat : 0.0F;
            }
        }

        public float[] sideHeat() {
            return sideHeat;
        }

        public float heatRequirement() {
            AxRecipe cur = getCurRecipe();
            return isConsumeHeat && cur.isConsumeHeat() ? cur.input.heat : 0.0F;
        }

        public void buildConfiguration(Table table) {
            if (block instanceof AxMulticrafter amc){
                selector.build(amc, this, table);
            }
        }

        public float getCurPowerStore() {
            return power == null ? 0.0F : power.status * powerCapacity;
        }

        public void setCurPowerStore(float powerStore) {
            if (power != null) {
                power.status = Mathf.clamp(powerStore / powerCapacity);
            }
        }

        public void draw() {
            drawer.draw(this);
        }

        public void drawLight() {
            super.drawLight();
            drawer.drawLight(this);
        }

        public Object config() {
            return curRecipeIndex;
        }

        public boolean shouldAmbientSound() {
            return efficiency > 0.0F;
        }

        public double sense(LAccess sensor) {
            if (sensor == LAccess.progress) {
                return (double)progress();
            } else {
                return sensor == LAccess.heat ? (double)warmup() : super.sense(sensor);
            }
        }

        public void write(Writes write) {
            super.write(write);
            write.f(craftingTime);
            write.f(warmup);
            write.i(curRecipeIndex);
            write.f(heat);
        }

        public void read(Reads read, byte revision) {
            super.read(read, revision);
            craftingTime = read.f();
            warmup = read.f();
            curRecipeIndex = Mathf.clamp(read.i(), 0, resolvedRecipes.size - 1);
            heat = read.f();
        }

        public float warmupTarget() {
            AxRecipe cur = getCurRecipe();
            return isConsumeHeat && cur.isConsumeHeat() ? Mathf.clamp(heat / cur.input.heat) : 1.0F;
        }

        public void updateEfficiencyMultiplier() {
            AxRecipe cur = getCurRecipe();
            if (isConsumeHeat && cur.isConsumeHeat()) {
                efficiency *= efficiencyScale();
                potentialEfficiency *= efficiencyScale();
            }

        }

        public float efficiencyScale() {
            AxRecipe cur = getCurRecipe();
            if (isConsumeHeat && cur.isConsumeHeat()) {
                float heatRequirement = cur.input.heat;
                float over = Math.max(heat - heatRequirement, 0.0F);
                return Math.min(Mathf.clamp(heat / heatRequirement) + over / heatRequirement * overheatScale, maxEfficiency);
            } else {
                return 1.0F;
            }
        }

        public float warmup() {
            return warmup;
        }

        public float progress() {
            AxRecipe cur = getCurRecipe();
            return Mathf.clamp(cur.craftTime > 0.0F ? craftingTime / cur.craftTime : 1.0F);
        }

        public void display(Table table) {
            super.display(table);
            AxMulticrafter.hoveredInfo = table;
        }

        public void rebuildHoveredInfo() {
            try {
                Table info = AxMulticrafter.hoveredInfo;
                if (info != null) {
                    info.clear();
                    display(info);
                }
            } catch (Exception var2) {
            }

        }

        public void createEffect(Effect effect) {
            if (effect != Fx.none) {
                if (effect == Fx.placeBlock) {
                    effect.at(x, y, (float)block.size);
                } else if (effect == Fx.coreBuildBlock) {
                    effect.at(x, y, 0.0F, block);
                } else if (effect == Fx.upgradeCore) {
                    effect.at(x, y, 0.0F, block);
                } else if (effect == Fx.upgradeCoreBloom) {
                    effect.at(x, y, (float)block.size);
                } else if (effect == Fx.rotateBlock) {
                    effect.at(x, y, (float)block.size);
                } else {
                    effect.at(x, y, 0.0F, this);
                }

            }
        }
    }
}
