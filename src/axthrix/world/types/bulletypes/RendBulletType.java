package axthrix.world.types.bulletypes;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import axthrix.world.util.AxUtil;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.World;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.Tile;

public class RendBulletType extends BulletType {

    /** Half-angle of the attack cone in degrees. Total sweep width is cone * 2. */
    public float cone = 55f;
    /** Maximum reach of the cone in world units. */
    public float length = 90f;
    /** Number of raycasts used to build the obstacle scan. Higher = more accurate wall blocking. */
    public int scanAccuracy = 24;

    /** Base tint of the faint background cone. */
    public Color rendColor = Color.valueOf("d42e2e");
    /** Color of the sweeping claw streaks. */
    public Color swipeColor = Color.valueOf("ff5555");

    /** Total number of damage swipes fired over the bullet's lifetime. */
    public int totalSwipes = 5;
    /** Total lifetime of the bullet in ticks. */
    public float swipeDuration = 28f;
    /** Duration in ticks of a single active sweep (the visible claw motion). */
    public float swipeInterval = 5f;
    /** Idle pause in ticks between swipes — claws are hidden during this window. */
    public float swipeGap = 8f;

    /** Base damage dealt per swipe before falloff. */
    public float baseDamage = 55f;
    /** Damage multiplier at max range. 1.0 = no falloff, 0.0 = zero damage at tip. */
    public float damageFalloff = 0.45f;
    /** Damage per tick applied by the rend DoT after the delay expires. */
    public float rendDoTDamage = 7f;
    /** Delay in ticks before the rend DoT begins ticking. */
    public float dotDuration = 150f;

    /** Angular separation in degrees between the 3 parallel claw marks. */
    public float clawSpread = 14f;
    /** Fraction of length at which the shortest claw begins. Range 0-1. */
    public float clawStartDepth = 0.35f;
    /** Maximum perpendicular width of each claw slash at the peak of the sin envelope. */
    public float clawThickness = 8f;
    /** Ring buffer size for sweep angle history. More = longer visible trail. */
    public int angleHistorySize = 32;

    private int currentSwipe = 0;
    private float swipeCycleTime = 0f;

    public RendBulletType() {
        speed = 0.001f;
        lifetime = swipeDuration;
        hitEffect = Fx.none;
        despawnEffect = Fx.none;
        keepVelocity = false;
        collides = false;
        pierce = true;
        hittable = false;
        absorbable = false;
        damage = baseDamage;
        layer = Layer.bullet - 1f;
    }

    @Override
    public float calculateRange() { return length; }

    @Override
    public void init() {
        super.init();
        drawSize = Math.max(drawSize, length * 2.5f);
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        float[] data = new float[scanAccuracy + angleHistorySize + 1];
        for (int i = scanAccuracy; i < scanAccuracy + angleHistorySize; i++) {
            data[i] = Float.NaN;
        }
        b.data = data;
        currentSwipe = 0;
        swipeCycleTime = 0f;
    }

    @Override
    public void update(Bullet b) {
        if (!(b.data instanceof float[] data)) return;

        if (b.timer(2, 10f)) updateScanData(b, data);

        float cycleLength = swipeInterval + swipeGap;
        swipeCycleTime += Time.delta;

        if (swipeCycleTime >= cycleLength && currentSwipe < totalSwipes) {
            swipeCycleTime -= cycleLength;
            performSwipe(b, data);
            currentSwipe++;
            clearAngleHistory(data);
        }

        if (swipeCycleTime <= swipeInterval) {
            float sweepProgress = swipeCycleTime / swipeInterval;
            boolean leftToRight = (currentSwipe % 2 == 0);
            float sweepAngle = b.rotation() + Mathf.lerp(-cone, cone, sweepProgress) * (leftToRight ? 1f : -1f);
            pushAngleHistory(data, sweepAngle);
        }

        if (b.time >= lifetime - 4f && currentSwipe < totalSwipes) {
            performSwipe(b, data);
            currentSwipe = totalSwipes;
        }
    }

