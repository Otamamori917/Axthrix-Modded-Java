package axthrix.world.types;

import arc.util.Time;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.gen.Groups;
import mindustry.gen.WeatherState;
import mindustry.type.weather.ParticleWeather;

import java.util.concurrent.atomic.AtomicInteger;

public class TemperatureWeather extends ParticleWeather {
    public float temperaturePerSecond = 3f; // Changed to per-second for clarity

    public TemperatureWeather(String name){
        super(name);
    }

    @Override
    public void updateEffect(WeatherState state){
        super.updateEffect(state);

        // Convert per-second to per-frame
        float deltaTemp = temperaturePerSecond * Time.delta / 60f;

        if(TemperatureLogic.debugLogging) {
            arc.util.Log.info("[Weather] Applying @ temp (per-second: @, delta: @)",
                    deltaTemp, temperaturePerSecond, Time.delta);
        }

        // Apply to all units
        Groups.unit.each(unit -> {
            TemperatureLogic.applyTemperatureUnit(unit, deltaTemp);
        });

        // Apply to all buildings (including walls)
        AtomicInteger buildingCount = new AtomicInteger();
        Groups.build.each(build -> {
            TemperatureLogic.applyTemperatureBuilding(build, deltaTemp);
            buildingCount.getAndIncrement();
        });

        if(TemperatureLogic.debugLogging && buildingCount.get() > 0) {
            arc.util.Log.info("[Weather] Applied @ temp to @ buildings", deltaTemp, buildingCount);
        }
    }
}