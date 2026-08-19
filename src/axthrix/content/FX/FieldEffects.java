package axthrix.content.FX;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.game.Team;
import mindustry.graphics.Layer;
import mindustry.type.StatusEffect;

public class FieldEffects {
    public static final Effect cryo = new Effect(300f, 200f, e -> {
        float r = e.rotation > 0 ? e.rotation : 80f;
        float fadeTicks = 290f;
        float alpha = e.time >= e.lifetime - fadeTicks
                ? Mathf.clamp((e.lifetime - e.time) / fadeTicks)
                : 1f;
        Color col = e.color;

        Draw.z(Layer.effect + 0.3f);
        Draw.color(col, alpha * 0.06f);
        Fill.circle(e.x, e.y, r);
        Draw.color(Color.white, col, 0.4f);
        Draw.alpha(alpha * 0.25f);
        Lines.stroke(1.4f);
        Lines.circle(e.x, e.y, r * (0.98f + 0.02f * Mathf.sin(Time.time * 2f)));

        int arms = 5, steps = 28;
        for (int a = 0; a < arms; a++) {
            float base = a * (360f / arms) + Time.time * 12f;
            Draw.color(col, alpha * 0.45f);
            Lines.stroke(1.1f);
            Lines.beginLine();
            for (int s = 0; s < steps; s++) {
                float t = s / (float) (steps - 1);
                float ang = base + t * 220f;
                float rad = r * (1f - t * 0.92f);
                Lines.linePoint(e.x + Mathf.cosDeg(ang) * rad, e.y + Mathf.sinDeg(ang) * rad);
            }
            Lines.endLine();
        }

        for (int i = 0; i < 16; i++) {
            float seed = (Time.time * 0.02f + i * 0.17f) % 1f;
            float t = 1f - seed;
            float ang = i * (360f / 16f) + Time.time * 18f;
            float rad = r * (0.15f + t * 0.8f);
            Draw.color(Color.white, col, 0.5f);
            Draw.alpha(alpha * (0.15f + 0.25f * t));
            Fill.circle(e.x + Mathf.cosDeg(ang) * rad, e.y + Mathf.sinDeg(ang) * rad, 1.1f + t * 1.4f);
        }
        Draw.reset();
    });

    public static final Effect statik = new Effect(580f, 200f, e -> {
        float r = e.rotation > 0 ? e.rotation : 80f;
        float fadeTicks = 570f;
        float alpha = e.time >= e.lifetime - fadeTicks
                ? Mathf.clamp((e.lifetime - e.time) / fadeTicks)
                : 1f;
        Color col = e.color;
        int nodes = 12;
        Rand rand = new Rand();

        Draw.z(Layer.effect + 0.3f);
        Draw.color(col, alpha * 0.07f);
        Fill.circle(e.x, e.y, r * (0.97f + 0.03f * Mathf.sin(Time.time * 1.8f)));
        Draw.color(col, alpha * 0.35f);
        Lines.stroke(1.6f);
        Lines.circle(e.x, e.y, r);

        float[] nx = new float[nodes], ny = new float[nodes];
        float rot = Time.time * 8f;
        for (int i = 0; i < nodes; i++) {
            float ang = (360f / nodes) * i + rot;
            nx[i] = e.x + Mathf.cosDeg(ang) * r;
            ny[i] = e.y + Mathf.sinDeg(ang) * r;
        }
        for (int i = 0; i < nodes; i++) {
            int n = (i + 1) % nodes;
            drawArc(e.x, e.y, r, nx[i], ny[i], nx[n], ny[n], col, alpha, 1.2f, i * 1000L + (int)(Time.time / 4), rand);
        }
        Draw.reset();
    });

    private static void drawArc(float cx, float cy, float radius,
                                float x1, float y1, float x2, float y2,
                                Color col, float alpha, float thick, long seed, Rand rand) {
        float dst = Mathf.dst(x1, y1, x2, y2);
        if (dst < 1f) return;
        int links = Math.max(2, Mathf.ceil(dst / 5f));
        float spacing = dst / links;
        Tmp.v1.set(x2, y2).sub(x1, y1).nor();
        rand.setSeed(seed);
        Draw.color(col, alpha);
        Lines.stroke(thick);
        Lines.beginLine();
        Lines.linePoint(x1, y1);
        for (int i = 1; i < links; i++) {
            Tmp.v2.setToRandomDirection(rand).scl(5f / 3.5f);
            float px = x1 + Tmp.v1.x * i * spacing + Tmp.v2.x;
            float py = y1 + Tmp.v1.y * i * spacing + Tmp.v2.y;
            float d = Mathf.dst(cx, cy, px, py);
            if (d > radius * 1.1f) {
                px = cx + (px - cx) * (radius / d);
                py = cy + (py - cy) * (radius / d);
            }
            Lines.linePoint(px, py);
        }
        Lines.linePoint(x2, y2);
        Lines.endLine();
    }
}

