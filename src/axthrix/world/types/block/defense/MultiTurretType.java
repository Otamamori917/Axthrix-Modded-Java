package axthrix.world.types.block.defense;

import arc.graphics.g2d.*;
import arc.struct.Seq;
import axthrix.world.types.weapontypes.BlockWeapon;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;

public class MultiTurretType extends Turret {

    public DrawBlock drawer;

    public Seq<BlockWeapon> weapons;


    public MultiTurretType(String name) {
        super(name);
        drawer = new DrawDefault();
        weapons = new Seq<>();
        itemCapacity = 10;
        canPickup = false; //no MountUnitType synergy
    }

    @Override
    public void setStats() {
        super.setStats();
        for (BlockWeapon weapon : weapons) {
            weapon.addStats(this);
        }
    }

    @Override
    public void load() {
        super.load();
        drawer.load(this);
        for (BlockWeapon weapon : weapons) {
            weapon.load(this);
        }
    }

    @Override
    public void init() {
        for (BlockWeapon weapon : weapons) {
            weapon.init(this);
        }
        super.init();
    }

    public TextureRegion[] icons() {
        return this.drawer.finalIcons(this);
    }

    public void getRegionsToOutline(Seq<TextureRegion> out) {
        this.drawer.getRegionsToOutline(this, out);
    }

    public class MultiTurretTypeBuild extends TurretBuild {
        Seq<BlockWeapon.BlockWeaponMount> weaponMounds = new Seq<>();

        @Override
        public void created() {
            super.created();
            for (BlockWeapon weapon : weapons) {
                weaponMounds.add(new BlockWeapon.BlockWeaponMount(weapon));
            }
        }

        @Override
        public void updateTile() {
            super.updateTile();
            for (BlockWeapon.BlockWeaponMount weaponMount : weaponMounds) {
                weaponMount.weapon.update(this,weaponMount);
                if (weaponMount.shoot) {
                }
            }
        }

        @Override
        public void draw() {
            drawer.draw(this);
            for (BlockWeapon.BlockWeaponMount weaponMount : weaponMounds) {
                weaponMount.weapon.draw(this,weaponMount);
            }
        }
    }
}
