package axthrix.content;

import arc.graphics.Color;
import arc.util.Time;
import axthrix.world.types.TemperatureWeather;
import mindustry.content.Weathers;
import mindustry.gen.Sounds;
import mindustry.type.Weather;
import mindustry.type.weather.ParticleWeather;
import mindustry.world.meta.Attribute;

public class AxthrixWeathers {
    public static Weather lightHeat, heavyHeat, lightCyro, heavyCyro;
    public static void load(){
        lightHeat = new TemperatureWeather("light-heat"){{
            color =  Color.valueOf("currents");
            noiseColor = Color.orange;
            color = Color.coral;
            noiseLayers = 3;
            noiseLayerAlphaM = 0.1f;
            noiseLayerSpeedM = 0.9f;
            noiseLayerSclM = 0.6f;
            noiseScale = 3000;
            xspeed = 1f;
            yspeed = 0.01f;
            drawNoise = true;
            opacityMultiplier = 2f;
            statusGround = true;
            useWindVector = true;
            hidden = false;
            sizeMax = 700;
            sizeMin = 10;
            minAlpha = 0.1f;
            maxAlpha = 0.4f;
            density = 90000;
            baseSpeed = 0.5f;
            status = AxthrixStatus.burning;
            duration = 1.2f * Time.toMinutes;
        }};
        lightCyro = new TemperatureWeather("heavy-cryo"){{
            color = noiseColor = Color.cyan;
            particleRegion = "particle";
            drawNoise = true;
            useWindVector = true;
            sizeMax = 140f;
            sizeMin = 70f;
            minAlpha = 0f;
            maxAlpha = 0.2f;
            density = 1500f;
            baseSpeed = 5.4f;
            attrs.set(Attribute.light, -0.1f);
            attrs.set(Attribute.water, -0.1f);
            opacityMultiplier = 0.35f;
            force = 0.1f;
            sound = Sounds.wind3;
            status = AxthrixStatus.freezing;
            soundVol = 0.8f;
            duration = 7f * Time.toMinutes;
        }};
    }
}
