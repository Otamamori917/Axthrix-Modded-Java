package axthrix.world.types.block.defense;

import arc.struct.Seq;
import axthrix.world.types.perks.Perk;
import axthrix.world.util.AxPartParms;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.world.draw.DrawTurret;

public class PerkTurretType extends HeadTurretClass {

    public Seq<Perk> perks = new Seq<>();

    /**
     * Index into perks that drives AxPartParms.perkparams for DrawPerkTurretType.
     * Param 0 = smoothProgress (0.0-1.0), param 1 = smoothActivated (0.0-1.0).
     * Defaults to 0. Clamped to valid range automatically.
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

        @Override
        public void created() {
            super.created();
            perkStates.clear();
            for(Perk perk : perks) {
                perkStates.add(perk.copy());
            }
        }

        @Override
        public void updateTile() {
            // Apply post-perk reload boost by advancing the reload counter
            if(applyingReloadBoost && pendingReloadMultiplier < 1f) {
                float extraProgress = reloadCounter * (1f / pendingReloadMultiplier - 1f) * arc.util.Time.delta / reload;
                reloadCounter = Math.min(reloadCounter + extraProgress, reload);
                if(reloadCounter >= reload) {
                    applyingReloadBoost = false;
                    pendingReloadMultiplier = 1f;
                }
            }

            super.updateTile();

            boolean firedThisTick = firedLastTick;
            firedLastTick = false;

            for(Perk state : perkStates) {
                state.update(null, this);
                state.tickTimer(null, this, firedThisTick);
            }

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

        // ---- Perk hit/miss routing ----

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

        // ---- Perk param accessors (used by DrawPerkTurretType) ----

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