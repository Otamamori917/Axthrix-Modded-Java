package axthrix.world.types.block.defense;

import arc.struct.Seq;
import axthrix.world.types.perks.Perk;
import mindustry.gen.Bullet;

/**
 * A power turret that supports the modular perk system.
 * Perks are defined on the turret block and each build instance gets deep-copied independent perk states.
 */
public class PerkTurretType extends HeadTurretClass {
    // ---- Configuration ----

    /**
     * Perks attached to this turret type.
     */
    public Seq<Perk> perks = new Seq<>();

    /**
     * Index into perks that drives AxPartParms.perkparams (part params 0 and 1).
     * Defaults to 0. Clamped to valid range automatically.
     */
    public int primaryPerkIndex = 0;

    // ---- Constructor ----

    public PerkTurretType(String name) {
        super(name);
    }

    // ---- Perk Registration Helper ----

    /**
     * Adds a perk to this turret. Returns this for chaining.
     */
    public PerkTurretType withPerk(Perk perk) {
        perks.add(perk);
        return this;
    }

    // ---- Build Class ----

    public class PerkTurretTypeBuild extends HeadTurretBuild {

        /** Per-build deep copies of all perk states. */
        public Seq<Perk> perkStates = new Seq<>();

        /** Whether the turret fired during the last update tick. */
        public boolean firedLastTick = false;

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
            super.updateTile();

            boolean firedThisTick = firedLastTick;
            firedLastTick = false;

            for(Perk state : perkStates) {
                state.update(null, this);
                state.tickTimer(null, this, firedThisTick);
            }
        }

        @Override
        public void handleBullet(Bullet bullet, float offsetX, float offsetY, float angleOffset) {
            super.handleBullet(bullet, offsetX, offsetY, angleOffset);
            firedLastTick = true;
        }

        /**
         * Call from hitEntity() when a bullet from this turret hits an enemy.
         * @param targetX World X of the hit target.
         * @param targetY World Y of the hit target.
         */
        public void onHit(float targetX, float targetY) {
            float dist = arc.math.Mathf.dst(x, y, targetX, targetY);
            for(Perk state : perkStates) {
                state.registerHit(null, this, targetX, targetY, dist);
            }
        }

        /**
         * Call from despawned() when a bullet from this turret misses.
         */
        public void onMiss() {
            for(Perk state : perkStates) {
                state.registerMiss(null, this);
            }
        }

        public float getPerkProgress() {
            Perk primary = getPrimaryPerk();
            return primary != null ? primary.getProgress() : 0f;
        }

        public float getPerkActivated() {
            Perk primary = getPrimaryPerk();
            return primary != null ? primary.getActivatedParam() : 0f;
        }

        /** Returns the primary perk state, or null if none. */
        public Perk getPrimaryPerk() {
            if(perkStates.isEmpty()) return null;
            int idx = Math.min(primaryPerkIndex, perkStates.size - 1);
            return perkStates.get(idx);
        }
    }
}
