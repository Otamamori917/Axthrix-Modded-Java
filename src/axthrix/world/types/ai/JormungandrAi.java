package axthrix.world.types.ai;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import arc.util.Nullable;
import axthrix.world.types.block.Egg;
import axthrix.world.types.block.defense.JormungandrNest;
import axthrix.world.types.unittypes.JormungandrUnitType;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.OreBlock;

import static arc.math.Mathf.dst;

public class JormungandrAi extends AIController {

    protected JormungandrUnitType type;

    // ---- wander ----
    protected float wanderX, wanderY;
    protected float wanderTimer = 0f;
    protected static final float WANDER_INTERVAL = 180f;
    protected static final float WANDER_RADIUS = 160f;

    // ---- flee ----
    @Nullable protected Unit fleeTarget = null;
    protected float fleeTimer = 0f;
    protected static final float FLEE_DURATION = 120f;

    // ---- nest build ----
    public float nestSiteX, nestSiteY;
    public boolean nestSiteChosen = false;
    protected static final float NEST_SITE_RADIUS = 80f;
    protected static final float MIN_TICKS_TO_NEST = 600f;

    protected enum BuildState { SEEK_RESOURCE, CARRY_TO_NEST, AT_NEST }
    protected BuildState buildState = BuildState.SEEK_RESOURCE;

    @Nullable protected Tile oreTile = null;
    protected float mineTimer = 0f;
    protected static final float MINE_TICKS = 60f;
    protected boolean carryingResource = false;

    // ---- needs ----
    protected enum NeedsState { NONE, SEEK, FEED, HUNT }
    protected NeedsState needsState = NeedsState.NONE;

    @Nullable protected Tile foodTile = null;
    @Nullable protected Tile waterTile = null;
    @Nullable protected Unit preyTarget = null;
    protected float feedTimer = 0f;
    protected static final float FEED_TICKS = 90f;
    protected static final float DRINK_TICKS = 40f;

    // ---- rest ----
    protected boolean restingAtNest = false;

    // ---- egg laying ----
    public enum EggLayState { INACTIVE, FIND_TILE, MOVE_TO_TILE, LAY_EGG, DONE }
    public EggLayState eggLayState = EggLayState.INACTIVE;
    public int eggsLeft = 0;
    @Nullable protected Tile eggTargetTile = null;

    // ---- aggro ----
    @Nullable protected Teamc aggroTarget = null;

    @Override
    public void unit(Unit unit) {
        this.unit = unit;
        if (unit != null && unit.type instanceof JormungandrUnitType jt) {
            this.type = jt;
        }
        if (unit != null) {
            wanderX = unit.x;
            wanderY = unit.y;
        }
    }

    @Override
    public Unit unit() { return unit; }

    @Override
    public void updateUnit() {
        if (unit == null || type == null) return;
        updateVisuals();
        updateMovement();
    }

    @Override
    public void updateVisuals() {
        if (unit == null) return;
        try {
            super.updateVisuals();
        } catch (Exception ignored) {}
    }

    public void updateMovement() {
        if (unit == null || type == null) return;

        if (type.isAgingUp(unit)) {
            unit.vel.setZero();
            return;
        }

        if (type.pollNestDestroyed(unit)) onNestDestroyed();

        float speedMult = type.getNeedsSpeedMult(unit);
        float needsFraction = type.getNeedsFraction(unit);

        if (eggLayState != EggLayState.INACTIVE && eggLayState != EggLayState.DONE) {
            updateEggLaying(speedMult);
            return;
        }
        if (eggLayState == EggLayState.DONE) eggLayState = EggLayState.INACTIVE;

        if (needsFraction < type.criticalNeedsThreshold) {
            setResting(false);
            updateNeeds();
            return;
        }

        if (type.isHatchling) {
            updateFlee();
            if (fleeTarget != null) { setResting(false); flee(); return; }
        }
        if (needsFraction < type.lowNeedsThreshold) {
            setResting(false);
            updateNeeds();
            return;
        }

        if (hasCompletedNest() && !type.shouldAbandonNest(unit)) {
            updateRest();
            updateAggro();
            if (aggroTarget != null) aimAt(aggroTarget);
            return;
        }

        if (type.canNest && !type.hasActiveBuild(unit) && !nestSiteChosen) {
            float timeLeft = type.stageTime - type.stageTick.getOrDefault(unit, 0f);
            if (timeLeft >= MIN_TICKS_TO_NEST) {
                Tmp.v1.rnd(Mathf.random(NEST_SITE_RADIUS));
                nestSiteX = unit.x + Tmp.v1.x;
                nestSiteY = unit.y + Tmp.v1.y;
                nestSiteChosen = true;
                type.startNest(unit, nestSiteX, nestSiteY);
                buildState = BuildState.SEEK_RESOURCE;
            }
        }

        if (nestSiteChosen && type.hasActiveBuild(unit)) {
            updateNestBuild(speedMult);
            updateAggro();
            if (aggroTarget != null) aimAt(aggroTarget);
            return;
        }
        updateAggro();
        if (aggroTarget != null) { aimAt(aggroTarget); return; }
        updateWander(speedMult);
    }

