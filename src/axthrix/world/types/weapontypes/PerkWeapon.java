//package axthrix.world.types.weapontypes;
//
//import arc.struct.ObjectIntMap;
//import arc.struct.ObjectMap;
//import arc.struct.Seq;
//import axthrix.world.types.perks.Perk;
//import axthrix.world.util.AxPartParms;
//import mindustry.entities.units.WeaponMount;
//import mindustry.gen.Unit;
//import mindustry.type.Weapon;
//
///**
// * A weapon type that supports a modular perk system.
// * Perks are independent effects tracking their own hit streaks, stacks, and timers.
// * Multiple perks can be active simultaneously and each unit gets its own perk state.
// *
// * Custom part params (via AxPartParms.perkparams, read via AxPartParms.PerkPartProgress):
// *   perkProgress:  Primary perk charge progress (0.0 = no progress, 1.0 = max stacks).
// *   perkActivated: Primary perk activated state (1.0 = at max stacks, 0.0 = not).
// *
// * Integrating hit/miss callbacks:
// *   In your bullet type's hitEntity(), call:
// *     if(mount.weapon instanceof PerkWeaponType pw) pw.onHit(unit, mount, tx, ty, sx, sy);
// *   In your bullet type's despawned(), call:
// *     if(mount.weapon instanceof PerkWeaponType pw) pw.onMiss(unit, mount);
// *
// * Usage example:
// * <pre>
// *   PerkWeaponType charon = new PerkWeaponType("charon") {{
// *       perks.add(new CharonPerk());
// *   }};
// * </pre>
// */
//public class PerkWeapon extends Weapon {
//
//    // ---- Configuration ----
//
//    /**
//     * Perks attached to this weapon type.
//     * Each unit that mounts this weapon gets deep-copied independent perk states.
//     */
//    public Seq<Perk> perks = new Seq<>();
//
//    /**
//     * Index into perks that drives AxPartParms.perkparams (part params 0 and 1).
//     * Defaults to 0. Clamped to valid range automatically.
//     */
//    public int primaryPerkIndex = 0;
//
//    // ---- Per-unit perk state storage ----
//    // Weapon is a shared type object, so per-unit state must be stored externally.
//    // We key by unit ID for fast lookup.
//    private final ObjectMap<Integer, Seq<Perk>> unitPerkStates = new ObjectMap<>();
//    private final ObjectIntMap<Integer> unitFiredTick = new ObjectIntMap<>();
//
//    // ---- Constructor ----
//
//    public PerkWeapon(String name) {
//        super(name);
//    }
//
//    public PerkWeapon() {
//        super();
//    }
//
//    // ---- Perk Registration Helper ----
//
//    /**
//     * Adds a perk to this weapon. Returns this for chaining.
//     */
//    public PerkWeapon withPerk(Perk perk) {
//        perks.add(perk);
//        return this;
//    }
//
//    // ---- Per-unit State Management ----
//
//    /**
//     * Gets or creates the perk state list for the given unit.
//     * Perk states are deep-copied from the template perks list.
//     */
//    private Seq<Perk> getStates(Unit unit) {
//        return unitPerkStates.get(unit.id, () -> {
//            Seq<Perk> states = new Seq<>();
//            for(Perk p : perks) states.add(p.copy());
//            return states;
//        });
//    }
//
//    /** Removes state for a unit when it dies/despawns. Call from a death event or override removed(). */
//    public void removeState(Unit unit) {
//        unitPerkStates.remove(unit.id);
//        unitFiredTick.remove(unit.id);
//    }
//
//    // ---- Update ----
//
//    @Override
//    public void update(Unit unit, WeaponMount mount) {
//        super.update(unit, mount);
//
//        Seq<Perk> states = getStates(unit);
//        int lastFired = unitFiredTick.get(unit.id, -1);
//        // fired this tick if lastFired matches current tick approximation via Time
//        boolean fired = lastFired == (int) arc.util.Time.time;
//
//        for(Perk state : states) {
//            state.update(unit, null);
//            state.tickTimer(unit, null, fired);
//        }
//    }
//
//    // ---- Shoot ----
//
//    @Override
//    public void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
//        super.shoot(unit, mount, shootX, shootY, rotation);
//        unitFiredTick.put(unit.id, (int) arc.util.Time.time);
//    }
//
//    // ---- Hit / Miss Routing ----
//
//    /**
//     * Call this from your bullet type's hitEntity() when it hits an enemy.
//     * Routes the hit into all perk states for this unit.
//     *
//     * @param unit        The unit that owns this weapon.
//     * @param mount       The weapon mount (unused here but kept for API consistency).
//     * @param targetX     World X of the hit target.
//     * @param targetY     World Y of the hit target.
//     * @param shooterX    World X of the shooter (unit.x at fire time, or b.x).
//     * @param shooterY    World Y of the shooter.
//     */
//    public void onHit(Unit unit, WeaponMount mount, float targetX, float targetY, float shooterX, float shooterY) {
//        float dist = arc.math.Mathf.dst(shooterX, shooterY, targetX, targetY);
//        for(Perk state : getStates(unit)) {
//            state.registerHit(unit, null, targetX, targetY, dist);
//        }
//    }
//
//    /**
//     * Call this from your bullet type's despawned() when a bullet expires without hitting.
//     * Routes the miss into all perk states for this unit.
//     *
//     * @param unit  The unit that owns this weapon.
//     * @param mount The weapon mount.
//     */
//    public void onMiss(Unit unit, WeaponMount mount) {
//        for(Perk state : getStates(unit)) {
//            state.registerMiss(unit, null);
//        }
//    }
//
//    // ---- Draw ----
//
//    @Override
//    public void draw(Unit unit, WeaponMount mount) {
//        // Set perk part params before super.draw() so parts can read them
//        if(parts.size > 0) {
//            Seq<Perk> states = getStates(unit);
//            Perk primary = getPrimaryPerk(states);
//
//            float progress = primary != null ? primary.getProgress() : 0f;
//            float activated = primary != null ? primary.getActivatedParam() : 0f;
//
//            AxPartParms.perkparams.set(progress, activated);
//        }
//
//        super.draw(unit, mount);
//
//        // Draw per-perk custom visuals on top
//        Seq<Perk> states = getStates(unit);
//        if(!states.isEmpty()) {
//            float wx = unit.x + arc.math.Angles.trnsx(unit.rotation - 90f, x, y);
//            float wy = unit.y + arc.math.Angles.trnsy(unit.rotation - 90f, x, y);
//            float wr = mount.rotation;
//
//            for(Perk state : states) {
//                state.draw(wx, wy, wr);
//            }
//        }
//    }
//
//    // ---- Helpers ----
//
//    /** Returns the primary perk from a states list, or null if the list is empty. */
//    public Perk getPrimaryPerk(Seq<Perk> states) {
//        if(states.isEmpty()) return null;
//        int idx = Math.min(primaryPerkIndex, states.size - 1);
//        return states.get(idx);
//    }
//
//    /** Returns the primary perk state for a given unit, or null. */
//    public Perk getPrimaryPerkForUnit(Unit unit) {
//        return getPrimaryPerk(getStates(unit));
//    }
//}