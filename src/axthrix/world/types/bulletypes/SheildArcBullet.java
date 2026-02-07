package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;

import java.util.HashMap;

public class SheildArcBullet extends BulletType {

    public float max;
    @Nullable
    public String sprite;

    HashMap<Integer, Float> currentHp = new HashMap<>();
    private static Vec2 paramPos = new Vec2();

    public float radius = 60.0F;
    public float x = 0.0F;
    public float y = 0.0F;
    public float width = 6.0F;
    public boolean offsetRegion = false;
    public Effect damageEffect = Fx.chainLightning;
    protected float widthScale;
    protected HashMap<Integer, Float> alpha = new HashMap<>();


    public SheildArcBullet(float maxhp, String bulletSprite){
        super(2,300);
        max = maxhp;
        sprite = bulletSprite;
        keepVelocity = absorbable = hittable = false;
        hitSize = 8;
        removeAfterPierce = false;
        pierceCap = 12;
        hitEffect = despawnEffect = Fx.none;
    }
    @Override
    public void update(Bullet b){
        if(widthScale != 1){
            widthScale += 0.2f;
        }
        if (!currentHp.containsKey(b.id)){
            currentHp.put(b.id,max);
        }
        if (!alpha.containsKey(b.id)){
            alpha.put(b.id,0f);
        }else{
            alpha.replace(b.id,Math.max(alpha.get(b.id) - Time.delta / 10.0F, 0.0F));
        }
        Groups.bullet.each(eb -> {
            if (eb.team != b.team) {
                float d = Mathf.dst(eb.x, eb.y, b.x, b.y);
                if (d < b.hitSize*2) {
                    if(eb.type.absorbable && !(eb.type instanceof SheildArcBullet)){
                        eb.absorb();
                        Fx.absorb.at(eb);
                        alpha.replace(b.id,1.0f);
                        damageEffect.at(eb.x, eb.y, b.angleTo(eb),  b.team.color);
                        currentHp.replace(b.id,currentHp.get(b.id) - eb.damage);
                    }
                }
            }
        });
        if (currentHp.get(b.id) <= 0){
            b.remove();
        }
    }

    @Override
    public void draw(Bullet b) {
        if (widthScale > 0.001F) {
            Draw.z(125.0F);
            if(alpha.get(b.id) != null){
                Draw.color(b.team.color, Color.white, Mathf.clamp(alpha.get(b.id)));
            }else{
                Draw.color(b.team.color, Color.white, Mathf.clamp(0));
            }
            Vec2 pos = paramPos.set(x, y).rotate(b.rotation() - 90.0F).add(b);
            if (!Vars.renderer.animateShields) {
                Draw.alpha(0.4F);
            }

            if (sprite != null) {
                Vec2 rp = offsetRegion ? pos : Tmp.v1.set(b);
                Draw.yscl = widthScale;
                Draw.rect(sprite, rp.x, rp.y, b.rotation() - 90.0F);
                Draw.yscl = 1.0F;
            }

            Draw.reset();
        }

    }
}
