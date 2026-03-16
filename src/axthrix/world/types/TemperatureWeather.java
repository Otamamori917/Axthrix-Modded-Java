package axthrix.world.types;

import arc.util.Time;
import axthrix.world.types.bulletypes.TemperatureBulletType;
import mindustry.gen.Groups;
import mindustry.gen.WeatherState;
import mindustry.type.weather.ParticleWeather;

public class TemperatureWeather extends ParticleWeather {
    public float temperaturePerTick = 0.05f;

    public TemperatureWeather(String name){
        super(name);
    }

    @Override
    public void updateEffect(WeatherState state){
        super.updateEffect(state);

        Groups.unit.each(unit -> {
            TemperatureBulletType.applyTemperatureUnit(unit, temperaturePerTick);
        });
        Groups.build.each( build -> {
            TemperatureBulletType.applyTemperatureBuilding(build,temperaturePerTick);
        });
    }
}