    protected void updateEggLaying(float speedMult) {
        switch (eggLayState) {
            case FIND_TILE -> {
                if (eggsLeft <= 0) { eggLayState = EggLayState.DONE; return; }
                eggTargetTile = findValidEggTile();
                if (eggTargetTile == null) {
                    eggLayState = EggLayState.DONE;
                } else {
                    eggLayState = EggLayState.MOVE_TO_TILE;
                }
            }
            case MOVE_TO_TILE -> {
                if (eggTargetTile == null) { eggLayState = EggLayState.FIND_TILE; return; }
                float tx = eggTargetTile.worldx();
                float ty = eggTargetTile.worldy();
                if (unit.within(tx, ty, 12f)) {
                    eggLayState = EggLayState.LAY_EGG;
                } else {
                    pathfindTo(tx, ty, speedMult);
                }
            }
            case LAY_EGG -> {
                if (eggTargetTile == null) { eggLayState = EggLayState.FIND_TILE; return; }
                if (eggTargetTile.build != null) {
                    eggTargetTile = null;
                    eggLayState = EggLayState.FIND_TILE;
                    return;
                }
                type.layEggAt(unit, eggTargetTile);
                eggsLeft--;
                eggTargetTile = null;
                eggLayState = eggsLeft > 0 ? EggLayState.FIND_TILE : EggLayState.DONE;
            }
            case DONE -> updateWander(speedMult);
        }
    }

    @Nullable
    protected Tile findValidEggTile() {
        if (!(type.eggBlock instanceof Egg egg)) return null;

        int tileRadius = (int) (type.foodSeekRange / 8f);
        int tx = unit.tileX(), ty = unit.tileY();
        Tile best = null;
        float bestDst = Float.MAX_VALUE;

        for (int dx = -tileRadius; dx <= tileRadius; dx++) {
            for (int dy = -tileRadius; dy <= tileRadius; dy++) {
                Tile t = Vars.world.tile(tx + dx, ty + dy);
                if (t == null || t.build != null) continue;
                if (egg.getValidation(egg.attributes, t.worldx(), t.worldy())) {
                    float d = dst(unit.x, unit.y, t.worldx(), t.worldy());
                    if (d < bestDst) { bestDst = d; best = t; }
                }
            }
        }
        return best;
    }

    protected boolean hasCompletedNest() {
        if (!nestSiteChosen) return false;
        Tile t = Vars.world.tileWorld(nestSiteX, nestSiteY);
        return t != null
                && t.build instanceof JormungandrNest.JormungandrNestBuild nb
                && nb.drawCoiled;
    }

    protected void updateRest() {
        Tile t = Vars.world.tileWorld(nestSiteX, nestSiteY);
        if (t == null) return;
        float nx = t.worldx(), ny = t.worldy();

        if (unit.within(nx, ny, 8f)) {
            setResting(true);
            unit.vel.setZero();
        } else {
            setResting(false);
            moveTo(nx, ny, 1f);
            faceMovement();
        }
    }

    protected void setResting(boolean value) {
        restingAtNest = value;
        type.setResting(unit, value);
    }

    protected void updateNeeds() {
        switch (needsState) {
            case NONE -> pickNeedsTarget();
            case SEEK -> updateSeekNeeds();
            case FEED -> updateFeed();
            case HUNT -> updateHunt();
        }
    }

    protected void pickNeedsTarget() {
        if (type.huntsUnits && !type.preyTypes.isEmpty()) {
            preyTarget = findPrey();
            if (preyTarget != null) { needsState = NeedsState.HUNT; return; }
        }
        foodTile = findFoodTile();
        if (foodTile != null) { needsState = NeedsState.SEEK; waterTile = null; return; }
        waterTile = findWaterTile();
        if (waterTile != null) { needsState = NeedsState.SEEK; foodTile = null; return; }
        needsState = NeedsState.NONE;
    }

