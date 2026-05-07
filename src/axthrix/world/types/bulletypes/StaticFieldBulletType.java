package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Hitboxc;
import mindustry.graphics.Layer;

/**
 * A bullet that travels to a target and on hit spawns a persistent pulsing static field.
 * The field deals periodic damage and slows all enemies within its radius.
 * Units below minHealthFraction are immune to field damage.
 *
 * Visual: a faint translucent bubble with a cage of lightning arcs forming a net/fence pattern.
 * Arc nodes are placed around the bubble surface and connected by your lightningPart-style
 * zigzag bolts, forming a spherical electric cage.
 *
 * Register in AxthrixLoader:
 *   Events.run(EventType.Trigger.update, () -> StaticFieldBulletType.updateFields());
 *   Events.run(EventType.Trigger.draw,   () -> StaticFieldBulletType.drawFields());
 */
public class StaticFieldBulletType extends BasicBulletType {

    // ---- Field Configuration ----

    /**
     * Radius of the spawned field in world units.
     * Default: 200f (~25 tiles).
     */
    public float fieldRadius = 200f;

    /**
     * How long the field persists after spawning, in ticks.
     * Range: > 0. Default: 300 ticks (5 seconds).
     */
    public float fieldDuration = 300f;

    /**
     * Damage dealt to each enemy unit per pulse.
     * Range: >= 0.
     */
    public float fieldDamagePerPulse = 25f;

    /**
     * Ticks between each damage pulse.
     * Range: > 0. Default: 45 ticks (~0.75 seconds).
     */
    public float fieldPulseInterval = 45f;

    /**
     * Duration of the slow status applied per pulse, in ticks.
     * Range: >= 0.
     */
    public float fieldStatusDuration = 90f;

    /**
     * Minimum health fraction (0-1) a unit must have to receive field damage.
     * Range: 0.0 to 1.0. Default: 0.25.
     */
    public float minHealthFraction = 0.25f;

    /** Primary color of the field bubble, rim, and lightning arcs. */
    public Color fieldColor = Color.valueOf("ff9944");

    /**
     * Number of nodes evenly distributed around the bubble surface.
     * Arcs are drawn between neighbouring nodes to form the cage.
     * Range: >= 3. Recommended: 8-16.
     */
    public int nodeCount = 12;

    /**
     * How many extra non-adjacent node pairs get a random "cross" arc each frame,
     * adding density to the net. Range: >= 0.
     */
    public int crossArcCount = 4;

    /** Thickness of the lightning arcs. Range: > 0. */
    public float arcThickness = 1.2f;

    // ---- Active Field Tracking ----

    private static final Seq<StaticField> activeFields = new Seq<>();

    // ---- Constructor ----

