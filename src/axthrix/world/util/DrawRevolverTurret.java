package axthrix.world.util;

import axthrix.world.types.block.defense.RevolverTurret;
import mindustry.gen.Building;
import mindustry.world.draw.DrawTurret;

public class DrawRevolverTurret extends DrawTurret {

    public DrawRevolverTurret(String basePrefix) {
        this.basePrefix = basePrefix;
    }

    @Override
    public void draw(Building building){
        if(parts.size > 0) {
            if (building instanceof RevolverTurret.RevolverTurretBuild rb) {
                float progress = rb.progress();

                AxPartParms.axparams.set(
                        1f - progress,
                        rb.secondarySmoothReload
                );
            }
        }

        super.draw(building);
    }

}
