package axthrix.world.types.block.defense.parent;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.Tooltip;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.AxthrixLoader;
import axthrix.content.AxResources;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.AxFaction;
import axthrix.world.types.block.defense.ChargingShotgunTurret;
import axthrix.world.types.block.defense.temperature.ItemTempTurret;
import axthrix.world.types.block.defense.RevolverTurret;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.types.unittypes.MountUnitType;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.AxStats;
import axthrix.world.util.logics.AcceleratedLogic;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Blocks;
import mindustry.entities.Sized;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.draw.DrawTurret;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.graphics.Pal;
import mindustry.graphics.Layer;
import mindustry.graphics.Shaders;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.world.Block;

import mindustry.Vars;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.world;
import static mindustry.Vars.player;
import static mindustry.Vars.ui;

public class HeadTurretClass extends Turret {
    /** Whether this turret exists in the submerged layer. */
    public boolean waterBlock;
    /** If true, this turret can target units on both the surface AND underwater. */
    public boolean seeOutsideLayer = false;

    public float accumulationResistanceHeat = 1f;
    public float accumulationResistanceCold = 1f;
    public float effectResistanceHeat = 1f;
    public float effectResistanceCold = 1f;
    public float maxTempStorage = 100f;

    /** Factions this block belongs to. */
    public Seq<AxFaction> faction = new Seq<>();

    /// resource-gated tier upgrade support, shared by every turret type descending from this class.
    /// off by default - set true and configure the fields below to opt a specific block in
    public boolean upgradable = false;
    /// null on the final tier in the chain
    public Block nextTier;
    /// resources required to trigger the upgrade, separate from ammo
    public ItemStack[] upgradeRequirements = {};
    /// liquid resources required to trigger the upgrade (works together with items)
    public LiquidStack[] upgradeLiquidRequirements = {};
    /// time spent finishing the upgrade once requirements are fully stocked
    public float upgradeTime = 60f * 15f;

    public HeadTurretClass(String name) {
        super(name);
        update = true;
        solid = false;
        hasShadow = false;

        config(Boolean.class, (HeadTurretBuild build, Boolean value) -> {
            if (!upgradable) return;
            build.toggleUpgrade();
        });
    }

    @Override
    public void setBars(){
        super.setBars();
        if(coolant == null){
            removeBar("liquid");
        }
    }

    @Override
    public void init() {
        if (upgradable) {
            configurable = true;

            if (upgradeLiquidRequirements.length > 0) {
                hasLiquids = true;

                float total = 0f;
                for (LiquidStack stack : upgradeLiquidRequirements) {
                    total += stack.amount;
                }
                liquidCapacity = Math.max(liquidCapacity, total);
            }
        }


        super.init();
        if (waterBlock) {
            floating = true;
            placeableLiquid = true;
        }
    }

    @Override
    public void setStats() {
        super.setStats();


        if (coolant == null){
            stats.remove(Stat.liquidCapacity);
        }

        if (faction.any()) {
            stats.add(AxStats.faction, Core.bundle.get("team." + faction.peek().name));
        }

        stats.add(Stat.abilities, waterBlock ? "[blue]Submerged Only[]" : "[accent]Surface Only[]");
        if (seeOutsideLayer) {
            stats.add(Stat.shootRange, "Can target multiple layers");
        }

        if (upgradable && nextTier != null) {
            stats.add(new Stat("aj-upgrade"), table -> table.add(Core.bundle.format("stat.aj-upgrade-time", Strings.autoFixed(upgradeTime / 60f, 1))));
        }
    }

