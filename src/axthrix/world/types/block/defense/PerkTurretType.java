package axthrix.world.types.block.defense;

import arc.struct.Seq;
import arc.util.Time;
import axthrix.world.types.perks.*;
import axthrix.world.util.AxPartParms;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
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

    public class PerkTurretTypeBuild extends HeadTurretBuild {

        public Seq<Perk> perkStates = new Seq<>();
        public boolean firedLastTick = false;

        /**
         * Reload multiplier for the shot immediately after a perk shot fires.
         * Set by BulletPerk.onPendingShotFired(). Values < 1.0 = faster reload. 1.0 = no boost.
         */
        public float pendingReloadMultiplier = 1f;
        private boolean applyingReloadBoost = false;

        private float combinedDamageMultiplier = 1f;

        // Base range stored on created() to restore after range buffs
        private float baseRange = 0f;

        @Override
        public void created() {
            super.created();
            perkStates.clear();
            for(Perk perk : perks) {
                perkStates.add(perk.copy());
            }
            baseRange = range;
        }

        @Override
        public void updateTile() {
            // Apply post-perk reload boost
            if(applyingReloadBoost && pendingReloadMultiplier < 1f) {
                float extraProgress = reloadCounter * (1f / pendingReloadMultiplier - 1f) * Time.delta / reload;
                reloadCounter = Math.min(reloadCounter + extraProgress, reload);
                if(reloadCounter >= reload) {
                    applyingReloadBoost = false;
                    pendingReloadMultiplier = 1f;
                }
            }

            super.updateTile();

            boolean firedThisTick = firedLastTick;
            firedLastTick = false;

            // Reset combined buffs each tick before recalculating
            // Cached combined turret buff values — recalculated each tick
            float combinedReloadMultiplier = 1f;
            combinedDamageMultiplier = 1f;
            float combinedRangeBonus = 0f;

            for(Perk state : perkStates) {
                state.update(null, this);
                state.tickTimer(null, this, firedThisTick);

                // Aggregate turret-side buffs from each perk type
                if(state instanceof ShotBuffPerk sb) {
                    combinedReloadMultiplier *= sb.getShotReloadMultiplier();
                    combinedDamageMultiplier *= sb.getShotDamageMultiplier();
                } else if(state instanceof DurationPerk dp) {
                    float s = dp.getBuffScale();
                    if(s > 0f) {
                        combinedReloadMultiplier *= dp.getTurretReloadMultiplierFlat(s);
                        combinedDamageMultiplier *= dp.getTurretDamageMultiplierFlat(s);
                        combinedRangeBonus += dp.getTurretRangeBonus(s);
                    }
                //} else if(state instanceof RangePerk rp) {
                //    rp.updateForTurret(this);
                //    float s = rp.getCurrentScale();
                //    combinedReloadMultiplier *= rp.getTurretReloadMultiplierFlat(s);
                //    combinedDamageMultiplier *= rp.getTurretDamageMultiplierFlat(s);
                //    combinedRangeBonus += rp.getTurretRangeBonus(s);
                } else if(state instanceof DistancePerk dist) {
                    // DistancePerk applies consumed buffs at shoot time via useAmmo()
                    // Range bonus applied here if stacks were recently consumed
                    combinedRangeBonus += dist.getConsumedRangeBonus();
                } else {
                    // Generic stacking perk — apply stacking turret buffs
                    combinedReloadMultiplier *= state.getTurretReloadMultiplier();
                    combinedDamageMultiplier *= state.getTurretDamageMultiplier();
                }
            }

            // Apply reload multiplier by modifying reloadCounter advancement
            // (we scale the counter increment rather than the reload field itself)
            if(combinedReloadMultiplier < 1f) {
                float bonus = reloadCounter * (1f / combinedReloadMultiplier - 1f) * Time.delta / reload;
                reloadCounter = Math.min(reloadCounter + bonus, reload);
            }

            // Apply range bonus
            range = baseRange + combinedRangeBonus;

            // Feed smoothed part params for DrawPerkTurretType
            if(drawer instanceof DrawTurret dt && dt.parts.size > 0) {
                Perk primary = getPrimaryPerk();
                AxPartParms.perkparams.set(
                        primary != null ? primary.smoothProgress : 0f,
                        primary != null ? primary.smoothActivated : 0f
                );
            }
        }

        @Override
        public void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset) {
            super.handleBullet(bullet, offsetX, offsetY, angleOffset);
            firedLastTick = true;
            // Apply combined damage multiplier to the bullet
            if(combinedDamageMultiplier != 1f) {
                bullet.damage *= combinedDamageMultiplier;
            }
            // Notify ShotBuffPerks with resetOnShot=true
            for(Perk state : perkStates) {
                if(state instanceof ShotBuffPerk sb && sb.resetOnShot) {
                    sb.consumeShot(null, this);
                }
                if(state instanceof DistancePerk dist) {
                    dist.onShoot(null, this);
                    // Apply consumed distance buff to this bullet
                    bullet.damage *= dist.getConsumedDamageMultiplier();
                }
            }
        }

        // ---- Shot replacement via ammo system ----

        @Override
        public BulletType peekAmmo() {
            BulletType perk = getPendingPerkBullet();
            return perk != null ? perk : super.peekAmmo();
        }

        @Override
        public BulletType useAmmo() {
            for(Perk state : perkStates) {
                if(state.pendingShot) {
                    BulletType bt = state.getPendingBullet();
                    if(bt != null) {
                        state.pendingShot = false;
                        state.onPendingShotFired(this);
                        if(pendingReloadMultiplier < 1f) {
                            applyingReloadBoost = true;
                        }
                        return bt;
                    }
                }
            }
            return super.useAmmo();
        }

        private BulletType getPendingPerkBullet() {
            for(Perk state : perkStates) {
                if(state.pendingShot) {
                    BulletType bt = state.getPendingBullet();
                    if(bt != null) return bt;
                }
            }
            return null;
        }

        // ---- Perk hit/miss/received-hit routing ----

        public void onHit(float targetX, float targetY) {
            float dist = arc.math.Mathf.dst(x, y, targetX, targetY);
            for(Perk state : perkStates) {
                state.registerHit(null, this, targetX, targetY, dist);
            }
        }

        public void onMiss() {
            for(Perk state : perkStates) {
                state.registerMiss(null, this);
            }
        }

        /**
         * Call when this turret takes damage from an enemy bullet.
         * Routes into RECEIVE_HITS perks.
         */
        public void onReceivedHit() {
            for(Perk state : perkStates) {
                state.registerReceivedHit(null, this);
            }
        }

        // ---- Perk param accessors ----

        public float getPerkProgress() {
            Perk primary = getPrimaryPerk();
            return primary != null ? primary.smoothProgress : 0f;
        }

        public float getPerkActivated() {
            Perk primary = getPrimaryPerk();
            return primary != null ? primary.smoothActivated : 0f;
        }

        public Perk getPrimaryPerk() {
            if(perkStates.isEmpty()) return null;
            int idx = Math.min(primaryPerkIndex, perkStates.size - 1);
            return perkStates.get(idx);
        }
    }
}