package axthrix.world.types.block.defense;

import arc.func.Floatp;
import arc.func.Prov;
import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.world.types.perks.*;
import axthrix.world.util.AxPartParms;
import axthrix.world.util.AxStats;
import axthrix.world.util.PerkStats;
import axthrix.world.util.ui.TemperatureBar;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.draw.DrawTurret;

public class PerkTurretType extends HeadTurretClass {

    public Seq<Perk> perks = new Seq<>();

    /**
     * Index into perks that drives AxPartParms.perkparams for DrawPerkTurretType.
     * Param 0 = smoothProgress (0.0-1.0), param 1 = smoothActivated (0.0-1.0).
     */
    public int primaryPerkIndex = 0;

    public PerkTurretType(String name) {
        super(name);
    }

    public PerkTurretType withPerk(Perk perk) {
        perks.add(perk);
        return this;
    }

    @Override
    public void setStats() {
        super.setStats();
        if (!perks.isEmpty()) {
            stats.add(AxStats.perkSystem, t -> PerkStats.addPerkStats(t, perks));
        }
    }

    @Override
    public void setBars() {
        super.setBars();

        if (perks.any()) {
            for (Perk perk : perks) {
                String barName = "perk-" + perk.name;

                addBar(barName, (PerkTurretTypeBuild b) -> {
                    PerkStateData s = b.getState(perk);

                    return new Bar(
                            () -> {  // Title
                                if (perk instanceof DurationPerk dp) {
                                    return perk.name + " [accent]" + (int)(dp.getBuffProgress(s) * 100) + "%[]";
                                }
                                if (perk instanceof BulletPerk) {
                                    return perk.name + (s.pendingShot ? " [green]READY[]" : " " + s.currentStacks + "/" + perk.maxStacks);
                                }
                                if (perk instanceof RangePerk || perk instanceof SpeedPerk) {
                                    return perk.name + " [accent]" + (int)(s.smoothProgress * 100) + "%[]";
                                }
                                return perk.name + " " + s.currentStacks + "/" + perk.maxStacks;
                            },
                            () -> {  // Color
                                boolean isActive = s.smoothActivated > 0.4f ||
                                        (perk instanceof DurationPerk dp && dp.getBuffScale(s) > 0f) ||
                                        (perk instanceof BulletPerk && s.pendingShot);
                                return isActive ? mindustry.graphics.Pal.remove :
                                        (perk.color != null ? perk.color : mindustry.graphics.Pal.accent);
                            },
                            () -> {  // Fraction
                                if (perk instanceof DurationPerk dp) return dp.getBuffProgress(s);
                                if (perk instanceof RangePerk || perk instanceof SpeedPerk) return s.smoothProgress;
                                if (perk instanceof BulletPerk) return s.smoothProgress;
                                return perk.maxStacks > 0 ? (float) s.currentStacks / perk.maxStacks : 0f;
                            }
                    );
                });
            }
        }
    }


    public class PerkTurretTypeBuild extends HeadTurretBuild {

        public boolean firedLastTick = false;

        /**
         * Reload multiplier for the shot immediately after a perk shot fires.
         * Set by BulletPerk.onPendingShotFired(). Values < 1.0 = faster reload.
         */
        public float pendingReloadMultiplier = 1f;
        private boolean applyingReloadBoost = false;

        private float combinedDamageMultiplier = 1f;
        private float baseRange = 0f;

        /** Gets PerkStateData for a given perk using this building's id as the owner key. */
        public PerkStateData getState(Perk perk) {
            return Perk.getState(id, perk.name);
        }

        @Override
        public void created() {
            super.created();
            baseRange = range;
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            Perk.clearState(id);
        }