    @Override
    public boolean canBreak(Tile tile) {
        if (upgradable && tile.build instanceof HeadTurretBuild htb && (htb.upgrading || htb.reversing)) {
            return false;
        }
        return waterBlock == LayerManager.isPlayerSubmerged();
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation) {
        if (tile == null) return false;
        if (waterBlock) {
            return LayerManager.isPlayerSubmerged() &&
                    (tile.floor() == AxthrixEnvironment.tharaxianDeep || tile.floor() == Blocks.deepwater);
        }
        return !LayerManager.isPlayerSubmerged() && super.canPlaceOn(tile, team, rotation);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid) {
        super.drawPlace(x, y, rotation, valid);

        if (!upgradable) return;

        Block stage = nextTier;
        while (stage != null) {
            boolean stageUnlocked = !Vars.state.isCampaign() || stage.unlockedNow();

            if (stageUnlocked) {
                float wx = x * Vars.tilesize + stage.offset;
                float wy = y * Vars.tilesize + stage.offset;
                drawUpgradeBox(wx, wy, stage.size, Pal.accent);
            }

            stage = (stage instanceof HeadTurretClass htc) ? htc.nextTier : null;
        }
    }

    public static void drawUpgradeBox(float wx, float wy, int size, Color col) {
        float ext = size * Vars.tilesize / 2f;
        int dashes = Math.round((size * Vars.tilesize) * 4);

        Lines.stroke(3f, Color.gray);
        Lines.dashLine(wx - ext, wy - ext, wx + ext, wy - ext, dashes);
        Lines.dashLine(wx + ext, wy - ext, wx + ext, wy + ext, dashes);
        Lines.dashLine(wx + ext, wy + ext, wx - ext, wy + ext, dashes);
        Lines.dashLine(wx - ext, wy + ext, wx - ext, wy - ext, dashes);

        Lines.stroke(1f, col);
        Lines.dashLine(wx - ext, wy - ext, wx + ext, wy - ext, dashes);
        Lines.dashLine(wx + ext, wy - ext, wx + ext, wy + ext, dashes);
        Lines.dashLine(wx + ext, wy + ext, wx - ext, wy + ext, dashes);
        Lines.dashLine(wx - ext, wy + ext, wx - ext, wy - ext, dashes);

        Draw.color();
        Lines.stroke(1f);
    }

    public class HeadTurretBuild extends TurretBuild {
        public ObjectIntMap<Item> upgradeStock = new ObjectIntMap<>();
        public ObjectFloatMap<Liquid> upgradeLiquidStock = new ObjectFloatMap<>();
        public float upgradeCounter = 0f;
        /// whether the player has started the upgrade attempt - gates item intake and progress
        public boolean upgrading = false;
        /// reverses visuals for a clean look
        public boolean reversing = false;


        public boolean needsUpgradeItem(Item item) {
            if (!upgradable || nextTier == null || !nextTierUnlocked()) return false;

            for (ItemStack stack : upgradeRequirements) {
                if (stack.item == item && upgradeStock.get(item, 0) < stack.amount) return true;
            }
            return false;
        }

        public boolean upgradeItemRequirementsMet() {
            if (!upgradable || nextTier == null) return false;
            for (ItemStack stack : upgradeRequirements) {
                if (upgradeStock.get(stack.item, 0) < stack.amount) return false;
            }
            return true;
        }

        public boolean acceptUpgradeItem(Item item) {
            return needsUpgradeItem(item);
        }

        public void handleUpgradeItem(Item item) {
            if (needsUpgradeItem(item)) {
                upgradeStock.increment(item, 0, 1);
            }
        }

        /** Smart transfer: deposit from unit if possible, otherwise collect into unit */
        public void transferUpgradeItem(Item item, Unit unit) {
            if (unit == null) return;

            if (needsUpgradeItem(item) && unit.stack.item == item && unit.stack.amount > 0) {
                int needed = 0;
                for (ItemStack stack : upgradeRequirements) {
                    if (stack.item == item) {
                        needed = stack.amount - upgradeStock.get(item, 0);
                        break;
                    }
                }
                if (needed > 0) {
                    int take = Math.min(needed, unit.stack.amount);
                    unit.stack.amount -= take;
                    if (unit.stack.amount == 0) unit.clearItem();
                    upgradeStock.increment(item, 0, take);
                    return;
                }
            }

            collectUpgradeItem(item, unit);
        }

