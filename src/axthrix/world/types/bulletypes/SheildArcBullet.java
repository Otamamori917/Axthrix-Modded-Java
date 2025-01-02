package axthrix.world.types.bulletypes;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Pal;

import java.util.HashMap;

import static mindustry.Vars.state;

public class SheildArcBullet extends BulletType {

    public float max = 100;
    @Nullable
    public String sprite;

    HashMap<Integer, Float> currentHp = new HashMap<>();
    private static Vec2 paramPos = new Vec2();

    public float radius = 60.0F;
    public float x = 0.0F;
    public float y = 0.0F;
    public float width = 6.0F;
    public boolean offsetRegion = false;

    public boolean hitBuildings = true, hitUnits = true;

    public Sound collideSound = Sounds.flux;
    public Effect damageEffect = Fx.chainLightning;
    protected float widthScale;
    protected float alpha;
    protected boolean anyNearby = false;

    private static final Seq<Healthc> all = new Seq<>();


    public SheildArcBullet(float maxhp, String bulletSprite){
        super(2,150);
        max = maxhp;
        sprite = bulletSprite;
        removeAfterPierce = false;
        pierceCap = 12;
    }
    @Override
    public void update(Bullet b){
        if(widthScale != 1){
            widthScale += 0.2f;
        }
        if (!currentHp.containsKey(b.id)){
            currentHp.put(b.id,max);
        }
        Groups.bullet.each(bb -> {
            if (bb.team != b.team) {
                float d = Mathf.dst(bb.x, bb.y, b.x, b.y);
                if (d < b.hitSize*2) {
                    currentHp.replace(b.id,currentHp.get(b.id) - bb.damage);
                    bb.remove();
                }
            }
        });


        float rx = Tmp.v1.x, ry = Tmp.v1.y;
        anyNearby = false;

        all.clear();

        if(hitUnits){
            Units.nearby(null, rx, ry, b.hitSize * 2, other -> {
                if(other.checkTarget(collidesAir, collidesGround) && other.targetable(b.team) && (other.team != b.team || other.damaged())){
                    all.add(other);
                }
            });
        }

        if(hitBuildings && collidesGround){
            Units.nearbyBuildings(rx, ry, b.hitSize * 2, bb -> {
                if((bb.team != Team.derelict || state.rules.coreCapture) && (bb.team != b.team || bb.damaged())){
                    all.add(bb);
                }
            });
        }

        all.sort(h -> h.dst2(rx, ry));
        int len = Math.min(all.size, 1000);
        for(int i = 0; i < len; i++){
            Healthc other = all.get(i);


            if(((Teamc)other).team() == b.team){
                if(other.damaged() && healPercent > 0){
                    anyNearby = true;
                    other.heal(healPercent / 100f * other.maxHealth());
                    healEffect.at(other);
                    damageEffect.at(rx, ry, 0f,  b.team.color, other);
                    hitEffect.at(rx, ry, b.angleTo(other),  b.team.color);

                    if(other instanceof Building bd){
                        Fx.healBlockFull.at(bd.x, bd.y, 0f, Pal.heal, bd.block);
                    }
                    currentHp.replace(b.id,currentHp.get(b.id) - (max / pierceCap));
                }
            }else{
                anyNearby = true;
                if(other instanceof Building bd){
                    bd.damage(b.team, damage * state.rules.unitDamage(b.team));
                }else{
                    other.damage(damage * state.rules.unitDamage(b.team));
                }
                if(other instanceof Statusc s){
                    s.apply(status, statusDuration);
                }
                hitEffect.at(other.x(), other.y(), b.angleTo(other),  b.team.color);
                damageEffect.at(rx, ry, 0f,  b.team.color, other);
                hitEffect.at(rx, ry, b.angleTo(other), b.team.color);
                currentHp.replace(b.id,currentHp.get(b.id) - (max / (pierceCap*2)));
            }
        }

        if(anyNearby){
            collideSound.at(b);
        }
        if (currentHp.get(b.id) <= 0){
            b.remove();
        }
    }

    @Override
    public void draw(Bullet b) {
        if (widthScale > 0.001F) {
            Draw.z(125.0F);
            Draw.color(b.team.color, Color.white, Mathf.clamp(alpha));
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
