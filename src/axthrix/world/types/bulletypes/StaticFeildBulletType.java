package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Hitboxc;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

/**
 * A bullet that travels to a target and on hit spawns a persistent pulsing field.
 * The field deals periodic damage and slows all enemies within its radius.
 * Units below minHealthFraction are immune to field damage.
 */
public class StaticFeildBulletType extends BasicBulletType {

    // ---- Field Configuration ----

    /**
     * Radius of the spawned field in world units.
     */
    public float fieldRadius = 200f;

    /**
     * How long the field persists after spawning, in ticks.
     */
    public float fieldDuration = 300f;

    /**
     * Damage dealt to each enemy unit per pulse.
     */
    public float fieldDamagePerPulse = 25f;

    /**
     * Ticks between each damage pulse.
     */
    public float fieldPulseInterval = 45f;

    /**
     * Duration of the slow status applied per pulse, in ticks.
     */
    public float fieldStatusDuration = 90f;

    /**
     * Minimum health fraction (0-1) a unit must have to receive field damage.
     */
    public float minHealthFraction = 0.25f;

    /** Color of the field bubble and sparks. */
    public Color fieldColor = Color.valueOf("ff9944");

    /**
     * Number of electric arc sparks drawn on the field bubble.
     */
    public int sparkCount = 8;

    // ---- Active Field Tracking ----

    private static final Seq<CharonField> activeFields = new Seq<>();

    // ---- Constructor ----

    public StaticFeildBulletType() {
        speed = 12f;
        lifetime = 70f;
        damage = 30f;
        hitSize = 6f;
        pierce = false;
        pierceCap = 1;
        collidesAir = true;
        collidesGround = true;
        keepVelocity = false;
        hittable = false;

        frontColor = Color.valueOf("ffaa55");
        backColor = Color.valueOf("ff6622");
        width = 6f;
        height = 14f;

        despawnEffect = mindustry.content.Fx.none;
        hitEffect = mindustry.content.Fx.none;
    }

    // ---- Bullet Lifecycle ----

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        spawnField(b.x, b.y, b.team);
        b.remove();
    }

    @Override
    public void hit(Bullet b, float x, float y) {
        super.hit(b, x, y);
        spawnField(x, y, b.team);
    }

    // ---- Field Spawning ----

    /**
     * Spawns a Charon field at the given world position.
     */
    public void spawnField(float x, float y, mindustry.game.Team team) {
        activeFields.add(new CharonField(
                x, y, team,
                fieldDuration, fieldRadius,
                fieldDamagePerPulse, fieldPulseInterval,
                fieldStatusDuration, minHealthFraction,
                fieldColor, sparkCount
        ));
    }

    // ---- Static Update/Draw (register in AxthrixLoader) ----

    /** Update all active Charon fields. Register in AxthrixLoader's update event. */
    public static void updateFields() {
        activeFields.removeAll(field -> {
            field.update();
            return field.isDone();
        });
    }

    /** Draw all active Charon fields. Register in AxthrixLoader's draw event. */
    public static void drawFields() {
        for(CharonField field : activeFields) {
            field.draw();
        }
    }

    // ---- Inner Field Class ----

    public static class CharonField {
        public float x, y;
        public mindustry.game.Team team;
        public float timeLeft;
        public float radius;
        public float damagePerPulse;
        public float pulseInterval;
        public float pulseTimer;
        public float statusDuration;
        public float minHealthFraction;
        public Color color;
        public int sparkCount;

        // Spark animation state
        private final float[] sparkAngles;
        private final float[] sparkLengths;
        private final float[] sparkPhases;
        private final float[] sparkSpeeds;

        public CharonField(float x, float y, mindustry.game.Team team,
                           float duration, float radius, float damage,
                           float pulseInterval, float statusDuration,
                           float minHealthFraction, Color color, int sparkCount) {
            this.x = x;
            this.y = y;
            this.team = team;
            this.timeLeft = duration;
            this.radius = radius;
            this.damagePerPulse = damage;
            this.pulseInterval = pulseInterval;
            this.pulseTimer = 0f;
            this.statusDuration = statusDuration;
            this.minHealthFraction = minHealthFraction;
            this.color = color;
            this.sparkCount = sparkCount;

            sparkAngles = new float[sparkCount];
            sparkLengths = new float[sparkCount];
            sparkPhases = new float[sparkCount];
            sparkSpeeds = new float[sparkCount];
            for(int i = 0; i < sparkCount; i++) {
                sparkAngles[i] = Mathf.random(360f);
                sparkLengths[i] = Mathf.random(0.25f, 0.65f);
                sparkPhases[i] = Mathf.random(360f);
                sparkSpeeds[i] = Mathf.random(25f, 55f) * (Mathf.chance(0.5) ? 1f : -1f);
            }
        }

        public void update() {
            timeLeft -= Time.delta;
            pulseTimer += Time.delta;

            if(pulseTimer >= pulseInterval) {
                pulseTimer = 0f;
                pulse();
            }
        }

        private void pulse() {
            Groups.unit.each(unit -> {
                if(unit.team == team) return;
                if(unit.dst(x, y) > radius) return;
                if(unit.healthf() < minHealthFraction) return;

                unit.damage(damagePerPulse);
                unit.apply(StatusEffects.slow, statusDuration);
            });
        }

        public void draw() {
            float alpha = Mathf.clamp(timeLeft / 30f);
            float breathe = 0.93f + 0.07f * Mathf.sin(Time.time * 2.5f);

            Draw.z(Layer.effect + 0.3f);

            // Bubble fill
            Draw.color(color, alpha * 0.09f);
            Fill.circle(x, y, radius * breathe);

            // Bubble rim
            Draw.color(color, alpha * 0.40f);
            Lines.stroke(1.8f);
            Lines.circle(x, y, radius * breathe);

            // Inner soft ring
            Draw.color(color, alpha * 0.06f);
            Fill.circle(x, y, radius * breathe * 0.85f);

            // Electric sparks along bubble surface
            Lines.stroke(1.1f);
            for(int i = 0; i < sparkCount; i++) {
                float baseAngle = sparkAngles[i] + Time.time * sparkSpeeds[i];
                float phase = sparkPhases[i] + Time.time * 180f;
                float jitter = Mathf.sin(phase) * 25f;
                float len = sparkLengths[i] * radius;

                float sx = x + Mathf.cosDeg(baseAngle) * radius * breathe;
                float sy = y + Mathf.sinDeg(baseAngle) * radius * breathe;
                float ex = sx - Mathf.cosDeg(baseAngle + jitter) * len;
                float ey = sy - Mathf.sinDeg(baseAngle + jitter) * len;

                float sparkAlpha = alpha * (0.45f + 0.55f * Mathf.absin(phase, 1f, 1f));
                Draw.color(color, sparkAlpha);
                Lines.line(sx, sy, ex, ey);

                // Bright white core on spark
                Draw.color(arc.graphics.Color.white, sparkAlpha * 0.5f);
                Lines.line(sx, sy,
                        sx + (ex - sx) * 0.3f,
                        sy + (ey - sy) * 0.3f);
            }

            Draw.reset();
        }

        public boolean isDone() {
            return timeLeft <= 0f;
        }
    }
}