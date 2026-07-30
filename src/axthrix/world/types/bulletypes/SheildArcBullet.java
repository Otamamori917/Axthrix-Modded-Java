package axthrix.world.types.bulletypes;

import arc.audio.Sound;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.ShieldArcAbility;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import mindustry.graphics.Layer;

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
    public Effect pushEffect = Fx.circleColorSpark;
    public boolean pushUnits = true;
    public float missileUnitMultiplier = 2f;
    public float angle = 80f;
    protected float widthScale;
    protected HashMap<Integer, Float> alpha = new HashMap<>();
    public float chanceDeflect = -1f;
    public float reflectBuildingDamage = 1f;
    public float reflectVel = 1f;
    public float reflectTime = 1f - 0.5f;
    public Sound deflectSound = Sounds.none;
    public Sound breakSound = Sounds.shieldBreakSmall;
    public boolean drawArc = true;





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


        float reach = radius + width;
        Groups.bullet.intersect(paramPos.x - reach, paramPos.y - reach, reach * 2f, reach * 2f,



                eb -> {
                    if(eb.team != b.team && eb.type.absorbable && currentHp.get(b.id) > 0 &&
                            !(eb.within(paramPos, radius - width) && paramPos.within(eb.x - eb.deltaX, eb.y - eb.deltaY, radius - width)) &&
                            (Tmp.v1.set(eb).add(eb.deltaX, eb.deltaY).within(paramPos, radius + width) || eb.within(paramPos, radius + width)) &&
                            (Angles.within(paramPos.angleTo(eb), b.rotation() + angleOffset, angle / 2f) || Angles.within(paramPos.angleTo(eb.x + eb.deltaX, eb.y + eb.deltaY), b.rotation() + angleOffset, angle / 2f))){

                        wasHit(eb,b);
                        if(chanceDeflect > 0f && eb.vel.len() >= 0.1f && eb.type.reflectable && Mathf.chance(chanceDeflect)){

                            deflectSound.at(paramPos, Mathf.random(0.9f, 1.1f));
                            eb.trns(-eb.vel.x, -eb.vel.y);

                            float penX = Math.abs(paramPos.x - eb.x), penY = Math.abs(paramPos.y - eb.y);

                            if(penX > penY){
                                eb.vel.x *= -1;
                                eb.vel.y *= reflectVel;
                            }else{
                                eb.vel.y *= -1;
                                eb.vel.x *= reflectVel;
                            }

                            eb.owner = b;
                            eb.team = b.team;
                            eb.time = eb.lifetime * reflectTime;
                            if(reflectBuildingDamage > 0f){
                                eb.buildingDamageMultiplier = reflectBuildingDamage;
                            }

                        }else{
                            eb.absorb();
                            Fx.absorb.at(eb);

                            hitSound.at(eb.x, eb.y, 1f + Mathf.range(0.1f), hitSoundVolume);
                        }

                        // break shield
                        if(currentHp.get(b.id) <= eb.damage()){

                            Fx.arcShieldBreak.at(paramPos.x, paramPos.y, 0, b.type.hitColor);

                            breakSound.at(paramPos.x, paramPos.y);
                            b.remove();
                        }

                        // shieldDamage for consistency
                        currentHp.replace(b.id,currentHp.get(b.id)- eb.type.shieldDamage(eb));
                    }
                });


        Units.nearbyEnemies(b.team, paramPos.x - reach, paramPos.y - reach, reach * 2f, reach * 2f,
                unit -> {
                    if(currentHp.get(b.id) > 0 && unit.targetable(b.team) &&
                            !(unit.within(paramPos, radius - width) && paramPos.within(unit.x - unit.deltaX, unit.y - unit.deltaY, radius - width)) &&
                            (Tmp.v1.set(unit).add(unit.deltaX, unit.deltaY).within(paramPos, radius + width) || unit.within(paramPos, radius + width)) &&
                            (Angles.within(paramPos.angleTo(unit), b.rotation() + angleOffset, angle / 2f) || Angles.within(paramPos.angleTo(unit.x + unit.deltaX, unit.y + unit.deltaY), b.rotation() + angleOffset, angle / 2f))){

                        if(unit.isMissile() && missileUnitMultiplier >= 0f){
                            Call.unitSafeDeath(unit);
                            Fx.absorb.at(unit);
                            pushEffect.at(unit.x, unit.y,b.team.color);

                            currentHp.replace(b.id,currentHp.get(b.id)-unit.health() * missileUnitMultiplier * Vars.state.rules.unitDamage(unit.team));

                        }else if(pushUnits){

                            float overlapDst = reach - unit.dst(paramPos.x, paramPos.y);

                            if(overlapDst > 0){
                                if(Angles.angleDist(unit.angleTo(paramPos), unit.vel.angle()) < 90f){
                                    unit.vel.setZero();
                                }
                                unit.move(Tmp.v1.set(unit).sub(b).setLength(overlapDst + 0.01f));

                                if(Mathf.chanceDelta(0.3f * Time.delta)){
                                    pushEffect.at(unit.x, unit.y, b.team.color);
                                }
                            }
                        }
                    }
                });
        if (currentHp.get(b.id) <= 0){
            b.remove();
        }
    }

    public void wasHit(Bullet enemyBullet, Bullet thisBullet){
        alpha.replace(thisBullet.id,1f);
    }

    @Override
    public void draw(Bullet b){
        if(widthScale > 0.001f){
            Draw.z(Layer.shields);

            Draw.color(b.team.color, Color.white, Mathf.clamp(alpha.get(b.id) == null ? alpha.get(b.id) : 0));
            var pos = paramPos.set(x, y).rotate(b.rotation() - 90f).add(b);

            if(!Vars.renderer.animateShields){
                Draw.alpha(0.4f);
            }

            if(sprite != null){
                Vec2 rp = offsetRegion ? pos : Tmp.v1.set(b);
                Draw.yscl = widthScale;
                Draw.rect(sprite, rp.x, rp.y, b.rotation() - 90);
                Draw.yscl = 1f;
            }

            if(drawArc){
                Lines.stroke(width * widthScale);
                Lines.arc(pos.x, pos.y, radius, angle / 360f, b.rotation() + angleOffset - angle / 2f);
            }
            Draw.reset();
        }
    }
}
