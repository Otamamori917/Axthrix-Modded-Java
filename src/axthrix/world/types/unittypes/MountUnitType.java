package axthrix.world.types.unittypes;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.world.types.block.storage.LiquidDeposit;
import axthrix.world.util.draw.DrawIPayloadTurret;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.part.DrawPart;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Payloadc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.liquid.LiquidRouter;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.power.PowerBlock;
import mindustry.world.blocks.power.PowerGenerator;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawTurret;
import mindustry.world.modules.ItemModule;
import mindustry.world.modules.LiquidModule;
import mindustry.world.modules.PowerModule;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustry.Vars.*;

public class MountUnitType extends AxUnitType {

    public Seq<MountSlot> slots = new Seq<>();

    public float liquidCapacity = 160f;
    public float liquidPickupRange = 40f;
    public float powerCapacity = 1000f;
    public float itemFeedPerTick = 1f;
    public float liquidFeedPerTick = 1f;
    public float powerFeedPerTick = 5f;

    public float nonWallAbsorbRatio = 0.05f;
    public float wallAbsorbRatio = 0.55f;
    public float wallCoverageArc = 360f;

    private static final IntMap<MountData> data = new IntMap<>();
    private static boolean damageEventHooked = false;

    public MountUnitType(String name) {
        super(name);
    }

    @Override
    public void init() {
        super.init();
        if (!damageEventHooked) {
            damageEventHooked = true;
            Events.on(EventType.UnitDamageEvent.class, e -> {
                if (e.bullet != null) {
                    onUnitDamage(e.unit, e.bullet.damage, e.bullet.x, e.bullet.y);
                }
            });
        }
    }

    public MountSlot addSlot(float x, float y, int maxSize) {
        MountSlot s = new MountSlot();
        s.index = slots.size;
        s.x = x;
        s.y = y;
        s.maxSize = maxSize;
        slots.add(s);
        return s;
    }

    public static MountData data(Unit unit) {
        MountData d = data.get(unit.id);
        if (d == null) {
            d = new MountData();
            data.put(unit.id, d);
        }
        if (unit.type instanceof MountUnitType mt) {
            while (d.modules.size < mt.slots.size) {
                d.modules.add(new ModuleState());
            }
        }
        return d;
    }

    public static void clear(Unit unit) {
        data.remove(unit.id);
    }

    @Override
    public void update(Unit unit) {
        super.update(unit);

        if (unit.dead) {
            clear(unit);
            return;
        }

        MountData d = data(unit);
        if (!(unit instanceof Payloadc)) return;

        if (!net.client()) {
            updateLiquidPickup(unit, d);
            updateFeeds(unit, d);
        }

        for (int i = 0; i < slots.size; i++) {
            ModuleState m = d.modules.get(i);
            if (m.block == null) continue;

            if (m.health <= 0f) {
                destroyModule(unit, d, i, true);
                continue;
            }

            Tmp.v1.set(slots.get(i).x, slots.get(i).y).rotate(unit.rotation - 90f);
            float wx = unit.x + Tmp.v1.x;
            float wy = unit.y + Tmp.v1.y;

            if (m.build != null) {
                ensureModules(m.build);
                m.build.x = wx;
                m.build.y = wy;
                m.build.tile = emptyTile;
                m.build.team = unit.team;

                if (!net.client()) {
                    try {
                        m.build.update();
                    } catch (Throwable ignored) {
                    }
                    harvestPower(d, m);
                    feedPower(d, m);
                }
            }
        }

        if (player != null && player.unit() == unit) {
            if (AxthrixLoader.toggleTapped()) toggleEquip(unit);
            if (AxthrixLoader.activateTapped()) cyclePayload(unit);
            if (AxthrixLoader.activateBackTapped()) cycleSlotOrPayloadBack(unit);
        }
    }

