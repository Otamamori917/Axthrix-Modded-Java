package axthrix.world.types.ai;

import mindustry.entities.units.AIController;

import arc.math.geom.Position;

public class WildAi extends AIController {

    public WildAi() {
    }

    public void Move(Position posc, Float circleLength){
        moveTo(posc, circleLength, 20.0F);
    }
}
