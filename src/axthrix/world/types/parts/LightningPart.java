package axthrix.world.types.parts;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.FX.AxthrixFfx;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.part.DrawPart;

import java.util.HashMap;

public class LightningPart extends DrawPart {
    public float x;
    public float y;
    public float x2;
    public float y2;
    public Color color;
    public Color color2;
    public float thickness;
    public boolean mirror;
    public float layer;
    public float layerOffset;
    //how many ticks in between spawning the effect
    public float spawnRate;


    public HashMap<Integer, Float> delay = new HashMap<>();

    public LightningPart() {
        color = Color.white;
        thickness = 1;
        mirror = false;
        layer = 0F;
        layerOffset = 0.0F;
        spawnRate = 10;
    }

    public void draw(DrawPart.PartParams params) {
        if (!delay.containsKey(params.hashCode())){
            delay.put(params.hashCode(),0f);
        }
        int len = mirror && params.sideOverride == -1 ? 2 : 1;
        Draw.color(color);

        if(delay.get(params.hashCode()) >= spawnRate){
            for(int s = 0; s < len; ++s) {
                int i = params.sideOverride == -1 ? s : params.sideOverride;
                float sign = (float)((i == 0 ? 1 : -1) * params.sideMultiplier);
                Tmp.v1.set(x * sign, y).rotate(params.rotation - 90.0F);
                Tmp.v2.set(x2 * sign, y2).rotate(params.rotation - 90.0F);
                float rx = params.x + Tmp.v1.x;
                float ry = params.y + Tmp.v1.y;
                float rx2 = params.x + Tmp.v2.x;
                float ry2 = params.y + Tmp.v2.y;
                if(Vars.state.isPlaying()){
                    AxthrixFfx.lightningPart(rx2,ry2,thickness,layer,layerOffset,color2).at(rx, ry, Angles.angle(rx,ry,rx2,ry2), color);
                    delay.replace(params.hashCode(),0f);
                }
            }
        }

        Draw.reset();
        delay.replace(params.hashCode(),delay.get(params.hashCode())+1);
    }

    public void load(String name) {
    }
}