    protected void updateSeekNeeds() {
        Tile target = foodTile != null ? foodTile : waterTile;
        if (target == null) { needsState = NeedsState.NONE; return; }
        float tx = target.worldx(), ty = target.worldy();
        if (unit.within(tx, ty, 12f)) { feedTimer = 0f; needsState = NeedsState.FEED; }
        else { pathfindTo(tx, ty, 1f); }
    }

    protected void updateFeed() {
        Tile target = foodTile != null ? foodTile : waterTile;
        if (target == null) { needsState = NeedsState.NONE; return; }
        boolean isWater = waterTile != null && foodTile == null;
        feedTimer += 1f;
        facePoint(target.worldx(), target.worldy());
        if (feedTimer >= (isWater ? DRINK_TICKS : FEED_TICKS)) {
            type.restoreNeeds(unit, type.needsRestoreAmount);
            feedTimer = 0f;
            foodTile = null; waterTile = null;
            needsState = NeedsState.NONE;
            oreTile = null;
            mineTimer = 0f;
        }
    }

    protected void updateHunt() {
        if (preyTarget == null || preyTarget.dead() || !preyTarget.isAdded()) {
            preyTarget = null; needsState = NeedsState.NONE; return;
        }
        aimAt(preyTarget);
        moveTo(preyTarget.x, preyTarget.y, 1f);
        if (preyTarget.dead()) {
            type.restoreNeeds(unit, type.needsRestoreAmount);
            preyTarget = null; needsState = NeedsState.NONE;
        }
    }

    @Nullable
    protected Tile findFoodTile() {
        if (type.foodBlocks.isEmpty()) return null;
        int tileRadius = (int) (type.foodSeekRange / 8f);
        int tx = unit.tileX(), ty = unit.tileY();
        Tile best = null; float bestDst = Float.MAX_VALUE;
        for (int dx = -tileRadius; dx <= tileRadius; dx++) {
            for (int dy = -tileRadius; dy <= tileRadius; dy++) {
                Tile t = Vars.world.tile(tx + dx, ty + dy);
                if (t == null) continue;
                if (type.foodBlocks.contains(t.overlay()) || type.foodBlocks.contains(t.floor())) {
                    float d = dst(unit.x, unit.y, t.worldx(), t.worldy());
                    if (d < bestDst) { bestDst = d; best = t; }
                }
            }
        }
        return best;
    }

    @Nullable
    protected Tile findWaterTile() {
        int tileRadius = (int) (type.foodSeekRange / 8f);
        int tx = unit.tileX(), ty = unit.tileY();
        Tile best = null; float bestDst = Float.MAX_VALUE;
        for (int dx = -tileRadius; dx <= tileRadius; dx++) {
            for (int dy = -tileRadius; dy <= tileRadius; dy++) {
                Tile t = Vars.world.tile(tx + dx, ty + dy);
                if (axthrix.world.types.sea.managers.LayerManager.isWater(t)) {
                    float d = dst(unit.x, unit.y, t.worldx(), t.worldy());
                    if (d < bestDst) { bestDst = d; best = t; }
                }
            }
        }
        return best;
    }

    @Nullable
    protected Unit findPrey() {
        Unit best = null; float bestDst = type.foodSeekRange;
        for (Unit u : Groups.unit) {
            if (u.team == unit.team || !type.preyTypes.contains(u.type)) continue;
            float d = unit.dst(u);
            if (d < bestDst) { bestDst = d; best = u; }
        }
        return best;
    }

    @Nullable
    protected Tile findNearestOre() {
        int tileRadius = (int) (type.nestSeekRange / 8f);
        int tx = unit.tileX(), ty = unit.tileY();
        Tile best = null; float bestDst = Float.MAX_VALUE;
        for (int dx = -tileRadius; dx <= tileRadius; dx++) {
            for (int dy = -tileRadius; dy <= tileRadius; dy++) {
                Tile t = Vars.world.tile(tx + dx, ty + dy);
                if (t != null && isOreTile(t)) {
                    float d = dst(unit.x, unit.y, t.worldx(), t.worldy());
                    if (d < bestDst) { bestDst = d; best = t; }
                }
            }
        }
        return best;
    }

    protected boolean isOreTile(Tile t) {
        if (type.nestResource == null) return false;
        return t.overlay() instanceof OreBlock ore && ore.itemDrop == type.nestResource;
    }

    protected void onNestDestroyed() {
        nestSiteChosen = false;
        buildState = BuildState.SEEK_RESOURCE;
        oreTile = null; mineTimer = 0f; carryingResource = false;
        restingAtNest = false;
        type.setResting(unit, false);
    }

