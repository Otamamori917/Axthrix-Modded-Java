package axthrix.world.types.block.defense;

import arc.Core;
import arc.audio.Sound;
import arc.math.Mathf;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.AxthrixSounds;
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
    ///How close the enemy must be to start not fully charging.
    ///By percentage  1.25/75%  2/50%  3.33/30%  4/25%  5/20%  6.66/15%  10/10%
    public float aiChargeThreshold = 2;

    public ChargingShotgunTurret(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("charge", (ChargingShotgunBuild entity) -> new Bar(
                () -> Core.bundle.format("bar.aj-charge", Strings.autoFixed(entity.chargeProgress * 100f, 0)),
                () -> Tmp.c1.set(Pal.ammo).lerp(Pal.surge, entity.chargeProgress),
                () -> entity.chargeProgress
        ));
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(Stat.inaccuracy);
        stats.remove(Stat.reload);
        stats.remove(Stat.shootRange);
        stats.remove(Stat.range);




        stats.add(Stat.inaccuracy, minSpread + " - " + maxSpread+ " degrees");
        stats.add(Stat.charge, Strings.autoFixed(maxChargeTime / 60f, 1) + " seconds");
        stats.add(Stat.reload, "Min: " + 60.0F / reload + "/sec" +
                " - Max: " + 60.0F / (reload + maxChargeTime) + "/sec");
        stats.add(Stat.shootRange, "Min: " + AxUtil.GetRange(4f, 10 * minLifetimeMultiplier) + " blocks" +
                " - Max: " + AxUtil.GetRange(4f, 10 * maxLifetimeMultiplier) + " blocks");
    }

    public class ChargingShotgunBuild extends ItemTurretBuild {
        public float chargeTime = 0f;
        public float chargeProgress = 0f;
        public boolean charging = false;
        public boolean readyToFire = false;
        public float chargeEffectTimer = 0f;
        public float currentSpread = maxSpread; // Store current spread
        public float currentLifetimeMultiplier = minLifetimeMultiplier; // Store current lifetime mult

        @Override
        public void updateTile(){
            super.updateTile();

            // Don't interfere while waiting to fire
            if(readyToFire) return;

            if(hasAmmo()){
                // Get controlling player if any
                mindustry.gen.Player controllingPlayer = null;
                if(isControlled() && unit.controller() instanceof mindustry.gen.Player p){
                    controllingPlayer = p;
                }

                boolean playerHoldingFire = controllingPlayer != null && controllingPlayer.shooting;
                boolean aiShooting = !isControlled() && target != null;

                if(playerHoldingFire || aiShooting){
                    chargeEffectTimer += Time.delta;
                    if(chargeEffectTimer >= chargingEffectInterval){
                        for(int i = 0; i < 2; i++){
                            float side = i == 0 ? 1f : -1f;
                            float dist = (block.size * 4f) + chargeEffectOffset;

                            Tmp.v1.trns(rotation + 90f, dist * side);
                            float ex = x + Tmp.v1.x;
                            float ey = y + Tmp.v1.y;

                            float angle = rotation + 180f - (45f * side);

                            chargingEffect.at(ex, ey, angle, Tmp.c1.set(Pal.ammo).lerp(Pal.surge, chargeProgress));
                        }
                        chargeEffectTimer = 0f;
                    }

                    if(!charging){
                        charging = true;
                        chargeTime = 0f;
                        chargingSound.at(x, y);
                    }

                    if(isControlled()){
                        chargeTime = Math.min(chargeTime + Time.delta, maxChargeTime);
                        chargeProgress = chargeTime / maxChargeTime;

                        // Update spread and lifetime in real-time
                        currentSpread = Mathf.lerp(maxSpread, minSpread, chargeProgress);
                        currentLifetimeMultiplier = Mathf.lerp(minLifetimeMultiplier, maxLifetimeMultiplier, chargeProgress);
                    }else{
                        float desiredCharge = Mathf.clamp(dst(target) / (range()/aiChargeThreshold));
                        float desiredChargeTime = desiredCharge * maxChargeTime;

                        if(chargeTime < desiredChargeTime){
                            chargeTime = Math.min(chargeTime + Time.delta, desiredChargeTime);
                            chargeProgress = chargeTime / maxChargeTime;

                            // Update spread and lifetime
                            currentSpread = Mathf.lerp(maxSpread, minSpread, chargeProgress);
                            currentLifetimeMultiplier = Mathf.lerp(minLifetimeMultiplier, maxLifetimeMultiplier, chargeProgress);
                        }else{
                            readyToFire = true;
                        }
                    }
                }else{
                    if(isControlled() && charging && chargeTime > 0){
                        chargingSound.stop();
                        readyToFire = true;
                    }else{
                        resetCharge();
                    }
                }
            }else{
                resetCharge();
            }
        }

        @Override
        protected void updateShooting(){
            if(readyToFire){
                super.updateShooting();
            }
        }

        @Override
        protected void shoot(BulletType type){
            float originalInaccuracy = inaccuracy;
            inaccuracy = currentSpread;
            BulletType modifiedType = type.copy();
            modifiedType.lifetime = type.lifetime * currentLifetimeMultiplier;
            super.shoot(modifiedType);
            inaccuracy = originalInaccuracy;
            resetCharge();
        }

        public void resetCharge(){
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