package axthrix.world.types.block.defense;

import arc.Core;
import arc.audio.Sound;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.AxthrixSounds;
import axthrix.world.types.block.defense.parent.AxItemTurret;
import axthrix.world.util.AxUtil;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class ChargingShotgunTurret extends AxItemTurret {

    public float maxChargeTime = 80f;
    public float minSpread = 2f;
    public float maxSpread = 35f;
    public float minLifetimeMultiplier = 1f;
    public float maxLifetimeMultiplier = 2f;

    public Effect chargingEffect = Fx.shootBigSmoke;
    public Sound chargingSound = AxthrixSounds.parserCharge;
    public float chargingEffectInterval = 15f;
    public float chargeEffectOffset = 0f;

    /// How close the enemy must be to start not fully charging.
    public float aiChargeThreshold = 0.5f;

    public ChargingShotgunTurret(String name) {
        super(name);
        canOverdrive = false;
    }

    @Override
    public void setBars() {
        super.setBars();

        addBar("charge", (ChargingShotgunBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.aj-charge", Strings.autoFixed(entity.chargeProgress * 100f, 0)),
                () -> Tmp.c1.set(Pal.ammo).lerp(Pal.surge, entity.chargeProgress),
                () -> entity.chargeProgress
        ));
    }

    @Override
    public void setStats() {
        super.setStats();

        stats.remove(Stat.inaccuracy);
        stats.remove(Stat.reload);
        stats.remove(Stat.shootRange);
        stats.remove(Stat.range);

        stats.add(Stat.inaccuracy, minSpread + " - " + maxSpread + " " + StatUnit.degrees.localized());
        stats.add(Stat.charge, Strings.autoFixed(maxChargeTime / 60f, 1) + " " + StatUnit.seconds.localized());
        stats.add(Stat.reload, Core.bundle.format("stat.aj-min") + ": " + 60f / reload + " " + StatUnit.perSecond.localized() +
                " - " + Core.bundle.format("stat.aj-max") + ": " + 60f / (reload + maxChargeTime) + " " + StatUnit.perSecond.localized());
        stats.add(Stat.shootRange, Core.bundle.format("stat.aj-min") + ": " + AxUtil.GetRange(4f, 10 * minLifetimeMultiplier) + " " + StatUnit.blocks.localized() +
                " - " + Core.bundle.format("stat.aj-max") + ": " + AxUtil.GetRange(4f, 10 * maxLifetimeMultiplier) + " " + StatUnit.blocks.localized());
    }

    public class ChargingShotgunBuild extends AxItemTurretBuild {

        public float chargeTime = 0f;
        public float chargeProgress = 0f;
        public boolean charging = false;
        public boolean readyToFire = false;
        public float chargeEffectTimer = 0f;

        public float currentSpread = maxSpread;
        public float currentLifetimeMultiplier = minLifetimeMultiplier;

        protected float custCounter;

        @Override
        public void updateTile() {
            super.updateTile();

            custCounter += Time.delta;

            if(custCounter >= reload){
                if (isShooting() && hasAmmo()) {
                    if (!charging) {
                        charging = true;
                        chargeTime = 0f;
                        chargingSound.at(x, y);
                    }

                    // Charging visual effect
                    chargeEffectTimer += Time.delta;
                    if (chargeEffectTimer >= chargingEffectInterval) {
                        spawnChargeEffects();
                        chargeEffectTimer = 0f;
                    }

                    // Increase charge
                    chargeTime = Math.min(chargeTime + Time.delta, maxChargeTime);
                    chargeProgress = chargeTime / maxChargeTime;

                    currentSpread = Mathf.lerp(maxSpread, minSpread, chargeProgress);
                    currentLifetimeMultiplier = Mathf.lerp(minLifetimeMultiplier, maxLifetimeMultiplier, chargeProgress);

                } else if (charging) {
                    // Player released the key (or AI lost target)
                    if (reloadCounter >= reload) {
                        shootCharged();
                        custCounter = 0;
                    } else {
                        readyToFire = true;
                    }
                    charging = false;
                }

                // AI auto fire when fully charged
                if (!isControlled() && charging && chargeProgress >= 0.95f && reloadCounter >= reload) {
                    shootCharged();
                    custCounter = 0;
                }
            }else{
                resetCharge();
            }


        }

        private void spawnChargeEffects() {
            for (int i = 0; i < 2; i++) {
                float side = i == 0 ? 1f : -1f;
                float dist = (block.size * 4f) + chargeEffectOffset;

                Tmp.v1.trns(rotation + 90f, dist * side);
                float ex = x + Tmp.v1.x;
                float ey = y + Tmp.v1.y;
                float angle = rotation + 180f - (45f * side);

                chargingEffect.at(ex, ey, angle, Tmp.c1.set(Pal.ammo).lerp(Pal.surge, chargeProgress));
            }
        }

        private void shootCharged() {
            float originalInaccuracy = inaccuracy;
            inaccuracy = currentSpread;

            BulletType modified = peekAmmo().copy();
            modified.lifetime *= currentLifetimeMultiplier;

            super.shoot(modified);

            inaccuracy = originalInaccuracy;
            resetCharge();
        }

        @Override
        protected void updateShooting() {
            if (readyToFire && reloadCounter >= reload) {
                shootCharged();
                custCounter = 0;
                readyToFire = false;
            }
        }

        public void resetCharge() {
            charging = false;
            chargeTime = 0f;
            chargeProgress = 0f;
            readyToFire = false;
            chargeEffectTimer = 0f;
            currentSpread = maxSpread;
            currentLifetimeMultiplier = minLifetimeMultiplier;
        }
    }
}