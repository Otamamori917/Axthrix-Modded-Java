package axthrix.world.types.ai;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import arc.util.Tmp;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.block.Egg;
import axthrix.world.types.unittypes.IkatusaUnitType;
import axthrix.world.types.unittypes.LeggedWaterUnit;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import static arc.math.Mathf.dst;

public class WildAi extends AIController {

    protected IkatusaUnitType type;

    // Wander
    protected float wanderX, wanderY;
    protected float wanderTimer = 0f;
    protected static final float WANDER_INTERVAL = 180f;
    protected static final float WANDER_RADIUS = 160f;

    // Flee
    @Nullable protected Unit fleeTarget = null;
    protected float fleeTimer = 0f;
    protected static final float FLEE_DURATION = 120f;

    // Aggro
    @Nullable protected Teamc aggroTarget = null;

    // Needs
    @Nullable protected Tile foodTile = null;
    @Nullable protected Tile waterTile = null;
    @Nullable protected Unit preyTarget = null;

    // Pairing / Brooding
    @Nullable protected Unit mateTarget = null;
    protected float broodTimer = 0f;
    protected static final float BROOD_DURATION = 120f; // ~2 seconds

    // Digging
    @Nullable protected Tile digTarget = null;

    @Override
    public void unit(Unit unit) {
        this.unit = unit;
        if (unit != null && unit.type instanceof IkatusaUnitType it) {
            this.type = it;
        }
        if (unit != null) {
            wanderX = unit.x;
            wanderY = unit.y;
        }
    }

    @Override
    public void updateUnit() {
        if (unit == null || type == null) return;
        updateVisuals();
        updateMovement();
    }

    public void updateMovement() {
        if (unit == null || type == null) return;

        updateFlee();
        if (fleeTarget != null) {
            flee();
            return;
        }

        updateAggro();
        if (aggroTarget != null) {
            aimAt(aggroTarget);
            return;
        }

        // Water preference
        if (type.onDeepWater(unit) || (type instanceof LeggedWaterUnit)) {
            if (waterTile == null || !isWater(waterTile)) waterTile = findWaterTile();
            if (waterTile != null) {
                moveTo(waterTile.worldx(), waterTile.worldy());
                faceMovement();
                return;
            }
        }

        // Hunt / Food
        if (type.huntsUnits && preyTarget == null) preyTarget = findPrey();
        if (preyTarget != null) {
            moveTo(preyTarget.x, preyTarget.y);
            aimAt(preyTarget);
            return;
        }

        if (foodTile == null || !isFood(foodTile)) foodTile = findFoodTile();
        if (foodTile != null) {
            moveTo(foodTile.worldx(), foodTile.worldy());
            faceMovement();
            return;
        }

        // Advanced Pairing
        if (type.egg != null) {
            if (mateTarget != null) {
                handleBrooding();
                return;
            }

            if (!type.ismale) { // Female
                Unit male = findReadyMale();
                if (male != null) {
                    mateTarget = male;
                    broodTimer = 0f;
                    return;
                }
            } else { // Male - hunt females
                Unit female = findReadyFemale();
                if (female != null) {
                    mateTarget = female;
                    broodTimer = 0f;
                    return;
                }
            }

            // Normal egg laying if no mate
            Tile valid = findValidEggTile();
            if (valid != null) {
                moveTo(valid.worldx(), valid.worldy());
            } else if (type.digs) {
                if (digTarget == null) digTarget = findCrimsonSandPatch();
                if (digTarget != null) {
                    moveTo(digTarget.worldx(), digTarget.worldy());
                    if (unit.within(digTarget.worldx(), digTarget.worldy(), 12f)) {
                        digCrimsonPatch();
                    }
                }
            }
        }

        updateWander();
    }

    protected void handleBrooding() {
        if (mateTarget == null || mateTarget.dead() || !mateTarget.isAdded()) {
            mateTarget = null;
            return;
        }

        broodTimer += 1f;
        moveTo(mateTarget.x, mateTarget.y);

        if (broodTimer >= BROOD_DURATION) {
            if (!type.ismale) {
                // Female can start laying here if you have eggLayState in the unit type
            }
            mateTarget = null;
        }
    }

    @Nullable
    protected Unit findReadyMale() {
        Unit best = null;
        float bestD = Float.MAX_VALUE;
        for (Unit u : Groups.unit) {
            if (u.team != unit.team || !(u.type instanceof IkatusaUnitType iku) || !iku.ismale) continue;
            if (u.within(unit, unit.hitSize * 8f)) {
                float d = unit.dst(u);
                if (d < bestD) {
                    bestD = d;
                    best = u;
                }
            }
        }
        return best;
    }

    @Nullable
    protected Unit findReadyFemale() {
        Unit best = null;
        float bestD = Float.MAX_VALUE;
        for (Unit u : Groups.unit) {
            if (u.team != unit.team || !(u.type instanceof IkatusaUnitType iku) || iku.ismale) continue;
            if (u.within(unit, unit.hitSize * 8f)) {
                float d = unit.dst(u);
                if (d < bestD) {
                    bestD = d;
                    best = u;
                }
            }
        }
        return best;
    }

