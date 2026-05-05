package axthrix.world.types.sea.unit;

import axthrix.world.types.unittypes.AxUnitType;
import mindustry.gen.UnitEntity;

public class SeaCoreConstructorUnitType extends AxUnitType {
    public SeaCoreConstructorUnitType(String name) {
        super(name);
        flying = true;
        health = 500;
        engineSize = 0;
        constructor = UnitEntity::create;
        controller = u -> new SeaCoreConstructorAI();
    }
}