    public StaticFieldBulletType() {
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

    /** Spawns a static field at the given world position. */
    public void spawnField(float x, float y, mindustry.game.Team team) {
        activeFields.add(new StaticField(
                x, y, team,
                fieldDuration, fieldRadius,
                fieldDamagePerPulse, fieldPulseInterval,
                fieldStatusDuration, minHealthFraction,
                fieldColor, nodeCount, crossArcCount, arcThickness
        ));
    }

    // ---- Static Update/Draw (register in AxthrixLoader) ----

    /** Update all active static fields. Register in AxthrixLoader's update event. */
    public static void updateFields() {
        activeFields.removeAll(field -> {
            field.update();
            return field.isDone();
        });
    }

    /** Draw all active static fields. Register in AxthrixLoader's draw event. */
    public static void drawFields() {
        for(StaticField field : activeFields) {
            field.draw();
        }
    }

    // ---- Inner Field Class ----

    public static class StaticField {
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
        public int nodeCount;
        public int crossArcCount;
        public float arcThickness;

        // Per-node angular offsets that drift over time, giving the cage a rotating feel
        private final float[] nodePhaseOffsets;
        // Per-arc random seeds for reproducible zigzag shapes
        private final Rand rand = new Rand();

        public StaticField(float x, float y, mindustry.game.Team team,
                           float duration, float radius, float damage,
                           float pulseInterval, float statusDuration,
                           float minHealthFraction, Color color,
                           int nodeCount, int crossArcCount, float arcThickness) {
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
            this.nodeCount = nodeCount;
            this.crossArcCount = crossArcCount;
            this.arcThickness = arcThickness;

            nodePhaseOffsets = new float[nodeCount];
            for(int i = 0; i < nodeCount; i++) {
                nodePhaseOffsets[i] = Mathf.random(360f);
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
            float breathe = 0.97f + 0.03f * Mathf.sin(Time.time * 1.8f);
            float r = radius * breathe;

            Draw.z(Layer.effect + 0.3f);

            // ---- Bubble fill ----
            Draw.color(color, alpha * 0.07f);
            Fill.circle(x, y, r);

            // ---- Bubble rim ----
            Draw.color(color, alpha * 0.35f);
            Lines.stroke(1.6f);
            Lines.circle(x, y, r);

            // ---- Lightning net ----
            // Compute node positions on the bubble surface.
            // Nodes drift slowly so the cage appears to rotate.
            float[] nx = new float[nodeCount];
            float[] ny = new float[nodeCount];
            float rotOffset = Time.time * 8f; // slow global rotation
            for(int i = 0; i < nodeCount; i++) {
                float angle = (360f / nodeCount) * i + rotOffset + nodePhaseOffsets[i] * 0.05f;
                nx[i] = x + Mathf.cosDeg(angle) * r;
                ny[i] = y + Mathf.sinDeg(angle) * r;
            }

            // Draw arcs between adjacent nodes (the "fence" edges)
            for(int i = 0; i < nodeCount; i++) {
                int next = (i + 1) % nodeCount;
                drawLightningArc(
                        nx[i], ny[i], nx[next], ny[next],
                        color, alpha, arcThickness,
                        (i * 1000L + (int)(Time.time / 4))  // reseed every ~4 ticks for flicker
                );
            }

            // Draw cross-arcs between non-adjacent random node pairs for net density
            rand.setSeed((long)(Time.time / 6)); // reseed slowly so cross-arcs change gradually
            for(int c = 0; c < crossArcCount; c++) {
                int a = rand.nextInt(nodeCount);
                int b = (a + 2 + rand.nextInt(nodeCount - 3)) % nodeCount; // skip immediate neighbours
                float crossAlpha = alpha * (0.3f + 0.3f * Mathf.sin(Time.time * 5f + c));
                drawLightningArc(
                        nx[a], ny[a], nx[b], ny[b],
                        color, crossAlpha, arcThickness * 0.7f,
                        (c * 7777L + (int)(Time.time / 6))
                );
            }

            Draw.reset();
        }

        /**
         * Draws a single lightning arc between two points using a zigzag polyline,
         * matching the style of AxthrixFfx.lightningPart().
         */
        private void drawLightningArc(float x1, float y1, float x2, float y2,
                                      Color arcColor, float alpha, float thickness, long seed) {
            float dst = Mathf.dst(x1, y1, x2, y2);
            if(dst < 1f) return;

            float range = 5f;
            int links = Math.max(2, Mathf.ceil(dst / range));
            float spacing = dst / links;

            // Normal vector direction from start to end
            Tmp.v1.set(x2, y2).sub(x1, y1).nor();
            float normx = Tmp.v1.x;
            float normy = Tmp.v1.y;

            rand.setSeed(seed);

            Draw.color(arcColor, alpha);
            Lines.stroke(thickness);
            Lines.beginLine();
            Lines.linePoint(x1, y1);

            for(int i = 1; i < links; i++) {
                float len = i * spacing;
                // Perpendicular jitter (offset perpendicular to the arc direction)
                Tmp.v1.setToRandomDirection(rand).scl(range / 3.5f);
                float px = x1 + normx * len + Tmp.v1.x;
                float py = y1 + normy * len + Tmp.v1.y;
                // Clamp jitter so points don't fly too far off the bubble surface
                float distFromCenter = Mathf.dst(this.x, this.y, px, py);
                if(distFromCenter > radius * 1.1f) {
                    // pull back toward the bubble surface
                    float pull = radius / distFromCenter;
                    px = this.x + (px - this.x) * pull;
                    py = this.y + (py - this.y) * pull;
                }
                Lines.linePoint(px, py);
            }

            Lines.linePoint(x2, y2);
            Lines.endLine();

            // Bright white core on the arc
            Draw.color(arc.graphics.Color.white, alpha * 0.35f);
            Lines.stroke(thickness * 0.4f);
            Lines.line(x1, y1, x2, y2);
        }

        public boolean isDone() {
            return timeLeft <= 0f;
        }
    }
}