    /// Ring buffer storing sweep angle history per frame.
    private int historyStart() { return scanAccuracy; }
    private int historyIndexSlot() { return scanAccuracy + angleHistorySize; }

    /// Pushes the current sweep angle into the ring buffer, advancing the write head.
    private void pushAngleHistory(float[] data, float angle) {
        int idx = (int) data[historyIndexSlot()];
        data[historyStart() + idx] = angle;
        data[historyIndexSlot()] = (idx + 1) % angleHistorySize;
    }

    /// Resets all angle history entries to NaN and zeroes the write head. Called at the start of each new swipe.
    private void clearAngleHistory(float[] data) {
        for (int i = historyStart(); i < historyStart() + angleHistorySize; i++) {
            data[i] = Float.NaN;
        }
        data[historyIndexSlot()] = 0;
    }

    /// Returns the sweep angle at the given history offset. offset=0 is the most recent frame, offset=1 is one frame older, etc.
    private float getHistoryAngle(float[] data, int offset) {
        int writeHead = (int) data[historyIndexSlot()];
        int idx = ((writeHead - 1 - offset) % angleHistorySize + angleHistorySize) % angleHistorySize;
        return data[historyStart() + idx];
    }

    private void updateScanData(Bullet b, float[] data) {
        int[] idx = {0};
        AxUtil.shotgunRange(scanAccuracy, cone, b.rotation(), ang -> {
            Tmp.v1.trns(ang, length).add(b);
            World.raycastEachWorld(b.x, b.y, Tmp.v1.x, Tmp.v1.y, (cx, cy) -> {
                Tile tile = Vars.world.tile(cx, cy);
                boolean blocked = tile != null && tile.build != null && tile.team() != b.team && tile.block() != null && tile.block().absorbLasers;
                float dst = blocked ? Math.min(b.dst(cx * Vars.tilesize, cy * Vars.tilesize), length) : length;
                data[idx[0]] = dst * dst;
                return blocked;
            });
            idx[0]++;
        });
    }

    private void performSwipe(Bullet b, float[] data) {
        Tmp.r1.setCentered(b.x, b.y, length * 2f);

        Groups.unit.intersect(Tmp.r1.x, Tmp.r1.y, Tmp.r1.width, Tmp.r1.height, unit -> {
            if (unit.team == b.team || !b.within(unit, length + unit.hitSize * 0.6f)) return;

            float angleToUnit = b.angleTo(unit);
            if (!Angles.within(b.rotation(), angleToUnit, cone + 10f)) return;

            float angleDist = AxUtil.angleDistSigned(angleToUnit, b.rotation());
            float coneProgress = (angleDist + cone) / (cone * 2f);

            int index = Mathf.clamp(Mathf.round(coneProgress * (data.length - 1)), 0, data.length - 1);
            float dist = b.dst(unit);

            if (dist * dist > data[index]) return;

            float falloff = 1f - (dist / length) * (1f - damageFalloff);
            float finalDamage = damage * falloff;

            if (HarpoonBulletType.isHarpooned(unit)) finalDamage *= 1.65f;

            unit.damage(finalDamage);
            unit.apply(status, statusDuration);

            if (!unit.isFlying()) {
                Tmp.v1.trns(b.angleTo(unit), 3.8f);
                unit.impulse(Tmp.v1);
            }

            if (!Vars.net.client()) applyRendDoT(unit, b.team);

            Fx.smoke.at(unit.x, unit.y);
        });

        AxUtil.castConeTile(b.x, b.y, length, b.rotation(), cone, (building, tile) -> {
            if (building != null && building.team != b.team) {
                building.damage(damage * 0.65f);
            }
        }, null, data);
    }

    private void applyRendDoT(Unit unit, Team team) {
        Time.run(dotDuration, () -> {
            if (!unit.isValid() || unit.team == team) return;
            for (int i = 0; i < 6; i++) {
                Time.run(i * 18f, () -> {
                    if (unit.isValid()) {
                        unit.damage(rendDoTDamage);
                        Fx.smoke.at(unit.x + Mathf.range(8f), unit.y + Mathf.range(8f));
                    }
                });
            }
        });
    }

