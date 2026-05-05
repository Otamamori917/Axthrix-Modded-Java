package axthrix.world.types.sea.unit;

import arc.math.geom.Vec2;
import arc.util.Interval; // Used to prevent rapid toggling
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.sea.managers.LayerManager;
import mindustry.ai.types.CommandAI;
import mindustry.content.Blocks;
import mindustry.world.Tile;
import static mindustry.Vars.world;

public class DiveAI extends CommandAI {
    private static final float sensorDist = 24f;
    private final Vec2 lookAhead = new Vec2();
    private final Interval timer = new Interval();

    @Override
    public void updateUnit() {
        if (!(unit.type instanceof SubmarineUnitType sub)) {
            super.updateUnit();
            return;
        }

        boolean isSub = LayerManager.isSubmerged(unit);
        Tile current = unit.tileOn();
        boolean onDeep = LayerManager.isDeep(current);

        lookAhead.trns(unit.rotation, sensorDist).add(unit.x, unit.y);
        Tile nextTile = world.tileWorld(lookAhead.x, lookAhead.y);

        if (isSub && nextTile != null && !LayerManager.isDeep(nextTile)) {
            safeToggle(sub);
            isSub = false;
        }

        if (target != null) {
            boolean targetSub = LayerManager.isSubmerged(target);
            if (targetSub != isSub && onDeep) {
                safeToggle(sub);
                isSub = !isSub;
            }
        }
        else if (onDeep && !isSub) {
            safeToggle(sub);
        }

        super.updateUnit();
    }

    /** Toggles dive only if enough time has passed to prevent spam. */
    private void safeToggle(SubmarineUnitType sub) {
        if (timer.get(0, 30f)) {
            sub.handleDiveToggle(unit);
        }
    }
}