        /** Empties one item's stockpile into the requesting unit's inventory, up to its capacity */
        public void collectUpgradeItem(Item item, Unit collector) {
            if (collector == null) return;

            int has = upgradeStock.get(item, 0);
            if (has <= 0) return;

            int capacity = collector.type.itemCapacity;
            int held = (collector.item() == item) ? collector.stack.amount : 0;

            if (collector.item() != null && collector.item() != item && held > 0) return;

            int space = capacity - held;
            if (space <= 0) return;

            int amount = Math.min(space, has);
            collector.addItem(item, amount);
            upgradeStock.increment(item, 0, -amount);
        }


        public boolean needsUpgradeLiquid(Liquid liquid) {
            if (!upgradable || nextTier == null || !nextTierUnlocked()) return false;

            for (LiquidStack stack : upgradeLiquidRequirements) {
                if (stack.liquid == liquid && upgradeLiquidStock.get(liquid, 0f) < stack.amount) {
                    return true;
                }
            }
            return false;
        }

        public boolean upgradeLiquidRequirementsMet() {
            if (!upgradable || nextTier == null) return false;
            for (LiquidStack stack : upgradeLiquidRequirements) {
                if (upgradeLiquidStock.get(stack.liquid, 0f) < stack.amount) return false;
            }
            return true;
        }

        public boolean acceptUpgradeLiquid(Liquid liquid) {
            return needsUpgradeLiquid(liquid);
        }

        public void handleUpgradeLiquid(Liquid liquid, float amount) {
            if (!needsUpgradeLiquid(liquid) || amount <= 0f) return;

            float current = upgradeLiquidStock.get(liquid, 0f);
            float needed = 0f;
            for (LiquidStack stack : upgradeLiquidRequirements) {
                if (stack.liquid == liquid) {
                    needed = stack.amount;
                    break;
                }
            }

            float add = Math.min(amount, needed - current);
            if (add > 0f) {
                upgradeLiquidStock.put(liquid, current + add);
            }
        }

        /** Smart transfer for liquids (only works with MountUnitType) */
        public void transferUpgradeLiquid(Liquid liquid, Unit unit) {
            if (unit == null || !(unit.type instanceof MountUnitType mount)) return;

            MountUnitType.MountData d = MountUnitType.data(unit);

            if (needsUpgradeLiquid(liquid) && d.liquid == liquid && d.liquidAmount > 0f) {
                float needed = 0f;
                for (LiquidStack stack : upgradeLiquidRequirements) {
                    if (stack.liquid == liquid) {
                        needed = stack.amount - upgradeLiquidStock.get(liquid, 0f);
                        break;
                    }
                }
                if (needed > 0f) {
                    float take = Math.min(needed, d.liquidAmount);
                    if (take > 0f) {
                        d.liquidAmount -= take;
                        if (d.liquidAmount <= 0.001f) {
                            d.liquidAmount = 0f;
                            d.liquid = null;
                        }
                        upgradeLiquidStock.put(liquid, upgradeLiquidStock.get(liquid, 0f) + take);
                        return;
                    }
                }
            }

            collectUpgradeLiquid(liquid, unit);
        }

        public void collectUpgradeLiquid(Liquid liquid, Unit collector) {
            if (collector == null || !(collector.type instanceof MountUnitType mount)) return;

            float has = upgradeLiquidStock.get(liquid, 0f);
            if (has <= 0f) return;

            MountUnitType.MountData d = MountUnitType.data(collector);

            if (d.liquid != null && d.liquid != liquid && d.liquidAmount > 0.01f) return;

            float space = mount.liquidCapacity - d.liquidAmount;
            if (space <= 0f) return;

            float take = Math.min(space, has);
            if (take <= 0f) return;

            if (d.liquidAmount <= 0.001f || d.liquid == null) {
                d.liquid = liquid;
            }
            d.liquidAmount += take;
            upgradeLiquidStock.put(liquid, has - take);
        }

