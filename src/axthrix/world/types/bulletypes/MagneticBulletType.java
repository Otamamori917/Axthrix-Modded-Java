package axthrix.world.types.bulletypes;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.math.Angles;
import arc.math.Mat;
import arc.math.Mathf;
import arc.struct.IntSeq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.AxUtil;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Units;
import mindustry.entities.bullet.ContinuousBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Healthc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Tile;

public class MagneticBulletType extends ContinuousBulletType {
    public float coneAngleArc = 45f;
    /** Fraction of damage dealt that becomes healing */
    public float damageToHealRatio = 0.25f;
    public int scanAccuracy = 30;
    /** Max simultaneous targets (-1 = unlimited) */
    public int maxTargets = -1;

    /** Heal allies near the shooter */
    public boolean healAllies = true;
    /** Radius around the shooter for ally healing */
    public float healRange = 48f;
    /** 0 = no distance penalty, 1 = zero contribution at max range */
    public float healDistanceFalloff = 1f;

    public float laserWidth = 6f;
    public float particleSizeMin = 0.8f;
    public float particleSizeMax = 1.2f;
    public float particleSpeedMultiplier = 1.5f;
    public Color beamColor = Pal.accent;
    public Color particleColorFrom = Color.darkGray;
    public Color particleColorTo = Color.black;
    public Color refractionColor = Color.green;

    public static FrameBuffer sweepBuffer;
    private static final int fBufferSize = 1024;

    public MagneticBulletType() {
        super();
        speed = 0.001f;
        keepVelocity = false;
        collides = false;
        pierce = true;
        hittable = false;
        absorbable = false;
        hitEffect = Fx.none;
        despawnEffect = Fx.none;
        knockback = -2f;
        impact = true;
    }

    @Override
    public void init() {
        super.init();
        drawSize = Math.max(drawSize, length * 2f);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        MagData data = new MagData(scanAccuracy);
        for (int i = 0; i < scanAccuracy; i++) data.rays[i] = length;
        b.data = data;
    }

    @Override
    public void update(Bullet b) {
        if (!(b.data instanceof MagData data)) return;
        if (!(b.owner instanceof Healthc shooter)) return;

        data.hitUnitIds.clear();
        data.hitBuildingPos.clear();

        final int[] idx = {0};
        AxUtil.shotgunRange(scanAccuracy, coneAngleArc, b.rotation(), ang -> {
            Tmp.v1.trns(ang, length).add(b);
            Vars.world.raycastEachWorld(b.x, b.y, Tmp.v1.x, Tmp.v1.y, (cx, cy) -> {
                Tile tile = Vars.world.tile(cx, cy);
                boolean isLaserShield = tile != null && tile.build != null
                        && tile.team() != b.team
                        && tile.block() != null
                        && tile.block().absorbLasers;

                data.rays[idx[0]] = isLaserShield
                        ? Math.min(b.dst(cx * Vars.tilesize, cy * Vars.tilesize), length)
                        : length;
                return isLaserShield;
            });
            idx[0]++;
        });

        if (!b.timer(1, 4f)) return;

        float calculatedDamageThisTick = damage * Time.delta * 4f;
        final float[] healPool = {0f};
        final int[] currentTargetCount = {0};

        final float ox = shooter.x();
        final float oy = shooter.y();

        Tmp.r1.setCentered(b.x, b.y, 1f);
        AxUtil.shotgunRange(3, coneAngleArc, b.rotation(), ang -> {
            Tmp.v1.trns(ang, length).add(b);
            Tmp.r1.merge(Tmp.v1);
        });

        Groups.unit.intersect(Tmp.r1.x, Tmp.r1.y, Tmp.r1.width, Tmp.r1.height, unit -> {
            if (maxTargets != -1 && currentTargetCount[0] >= maxTargets) return;

            if (b.within(unit, length + unit.hitSize / 2f)
                    && Angles.within(b.rotation(), b.angleTo(unit), coneAngleArc)) {

                int index = Mathf.clamp(Mathf.round(
                        ((AxUtil.angleDistSigned(b.angleTo(unit), b.rotation()) + coneAngleArc)
                                / (coneAngleArc * 2f)) * (data.rays.length - 1)
                ), 0, data.rays.length - 1);

                if (b.dst(unit) < data.rays[index] && unit.team != b.team) {
                    Tmp.v3.set(unit).sub(b).nor().scl(knockback * 80f);
                    if (impact) Tmp.v3.setAngle(b.rotation() + (knockback < 0 ? 180f : 0f));
                    unit.impulse(Tmp.v3);

                    unit.apply(status, statusDuration);
                    unit.damage(calculatedDamageThisTick);

                    float dist = Mathf.dst(ox, oy, unit.x, unit.y);
                    float falloff = 1f - Mathf.clamp(dist / length) * healDistanceFalloff;
                    healPool[0] += calculatedDamageThisTick * damageToHealRatio * Math.max(falloff, 0f);

                    data.hitUnitIds.add(unit.id);
                    currentTargetCount[0]++;
                }
            }
        });

        Vars.indexer.allBuildings(b.x, b.y, range, other -> {
            if (maxTargets != -1 && currentTargetCount[0] >= maxTargets) return;

            if (other.team != b.team) {
                float angleToBuild = b.angleTo(other);
                if (Angles.within(b.rotation(), angleToBuild, coneAngleArc)) {
                    int index = Mathf.clamp(Mathf.round(
                            ((AxUtil.angleDistSigned(angleToBuild, b.rotation()) + coneAngleArc)
                                    / (coneAngleArc * 2f)) * (data.rays.length - 1)
                    ), 0, data.rays.length - 1);

                    if (b.dst(other) < data.rays[index]) {
                        float buildDamage = calculatedDamageThisTick * buildingDamageMultiplier;
                        other.damage(buildDamage);

                        float dist = Mathf.dst(ox, oy, other.x, other.y);
                        float falloff = 1f - Mathf.clamp(dist / length) * healDistanceFalloff;
                        healPool[0] += buildDamage * damageToHealRatio * Math.max(falloff, 0f);

                        data.hitBuildingPos.add(other.pos());
                        currentTargetCount[0]++;
                    }
                }
            }
        });

        applyHealing(b, shooter, healPool[0], ox, oy);
    }