    private void updateLiquidPickup(Unit unit, MountData d) {
        Units.nearbyBuildings(unit.x, unit.y, liquidPickupRange, b -> {
            if (b.team != unit.team) return;

            if (b instanceof LiquidRouter.LiquidRouterBuild lr) {
                if (d.liquidAmount >= liquidCapacity - 0.001f) return;
                Liquid liq = lr.liquids.current();
                float amt = lr.liquids.currentAmount();
                if (liq == null || amt <= 0f) return;
                if (d.liquid != null && d.liquid != liq && d.liquidAmount > 0.01f) return;

                float take = Math.min(1f, Math.min(amt, liquidCapacity - d.liquidAmount));
                if (take <= 0f) return;
                d.liquid = liq;
                d.liquidAmount += take;
                lr.liquids.remove(liq, take);
                return;
            }

            if (b instanceof LiquidDeposit.LiquidDepositBuild ld) {
                if (d.liquid == null || d.liquidAmount <= 0f) return;
                if (ld.liquids.currentAmount() >= ld.block.liquidCapacity) return;
                Liquid cur = ld.liquids.current();
                if (cur != null && cur != d.liquid && ld.liquids.currentAmount() > 0.01f) return;

                float room = ld.block.liquidCapacity - ld.liquids.get(d.liquid);
                float put = Math.min(1f, Math.min(d.liquidAmount, room));
                if (put <= 0f) return;
                ld.liquids.add(d.liquid, put);
                d.liquidAmount -= put;
                if (d.liquidAmount <= 0.001f) {
                    d.liquidAmount = 0f;
                    d.liquid = null;
                }
            }
        });
    }

    private void updateFeeds(Unit unit, MountData d) {
        for (int i = 0; i < slots.size; i++) {
            ModuleState m = d.modules.get(i);
            if (m.build == null || m.block == null) continue;
            Building b = m.build;
            ensureModules(b);

            if (unit.stack.amount > 0 && unit.stack.item != null && b.block.consumesItem(unit.stack.item)) {
                int moved = Math.min(unit.stack.amount, (int) itemFeedPerTick);
                moved = Math.min(moved, b.acceptStack(unit.stack.item, moved, b));
                if (moved > 0) {
                    b.handleStack(unit.stack.item, moved, b);
                    unit.stack.amount -= moved;
                    if (unit.stack.amount <= 0) unit.clearItem();
                }
            }

            if (b.items != null && b.block.hasItems) {
                for (ModuleState storage : d.modules) {
                    if (storage == m || storage.build == null || storage.build.items == null) continue;
                    if (!(storage.block instanceof StorageBlock)
                            && storage.block.itemCapacity <= 0) continue;

                    storage.build.items.each((item, amount) -> {
                        if (amount <= 0 || !b.block.consumesItem(item)) return;
                        int space = b.acceptStack(item, amount, b);
                        if (space <= 0) return;
                        int n = Math.min(space, Math.min(amount, (int) itemFeedPerTick));
                        if (n > 0) {
                            storage.build.items.remove(item, n);
                            b.handleStack(item, n, b);
                        }
                    });
                }
            }

            if (d.liquid != null && d.liquidAmount > 0f && b.block.hasLiquids && b.liquids != null) {
                float room = b.block.liquidCapacity - b.liquids.get(d.liquid);
                float move = Math.min(liquidFeedPerTick, Math.min(d.liquidAmount, room));
                if (move > 0f && b.acceptLiquid(b, d.liquid)) {
                    b.handleLiquid(b, d.liquid, move);
                    d.liquidAmount -= move;
                    if (d.liquidAmount <= 0.001f) {
                        d.liquidAmount = 0f;
                        d.liquid = null;
                    }
                }
            }
        }

//        if (unit.stack.amount > 0 && unit.stack.item != null && unit.stack.item.charge > 0f && d.power < powerCapacity) {
//            d.power = Math.min(powerCapacity, d.power + unit.stack.item.charge * 0.15f);
//        }
    }

    private void harvestPower(MountData d, ModuleState m) {
        if (!(m.block instanceof PowerGenerator gen) || m.build == null) return;
        float out = gen.powerProduction * m.build.efficiency * Time.delta;
        if (out > 0f) {
            d.power = Math.min(powerCapacity, d.power + out);
        }
    }

    private void feedPower(MountData d, ModuleState m) {
        if (m.build == null || m.block == null || m.block.consPower == null || m.build.power == null) return;
        float usage = m.block.consPower.usage * Time.delta;
        if (usage <= 0f) {
            m.build.power.status = 1f;
            return;
        }
        float take = Math.min(usage, d.power);
        d.power -= take;
        m.build.power.status = Mathf.clamp(take / usage);
    }

