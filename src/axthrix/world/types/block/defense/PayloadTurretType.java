package axthrix.world.types.block.defense;

import arc.math.*;
import arc.math.geom.Vec2;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.block.PayloadAmmoBlock;
import axthrix.world.types.block.defense.parent.AxPayloadAmmoTurret;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.AxStatValues;
import axthrix.world.util.TempUnit;
import axthrix.world.util.draw.DrawIPayloadTurret;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Damage;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.*;
import mindustry.world.meta.*;

import static mindustry.Vars.tilesize;

public class PayloadTurretType extends AxPayloadAmmoTurret {
    public float payloadSpeed = 0.7f;
    public float minLoadWarmup = 1f;

    /// If true, the turret will fire multiple bullets based on payload block size
    public boolean dynamicBurst = false;
    public int bulletsPerSize = 10;

    public PayloadTurretType(String name) {
        super(name);

        maxAmmo = 1;
        shootEffect = smokeEffect = Fx.none;
        outlinedIcon = 3;

        drawer = new DrawIPayloadTurret(true);
    }


    @Override
    public void setStats() {
        super.setStats();
        stats.remove(Stat.ammo);

        stats.add(Stat.ammo, AxStatValues.ammo(ammoTypes, 0, true));

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


        @Override
        public void updateTile() {
            if (moveInPayload()) {
                payloads.add(payload.content());
                payload = null;
            }


            super.updateTile();
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
                return;
            }

            UnlockableContent ammo = currentAmmo();
            int size = (ammo instanceof Block b) ? b.size : 1;
            int shotCount = size * bulletsPerSize;

            float spacingMult = 1f;


            for (int i = 0; i < shotCount; i++) {
                Time.run(i * spacingMult, () -> {
                    if (!isValid()) return;

                    super.shoot(type);
                });
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