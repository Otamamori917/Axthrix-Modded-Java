package axthrix.world.types.bulletypes;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Time;
import axthrix.content.AxthrixSounds;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.core.GameState;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

import java.util.HashMap;

public class ReflectiveCoinBullet extends AnimationBulletType {

    public float reflectedLifeMin = 2,reflectedLifeMax= 2.4f,reflectedVelocityMin = 1.4f, reflectedVelocityMax = 1.8f;
    public ReflectiveCoinBullet(){
        super();
        speed = 4;
        keepVelocity = absorbable = hittable = false;
        hitSize = 8;
        removeAfterPierce = false;
        pierceCap = 12;
        hitEffect = despawnEffect = Fx.none;
        name  = "aj-coin";
    }
    @Override
    public void update(Bullet b){
        super.update(b);
        Groups.bullet.each(eb -> {
            float d = Mathf.dst(eb.x, eb.y, b.x, b.y);
            if (d < b.hitSize / 2 && eb.type.reflectable && !(eb.type instanceof ReflectiveCoinBullet)) {
                AxthrixSounds.payitback.at(b.x,b.y);
                if (eb.team != b.team) {
                    BulletType bulletcpy = eb.type.copy();
                    bulletcpy.pierce = false;
                    bulletcpy.pierceBuilding = false;
                    bulletcpy.removeAfterPierce = true;
                    bulletcpy.fragOnHit = true;

                    bulletcpy.create(b, eb.x, eb.y, eb.rotation() - 180, Mathf.random(reflectedVelocityMin, reflectedVelocityMax), Mathf.random(reflectedLifeMin, reflectedLifeMax));

                    eb.type.fragOnAbsorb = false;
                    eb.absorb();
                    b.remove();
                }/* else {

                }*/
            }
        });
    }
}
