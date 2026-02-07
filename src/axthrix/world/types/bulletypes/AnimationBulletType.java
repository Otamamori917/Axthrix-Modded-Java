package axthrix.world.types.bulletypes;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.util.Log;
import mindustry.Vars;
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


        Log.info((b.type.speed / b.vel.cpy().len()) * frameTime);
        if(followBulletSpeed ? tick.get(b) >= frameTime + 1 : tick.get(b) >= ((b.type.speed / b.vel.cpy().len())*2) * frameTime){
            frame.replace(b,frame.get(b)+1);
            tick.replace(b,0f);
        }
        if(frame.get(b) >= frames && loop){
            frame.replace(b,0);
        }


        Draw.rect(Core.atlas.find(name + "-" + frame.get(b)),b.x,b.y,width,height,b.rotation());

        if(!Vars.state.isPaused()){
            tick.replace(b,tick.get(b)+1);
        }
    }
}