        public boolean upgradeRequirementsMet() {
            return upgradeItemRequirementsMet() && upgradeLiquidRequirementsMet();
        }

        public float upgradeProgress() {
            if (!upgradable || nextTier == null) return 0f;

            if (upgradeRequirementsMet()) {
                return upgradeTime <= 0f ? 1f : Math.min(upgradeCounter / upgradeTime, 1f);
            }

            float total = 0f, have = 0f;

            for (ItemStack stack : upgradeRequirements) {
                total += stack.amount;
                have += Math.min(upgradeStock.get(stack.item, 0), stack.amount);
            }
            for (LiquidStack stack : upgradeLiquidRequirements) {
                total += stack.amount;
                have += Math.min(upgradeLiquidStock.get(stack.liquid, 0f), stack.amount);
            }

            return total <= 0f ? 0f : (have / total) * 0.999f;
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return acceptUpgradeItem(item);
        }

        @Override
        public void handleItem(Building source, Item item) {
            handleUpgradeItem(item);
        }


        @Override
        public boolean acceptLiquid(Building source, Liquid liquid) {
            if (acceptUpgradeLiquid(liquid)) return true;

            return block.hasLiquids && super.acceptLiquid(source, liquid);
        }

        @Override
        public void handleLiquid(Building source, Liquid liquid, float amount) {
            if (needsUpgradeLiquid(liquid)) {
                float current = upgradeLiquidStock.get(liquid, 0f);
                float needed = 0f;

                for (LiquidStack stack : upgradeLiquidRequirements) {
                    if (stack.liquid == liquid) {
                        needed = stack.amount - current;
                        break;
                    }
                }

                float take = Math.min(amount, Math.max(needed, 0f));
                if (take > 0f) {
                    handleUpgradeLiquid(liquid, take);
                }

                float leftover = amount - take;
                if (leftover > 0f && block.hasLiquids && liquids != null) {
                    super.handleLiquid(source, liquid, leftover);
                }
            } else if (block.hasLiquids && liquids != null) {
                super.handleLiquid(source, liquid, amount);
            }
        }


        public void toggleUpgrade() {
            if (upgrading) {
                beginReversal();
            } else {
                if (!nextTierUnlocked()) return;
                upgrading = true;
                reversing = false;
            }
        }

        public void beginReversal() {
            upgrading = false;
            if (upgradeCounter > 0f) reversing = true;
        }

        @Override
        protected void shoot(BulletType type) {
            if (upgrading || reversing) return;
            super.shoot(type);
        }

        public boolean nextTierUnlocked() {
            if (!Vars.state.isCampaign()) return true;
            return nextTier != null && nextTier.unlocked();
        }

        public void updateUpgrade() {
            if (!upgradable || nextTier == null) return;

            if (reversing) {
                upgradeCounter = Math.max(upgradeCounter - Time.delta, 0f);
                if (upgradeCounter <= 0f) reversing = false;
                return;
            }

            if (!upgrading) return;

            if (!nextTierUnlocked() || !upgradeRequirementsMet()) {
                beginReversal();
                return;
            }

            float half = upgradeTime / 2f;

            if (upgradeCounter < half) {
                upgradeCounter = Math.min(upgradeCounter + Time.delta, half);
            } else if (canFitUpgrade()) {
                upgradeCounter = Math.min(upgradeCounter + Time.delta, upgradeTime);
            }

            if (upgradeCounter >= upgradeTime) {
                doUpgrade();
            }
        }

