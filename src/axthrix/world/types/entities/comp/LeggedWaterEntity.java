package axthrix.world.types.entities.comp;

import axthrix.content.units.AxthrixUnits;
import axthrix.world.types.unittypes.CnSUnitType;
import axthrix.world.types.unittypes.IkatusaUnitType;
import axthrix.world.types.unittypes.LeggedWaterUnit;
import mindustry.entities.EntityCollisions;
import mindustry.gen.Hitboxc;
import mindustry.gen.LegsUnit;

public class LeggedWaterEntity extends LegsUnit implements Hitboxc {

    public LeggedWaterEntity(){
        super();
    }
    @Override public String toString() {
        return "LegWaterUnit#" + id;
    }
    @Override
    public int classId() {
        return AxthrixUnits.classID(getClass());
    }

    @Override
    public EntityCollisions.SolidPred solidity() {
        //cant be bothered so just return this
        return EntityCollisions::solid;
    }

    @Override
    public float speed(){
        if(type instanceof LeggedWaterUnit lw){
            if(lw.deepSpeed >= 0 && lw.onDeepWater(this)) return lw.deepSpeed;
            return IkatusaUnitType.onWater(this) ? lw.navalSpeed : lw.speed;
        }
        return super.speed();
    }
}
