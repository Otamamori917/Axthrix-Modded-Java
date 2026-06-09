package axthrix.world.types.block.defense;

import arc.struct.ObjectMap;
import mindustry.entities.bullet.BulletType;
import mindustry.logic.LAccess;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;

public class AxPowerTurret extends PerkTurretType {
    public BulletType shootType;

    public AxPowerTurret(String name) {
        super(name);
        hasPower = true;
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
    }

    public void limitRange(float margin) {
        limitRange(shootType, margin);
    }

    public class AxPowerTurretBuild extends PerkTurretTypeBuild {

        @Override
        public void updateTile() {
            unit.ammo(power == null ? 0f : power.status * unit.type().ammoCapacity);
            super.updateTile();
        }

        @Override
        public double sense(LAccess sensor) {
            return switch(sensor) {
                case ammo -> power == null ? 0f : power.status;
                case ammoCapacity -> 1;
                default -> super.sense(sensor);
            };
        }

        @Override
        public BulletType useAmmo() {
            // Check for pending perk shot first — if one is queued, fire it instead
            BulletType perk = consumePerkShot();
            if(perk != null) return perk;
            return shootType;
        }

        @Override
        public boolean hasAmmo() {
            return true;
        }

        @Override
        public BulletType peekAmmo() {
            // Show perk bullet in peek if one is pending, else normal shoot type
            BulletType pending = getPendingPerkBullet();
            return pending != null ? pending : shootType;
        }
    }
}