        public boolean canFitUpgrade() {
            Tile center = tile;
            int size = nextTier.size;
            int offset = -(size - 1) / 2;

            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    Tile other = world.tile(center.x + offset + dx, center.y + offset + dy);
                    if (other == null) return false;
                    if (other.build != null && other.build != this) return false;
                }
            }
            return true;
        }

        public void doUpgrade() {
            if (nextTier == null || !canFitUpgrade()) return;

            float healthPercent = health / maxHealth;
            int rot = (int) rotation;
            Team upgradeTeam = team;
            Tile c = tile;

            upgradeStock.clear();
            upgradeLiquidStock.clear();
            upgradeCounter = 0f;

            c.setNet(nextTier, upgradeTeam, rot / 90);

            if (c.build != null) {
                c.build.health = c.build.maxHealth * healthPercent;
            }
        }

        @Override
        public void buildConfiguration(Table table) {
            if (upgradable && nextTier != null) {
                table.table(Styles.grayPanel, this::buildUpgradeTable);
            }
        }

        public void buildUpgradeTable(Table t) {
            t.left().defaults().left().pad(2);

            t.add(block.localizedName).color(Pal.accent);
            t.add(" -> ").color(Color.lightGray);
            t.add(nextTierUnlocked() ? nextTier.localizedName : "????").color(Pal.accent).growX();
            t.button("?", Styles.flatBordert, () -> ui.content.show(nextTier))
                    .right().size(32f).visible(nextTier::unlockedNow);
            t.row();

            if (!nextTierUnlocked()) {
                t.add("[#FF6666]" + Core.bundle.format("stat.aj-upgrade-locked", nextTier.localizedName)).growX().colspan(4).row();
                return;
            }

            if (upgradeRequirements.length > 0) {
                t.add(StatUnit.items.localized()).color(Pal.accent).colspan(4).padTop(6).row();

                Table items = new Table();
                items.left().defaults().pad(4).left();

                for (int i = 0; i < upgradeRequirements.length; i++) {
                    ItemStack stack = upgradeRequirements[i];

                    items.button(c -> {
                        c.image(stack.item.uiIcon).size(24f).padRight(4);

                        Label count = new Label("");
                        count.update(() -> {
                            int have = Math.min(upgradeStock.get(stack.item, 0), stack.amount);
                            count.setText(have + "/" + stack.amount);
                            count.setColor(have >= stack.amount ? Pal.accent : Color.lightGray);
                            count.addListener(new Tooltip(te -> te.table(Styles.black3, ft -> ft.add(Core.bundle.get("ui.aj-button-item"))).size(20,10)));
                        });
                        c.add(count);
                    }, Styles.flati, () -> {
                        if (player != null && player.unit() != null) {
                            transferUpgradeItem(stack.item, player.unit());
                        }
                    }).pad(4);

                    if ((i + 1) % 3 == 0) items.row();
                }

                t.add(items).growX().row();
            }

            if (upgradeLiquidRequirements.length > 0) {
                t.add(StatUnit.liquidUnits.localized()).color(Pal.accent).colspan(4).padTop(6).row();

                Table liquids = new Table();
                liquids.left().defaults().pad(4).left();

                for (int i = 0; i < upgradeLiquidRequirements.length; i++) {
                    LiquidStack stack = upgradeLiquidRequirements[i];

                    liquids.button(c -> {
                        c.image(stack.liquid.uiIcon).size(24f).padRight(4);

                        Label count = new Label("");
                        count.update(() -> {
                            float have = Math.min(upgradeLiquidStock.get(stack.liquid, 0f), stack.amount);
                            count.setText(Strings.autoFixed(have, 1) + "/" + Strings.autoFixed(stack.amount, 1));
                            count.setColor(have >= stack.amount ? Pal.accent : Color.lightGray);
                            count.addListener(new Tooltip(te -> te.table(Styles.black3, ft -> ft.add(Core.bundle.get("ui.aj-button-liquid"))).size(20,10)));
                        });
                        c.add(count);
                    }, Styles.flati, () -> {
                        if (player != null && player.unit() != null) {
                            transferUpgradeLiquid(stack.liquid, player.unit());
                        }
                    }).pad(4);

                    if ((i + 1) % 3 == 0) liquids.row();
                }

                t.add(liquids).growX().row();
            }

            t.table(bottom -> {
                Label progress = new Label("");
                progress.update(() -> progress.setText((int) (upgradeProgress() * 100) + "%"));
                bottom.add(progress).left().growX();

                bottom.button(Icon.play, Styles.emptyi, this::toggleUpgrade)
                        .update(i -> i.getStyle().imageUp = upgrading ? Icon.cancel : Icon.play)
                        .disabled(b -> reversing)
                        .size(32f).right();
            }).growX();
        }


        @Override
        public void drawSelect() {
            super.drawSelect();

            if (!upgradable || nextTier == null || !nextTierUnlocked() || !upgradeRequirementsMet()) return;

            float wx = tile.x * Vars.tilesize + nextTier.offset;
            float wy = tile.y * Vars.tilesize + nextTier.offset;
            drawUpgradeBox(wx, wy, nextTier.size, Pal.accent);
        }

        public void drawMaterialize(Block icons, float wx, float wy, float progress, Color tint) {
            Draw.draw(Layer.blockBuilding, () -> {
                Draw.color(tint);
                boolean noOverrides = icons.regionRotated1 == -1 && icons.regionRotated2 == -1;
                int i = 0;

                for (TextureRegion region : icons.getGeneratedIcons()) {
                    Shaders.blockbuild.region = region;
                    Shaders.blockbuild.time = Time.time;
                    Shaders.blockbuild.progress = progress;

                    Draw.rect(region, wx, wy, icons.rotate && (noOverrides || icons.regionRotated2 == i || icons.regionRotated1 == i) ? rotdeg() + icons.visualRotationOffset : 0);
                    Draw.flush();
                    i++;
                }

                Draw.color();
            });
        }

        public void drawUpgradeDeconstruct() {
            float half = upgradeTime / 2f;
            float progress = half <= 0f ? 0f : 1f - Mathf.clamp(upgradeCounter / half);
            drawMaterialize(block, x, y, progress, reversing ? Pal.accent : Pal.remove);
        }

        public void drawUpgradeConstruct() {
            float half = upgradeTime / 2f;
            float span = upgradeTime - half;
            float progress = span <= 0f ? 1f : Mathf.clamp((upgradeCounter - half) / span);
            float wx = tile.x * Vars.tilesize + nextTier.offset;
            float wy = tile.y * Vars.tilesize + nextTier.offset;
            drawMaterialize(nextTier, wx, wy, progress, reversing ? Pal.remove : Pal.accent);
        }

        public void drawUpgradeProgress() {
            if (!upgradable || nextTier == null || (!upgrading && !reversing)) return;

            float half = upgradeTime / 2f;

            if (upgradeCounter < half) {
                drawUpgradeDeconstruct();
            } else {
                drawUpgradeConstruct();
                if (upgrading && !canFitUpgrade()) {
                    drawUpgradeBlockers();
                }
            }
        }

        public void drawUpgradeBlockers() {
            Tile center = tile;
            int size = nextTier.size;
            int offset = -(size - 1) / 2;

            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    Tile other = world.tile(center.x + offset + dx, center.y + offset + dy);
                    boolean blocked = other == null || (other.build != null && other.build != this);
                    if (!blocked) continue;

                    float wx = (center.x + offset + dx) * Vars.tilesize;
                    float wy = (center.y + offset + dy) * Vars.tilesize;

                    Draw.color(Color.red, 0.45f);
                    Fill.square(wx, wy, Vars.tilesize / 2f);
                    Draw.color();
                }
            }
        }

        @Override
        public void draw() {
            if (this.block instanceof Turret t && t.drawer instanceof DrawTurret dt && dt.parts.size > 0) {

                if (this instanceof ItemTempTurret.ItemTempTurretBuild) {
                    AxPartParms.axparams.set(
                            0,
                            0,
                            TemperatureLogic.getTemperatureBuilding(this) / maxTempStorage
                    );
                }

                if (this instanceof AcceleratedLogic.AcceleratedTurretBuild ab) {
                    if (ab.getAcceleratedBonus() == 1 && ab.burnsOut()) {
                        AxPartParms.axparams.set(
                                0,
                                0,
                                ab.heatf()
                        );
                    } else {
                        AxPartParms.axparams.set(
                                0,
                                0,
                                ab.heatp()
                        );
                    }
                }

                if (this instanceof ChargingShotgunTurret.ChargingShotgunBuild cb) {
                    AxPartParms.axparams.set(
                            cb.chargeProgress,
                            0,
                            0
                    );
                }

                if (this instanceof RevolverTurret.RevolverTurretBuild rb) {
                    float progress = rb.progress();

                    AxPartParms.axparams.set(
                            1f - progress,
                            rb.secondarySmoothReload,
                            0
                    );
                }

                if (this instanceof PerkTurretType.PerkTurretTypeBuild pb) {
                    AxPartParms.perkparams.set(
                            pb.getPerkProgress(),
                            pb.getPerkActivated()
                    );
                }
            }

            boolean upgradeActive = upgradable && nextTier != null && (upgrading || reversing);
            if (upgradeActive) {
                drawUpgradeProgress();
                return;
            }

            if (AxthrixLoader.drawEnchancedShadows) {
                super.draw();
                return;
            }

            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();
            boolean onLiquid = tile.floor().isLiquid;

            boolean isShadow = (waterBlock && !playerSub) || (!waterBlock && playerSub && onLiquid);
            boolean isNativeWater = waterBlock && playerSub;
            float shadowWeight = isShadow ? (playerSub ? intensity : (1f - intensity)) : 0f;

            if (isShadow) {
                float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
                float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

                Draw.color(Color.black);
                float blurAlpha = (0.25f * shadowWeight) / 3f;
                float offset = 0.8f;

                if (drawer instanceof DrawTurret dt) {
                    Draw.alpha(blurAlpha);
                    Draw.rect(dt.preview, x + recoilOffset.x + ox + offset, y + recoilOffset.y + oy + offset, drawrot());
                    Draw.rect(dt.preview, x + recoilOffset.x + ox - offset, y + recoilOffset.y + oy - offset, drawrot());

                    Draw.alpha(0.25f * shadowWeight);
                    Draw.rect(dt.base, x + ox, y + oy);
                    Draw.alpha(0.15f * shadowWeight);
                    Draw.rect(dt.preview, x + recoilOffset.x + ox, y + recoilOffset.y + oy, drawrot());
                }
                Draw.reset();
            } else if (isNativeWater) {
                Color floorCol = tile.floor().mapColor;
                Tmp.c1.set(Color.white).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);

                Draw.color(Tmp.c1);
                super.draw();
                Draw.reset();
            } else {
                super.draw();
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();

            if (upgradable) updateUpgrade();

            float range = (block.size * 4f) + 4f;
            Groups.unit.intersect(x - range, y - range, range * 2, range * 2, unit -> {
                if (waterBlock == LayerManager.isSubmerged(unit)) {
                    if (!unit.isFlying()) applyBlockBounce(unit);
                }
            });
        }


        @Override
        public void write(Writes write) {
            super.write(write);

            write.i(2);

            write.f(upgradeCounter);
            write.bool(upgrading);
            write.bool(reversing);

            // items
            write.i(upgradeStock.size);
            for (ObjectIntMap.Entry<Item> entry : upgradeStock) {
                write.s(entry.key.id);
                write.i(entry.value);
            }

            // liquids
            write.i(upgradeLiquidStock.size);
            for (ObjectFloatMap.Entry<Liquid> entry : upgradeLiquidStock) {
                write.s(entry.key.id);
                write.f(entry.value);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            int ver = read.i();

            if (ver >= 1) {
                upgradeCounter = read.f();
                upgrading = read.bool();
                reversing = read.bool();

                upgradeStock.clear();
                int itemSize = read.i();
                for (int i = 0; i < itemSize; i++) {
                    short id = read.s();
                    int amount = read.i();
                    Item item = Vars.content.item(id);
                    if (item != null && amount > 0) {
                        upgradeStock.put(item, amount);
                    }
                }
            }

            if (ver >= 2) {
                upgradeLiquidStock.clear();
                int liqSize = read.i();
                for (int i = 0; i < liqSize; i++) {
                    short id = read.s();
                    float amount = read.f();
                    Liquid liquid = Vars.content.liquid(id);
                    if (liquid != null && amount > 0f) {
                        upgradeLiquidStock.put(liquid, amount);
                    }
                }
            }
        }


        private void applyBlockBounce(Unit unit) {
            float dx = unit.x - x, dy = unit.y - y;
            float len = Mathf.len(dx, dy);
            float minLen = (block.size * 4f) + (unit.hitSize / 2f);

            if (len < minLen) {
                if (len == 0) {
                    dx = 0.1f;
                    len = 0.1f;
                }
                dx /= len;
                dy /= len;
                float push = (minLen - len) + 0.2f;
                unit.vel.add(dx * push * 0.1f, dy * push * 0.1f);
                unit.x += dx * push;
                unit.y += dy * push;
            }
        }

        @Override
        protected void findTarget() {
            target = null;
            final float[] closest = {range + 1f};

            Units.nearbyEnemies(this.team, x, y, range, other -> {
                if (!SeaDetection(other)) return;
                if (cantSee(this.team, other)) return;

                float distance = dst(other);
                float priorityDist = other.type.coreUnitDock ? -1f : distance;

                if (priorityDist < closest[0]) {
                    target = other;
                    closest[0] = priorityDist;
                }
            });

            if (target == null) {
                Units.nearbyBuildings(x, y, range, other -> {
                    float distance = dst(other);

                    if (other.team != this.team && SeaDetection(other)) {
                        boolean isCore = other instanceof CoreBlock.CoreBuild;
                        float priorityDist = isCore ? -1f : distance;

                        if (priorityDist < closest[0]) {
                            target = other;
                            closest[0] = priorityDist;
                        }
                    }
                });
            }

            if (target == null) {
                super.findTarget();
            }
        }

        @Override
        protected boolean validateTarget() {
            if (isControlled() || logicControlled()) {
                return !invalidateTarget(target, canHeal() ? Team.derelict : team, x, y, range)
                        || isControlled() || logicControlled();
            }

            if (target != null && invalidateTarget(target, canHeal() ? Team.derelict : team, x, y, range)) {
                target = null;
            }

            if (target == null) return false;

            boolean inRange = target.within(this, range + (target instanceof Sized s ? s.hitSize() / 2f : 0f));
            if (!inRange) return false;

            if (!SeaDetection(target)) return false;

            if (target instanceof Unit u) {
                boolean submerged = LayerManager.isSubmerged(u);
                if (submerged) {
                    return u.isValid();
                } else {
                    return u.targetable(team) && u.isValid();
                }
            }

            return target instanceof Healthc h && h.isValid();
        }

        public boolean invalidateTarget(Posc target, Team team, float x, float y, float range) {
            return target == null ||
                    (range != Float.MAX_VALUE && !target.within(x, y, range + (target instanceof Sized hb ? hb.hitSize() / 2f : 0f))) ||
                    (target instanceof Teamc t && t.team() == team) ||
                    (target instanceof Healthc h && !h.isValid()) ||
                    (target instanceof Unit u && cantSee(team, u));
        }

        public boolean SeaDetection(Posc target) {
            if (target == null) return false;
            if (seeOutsideLayer) return true;
            return waterBlock == LayerManager.isSubmerged(target);
        }

        public boolean cantSee(Team team, Unit unit) {
            return !LayerManager.isSubmerged(unit) && !unit.targetable(team);
        }
    }
}