    public int takeItems(Unit unit, Item item, int amount) {
        int left = amount;
        if (unit.stack.item == item && unit.stack.amount > 0) {
            int n = Math.min(left, unit.stack.amount);
            unit.stack.amount -= n;
            left -= n;
            if (unit.stack.amount <= 0) unit.clearItem();
        }
        MountData d = data(unit);
        for (ModuleState m : d.modules) {
            if (left <= 0) break;
            if (m.build == null || m.build.items == null) continue;
            int n = Math.min(left, m.build.items.get(item));
            if (n > 0) {
                m.build.items.remove(item, n);
                left -= n;
            }
        }
        return amount - left;
    }

    public int putItems(Unit unit, Item item, int amount) {
        int left = amount;
        MountData d = data(unit);
        for (ModuleState m : d.modules) {
            if (left <= 0) break;
            if (m.build == null || m.build.items == null) continue;
            int n = m.build.acceptStack(item, left, m.build);
            if (n > 0) {
                m.build.handleStack(item, n, m.build);
                left -= n;
            }
        }
        return amount - left;
    }

    public void toggleEquip(Unit unit) {
        if (net.client()) return;
        doToggleEquip(unit);
    }

    private void doToggleEquip(Unit unit) {
        if (!(unit instanceof Payloadc pay)) return;
        MountData d = data(unit);
        if (slots.isEmpty()) return;

        int slot = Mathf.clamp(d.selectedSlot, 0, slots.size - 1);
        ModuleState m = d.modules.get(slot);

        if (m.block != null) {
            tryUnequip(unit, slot);
            return;
        }

        Seq<Payload> list = pay.payloads();
        if (list.isEmpty()) return;
        d.selectedPayload = Mathf.clamp(d.selectedPayload, 0, list.size - 1);
        tryEquip(unit, slot, d.selectedPayload);
    }

    public void cyclePayload(Unit unit) {
        if (!(unit instanceof Payloadc pay)) return;
        Seq<Payload> list = pay.payloads();
        if (list.isEmpty()) return;
        MountData d = data(unit);
        d.selectedPayload = (d.selectedPayload + 1) % list.size;
    }

    public void cyclePayloadBack(Unit unit) {
        if (!(unit instanceof Payloadc pay)) return;
        Seq<Payload> list = pay.payloads();
        if (list.isEmpty()) return;
        MountData d = data(unit);
        d.selectedPayload = (d.selectedPayload - 1 + list.size) % list.size;
    }

    public void cycleSlotOrPayloadBack(Unit unit) {
        MountData d = data(unit);
        if (slots.size <= 1) {
            cyclePayloadBack(unit);
            return;
        }
        d.selectedSlot = (d.selectedSlot - 1 + slots.size) % slots.size;
    }

    public boolean tryEquip(Unit unit, int slotIndex, int payloadIndex) {
        if (net.client()) return false;
        if (!(unit instanceof Payloadc pay)) return false;
        if (slotIndex < 0 || slotIndex >= slots.size) return false;

        MountData d = data(unit);
        ModuleState m = d.modules.get(slotIndex);
        if (m.block != null) return false;

        Seq<Payload> list = pay.payloads();
        if (payloadIndex < 0 || payloadIndex >= list.size) return false;
        Payload p = list.get(payloadIndex);
        if (!(p instanceof BuildPayload bp)) return false;

        Block block = bp.block();
        if (!slots.get(slotIndex).allows(block)) return false;

        list.remove(payloadIndex);
        m.block = block;
        m.health = block.health;
        m.maxHealth = block.health;
        m.build = bp.build != null ? bp.build : createModuleBuild(block, unit.team);
        m.build.team = unit.team;
        m.build.tile = emptyTile;
        ensureModules(m.build);
        return true;
    }

    public boolean tryUnequip(Unit unit, int slotIndex) {
        if (net.client()) return false;
        if (!(unit instanceof Payloadc pay)) return false;
        if (slotIndex < 0 || slotIndex >= slots.size) return false;

        MountData d = data(unit);
        ModuleState m = d.modules.get(slotIndex);
        if (m.block == null) return false;
        if (!hasPayloadSpace(pay, m.block)) return false;

        BuildPayload bp = new BuildPayload(m.block, unit.team);
        if (m.build != null) bp.build = m.build;
        pay.addPayload(bp);
        clearModule(m);
        return true;
    }

    private Building createModuleBuild(Block block, mindustry.game.Team team) {
        Building b = block.newBuilding();
        b.block = block;
        b.team = team;
        b.tile = emptyTile;
        ensureModules(b);
        return b;
    }

