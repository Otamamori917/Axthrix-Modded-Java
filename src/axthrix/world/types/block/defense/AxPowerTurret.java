package axthrix.world.types.block.defense;

import arc.Core;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.types.sea.block.SeaTurret;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.logic.LAccess;
import mindustry.world.blocks.defense.turrets.PowerTurret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.state;

public class AxPowerTurret extends SeaTurret {
    public BulletType shootType;

    public AxPowerTurret(String name){
        super(name);
        hasPower = true;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(this, shootType)));
    }

    public void limitRange(float margin){
        limitRange(shootType, margin);
    }

    public class AxPowerTurretBuild extends SeaTurretBuild{

        @Override
        public void updateTile(){
            unit.ammo(power == null ? 0f : power.status * unit.type().ammoCapacity);

            super.updateTile();
        }

        @Override
        public double sense(LAccess sensor){
            return switch(sensor){
                case ammo -> power == null ? 0f : power.status;
                case ammoCapacity -> 1;
                default -> super.sense(sensor);
            };
        }

        @Override
        public BulletType useAmmo(){
            //nothing used directly
            return shootType;
        }

        @Override
        public boolean hasAmmo(){
            //you can always rotate, but never shoot if there's no power
            return true;
        }

        @Override
        public BulletType peekAmmo(){
            return shootType;
        }
    }
}