package axthrix.world.types.block.defense;

import arc.Events;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.logic.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class AxItemTurret extends PerkTurretType {
    public ObjectMap<Item, BulletType> ammoTypes = new OrderedMap<>();

    public AxItemTurret(String name) {
        super(name);
        hasItems = true;
    }

    public void ammo(Object... objects) {
        ammoTypes = OrderedMap.of(objects);
    }

    public void limitRange() {
        limitRange(9f);
    }

    public void limitRange(float margin) {
        for(var entry : ammoTypes.entries()) {
            limitRange(entry.value, margin);
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.itemCapacity);
        stats.add(Stat.ammo, StatValues.ammo(ammoTypes));
        stats.add(Stat.ammoCapacity, maxAmmo / ammoPerShot, StatUnit.shots);
    }

    @Override
    public void setBars() {
        super.setBars();
        addBar("ammo", (AxItemTurretBuild entity) ->
                new Bar("stat.ammo", Pal.ammo, () -> (float)entity.totalAmmo / maxAmmo));
    }

    @Override
    public void init() {
        consume(new ConsumeItemFilter(i -> ammoTypes.containsKey(i)) {
            @Override
            public void build(Building build, Table table) {
                MultiReqImage image = new MultiReqImage();
                content.items().each(i -> filter.get(i) && i.unlockedNow(),
                        item -> image.add(new ReqImage(new Image(item.uiIcon),
                                () -> build instanceof AxItemTurretBuild it && !it.ammo.isEmpty() && ((AxItemTurret.ItemEntry)it.ammo.peek()).item == item)));
                table.add(image).size(8 * 4);
            }

            @Override
            public float efficiency(Building build) {
                return build instanceof AxItemTurretBuild it && it.ammo.size > 0 && (it.ammo.peek().amount >= ammoPerShot || it.cheating()) ? 1f : 0f;
            }

            @Override
            public void display(Stats stats) {}
        });

        if(targetGround) {
            ammoTypes.each((item, type) -> placeOverlapRange = Math.max(placeOverlapRange, range + type.rangeChange + placeOverlapMargin));
        }

        super.init();
    }

    public class AxItemTurretBuild extends PerkTurretTypeBuild {

        @Override
        public void onProximityAdded() {
            super.onProximityAdded();
            if(!hasAmmo() && cheating() && ammoTypes.size > 0) {
                handleItem(this, ammoTypes.keys().next());
            }
        }

        @Override
        public Object senseObject(LAccess sensor) {
            return switch(sensor) {
                case currentAmmoType -> ammo.size > 0 ? ((AxItemTurret.ItemEntry)ammo.peek()).item : null;
                default -> super.senseObject(sensor);
            };
        }

        @Override
        public void updateTile() {
            unit.ammo((float)unit.type().ammoCapacity * totalAmmo / maxAmmo);
            super.updateTile();
        }

        @Override
        public boolean hasAmmo() {
            // Allow shooting if a perk shot is pending, even if ammo is empty
            if(getPendingPerkBullet() != null) return true;
            return ammo.size > 0 && (ammo.peek().amount >= ammoPerShot || cheating());
        }

        @Override
        public BulletType peekAmmo() {
            BulletType perk = getPendingPerkBullet();
            if(perk != null) return perk;
            return ammo.size > 0 ? ammo.peek().type() : null;
        }

        @Override
        public BulletType useAmmo() {
            BulletType perk = consumePerkShot();
            if(perk != null) return perk;

            if(cheating()) return ammo.peek().type();
            var entry = (AxItemTurret.ItemEntry)ammo.peek();
            entry.amount -= ammoPerShot;
            totalAmmo -= ammoPerShot;
            if(entry.amount <= 0) ammo.pop();
            return entry.type();
        }

        @Override
        public int acceptStack(Item item, int amount, Teamc source) {
            BulletType type = ammoTypes.get(item);
            if(type == null) return 0;
            return Math.min((int)((maxAmmo - totalAmmo) / ammoTypes.get(item).ammoMultiplier), amount);
        }

        @Override
        public void handleStack(Item item, int amount, Teamc source) {
            for(int i = 0; i < amount; i++) handleItem(null, item);
        }

        @Override
        public int removeStack(Item item, int amount) { return 0; }

        @Override
        public void handleItem(Building source, Item item) {
            if(item == Items.pyratite) Events.fire(EventType.Trigger.flameAmmo);
            if(totalAmmo == 0) Events.fire(EventType.Trigger.resupplyTurret);

            BulletType type = ammoTypes.get(item);
            if(type == null) return;
            totalAmmo += type.ammoMultiplier;

            for(int i = 0; i < ammo.size; i++) {
                AxItemTurret.ItemEntry entry = (AxItemTurret.ItemEntry)ammo.get(i);
                if(entry.item == item) {
                    entry.amount += type.ammoMultiplier;
                    ammo.swap(i, ammo.size - 1);
                    return;
                }
            }
            ammo.add(new AxItemTurret.ItemEntry(item, (int)type.ammoMultiplier));
        }

        @Override
        public boolean acceptItem(Building source, Item item) {
            return ammoTypes.get(item) != null && totalAmmo + ammoTypes.get(item).ammoMultiplier <= maxAmmo;
        }

        @Override
        public byte version() { return 2; }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.b(ammo.size);
            for(AmmoEntry entry : ammo) {
                AxItemTurret.ItemEntry i = (AxItemTurret.ItemEntry)entry;
                write.s(i.item.id);
                write.s(i.amount);
            }
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            ammo.clear();
            totalAmmo = 0;
            int amount = read.ub();
            for(int i = 0; i < amount; i++) {
                Item item = Vars.content.item(revision < 2 ? read.ub() : read.s());
                int itemAmount = Math.min(read.s(), maxAmmo);
                if(item != null && ammoTypes.containsKey(item)) {
                    totalAmmo += itemAmount;
                    ammo.add(new AxItemTurret.ItemEntry(item, itemAmount));
                }
            }
        }
    }

    public class ItemEntry extends AmmoEntry {
        public Item item;

        ItemEntry(Item item, int amount) {
            this.item = item;
            this.amount = amount;
        }

        @Override
        public BulletType type() { return ammoTypes.get(item); }

        @Override
        public String toString() {
            return "ItemEntry{item=" + item + ", amount=" + amount + '}';
        }
    }
}