    protected void applyHealing(Bullet b, Healthc shooter, float pool, float ox, float oy) {
        if (pool <= 0f) return;

        shooter.heal(pool);
        healEffect(b, shooter.x(), shooter.y(), pool);

        if (!healAllies || healRange <= 0f) return;

        Units.nearby(b.team, ox, oy, healRange, unit -> {
            if (unit == shooter || unit.dead || unit.health >= unit.maxHealth) return;

            float dist = Mathf.dst(ox, oy, unit.x, unit.y);
            float falloff = 1f - Mathf.clamp(dist / healRange) * healDistanceFalloff;
            if (falloff <= 0f) return;

            float amount = pool * falloff;
            unit.heal(amount);
            healEffect(b, unit.x, unit.y, amount);
        });

        Vars.indexer.allBuildings(ox, oy, healRange, build -> {
            if (build.team != b.team || build == shooter || build.dead() || build.health >= build.maxHealth) return;

            float dist = Mathf.dst(ox, oy, build.x, build.y);
            float falloff = 1f - Mathf.clamp(dist / healRange) * healDistanceFalloff;
            if (falloff <= 0f) return;

            float amount = pool * falloff;
            build.heal(amount);
            healEffect(b, build.x, build.y, amount);
        });
    }

    protected void healEffect(Bullet b, float x, float y, float amount) {
        if (amount <= 0.05f) return;
        if (!b.timer(2, 20f)) return;

        Draw.z(Layer.effect);
        Draw.color(Pal.heal, 0.08f + Mathf.clamp(amount / 25f) * 0.06f);
        Fill.circle(x, y, 1.2f + amount * 0.03f);
        Draw.color(Pal.heal, 0.04f);
        Fill.circle(x, y, 2.2f + amount * 0.05f);
        Draw.reset();

        if (Mathf.chance(0.35f)) {
            float ang = Mathf.random(360f);
            float rad = Mathf.random(6f, 12f);
            Fx.healWaveDynamic.at(
                    x + Angles.trnsx(ang, rad),
                    y + Angles.trnsy(ang, rad),
                    0.12f,
                    Pal.heal
            );
        }
    }

