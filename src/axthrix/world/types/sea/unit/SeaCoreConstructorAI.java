package axthrix.world.types.sea.unit;

import arc.math.geom.Vec2;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.world.types.sea.block.SeaCore;
import axthrix.world.types.sea.managers.UnderwaterZone;
import mindustry.entities.units.AIController;
import mindustry.gen.*;

public class SeaCoreConstructorAI extends AIController {
    @Override
    public void updateUnit() {
        if (!UnderwaterZone.valid) {
            unit.kill();
            return;
        }

        float tx = UnderwaterZone.centerX * 8f;
        float ty = UnderwaterZone.centerY * 8f;

        if (unit.within(tx, ty, 10f)) {

            boolean exists = Groups.build.contains(b -> b instanceof SeaCore.SeaCoreBuild);

            if (!exists) {

                Call.constructFinish(unit.tileOn(), AxthrixCrafters.augaCore, unit, (byte)0, unit.team, null);
            }


            unit.kill();
        } else {
            unit.moveAt(new Vec2(tx, ty).sub(unit).limit(unit.speed()));
            unit.lookAt(tx, ty);
        }
    }
}
