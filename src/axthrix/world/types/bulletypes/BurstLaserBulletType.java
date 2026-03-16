package axthrix.world.types.bulletypes;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class BurstLaserBulletType extends TemperatureBulletType {
    public int burstCount = 10;
    public float burstSpacing = 8f;
    public float laserLength = 50f;
    public float laserWidth = 8f;

    public Color[] colors = {Color.valueOf("ec745855"), Color.valueOf("ec7458aa"), Color.valueOf("ff9c5a"), Color.white};

    public float explosionDamage = 100f;
    public float explosionRadius = 40f;
    public Effect explosionEffect = Fx.massiveExplosion;
    public Sound explosionSound = Sounds.explosionArtilleryShockBig;

    protected int burstsFired = 0;

    public BurstLaserBulletType(){
        super(0.01f, 30f);
        lifetime = 120f;
        hitEffect = Fx.hitLaser;
        despawnEffect = Fx.none;
        hitSize = 4f;
        drawSize = 400f;
        pierce = true;
        hittable = false;
        absorbable = false;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        burstsFired = 0;
    }

    @Override
    public void update(Bullet b){
        if(burstsFired < burstCount && b.timer(1, burstSpacing)){
            fireLaser(b, burstsFired);
            burstsFired++;
        }

        if(burstsFired >= burstCount){
            b.time = lifetime;
        }
    }

    protected void fireLaser(Bullet b, int index){
        float rot = b.rotation();
        boolean isLastBurst = index == burstCount - 1;

        drawLaser(b.x, b.y, rot, laserLength);

        Unit[] closestUnit = {null};
        Building[] closestBuilding = {null};
        float[] closestDist = {Float.MAX_VALUE};

        // Damage units
        mindustry.gen.Groups.unit.intersect(
                b.x - laserLength, b.y - laserLength,
                laserLength * 2, laserLength * 2,
                unit -> {
                    if(unit.team != b.team && !unit.dead){
                        float dst = b.dst(unit);

                        if(dst <= laserLength){
                            // Better angle check
                            float angleTo = b.angleTo(unit);
                            float angleToNorm = Mathf.mod(angleTo, 360f);
                            float rotNorm = Mathf.mod(rot, 360f);
                            float angleDiff = Math.abs(angleToNorm - rotNorm);
                            if(angleDiff > 180f) angleDiff = 360f - angleDiff;

                            if(angleDiff < 5f){
                                applyTemperatureUnit(unit, temperaturePerHit);

                                float finalDamage = damage;
                                float temp = getTemperatureUnit(unit);
                                if(temp > 0){
                                    finalDamage *= (1f + (temp * 0.01f));
                                }

                                unit.damage(finalDamage);
                                hitEffect.at(unit.x, unit.y, rot, colors[2]);

                                if(dst < closestDist[0]){
                                    closestDist[0] = dst;
                                    closestUnit[0] = unit;
                                }
                            }
                        }
                    }
                }
        );

        // Damage buildings
        Geometry.circle(
                (int)(b.x / 8f), (int)(b.y / 8f),
                (int)(laserLength / 8f),
                (cx, cy) -> {
                    Building build = mindustry.Vars.world.build(cx, cy);
                    if(build != null && build.team != b.team && !build.dead){
                        float dst = b.dst(build);
                        float angleTo = b.angleTo(build);
                        float angleToNorm = Mathf.mod(angleTo, 360f);
                        float rotNorm = Mathf.mod(rot, 360f);
                        float angleDiff = Math.abs(angleToNorm - rotNorm);
                        if(angleDiff > 180f) angleDiff = 360f - angleDiff;

                        if(dst <= laserLength && angleDiff < 10f){
                            applyTemperatureBuilding(build, temperaturePerHit);

                            float finalDamage = damage;
                            float temp = getTemperatureBuilding(build);
                            if(temp > 0){
                                finalDamage *= (1f + (temp * 0.01f));
                            }

                            build.damage(finalDamage);
                            hitEffect.at(build.x, build.y, rot, colors[2]);

                            if(dst < closestDist[0]){
                                closestDist[0] = dst;
                                closestBuilding[0] = build;
                            }
                        }
                    }
                }
        );

        // Explosion on last burst
        if(isLastBurst && (closestUnit[0] != null || closestBuilding[0] != null)){
            float expX = closestUnit[0] != null ? closestUnit[0].x : closestBuilding[0].x;
            float expY = closestUnit[0] != null ? closestUnit[0].y : closestBuilding[0].y;
            createExplosion(b, expX, expY);
        }
    }

    protected void drawLaser(float x, float y, float rotation, float length){
        Draw.z(Layer.bullet);

        float endX = x + Angles.trnsx(rotation, length);
        float endY = y + Angles.trnsy(rotation, length);

        for(int i = 0; i < colors.length; i++){
            Draw.color(colors[i]);
            Lines.stroke(laserWidth * (1f - i * 0.2f));
            Lines.line(x, y, endX, endY);
        }

        Fx.lancerLaserShoot.at(x, y, rotation, colors[2]);
        Fx.lancerLaserShootSmoke.at(endX, endY, rotation, colors[2]);

        Draw.reset();
    }

    protected void createExplosion(Bullet b, float x, float y){
        explosionSound.at(x, y);
        explosionEffect.at(x, y, explosionRadius, colors[2]);

        mindustry.entities.Damage.damage(b.team, x, y, explosionRadius, explosionDamage, true, false);

        mindustry.gen.Groups.unit.intersect(
                x - explosionRadius, y - explosionRadius,
                explosionRadius * 2, explosionRadius * 2,
                unit -> {
                    if(unit.team != b.team && unit.within(x, y, explosionRadius)){
                        applyTemperatureUnit(unit, temperaturePerHit * 2f);
                    }
                }
        );
    }

    @Override
    public void draw(Bullet b){
        // Lasers drawn in fireLaser
    }
}