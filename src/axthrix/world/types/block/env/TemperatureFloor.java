package axthrix.world.types.block.env;

import arc.graphics.Color;
import mindustry.world.blocks.environment.Floor;

public class TemperatureFloor extends Floor {
    /// Positive = heat, Negative = cold
    public float temperaturePerSecond = 5f;
    public float effectChance = 0.05f;

    public TemperatureFloor(String name){
        super(name);
        supportsOverlay = true;
        emitLight = true;
        lightRadius = 40f;
        lightColor = temperaturePerSecond > 0 ?
                Color.valueOf("ff6214") : Color.valueOf("6ecdec");
    }
}