    @Override
    public void draw(Bullet b) {
        if (!(b.data instanceof MagData data)) return;

        if (Core.settings.getBool("drawhitboxes")) {
            float z = Draw.z();
            Draw.z(Layer.max);

            float half = coneAngleArc;

            Tmp.v1.trns(b.rotation() - half, data.rays[0]).add(b);
            Draw.color(Color.lime.cpy().a(0.4f));
            if (!Vars.renderer.animateShields) Draw.alpha(0.3f);

            for (int i = 1; i < scanAccuracy; i++) {
                float ang = Mathf.lerp(-half, half, i / (scanAccuracy - 1f)) + b.rotation();
                Tmp.v2.trns(ang, data.rays[i]).add(b);
                Fill.tri(b.x, b.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
                Tmp.v1.set(Tmp.v2);
            }

            Draw.color();
            Draw.z(z);
        }

        if (data.hitUnitIds.size == 0 && data.hitBuildingPos.size == 0) return;

        if (sweepBuffer == null || sweepBuffer.getWidth() != fBufferSize) {
            if (sweepBuffer != null) sweepBuffer.dispose();
            sweepBuffer = new FrameBuffer(fBufferSize, fBufferSize);
        }

        float alpha = b.keepAlive() ? 1f : Mathf.clamp(1f - (b.time / b.lifetime));

        Mat oldProj = new Mat(Draw.proj());
        Draw.flush();

        sweepBuffer.begin(Color.clear);

        float snapX = Mathf.round(b.x);
        float snapY = Mathf.round(b.y);
        Draw.proj().setOrtho(
                snapX - fBufferSize / 4f,
                snapY - fBufferSize / 4f,
                fBufferSize / 2f,
                fBufferSize / 2f
        );

        for (int i = 0; i < data.hitUnitIds.size; i++) {
            Unit unit = Groups.unit.getByID(data.hitUnitIds.items[i]);
            if (unit != null && unit.isAdded()) {
                drawBeamCore(b.x, b.y, unit.x, unit.y, beamColor);
            }
        }
        for (int i = 0; i < data.hitBuildingPos.size; i++) {
            Building building = Vars.world.build(data.hitBuildingPos.items[i]);
            if (building != null && building.isValid()) {
                drawBeamCore(b.x, b.y, building.x, building.y, beamColor);
            }
        }

        Draw.flush();
        sweepBuffer.end();
        Draw.proj(oldProj);

        Draw.z(Layer.bullet - 0.01f);
        Draw.color(Color.white);
        Draw.alpha(alpha * 0.85f);

        TextureRegion region = new TextureRegion(sweepBuffer.getTexture());
        region.flip(false, true);
        Draw.rect(region, snapX, snapY, fBufferSize / 2f, fBufferSize / 2f);
        Draw.reset();

        Draw.z(Layer.bullet);
        for (int i = 0; i < data.hitUnitIds.size; i++) {
            Unit unit = Groups.unit.getByID(data.hitUnitIds.items[i]);
            if (unit != null && unit.isAdded()) {
                drawFieldEffects(b.x, b.y, unit.x, unit.y);
            }
        }
        for (int i = 0; i < data.hitBuildingPos.size; i++) {
            Building building = Vars.world.build(data.hitBuildingPos.items[i]);
            if (building != null && building.isValid()) {
                drawFieldEffects(b.x, b.y, building.x, building.y);
            }
        }
        Draw.reset();
    }

    private void drawBeamCore(float sx, float sy, float tx, float ty, Color color) {
        Draw.color(color);
        Lines.stroke(laserWidth);
        Draw.alpha(0.75f);
        Lines.line(sx, sy, tx, ty);
    }

    private void drawFieldEffects(float sx, float sy, float tx, float ty) {
        float beamLength = Mathf.dst(sx, sy, tx, ty);
        float beamAngle = Angles.angle(sx, sy, tx, ty);

        for (int i = 0; i < 3; i++) {
            float thicknessOffset = Mathf.sin(Time.time * 0.06f + i * 2.1f) * (laserWidth * 0.45f);
            float start = beamLength * (0.05f + i * 0.08f);
            float end = beamLength * (0.7f + i * 0.08f);

            Tmp.v1.trns(beamAngle, start, thicknessOffset).add(sx, sy);
            Tmp.v2.trns(beamAngle, end, -thicknessOffset * 0.5f).add(sx, sy);

            Draw.color(refractionColor);
            Lines.stroke(0.7f + Mathf.absin(Time.time + i, 12f, 0.6f));
            Draw.alpha(0.22f + Mathf.absin(Time.time * 0.5f + i, 16f, 0.1f));
            Lines.line(Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
        }

        int count = Mathf.round(beamLength / 9f);
        for (int p = 0; p < count; p++) {
            float progress = (Time.time * particleSpeedMultiplier * 0.35f + p / (float) count) % 1f;
            float dist = (1f - progress) * beamLength;
            float side = Mathf.sin(Time.time * 0.08f + p * 1.3f) * (laserWidth * 0.55f);

            Tmp.v1.trns(beamAngle, dist, side).add(sx, sy);

            Draw.color(particleColorFrom, particleColorTo, 0.5f + Mathf.absin(Time.time + p, 14f, 0.25f));
            Draw.alpha(0.35f + Mathf.absin(Time.time * 0.6f + p, 12f, 0.15f));

            float pSize = Mathf.lerp(particleSizeMin, particleSizeMax, Mathf.absin(p, 5f, 1f));
            Fill.poly(Tmp.v1.x, Tmp.v1.y, 3 + (p % 4), pSize, Time.time * 0.4f + p * 20f);
        }
    }

    public static class MagData {
        public final float[] rays;
        public final IntSeq hitUnitIds = new IntSeq();
        public final IntSeq hitBuildingPos = new IntSeq();

        public MagData(int scanAccuracy) {
            rays = new float[scanAccuracy];
        }
    }
}