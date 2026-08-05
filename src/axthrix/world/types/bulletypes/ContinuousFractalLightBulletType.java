package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.draw.AxDrawf;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.Vars;
import mindustry.entities.Damage;
import mindustry.entities.Units;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.ContinuousFlameBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Healthc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

public class ContinuousFractalLightBulletType extends ContinuousFlameBulletType {
    public float heatApplyPerTick = 0.5f;

    public int laserFractals = 3;
    public float fractalSpread = 45f;
    public Color[] fractalColors ;
    public float fractalWidthMult = 0.7f;
    public float fractalDamageMult = 0.30f;

    private final Vec2 splitPoint = new Vec2();

    public ContinuousFractalLightBulletType() {
        super();
        drawSize = 400f;

        reflectable = true;
        absorbable = false;
        pierceCap = 3;
        layer = Layer.light;
        colors = new Color[]{
                new Color(255F, 255F, 255F, 0.45F),
                new Color(255F, 255F, 255F, 0.1F),
                new Color(255F, 255F, 255F, 0.1F),
                new Color(255F, 255F, 255F, 0.05F),
                new Color(255F, 255F, 255F, 0.01F)
        };
    }

    public ContinuousFractalLightBulletType(int fractals,int offset) {
        super();
        drawSize = 400f;
        laserFractals = fractals;
        fractalColors = AxDrawf.generateRainbow(fractals+offset, 0.15f);
        reflectable = true;
        absorbable = false;
        pierceCap = 3;
        layer = Layer.light;
        colors = new Color[]{
                new Color(255F, 255F, 255F, 0.45F),
                new Color(255F, 255F, 255F, 0.1F),
                new Color(255F, 255F, 255F, 0.1F),
                new Color(255F, 255F, 255F, 0.05F),
                new Color(255F, 255F, 255F, 0.01F)
        };
    }

    @Override
    public void update(Bullet b) {
        float mult = b.fin(lengthInterp);
        float maxRange = length * mult;
        float mainLength = maxRange;
        boolean isHitting = false;

        Healthc closest = Damage.linecast(b, b.x, b.y, b.rotation(), maxRange);
        if (closest != null) {
            mainLength = Mathf.dst(b.x, b.y, closest.getX(), closest.getY());
            isHitting = true;
        }

        splitPoint.set(b.x + Mathf.cosDeg(b.rotation()) * mainLength, b.y + Mathf.sinDeg(b.rotation()) * mainLength);
        float remainingLength = maxRange - mainLength;

        if (isHitting && remainingLength > 5f && laserFractals > 0) {
            float totalSpread = fractalSpread * (laserFractals - 1);
            float startAngle = b.rotation() - (totalSpread / 2f);
            float stepAngle = laserFractals > 1 ? fractalSpread : 0f;

            for (int i = 0; i < laserFractals; i++) {
                float currentAngle = startAngle + (i * stepAngle);

                BulletType fractal = b.type().copy();
                fractal.damage = b.damage * fractalDamageMult;
                Bullet Fractal = fractal.create(b,b.x,b.y,b.rotation());
                Damage.collideLine(Fractal, b.team, splitPoint.x, splitPoint.y, currentAngle, remainingLength, false, true);
                Fractal.remove();
                injectAreaHeat(b, splitPoint.x, splitPoint.y, currentAngle, remainingLength);
            }
        }

        super.update(b);
    }
    private void injectAreaHeat(Bullet b, float sx, float sy, float angle, float len) {
        float step = 16f;
        float cos = Mathf.cosDeg(angle);
        float sin = Mathf.sinDeg(angle);

        for (float d = 0f; d < len; d += step) {
            float px = sx + cos * d;
            float py = sy + sin * d;

            Units.nearbyEnemies(b.team, px, py, width * 1.5f, unit -> {
                if (!unit.dead && unit.isValid()) {
                    TemperatureLogic.applyTemperatureUnit(unit, heatApplyPerTick * Time.delta);
                }
            });

            Vars.indexer.eachBlock(b.team, px, py, width * 1.5f, build -> build.team != b.team, build -> {
                if (build.isValid() && !build.dead) {
                    TemperatureLogic.applyTemperatureBuilding(build, heatApplyPerTick * Time.delta);
                }
            });
        }
    }

    @Override
    public void draw(Bullet b) {
        float mult = b.fin(lengthInterp);
        float maxRange = length * mult;
        float mainLength = maxRange;
        boolean isHitting = false;

        Healthc closest = Damage.linecast(b, b.x, b.y, b.rotation(), maxRange);
        if (closest != null) {
            mainLength = Mathf.dst(b.x, b.y, closest.getX(), closest.getY());
            isHitting = true;
        }

        float remainingLength = maxRange - mainLength;
        float sin = Mathf.sin(Time.time, oscScl, oscMag);

        for (int i = 0; i < colors.length; i++) {
            Draw.color(colors[i].write(Tmp.c1).mul(0.9f).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));
            Drawf.flame(b.x, b.y, divisions, b.rotation(),
                    mainLength * lengthWidthPans[i * 3] * (1f - sin),
                    width * lengthWidthPans[i * 3 + 1] * mult * (1f + sin),
                    lengthWidthPans[i * 3 + 2]
            );
        }

        if (drawFlare) {
            Draw.color(flareColor);
            Draw.z(flareLayer);
            float angle = Time.time * flareRotSpeed + (rotateFlare ? b.rotation() : 0f);
            for (int i = 0; i < 4; i++) {
                Drawf.tri(b.x, b.y, flareWidth, flareLength * (mult + sin), i * 90 + 45 + angle);
            }
            Draw.color();
            for (int i = 0; i < 4; i++) {
                Drawf.tri(b.x, b.y, flareWidth * flareInnerScl, flareLength * flareInnerLenScl * (mult + sin), i * 90 + 45 + angle);
            }
        }

        if (isHitting && remainingLength > 5f && laserFractals > 0) {
            float totalSpread = fractalSpread * (laserFractals - 1);
            float startAngle = b.rotation() - (totalSpread / 2f);
            float stepAngle = laserFractals > 1 ? fractalSpread : 0f;

            for (int i = 0; i < laserFractals; i++) {
                float currentAngle = startAngle + (i * stepAngle);

                Draw.color(fractalColors.length > 0 ? fractalColors[i % fractalColors.length] : Color.black);
                Drawf.flame(splitPoint.x, splitPoint.y, divisions, currentAngle,
                        remainingLength * (1f - sin),
                        (width * fractalWidthMult) * mult * (1f + sin),
                        0.2f
                );
            }
        }

        Tmp.v1.trns(b.rotation(), maxRange * 1.1f);
        Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, lightStroke, lightColor, lightOpacity);
        Draw.reset();
    }
}
