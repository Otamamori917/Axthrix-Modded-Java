package axthrix.world.util;

import axthrix.world.types.block.defense.ChargingShotgunTurret;
import mindustry.gen.Building;
import mindustry.world.draw.DrawTurret;

public class DrawChargingShotgunTurret extends DrawTurret{

    public DrawChargingShotgunTurret(String basePrefix) {
        this.basePrefix = basePrefix;
    }

    @Override
    public void draw(Building building){
        if(parts.size > 0) {
            // Check if this building implements the AcceleratedTurretBuild interface
            if (building instanceof ChargingShotgunTurret.ChargingShotgunBuild cb) {
                AxPartParms.axparams.set(
                        cb.chargeProgress, // Always 0 for consistency
                        0, // Always 0 for consistency
                        0 // Heat value 0-1 (from heatf() method)
                );
            }
        }

        super.draw(building);
    }
}
