package axthrix.world.types.bulletypes;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Log;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;

import java.util.HashMap;

public class AnimationBulletType extends BulletType {

    public int frames = 20;
    public float frameTime = 5;
    public boolean loop = true;
    public float width;
    public float height;
    public boolean followBulletSpeed = false;
    public float customAngle = -1;

    public HashMap<Bullet, Float> tick = new HashMap<>();
    public HashMap<Bullet, Integer> frame = new HashMap<>();
    public String name;

    @Override
    public void draw(Bullet b) {
        super.draw(b);
        if (!tick.containsKey(b)) {
            tick.put(b, 0f);
        }
        if (!frame.containsKey(b)){
            frame.put(b, 0);
        }


        if(followBulletSpeed ? tick.get(b) >= frameTime + 1 : tick.get(b) >= ((b.type.speed / b.vel.cpy().len())*2) * frameTime){
            frame.replace(b,frame.get(b)+1);
            tick.replace(b,0f);
        }
        if(frame.get(b) >= frames && loop){
            frame.replace(b,0);
        }

        float z = Draw.z();
        Draw.z(layer);
        Draw.rect(Core.atlas.find(name + "-" + frame.get(b)),b.x,b.y,width,height,customAngle != -1 ? customAngle : b.rotation());
        Draw.z(z);

        if(!Vars.state.isPaused()){
            tick.replace(b,tick.get(b)+1);
        }
    }
    @Override
    public void despawned(Bullet b) {
        super.despawned(b);
        tick.remove(b);
        frame.remove(b);
    }
}
