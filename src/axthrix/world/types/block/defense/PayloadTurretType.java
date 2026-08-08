package axthrix.world.types.block.defense;

import arc.math.*;
import arc.math.geom.Vec2;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.block.PayloadAmmoBlock;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.AxStatValues;
import axthrix.world.util.TempUnit;
import axthrix.world.util.draw.DrawIPayloadTurret;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

import static axthrix.content.blocks.PayloadAmmoBlocks.*;
import static mindustry.Vars.tilesize;

public class PayloadTurretType extends AxPayloadAmmoTurret {
    public float payloadSpeed = 0.7f;
    public float minLoadWarmup = 1f;

    /// If true, the turret will fire multiple bullets based on payload block size
    public boolean dynamicBurst = false;
    public int bulletsPerSize = 10;

    /// If true, this turret generates temperature via TemperatureLogic on every volley
    public boolean generatesHeat = false;
    /// Temperature added per volley, multiplied by the fired payload's block size. Negative values generate cold instead of heat.
    public float heatPerSize = 8f;
    /// Magnitude at which burst timing starts slowing down / degrading
    public float heatWarningThreshold = 55f;
    /// Magnitude at which the turret refuses to fire entirely
    public float heatCriticalThreshold = 85f;
    /// Consecutive ticks the turret can sit at/above critical before meltdown
    public float heatCriticalTimeLimit = 300f;
    /// Damage dealt to the nearby on meltdown
    public float meltdownDamage = 400f;
    public float meltdownRadius = 40f;

    /// Self-damage per second while the turret is at critical heat
    public float criticalSelfDamage = 35f;

    public PayloadTurretType(String name) {
        super(name);

        maxAmmo = 1;
        shootEffect = smokeEffect = Fx.none;
        outlinedIcon = 3;

        drawer = new DrawIPayloadTurret(true);
    }

    @Override
    public void setBars(){
        super.setBars();

        if(generatesHeat){
            addBar("temperature", (PayloadTurretTypeBuild entity) -> new TemperatureBar("Temp", entity));
        }
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.ammo);

        if(!generatesHeat) {
            stats.add(Stat.ammo, AxStatValues.ammo(ammoTypes, 0, true));
        } else {
            stats.add(Stat.ammo, AxStatValues.ammoheat(ammoTypes, 0, true, heatPerSize, reload));
            stats.add(Stat.heatCapacity, table -> {
                String heatCap = TempUnit.format(heatCriticalThreshold);
                table.add("[red]" + heatCap + "[] is critical");
                table.add("[accent]Meltdown after " + (int)(heatCriticalTimeLimit / 60) + " seconds[]").row();
                if (criticalSelfDamage > 0) {
                    table.add("[red]Takes " + criticalSelfDamage + " damage per second[] while critical");
                }
            });
        }
    }

    public void setWarmupTime(float seconds) {
        minLoadWarmup = 0f;
        minWarmup = 1f;
        linearWarmup = true;
        shootWarmupSpeed = 1 / (seconds * 60f);
    }

    public void setUsers() {
        for (var entry : ammoTypes.copy().entries()) {
            if (entry.key instanceof PayloadAmmoBlock m) {
                m.user = this;
                m.bullet = entry.value;
            }
        }
    }

    public class PayloadTurretTypeBuild extends AxPayloadTurretBuild {

        public Payload payload;
        public float payLen;
        public Vec2 payVector = new Vec2();

        protected float criticalTime = 0f;
        protected float selfDamageTimer = 0f;

        @Override
        public void updateTile() {
            if (moveInPayload()) {
                payloads.add(payload.content());
                payload = null;
            }

            if (generatesHeat) updateHeatState();

            super.updateTile();
        }

        protected void updateHeatState() {
            float heat = TemperatureLogic.getDirectedTemperature(this, heatPerSize);

            if (heat >= heatCriticalThreshold) {
                criticalTime += Time.delta;

                if (criticalSelfDamage > 0) {
                    selfDamageTimer += Time.delta;
                    if (selfDamageTimer >= 60f) {
                        damage(criticalSelfDamage);
                        selfDamageTimer -= 60f;
                    }
                }

                if (criticalTime >= heatCriticalTimeLimit) {
                    meltdown();
                }
            } else {
                criticalTime = 0f;
                selfDamageTimer = 0f;
            }
        }

        protected void meltdown() {
            Damage.damage(team, x, y, meltdownRadius, meltdownDamage);
            Fx.blockExplosionSmoke.at(x, y);
            TemperatureLogic.setTemperatureBuilding(this, 0f);

            criticalTime = 0f;
            kill();
        }

        @Override
        public boolean acceptPayload(Building source, Payload payload) {
            return this.payload == null && shootWarmup <= minLoadWarmup && super.acceptPayload(source, payload);
        }

        @Override
        public void handlePayload(Building source, Payload payload) {
            this.payload = payload;
            if (source != null) {
                this.payVector.set(source).sub(this).clamp(-size * tilesize / 2f, -size * tilesize / 2f, size * tilesize / 2f, size * tilesize / 2f);
            }
            payLen = payVector.len();
        }

        public void updatePayload() {
            if (payload != null) {
                payload.set(x + payVector.x, y + payVector.y, payload.rotation());
            }
        }

        public boolean moveInPayload() {
            if (payload == null) return false;

            updatePayload();
            payVector.approach(Vec2.ZERO, payloadSpeed * delta());
            return hasArrived();
        }

        public boolean hasArrived() {
            return payVector.isZero(0.01f);
        }

        public float payloadf() {
            return payVector.len() / payLen;
        }

        @Override
        protected void shoot(BulletType type) {
            if (!dynamicBurst) {
                super.shoot(type);
                if (generatesHeat) {
                    applyHeat();
                }
                return;
            }

            float heat = generatesHeat ? TemperatureLogic.getDirectedTemperature(this, heatPerSize) : 0f;

            if (generatesHeat && heat >= heatCriticalThreshold) {
                return;
            }

            UnlockableContent ammo = currentAmmo();
            int size = (ammo instanceof Block b) ? b.size : 1;
            int shotCount = size * bulletsPerSize;

            float spacingMult = 1f;
            if (generatesHeat && heat > heatWarningThreshold) {
                float heatFactor = Mathf.clamp((heat - heatWarningThreshold) / (heatCriticalThreshold - heatWarningThreshold), 0f, 1f);
                spacingMult = 1f + heatFactor * 2f;
            }

            float heatPerBullet = (size * heatPerSize) / Math.max(shotCount, 1);

            for (int i = 0; i < shotCount; i++) {
                Time.run(i * spacingMult, () -> {
                    if (!isValid()) return;

                    super.shoot(type);

                    if (generatesHeat) {
                        TemperatureLogic.applyTemperatureBuilding(this, heatPerBullet);
                    }
                });
            }
        }

        private void applyHeat() {
            if (!generatesHeat) return;
            int size = (currentAmmo() instanceof Block b) ? b.size : 1;
            TemperatureLogic.applyTemperatureBuilding(this, size * heatPerSize);
        }

        @Override
        public void draw(){
            super.draw();

            if(generatesHeat){
                float heat = TemperatureLogic.getDirectedTemperature(this, heatPerSize);
                AxPartParms.axparams.set(0, 0, Mathf.clamp(heat / maxTempStorage));
            }
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(payVector.x);
            write.f(payVector.y);
            write.f(payLen);
            Payload.write(payload, write);
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            if (revision >= 2) {
                payVector.set(read.f(), read.f());
                payLen = read.f();
                payload = Payload.read(read);
            }
        }

        @Override
        public byte version() {
            return 2;
        }
    }
}