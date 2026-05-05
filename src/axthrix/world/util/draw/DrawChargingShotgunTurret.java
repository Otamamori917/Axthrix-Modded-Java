package axthrix.world.util.draw;

import axthrix.world.types.block.defense.ChargingShotgunTurret;
import axthrix.world.util.AxPartParms;
import mindustry.gen.Building;
import mindustry.world.draw.DrawTurret;

public class DrawChargingShotgunTurret extends DrawTurret{

    public DrawChargingShotgunTurret(String basePrefix) {
        this.basePrefix = basePrefix;
    }

    @Override
    public void draw(Building building){
        if(parts.size > 0) {
            if (building instanceof ChargingShotgunTurret.ChargingShotgunBuild cb) {
                AxPartParms.axparams.set(
                        cb.chargeProgress,
                        0,
                        0
                );
            }
        }

        super.draw(building);
    }
}