    private void ensureModules(Building b) {
        if (b == null || b.block == null) return;
        if (b.block.hasItems && b.items == null) b.items = new ItemModule();
        if (b.block.hasLiquids && b.liquids == null) b.liquids = new LiquidModule();
        if (b.block.hasPower && b.power == null) b.power = new PowerModule();
    }

    private boolean hasPayloadSpace(Payloadc p, Block block) {
        float max = ((Unit) p).type.payloadCapacity;
        float used = 0f;
        for (Payload pl : p.payloads()) {
            if (pl instanceof BuildPayload b && b.block() != null) {
                used += b.block().size * b.block().size * tilePayload;
            }
        }
        return used + block.size * block.size * tilePayload <= max;
    }

    private void destroyModule(Unit unit, MountData d, int index, boolean fx) {
        ModuleState m = d.modules.get(index);
        if (m.block == null) return;
        if (fx) {
            Tmp.v1.set(slots.get(index).x, slots.get(index).y).rotate(unit.rotation - 90f);
            Fx.explosion.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);
        }
        clearModule(m);
    }

    private void clearModule(ModuleState m) {
        m.block = null;
        m.build = null;
        m.health = 0f;
        m.maxHealth = 0f;
    }

    public static void onUnitDamage(Unit unit, float damage, float bulletX, float bulletY) {
        if (!(unit.type instanceof MountUnitType type)) return;
        if (net.client() || damage <= 0.001f) return;

        MountData d = data(unit);
        float relative = Mathf.mod(Angles.angle(unit.x, unit.y, bulletX, bulletY) - unit.rotation, 360f);

        for (int i = 0; i < type.slots.size; i++) {
            ModuleState m = d.modules.get(i);
            if (m.block == null || !(m.block instanceof Wall)) continue;
            float facing = Angles.angle(type.slots.get(i).x, type.slots.get(i).y);
            if (Angles.angleDist(relative, facing) > type.wallCoverageArc / 2f) continue;

            float taken = Math.min(damage * type.wallAbsorbRatio, m.health);
            m.health -= taken;
            unit.health = Math.min(unit.maxHealth, unit.health + taken);
            if (m.health <= 0f) type.destroyModule(unit, d, i, true);
            return;
        }

        if (type.nonWallAbsorbRatio <= 0f) return;
        for (int i = 0; i < type.slots.size; i++) {
            ModuleState m = d.modules.get(i);
            if (m.block == null || m.block instanceof Wall) continue;
            float taken = Math.min(damage * type.nonWallAbsorbRatio, m.health);
            m.health -= taken;
            unit.health = Math.min(unit.maxHealth, unit.health + taken);
            if (m.health <= 0f) type.destroyModule(unit, d, i, true);
            break;
        }
    }

    @Override
    public void draw(Unit unit) {
        super.draw(unit);

        MountData d = data(unit);
        float layer = unit.isFlying() ? Layer.flyingUnitLow : Layer.groundUnit;

        for (int i = 0; i < slots.size; i++) {
            MountSlot slot = slots.get(i);
            ModuleState m = d.modules.get(i);
            Tmp.v1.set(slot.x, slot.y).rotate(unit.rotation - 90f);
            float wx = unit.x + Tmp.v1.x;
            float wy = unit.y + Tmp.v1.y;

            float moduleLayer = layer + 5f + i * 3f;

            if (m.block == null || m.build == null) {
                Draw.z(moduleLayer);
                Draw.color(Pal.gray, 0.35f);
                Fill.square(wx, wy, 2f + slot.maxSize, unit.rotation);
                Draw.color();
            } else {
                drawModule(unit, m, wx, wy, moduleLayer);
            }
        }

        drawLiquidLasers(unit,d);
    }

    private void drawLiquidLasers(Unit unit, MountData d) {
        Units.nearbyBuildings(unit.x, unit.y, liquidPickupRange, b -> {
            if (b.team != unit.team) return;

            if (b instanceof LiquidRouter.LiquidRouterBuild lr) {
                if (d.liquidAmount >= liquidCapacity - 0.01f) return;
                if (lr.liquids.currentAmount() <= 0f) return;
                if (d.liquid != null && d.liquid != lr.liquids.current() && d.liquidAmount > 0.01f) return;
                Liquid liq = lr.liquids.current();
                Draw.z(Layer.flyingUnit + 1f);
                Draw.color(liq.color);
                Drawf.laser(laser, laserEnd, lr.x, lr.y, unit.x, unit.y, 0.4f);
                Draw.z(Layer.flyingUnit + 1.1f);
                Drawf.laser(laserTop, laserTopEnd, lr.x, lr.y, unit.x, unit.y, 0.4f);
                Draw.color();
            }

            if (b instanceof LiquidDeposit.LiquidDepositBuild ld) {
                if (d.liquid == null || d.liquidAmount <= 0f) return;
                if (ld.liquids.currentAmount() >= ld.block.liquidCapacity) return;
                Draw.z(Layer.flyingUnit + 1f);
                Draw.color(d.liquid.color);
                Drawf.laser(laser, laserEnd, unit.x, unit.y, ld.x, ld.y, 0.4f);
                Draw.z(Layer.flyingUnit + 1.1f);
                Drawf.laser(laserTop, laserTopEnd, unit.x, unit.y, ld.x, ld.y, 0.4f);
                Draw.color();
            }
        });
    }

    private void drawModule(Unit unit, ModuleState m, float wx, float wy, float layer) {
        ensureModules(m.build);
        m.build.x = wx;
        m.build.y = wy;
        m.build.tile = emptyTile;
        m.build.team = unit.team;

        if (m.block instanceof Turret && m.build instanceof Turret.TurretBuild tb) {
            drawBuild(tb, unit, layer);
            return;
        }

        drawBlockRotated(m, unit, wx, wy, layer);
    }

    private void drawBlockRotated(ModuleState m, Unit unit, float wx, float wy, float layer) {
        Block block = m.block;
        float rot = unit.rotation - 90f;
        float size = block.size * tilesize;

        Draw.z(layer + 0.1f);
        Drawf.shadow(block.region, wx - size / 2f, wy - size / 2f, rot);

        Draw.z(layer + 0.15f);
        Draw.rect(block.region, wx, wy, size, size, rot);

        if (block.teamRegion.found()) {
            if (block.teamRegions[unit.team.id] == block.teamRegion) {
                Draw.color(unit.team.color);
            }
            Draw.rect(block.teamRegions[unit.team.id], wx, wy, size, size, rot);
            Draw.color();
        }

        if (block.variantRegions != null && block.variantRegions.length > 0) {
            Draw.rect(block.variantRegions[Math.min(block.variantRegions.length - 1, 0)], wx, wy, size, size, rot);
        }
    }

    public void drawBuild(Building build, Unit unit, float layer) {
        if (!(build.block instanceof Turret turret) || !(build instanceof Turret.TurretBuild tb)) return;
        if (!(turret.drawer instanceof DrawTurret drawer)) {
            try { build.draw(); } catch (Throwable ignored) {
                Draw.rect(turret.region, build.x, build.y, tb.drawrot());
            }
            return;
        }

        Draw.z(layer);
        if (drawer instanceof DrawIPayloadTurret dpt) {
            Draw.rect(drawer.base, build.x, build.y, unit.rotation);
            Draw.z(layer + 0.05f);
            Draw.rect(dpt.cover, build.x + tb.recoilOffset.x, build.y + tb.recoilOffset.y, unit.rotation);
        } else {
            Draw.rect(drawer.base, build.x, build.y, unit.rotation);
        }
        Draw.color();

        Draw.z(layer + 0.1f);
        Drawf.shadow(drawer.preview,
                build.x + tb.recoilOffset.x - turret.elevation,
                build.y + tb.recoilOffset.y - turret.elevation,
                tb.drawrot());

        Draw.z(layer + 0.2f);
        drawer.drawTurret(turret, tb);
        drawer.drawHeat(turret, tb);

        if (drawer.parts.size > 0) {
            if (drawer.outline.found()) {
                Draw.z(layer + 0.15f);
                Draw.rect(drawer.outline, build.x + tb.recoilOffset.x, build.y + tb.recoilOffset.y, tb.drawrot());
            }
            Draw.z(layer + 0.25f);
            float progress = tb.progress();
            var params = DrawPart.params.set(
                    tb.warmup(), 1f - progress, 1f - progress, tb.heat, tb.curRecoil, tb.charge,
                    tb.x + tb.recoilOffset.x, tb.y + tb.recoilOffset.y, tb.rotation
            );
            for (var part : drawer.parts) {
                params.setRecoil(part.recoilIndex >= 0 && tb.curRecoils != null
                        ? tb.curRecoils[part.recoilIndex] : tb.curRecoil);
                part.draw(params);
            }
        }
    }

    @Override
    public void display(Unit unit, Table table) {
        super.display(unit, table);
        MountData d = data(unit);

        table.table(bars -> {
            bars.defaults().growX().height(18f).pad(2f);
            bars.add(new Bar(
                    () -> d.liquid != null ? d.liquid.localizedName : "Liquid",
                    () -> d.liquid != null ? d.liquid.barColor() : Color.darkGray,
                    () -> liquidCapacity <= 0f ? 0f : data(unit).liquidAmount / liquidCapacity
            )).row();
            bars.add(new Bar("Power", Pal.powerBar, () -> powerCapacity <= 0f ? 0f : data(unit).power / powerCapacity)).row();
            for (int i = 0; i < slots.size; i++) {
                final int idx = i;
                bars.add(new Bar(
                        () -> {
                            ModuleState m = data(unit).modules.get(idx);
                            return m.block != null ? m.block.localizedName : "Slot " + (idx + 1);
                        },
                        () -> data(unit).modules.get(idx).block == null ? Pal.gray : Pal.bar,
                        () -> {
                            ModuleState m = data(unit).modules.get(idx);
                            return m.block == null || m.maxHealth <= 0f ? 0f : m.health / m.maxHealth;
                        }
                )).row();
            }
        }).growX();
        table.row();
    }

    public TextureRegion laser, laserEnd, laserTop, laserTopEnd;

    @Override
    public void load() {
        super.load();
        laser = Core.atlas.find("laser");
        laserEnd = Core.atlas.find("laser-end");
        laserTop = Core.atlas.find("laser-top");
        laserTopEnd = Core.atlas.find("laser-top-end");
    }

    public static void writeSaveData(DataOutput stream) throws IOException {
        stream.writeInt(data.size);
        for (IntMap.Entry<MountData> e : data) {
            stream.writeInt(e.key);
            MountData d = e.value;
            stream.writeFloat(d.power);
            stream.writeFloat(d.liquidAmount);
            stream.writeInt(d.liquid == null ? -1 : d.liquid.id);
            stream.writeInt(d.selectedSlot);
            stream.writeInt(d.selectedPayload);
            stream.writeInt(d.modules.size);
            for (ModuleState m : d.modules) {
                stream.writeInt(m.block == null ? -1 : m.block.id);
                stream.writeFloat(m.health);
                stream.writeFloat(m.maxHealth);
            }
        }
    }

    public static void readSaveData(DataInput stream) throws IOException {
        data.clear();
        int n = stream.readInt();
        for (int i = 0; i < n; i++) {
            int id = stream.readInt();
            MountData d = new MountData();
            d.power = stream.readFloat();
            d.liquidAmount = stream.readFloat();
            int lid = stream.readInt();
            d.liquid = lid < 0 ? null : content.liquid(lid);
            d.selectedSlot = stream.readInt();
            d.selectedPayload = stream.readInt();
            int ms = stream.readInt();
            for (int j = 0; j < ms; j++) {
                ModuleState m = new ModuleState();
                int bid = stream.readInt();
                m.health = stream.readFloat();
                m.maxHealth = stream.readFloat();
                if (bid >= 0) {
                    m.block = content.block(bid);
                    if (m.block != null) {
                        m.build = m.block.newBuilding();
                        m.build.block = m.block;
                        m.build.tile = emptyTile;
                        if (m.block.hasItems) m.build.items = new ItemModule();
                        if (m.block.hasLiquids) m.build.liquids = new LiquidModule();
                        if (m.block.hasPower) m.build.power = new PowerModule();
                    }
                }
                d.modules.add(m);
            }
            data.put(id, d);
        }
    }

    public static class MountSlot {
        public int index;
        public float x, y;
        public int maxSize = 1;
        public boolean allowTurrets = true;
        public boolean allowWalls = true;
        public boolean allowSupport = true;
        public boolean allowProduction = false;

        public boolean allows(Block block) {
            if (block == null || block.size > maxSize) return false;
            if (block instanceof Turret) return allowTurrets;
            if (block instanceof Wall) return allowWalls;
            if (block instanceof PowerBlock) return allowSupport;
            return allowProduction || allowSupport;
        }
    }

    public static class ModuleState {
        public @Nullable Block block;
        public @Nullable Building build;
        public float health;
        public float maxHealth;
    }

    public static class MountData {
        public Seq<ModuleState> modules = new Seq<>();
        public float power;
        public float liquidAmount;
        public @Nullable Liquid liquid;
        public int selectedSlot;
        public int selectedPayload;
    }
}