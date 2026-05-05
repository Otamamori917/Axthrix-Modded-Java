package axthrix.world.types.sea.unit;

import arc.math.geom.*;
import arc.struct.*;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.sea.managers.LayerManager;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.*;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import static mindustry.Vars.world;

public class SubmarineAi extends AIController {
    private static final float sensorDist = 50f;
    private boolean IAmSub = false;

    private int targetTimer = 0;
    private int diveTimer = 0;

    @Override
    public void updateUnit() {
        if (unit == null) return;
        IAmSub = LayerManager.isSubmerged(unit);

        if (IAmSub && !LayerManager.isDeep(unit.tileOn())) {
            toggleDive();
        }

        if (!Vars.state.isPaused()) targetTimer++;
        if (targetTimer >= 15) {
            targetTimer = 0;
            findNewTarget();
        }

        if (target != null) {
            handleCombatAndMovement();
        } else {
            if (LayerManager.isDeep(unit.tileOn()) && !IAmSub) toggleDive();
            super.updateUnit();
        }
    }

    void findNewTarget() {
        Building core = unit.closestEnemyCore();
        if (core != null && unit.within(core, unit.range() * 2f)) {
            target = core;
            return;
        }

        target = Units.closestBuilding(unit.team, unit.x, unit.y, unit.range(), b ->
                b.team != unit.team && b.block instanceof Turret && LayerManager.isSubmerged(b) == IAmSub
        );
        if (target != null) return;

        target = Units.closestEnemy(unit.team, unit.x, unit.y, unit.range(), t ->
                LayerManager.isSubmerged(t) == IAmSub
        );
        if (target != null) return;

        target = Units.closestEnemy(unit.team, unit.x, unit.y, unit.range(), t -> true);
    }

    void handleCombatAndMovement() {
        boolean targetSub = LayerManager.isSubmerged(target);
        Vec2 lookAhead = new Vec2().trns(unit.rotation, sensorDist).add(unit.x, unit.y);
        Tile nextTile = world.tileWorld(lookAhead.x, lookAhead.y);

        if (IAmSub && nextTile != null && !LayerManager.isDeep(nextTile)) {
            toggleDive();
            return;
        }

        if (targetSub && !IAmSub) {
            if (LayerManager.isDeep(unit.tileOn())) toggleDive();
            else retreatToDeep();
            return;
        } else if (!targetSub && IAmSub) {
            toggleDive();
        }

        moveTo(target, unit.range() * 0.8f);
        unit.lookAt(target);
        unit.controlWeapons(true);
    }

    private void toggleDive() {
        if (!Vars.state.isPaused()) diveTimer++;
        if (diveTimer >= 30) {
            diveTimer = 0;
            if (unit.type instanceof SubmarineUnitType sub) {
                sub.handleDiveToggle(unit);
            }
        }
    }

    private void retreatToDeep() {
        IntSet visited = new IntSet();
        IntQueue queue = new IntQueue();
        int startKey = Point2.pack(unit.tileX(), unit.tileY());
        queue.addLast(startKey);
        visited.add(startKey);

        int iterations = 0;
        while (queue.size > 0 && iterations < 400) {
            int current = queue.removeFirst();
            int cx = Point2.x(current), cy = Point2.y(current);
            Tile tile = world.tile(cx, cy);

            if (LayerManager.isDeep(tile)) {
                moveTo(tile, 0f);
                return;
            }

            for (int i = 0; i < 4; i++) {
                int nx = cx + (i == 0 ? 1 : i == 1 ? -1 : 0), ny = cy + (i == 2 ? 1 : i == 3 ? -1 : 0);
                int nkey = Point2.pack(nx, ny);
                if (nx >= 0 && ny >= 0 && nx < world.width() && ny < world.height() && !visited.contains(nkey)) {
                    visited.add(nkey);
                    queue.addLast(nkey);
                }
            }
            iterations++;
        }
    }
}