        @Override
        public void updateTile() {
            if(applyingReloadBoost && pendingReloadMultiplier < 1f) {
                float extraProgress = (1f / pendingReloadMultiplier - 1f) * Time.delta;
                reloadCounter = Math.min(reloadCounter + extraProgress, reload);
                if(reloadCounter >= reload) {
                    applyingReloadBoost = false;
                    pendingReloadMultiplier = 1f;
                }
            }

            super.updateTile();

            boolean firedThisTick = firedLastTick;
            firedLastTick = false;

            float combinedReloadMultiplier = 1f;
            combinedDamageMultiplier = 1f;
            float combinedRangeBonus = 0f;

            for(Perk perk : perks) {
                PerkStateData s = getState(perk);
                perk.update(null, this, s);
                perk.tickTimer(null, this, s, firedThisTick);

                if(perk instanceof ShotBuffPerk sb) {
                    combinedReloadMultiplier *= sb.getShotReloadMultiplier(s);
                    combinedDamageMultiplier *= sb.getShotDamageMultiplier(s);
                } else if(perk instanceof DurationPerk dp) {
                    float sc = dp.getBuffScale(s);
                    if(sc > 0f) {
                        combinedReloadMultiplier *= dp.getTurretReloadMultiplierFlat(sc);
                        combinedDamageMultiplier *= dp.getTurretDamageMultiplierFlat(sc);
                        combinedRangeBonus += dp.getTurretRangeBonus(sc);
                    }
                } else if(perk instanceof RangePerk rp) {
                    rp.updateForTurret(this, s);
                    float sc = rp.getCurrentScale(s);
                    combinedReloadMultiplier *= rp.getTurretReloadMultiplierFlat(sc);
                    combinedDamageMultiplier *= rp.getTurretDamageMultiplierFlat(sc);
                    combinedRangeBonus += rp.getTurretRangeBonus(sc);
                } else {
                    combinedReloadMultiplier *= perk.getTurretReloadMultiplier(s);
                    combinedDamageMultiplier *= perk.getTurretDamageMultiplier(s);
                }
            }

            if(combinedReloadMultiplier < 1f) {
                float bonus = (1f / combinedReloadMultiplier - 1f) * Time.delta;
                reloadCounter = Math.min(reloadCounter + bonus, reload);
            }

            range = baseRange + combinedRangeBonus;

            if(drawer instanceof DrawTurret dt && dt.parts.size > 0 && !perks.isEmpty()) {
                Perk primary = perks.get(Math.min(primaryPerkIndex, perks.size - 1));
                PerkStateData ps = getState(primary);
                AxPartParms.perkparams.set(ps.smoothProgress, ps.smoothActivated);
            }
        }

        @Override
        public void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset) {
            super.handleBullet(bullet, offsetX, offsetY, angleOffset);
            firedLastTick = true;
            if(combinedDamageMultiplier != 1f) {
                bullet.damage *= combinedDamageMultiplier;
            }
            for(Perk perk : perks) {
                PerkStateData s = getState(perk);
                if(perk instanceof ShotBuffPerk sb && sb.resetOnShot) {
                    sb.consumeShot(null, this, s);
                }
            }
        }

        // ---- Shot replacement ----

        /** Returns a pending perk bullet if any perk has one queued, else null. */
        public BulletType getPendingPerkBullet() {
            for(Perk perk : perks) {
                PerkStateData s = getState(perk);
                if(s.pendingShot) {
                    BulletType bt = perk.getPendingBullet();
                    if(bt != null) return bt;
                }
            }
            return null;
        }

        /**
         * Consumes the first pending perk shot and returns its bullet type, or null.
         * Called from AxPowerTurretBuild.useAmmo().
         */
        public BulletType consumePerkShot() {
            for(Perk perk : perks) {
                PerkStateData s = getState(perk);
                if(s.pendingShot) {
                    BulletType bt = perk.getPendingBullet();
                    if(bt != null) {
                        s.pendingShot = false;
                        perk.onPendingShotFired(this, s);
                        if(pendingReloadMultiplier < 1f) {
                            applyingReloadBoost = true;
                        }
                        return bt;
                    }
                }
            }
            return null;
        }

        // ---- Hit / miss / received-hit routing ----

        public void onHit(float targetX, float targetY) {
            float dist = arc.math.Mathf.dst(x, y, targetX, targetY);
            for(Perk perk : perks) {
                perk.registerHit(null, this, getState(perk), targetX, targetY, dist);
            }
        }

        public void onMiss() {
            for(Perk perk : perks) {
                perk.registerMiss(null, this, getState(perk));
            }
        }

        public void onReceivedHit() {
            for(Perk perk : perks) {
                perk.registerReceivedHit(null, this, getState(perk));
            }
        }

        // ---- Perk param accessors ----

        public float getPerkProgress() {
            if(perks.isEmpty()) return 0f;
            return getState(perks.get(Math.min(primaryPerkIndex, perks.size - 1))).smoothProgress;
        }

        public float getPerkActivated() {
            if(perks.isEmpty()) return 0f;
            return getState(perks.get(Math.min(primaryPerkIndex, perks.size - 1))).smoothActivated;
        }
    }
}