    @Override
    public void draw(Bullet b) {
        if (!(b.data instanceof float[] data)) return;

        float z = Draw.z();
        Draw.z(Layer.effect + 0.3f);

        float progress = b.time / lifetime;
        float alpha = Mathf.clamp(1f - progress * 0.8f);

        Draw.color(rendColor, alpha * 0.06f);
        drawCone(b);

        if (swipeCycleTime <= swipeInterval) {
            drawClawStreaks(b, data, alpha);
        }

        Draw.color();
        Draw.z(z);
    }

    private void drawCone(Bullet b) {
        Tmp.v1.trns(b.rotation() - cone, length * 0.85f).add(b);
        for (int i = 1; i < scanAccuracy; i++) {
            float ang = Mathf.lerp(-cone, cone, i / (scanAccuracy - 1f)) + b.rotation();
            Tmp.v2.trns(ang, length * 0.85f).add(b);
            Fill.tri(b.x, b.y, Tmp.v1.x, Tmp.v1.y, Tmp.v2.x, Tmp.v2.y);
            Tmp.v1.set(Tmp.v2);
        }
    }

    private void drawClawStreaks(Bullet b, float[] data, float alpha) {
        float[] clawDepths  = { clawStartDepth, clawStartDepth + 0.25f, clawStartDepth + 0.5f };
        float[] clawOffsets = { -clawSpread, 0f, clawSpread };

        float sweepProgress = Mathf.clamp(swipeCycleTime / swipeInterval);

        int validFrames = 0;
        for (int i = 0; i < angleHistorySize; i++) {
            if (!Float.isNaN(getHistoryAngle(data, i))) validFrames++;
            else break;
        }
        if (validFrames < 2) return;

        for (int claw = 0; claw < 3; claw++) {
            float radius = length * clawDepths[claw];
            float clawAngleOffset = clawOffsets[claw];

            for (int s = 0; s < validFrames - 1; s++) {
                float tTrail = s / (float)(validFrames - 1);

                float angLead = getHistoryAngle(data, s) + clawAngleOffset;
                float angTail = getHistoryAngle(data, s + 1) + clawAngleOffset;

                if (Float.isNaN(angLead) || Float.isNaN(angTail)) continue;

                float sinT = sweepProgress - tTrail * sweepProgress;
                float sinWidth = Mathf.sin(Mathf.clamp(sinT) * Mathf.PI);
                float halfSlash = clawThickness * 1.8f * sinWidth;

                float segAlpha = alpha * (1f - tTrail) * (1f - tTrail);

                if (halfSlash < 0.5f || segAlpha < 0.01f) continue;

                float cx1 = b.x + Mathf.cosDeg(angLead) * radius;
                float cy1 = b.y + Mathf.sinDeg(angLead) * radius;
                float cx2 = b.x + Mathf.cosDeg(angTail) * radius;
                float cy2 = b.y + Mathf.sinDeg(angTail) * radius;

                float perpLead = angLead + 90f;
                float perpTail = angTail + 90f;

                float x1 = cx1 + Mathf.cosDeg(perpLead) * halfSlash;
                float y1 = cy1 + Mathf.sinDeg(perpLead) * halfSlash;
                float x2 = cx1 - Mathf.cosDeg(perpLead) * halfSlash;
                float y2 = cy1 - Mathf.sinDeg(perpLead) * halfSlash;
                float x3 = cx2 - Mathf.cosDeg(perpTail) * halfSlash;
                float y3 = cy2 - Mathf.sinDeg(perpTail) * halfSlash;
                float x4 = cx2 + Mathf.cosDeg(perpTail) * halfSlash;
                float y4 = cy2 + Mathf.sinDeg(perpTail) * halfSlash;

                Draw.color(swipeColor, segAlpha * 0.9f);
                Fill.quad(x1, y1, x2, y2, x3, y3, x4, y4);
            }
        }
    }

    @Override
    public void drawLight(Bullet b) {
        Drawf.light(b.x, b.y, length * 1.4f, rendColor, 0.55f);
    }
}