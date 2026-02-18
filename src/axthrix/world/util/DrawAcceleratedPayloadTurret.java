package axthrix.world.util;

import mindustry.gen.Building;

public class DrawAcceleratedPayloadTurret extends DrawIPayloadTurret {

    public DrawAcceleratedPayloadTurret(boolean drawTurret, String basePrefix){
        super(drawTurret,basePrefix);
        this.drawTurret = drawTurret;
        this.basePrefix = basePrefix;
    }

    @Override
    public void draw(Building building){
        if(parts.size > 0) {
            float heat = 0f;

            // Check if this building implements the AcceleratedTurretBuild interface
            if (building instanceof AcceleratedLogic.AcceleratedTurretBuild ab) {
                if (ab.getAcceleratedBonus() == 1 && ab.burnsOut()){
                    heat = ab.heatf();
                }else{
                    heat = ab.heatp();
                }
            }

            AxPartParms.axparams.set(
                    0, // Always 0 for consistency
                    0, // Always 0 for consistency
                    heat // Heat value 0-1 (from heatf() method)
            );
        }

        super.draw(building);
    }
}