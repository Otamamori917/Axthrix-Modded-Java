package axthrix.world.types.block.defense;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import axthrix.world.types.block.defense.parent.PerkTurretType;
import axthrix.world.util.AxStatValues;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;

/** A turret that fires a continuous beam bullet with no reload or coolant necessary. The bullet only disappears when the turret stops shooting. */
public class AxContinuousTurret extends PerkTurretType {
    public BulletType shootType = Bullets.placeholder;
    /** Speed at which the turret can change its bullet "aim" distance. This is only used for point laser bullets. */
    public float aimChangeSpeed = Float.POSITIVE_INFINITY;
    public boolean scaleDamageEfficiency = false;

    public AxContinuousTurret(String name){
        super(name);

        coolantMultiplier = 1f;
        envEnabled |= Env.space;
        displayAmmoMultiplier = false;
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.ammo, AxStatValues.ammo(ObjectMap.of(this, shootType), false, false, null));
        stats.remove(Stat.reload);
        stats.remove(Stat.inaccuracy);
    }

    public class AxContinuousTurretBuild extends PerkTurretType.PerkTurretTypeBuild{
        public Seq<BulletEntry> bullets = new Seq<>();
        public float lastLength = size * 4f;

        @Override
        public float estimateDps(){
            if(!hasAmmo()) return 0f;
            return shootType.damage * 60f / (shootType instanceof ContinuousBulletType c ? c.damageInterval : 5f);
        }

        @Override
        protected void updateCooling(){
        }

        @Override
        public BulletType useAmmo(){
            return shootType;
        }

        @Override
        public boolean hasAmmo(){
            return canConsume();
        }

        @Override
        public boolean shouldConsume(){
            return isShooting();
        }

        @Override
        public BulletType peekAmmo(){
            return shootType;
        }

        @Override
        public float getAmmoFraction(){
            float ammoFract = efficiency;
            if(findConsumer(f -> f instanceof ConsumeLiquidBase) instanceof ConsumeLiquid cons){
                ammoFract = Math.min(ammoFract, liquids.get(cons.liquid) / liquidCapacity);
            }

            return ammoFract;
        }

        @Override
        public void updateTile(){
            super.updateTile();

            bullets.removeAll(b -> !b.bullet.isAdded() || b.bullet.type == null || b.bullet.owner != this);

            if(bullets.any()){
                for(var entry : bullets){
                    updateBullet(entry);
                }

                wasShooting = true;
                heat = 1f;
                curRecoil = recoil;
            }
        }

        protected void updateBullet(BulletEntry entry){
            float
                    bulletX = x + Angles.trnsx(rotation - 90, shootX + entry.x, shootY + entry.y),
                    bulletY = y + Angles.trnsy(rotation - 90, shootX + entry.x, shootY + entry.y),
                    angle = rotation + entry.rotation;

            entry.bullet.rotation(angle);
            entry.bullet.set(bulletX, bulletY);

            float shootLength = Math.min(dst(targetPos), range);
            float curLength = dst(entry.bullet.aimX, entry.bullet.aimY);
            float resultLength = Mathf.approachDelta(curLength, shootLength, aimChangeSpeed);
            Tmp.v1.trns(rotation, lastLength = resultLength).add(x, y);

            entry.bullet.aimX = Tmp.v1.x;
            entry.bullet.aimY = Tmp.v1.y;
            if(scaleDamageEfficiency){
                entry.bullet.damage = entry.bullet.type.damage * Math.min(efficiency, 1f) * timeScale * entry.bullet.damageMultiplier();
            }

            if(isShooting() && hasAmmo()){
                entry.bullet.time = entry.bullet.lifetime * entry.bullet.type.optimalLifeFract * Math.min(shootWarmup, efficiency);
                entry.bullet.keepAlive = true;
            }
        }

        @Override
        protected void updateReload(){
        }

        @Override
        protected void updateShooting(){
            if(bullets.any()){
                return;
            }

            if(canConsume() && !charging() && shootWarmup >= minWarmup){
                shoot(peekAmmo());
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, efficiency * rotateSpeed * delta());
        }

        @Override
        public void handleBullet(@Nullable Bullet bullet, float offsetX, float offsetY, float angleOffset){
            if(bullet != null){
                bullets.add(new BulletEntry(bullet, offsetX, offsetY, angleOffset, 0f));

                Tmp.v1.trns(rotation, shootY + lastLength).add(x, y);
                bullet.aimX = Tmp.v1.x;
                bullet.aimY = Tmp.v1.y;
            }
        }

        @Override
        public boolean shouldActiveSound(){
            return bullets.any();
        }

        @Override
        public float activeSoundVolume(){
            return 1f;
        }

        @Override
        public byte version(){
            return 3;
        }

        @Override
        public void write(Writes write){
            super.write(write);

            write.f(lastLength);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);

            if(revision >= 3){
                lastLength = read.f();
            }
        }
    }
}
