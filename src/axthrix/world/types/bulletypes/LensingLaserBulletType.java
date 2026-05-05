package axthrix.world.types.bulletypes;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

public class LensingLaserBulletType extends TemperatureBulletType {
    public float laserLength = 220f;
    public float damageInterval = 5f;
    public float width = 1f;

    public Color[] colors = {Color.valueOf("ec745855"), Color.valueOf("ec7458aa"), Color.valueOf("ff9c5a"), Color.white};
    public float[] tscales = {1f, 0.7f, 0.5f, 0.2f};
    public float[] strokes = {2f, 1.5f, 1f, 0.3f};
    public float[] lenscales = {1f, 1.12f, 1.15f, 1.17f};

    public float oscScl = 0.8f;
    public float oscMag = 1.5f;
    public float shake = 1f;

    public float finalDamageMultiplier = 80f;
    public Sound completionSound = Sounds.beamPlasmaSmall;
    public Effect completionEffect = Fx.explosion;

    public LensingLaserBulletType(){
        super(0f, 15f);
        hitEffect = Fx.hitLaser;
        despawnEffect = Fx.none;
        hitSize = 4f;
        drawSize = 420f;
        lifetime = 60f;
        pierce = true;
        hittable = false;
        absorbable = false;
        collides = false;
        keepVelocity = false;
    }

    @Override
    public void update(Bullet b){
        if(b.timer(1, damageInterval)){
            float rot = b.rotation();
            boolean isFinalTick = b.time >= lifetime - damageInterval;

            // Damage units in beam
            mindustry.gen.Groups.unit.intersect(
                    b.x - laserLength, b.y - laserLength,
                    laserLength * 2, laserLength * 2,
                    unit -> {
                        if(unit.team != b.team && !unit.dead){
                            // Check if unit is in laser path
                            float angleTo = b.angleTo(unit);
                            float angleToNorm = Mathf.mod(angleTo, 360f);
                            float rotNorm = Mathf.mod(rot, 360f);
                            float angleDiff = Math.abs(angleToNorm - rotNorm);
                            if(angleDiff > 180f) angleDiff = 360f - angleDiff;

                            if(angleDiff < 5f && b.dst(unit) <= laserLength){
                                TemperatureLogic.applyTemperatureUnit(unit, temperaturePerHit);

                                float tickDamage = isFinalTick ? damage * finalDamageMultiplier : damage;
                                float temp = TemperatureLogic.getTemperatureUnit(unit);
                                if(temp > 0){
                                    tickDamage *= (1f + (temp * 0.01f));
                                }

                                unit.damage(tickDamage);
                                hitEffect.at(unit.x, unit.y, rot, colors[2]);

                                if(isFinalTick){
                                    completionSound.at(unit);
                                    completionEffect.at(unit.x, unit.y, colors[2]);
                                }
                            }
                        }
                    }
            );

            // Damage buildings in beam
            Geometry.circle(
                    (int)(b.x / 8f), (int)(b.y / 8f),
                    (int)(laserLength / 8f),
                    (cx, cy) -> {
                        Building build = mindustry.Vars.world.build(cx, cy);
                        if(build != null && build.team != b.team && !build.dead){
                            float angleTo = b.angleTo(build);
                            float angleToNorm = Mathf.mod(angleTo, 360f);
                            float rotNorm = Mathf.mod(rot, 360f);
                            float angleDiff = Math.abs(angleToNorm - rotNorm);
                            if(angleDiff > 180f) angleDiff = 360f - angleDiff;

                            if(angleDiff < 10f && b.dst(build) <= laserLength){
                                TemperatureLogic.applyTemperatureBuilding(build, temperaturePerHit);

                                float tickDamage = isFinalTick ? damage * finalDamageMultiplier : damage;
                                float temp = TemperatureLogic.getTemperatureBuilding(build);
                                if(temp > 0){
                                    tickDamage *= (1f + (temp * 0.01f));
                                }

                                build.damage(tickDamage);
                                hitEffect.at(build.x, build.y, rot, colors[2]);
                            }
                        }
                    }
            );
        }

        if(shake > 0){
            Effect.shake(shake, shake, b);
        }
    }

    @Override
    public void draw(Bullet b){
        float fout = Mathf.clamp(b.time > lifetime - 10f ? 1f - (b.time - (lifetime - 10f)) / 10f : 1f);
        float baseLen = laserLength * fout;

        Draw.z(Layer.bullet);

        for(int i = 0; i < colors.length; i++){
            Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));

            for(int s : Mathf.signs){
                Tmp.v1.trns(b.rotation() + 90, (width + Mathf.absin(Time.time, oscScl, oscMag)) * s * fout * tscales[i]);

                float extend = baseLen * lenscales[i];

                Lines.stroke((width * strokes[i] + Mathf.absin(Time.time, oscScl, oscMag)) * fout);
                Lines.lineAngle(b.x + Tmp.v1.x, b.y + Tmp.v1.y, b.rotation(), extend, false);
            }
        }

        Tmp.v1.trns(b.rotation(), baseLen * 1.1f);
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, width * 1.4f * fout, colors[2], 0.6f);
        Draw.reset();
    }
}