    protected void updateNestBuild(float speedMult) {
        if (!type.hasActiveBuild(unit)) return;
        switch (buildState) {
            case SEEK_RESOURCE -> updateSeekResource(speedMult);
            case CARRY_TO_NEST -> updateCarryToNest(speedMult);
            case AT_NEST       -> buildState = BuildState.SEEK_RESOURCE;
        }
    }

    protected void updateSeekResource(float speedMult) {
        if (oreTile == null || !isOreTile(oreTile)) oreTile = findNearestOre();
        if (oreTile == null) { pathfindTo(nestSiteX, nestSiteY, speedMult); return; }
        float ox = oreTile.worldx(), oy = oreTile.worldy();
        if (unit.within(ox, oy, 12f)) {
            mineTimer += 1f; facePoint(ox, oy);
            if (mineTimer >= MINE_TICKS) {
                mineTimer = 0f; carryingResource = true; oreTile = null;
                buildState = BuildState.CARRY_TO_NEST;
            }
        } else { pathfindTo(ox, oy, speedMult); }
    }

    protected void updateCarryToNest(float speedMult) {
        if (unit.within(nestSiteX, nestSiteY, 12f)) {
            type.addNestProgress(unit, type.resourceProgressValue);
            carryingResource = false; buildState = BuildState.AT_NEST;
        } else { pathfindTo(nestSiteX, nestSiteY, speedMult); }
    }

    protected void updateWander(float speedMult) {
        wanderTimer += 1f;
        if (wanderTimer >= WANDER_INTERVAL || unit.within(wanderX, wanderY, 16f)) {
            wanderTimer = 0f;
            Tmp.v1.rnd(Mathf.random(WANDER_RADIUS));
            wanderX = unit.x + Tmp.v1.x; wanderY = unit.y + Tmp.v1.y;
        }
        moveTo(wanderX, wanderY, speedMult); faceMovement();
    }

    protected void updateFlee() {
        Unit threat = null; float bestDst = Float.MAX_VALUE;
        for (Unit u : Groups.unit) {
            if (u.team == unit.team || u.hitSize < unit.hitSize * 2f) continue;
            float d = unit.dst(u);
            if (d < type.lungeRange * 2f && d < bestDst) { bestDst = d; threat = u; }
        }
        if (threat != null) { fleeTarget = threat; fleeTimer = FLEE_DURATION; }
        else if (fleeTimer > 0f) { fleeTimer -= 1f; if (fleeTimer <= 0f) fleeTarget = null; }
    }

    protected void flee() {
        if (fleeTarget == null) return;
        float angle = Mathf.angle(fleeTarget.x - unit.x, fleeTarget.y - unit.y) + 180f;
        moveTo(unit.x + Mathf.cosDeg(angle) * WANDER_RADIUS,
                unit.y + Mathf.sinDeg(angle) * WANDER_RADIUS, 1f);
        faceMovement();
    }

    protected void updateAggro() {
        aggroTarget = null; float bestDst = type.lungeRange;
        for (Unit u : Groups.unit) {
            if (u.team == unit.team) continue;
            if (type.huntsUnits && type.preyTypes.contains(u.type)) continue;
            float d = unit.dst(u);
            if (d < bestDst) { bestDst = d; aggroTarget = u; }
        }
        if (aggroTarget == null) {
            Building b = Units.findEnemyTile(unit.team, unit.x, unit.y, type.lungeRange, bl -> true);
            if (b != null) aggroTarget = b;
        }
    }

    protected void pathfindTo(float tx, float ty, float speedMult) {
        if (unit == null) return;
        Tmp.v1.set(tx, ty);
        moveTo(Tmp.v1, 0f, 100, false, null);   // this calls the safe version below
        if (speedMult < 1f && unit.vel != null) {
            unit.vel.scl(speedMult);
        }
    }

    protected void aimAt(Teamc target) {
        if (target == null) return;
        float tx = target.getX(), ty = target.getY();
        unit.aimX = tx; unit.aimY = ty;
        unit.isShooting = unit.within(tx, ty, type.lungeRange);
        facePoint(tx, ty);
    }

    protected void moveTo(float tx, float ty, float speedMult) {
        if (unit == null) return;
        unit.moveAt(Vec2.ZERO.set(tx - unit.x, ty - unit.y).limit(unit.speed() * speedMult));
    }

    public void faceMovement() {
        if (unit.vel.len2() > 0.01f)
            unit.rotation = Mathf.slerpDelta(unit.rotation, unit.vel.angle(), 0.1f);
    }

    protected void facePoint(float tx, float ty) {
        unit.rotation = Mathf.slerpDelta(unit.rotation, Mathf.angle(tx - unit.x, ty - unit.y), 0.15f);
    }
}