    protected void updateWander() {
        wanderTimer += 1f;
        if (wanderTimer >= WANDER_INTERVAL || unit.within(wanderX, wanderY, 16f)) {
            wanderTimer = 0f;
            Tmp.v1.rnd(Mathf.random(WANDER_RADIUS));
            wanderX = unit.x + Tmp.v1.x;
            wanderY = unit.y + Tmp.v1.y;
        }
        moveTo(wanderX, wanderY);
        faceMovement();
    }

    protected void updateFlee() {
        Unit threat = null;
        float bestDst = Float.MAX_VALUE;
        for (Unit u : Groups.unit) {
            if (u.team == unit.team) continue;
            if (u.hitSize < unit.hitSize * 2f) continue;
            float d = unit.dst(u);
            if (d < type.range * 2f && d < bestDst) {
                bestDst = d;
                threat = u;
            }
        }
        if (threat != null) {
            fleeTarget = threat;
            fleeTimer = FLEE_DURATION;
        } else if (fleeTimer > 0f) {
            fleeTimer -= 1f;
            if (fleeTimer <= 0f) fleeTarget = null;
        }
    }

    protected void flee() {
        if (fleeTarget == null) return;
        float angle = Mathf.angle(fleeTarget.x - unit.x, fleeTarget.y - unit.y) + 180f;
        moveTo(unit.x + Mathf.cosDeg(angle) * WANDER_RADIUS,
                unit.y + Mathf.sinDeg(angle) * WANDER_RADIUS);
        faceMovement();
    }

    protected void updateAggro() {
        aggroTarget = null;
        float bestDst = type.range;
        for (Unit u : Groups.unit) {
            if (u.team == unit.team) continue;
            float d = unit.dst(u);
            if (d < bestDst) {
                bestDst = d;
                aggroTarget = u;
            }
        }
        if (aggroTarget == null) {
            Building b = Units.findEnemyTile(unit.team, unit.x, unit.y, type.range, bl -> true);
            if (b != null) aggroTarget = b;
        }
    }

    protected void aimAt(Teamc target) {
        if (target == null) return;
        float tx = target.getX(), ty = target.getY();
        unit.aimX = tx;
        unit.aimY = ty;
        unit.isShooting = unit.within(tx, ty, type.range);
        facePoint(tx, ty);
    }

    protected void moveTo(float tx, float ty) {
        if (unit == null) return;
        Tmp.v1.set(tx - unit.x, ty - unit.y).limit(unit.speed());
        unit.moveAt(Tmp.v1);
    }

    public void faceMovement() {
        if (unit == null) return;
        if (unit.vel.len2() > 0.01f) {
            unit.rotation = Mathf.slerpDelta(unit.rotation, unit.vel.angle(), 0.1f);
        }
    }

    protected void facePoint(float tx, float ty) {
        if (unit == null) return;
        float angle = Mathf.angle(tx - unit.x, ty - unit.y);
        unit.rotation = Mathf.slerpDelta(unit.rotation, angle, 0.15f);
    }

    // Food, Water, Prey, Dig helpers (kept as-is)
    @Nullable protected Tile findFoodTile() { /* your code */ return null; }
    protected boolean isFood(Tile t) { /* your code */ return false; }
    @Nullable protected Tile findWaterTile() { /* your code */ return null; }
    protected boolean isWater(Tile t) { return t != null && axthrix.world.types.sea.managers.LayerManager.isWater(t); }
    @Nullable protected Unit findPrey() { /* your code */ return null; }

    @Nullable
    protected Tile findValidEggTile() {
        if (!(type.egg instanceof Egg egg)) return null;
        int r = (int)(type.foodSeekRange / 8f);
        int tx = unit.tileX(), ty = unit.tileY();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                Tile t = Vars.world.tile(tx + dx, ty + dy);
                if (t != null && t.build == null && egg.getValidation(egg.attributes, t.worldx(), t.worldy())) {
                    return t;
                }
            }
        }
        return null;
    }

    @Nullable
    protected Tile findCrimsonSandPatch() {
        int r = (int)(type.foodSeekRange / 8f);
        int tx = unit.tileX(), ty = unit.tileY();
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                Tile t = Vars.world.tile(tx + dx, ty + dy);
                if (t != null && t.floor() == AxthrixEnvironment.crimsonSandFloor) {
                    return t;
                }
            }
        }
        return null;
    }

    protected void digCrimsonPatch() {
        if (digTarget == null || unit == null) return;

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                Tile t = Vars.world.tile(digTarget.x + dx, digTarget.y + dy);
                if (t == null) continue;

                if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
                    t.setFloor((Floor) AxthrixEnvironment.crimsonSandDeepFloor);
                } else if (dx >= 0 && dy >= 0) {
                    t.setFloor((Floor) AxthrixEnvironment.crimsonSandDarkSlope);
                } else {
                    t.setFloor((Floor) AxthrixEnvironment.crimsonSandLightSlope);
                }
            }
        }

        for (int i = 0; i < type.spawnAmount; i++) {
            Tile eggTile = Vars.world.tile(digTarget.x + Mathf.range(1), digTarget.y + Mathf.range(1));
            if (eggTile != null && eggTile.block().isAir()) {
                eggTile.setNet(type.egg, unit.team, 0);
            }
        }

        digTarget = null;
    }
}