package axthrix.world.util.draw;

import axthrix.world.types.block.defense.PerkTurretType;
import axthrix.world.util.AxPartParms;
import mindustry.gen.Building;
import mindustry.world.draw.DrawTurret;

public class DrawPerkTurretType extends DrawTurret {

    public DrawPerkTurretType(String basePrefix) {
        super(basePrefix);
    }

    public DrawPerkTurretType() {
        super();
    }

    @Override
    public void draw(Building building) {
        if(parts.size > 0 && building instanceof PerkTurretType.PerkTurretTypeBuild pb) {
            AxPartParms.perkparams.set(
                    pb.getPerkProgress(),   // perkProgress: 0.0 - 1.0
                    pb.getPerkActivated()   // perkActivated: 0.0 or 1.0
            );
        }

        super.draw(building);
    }
}