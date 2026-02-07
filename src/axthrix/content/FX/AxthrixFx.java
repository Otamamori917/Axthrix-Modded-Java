package axthrix.content.FX;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import axthrix.content.AxLiquids;
import mindustry.content.Items;
import mindustry.entities.*;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static mindustry.Vars.renderer;

public class AxthrixFx {
    public static final Rand rand = new Rand();
    public static final Vec2 v = new Vec2();
    public static final Effect

    ahhimaLiquidNow = new Effect(45f, e -> {
        color(Color.gray, Color.clear, e.fin());
        randLenVectors(e.id, 3, 2.5f + e.fin() * 6f, (x, y) -> Fill.circle(e.x + x, e.y + y, 0.2f + e.fin() * 3f));
        color(Color.valueOf("ff2a00"), Color.valueOf("ffcc00"), e.fout());
        randLenVectors(e.id + 1, 4, 1 + e.fin() * 4f, (x, y) -> Fill.circle(e.x + x, e.y + y, 0.2f + e.fout() * 1.3f));
    }),

    PlasmaFlame1 = new Effect(40f, e -> {
        float z = Draw.z();
        Draw.z(Layer.blockOver);
        color(Color.white, Pal.heal, e.fin());

        randLenVectors(e.id, 3, 2.5f + e.fin() * 4f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.4f + e.fslope() * 3f);
        });

        color();

        Drawf.light(e.x, e.y, 40f * e.fslope(), Color.gray, 2f);
        Draw.z(z);
    }),

    PlasmaFlame2 = new Effect(10f, e -> {
        color(Color.white, Pal.heal, e.fin());

        randLenVectors(e.id, 4, 4f + e.fin() * 4.5f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, 0.4f + e.fslope() * 3f);
        });

        color();

        Drawf.light(e.x, e.y, 40f * e.fslope(), Color.gray, 2f);
    }),


    unitBreakdown = new Effect(100f, e -> {
        if(!(e.data instanceof Unit select) || select.type == null) return;

        float scl = e.fout(Interp.pow2Out);
        float p = Draw.scl;
        Draw.scl *= scl;

        mixcol(Pal.darkMetal, 1f);
        rect(select.type.fullIcon, select.x, select.y, select.rotation - 90f);
        Lines.stroke(e.fslope());
        Lines.square(select.x, select.y, (e.fout() * 0.9f) * select.hitSize * 1.5f, 45);
        reset();

        Draw.scl = p;
    }),

    failedMake =  new Effect(30f, e -> {
        color(Pal.lightOrange, Color.lightGray, Pal.lightishGray, e.fin());
        alpha(e.fout(0.5f));
        e.scaled(7f, s -> {
            stroke(0.5f + s.fout());
            Lines.circle(e.x, e.y, s.fin() * 7f);
        });
        randLenVectors(e.id, 5, e.finpow() * 17f, (x, y) -> Fill.rect(
                e.x + x + Mathf.randomSeedRange((long) (e.id + e.rotation + 7), 3f * e.fin()),
                e.y + y + Mathf.randomSeedRange((long) (e.id + e.rotation + 8), 3f * e.fin()),
                1f, 2f, e.rotation + e.fin() * 50f * e.rotation
        ));
        Drawf.light(e.x, e.y, 20f, Pal.lightOrange, 0.6f * e.fout());
    }).layer(Layer.bullet),

    bolt = new Effect(12f, 1300f, e -> {
        if(!(e.data instanceof Seq)) return;
        Seq<Vec2> lines = e.data();
        int n = Mathf.clamp(1 + (int)(e.fin() * lines.size), 1, lines.size);
        for(int i = 2; i >= 0; i--){
            stroke(4.5f * (i / 2f + 1f));
            color(i == 0 ? Color.white : e.color);
            alpha(i == 2 ? 0.5f : 1f);

            beginLine();
            for(int j = 0; j < n; j++){
                linePoint(lines.get(j).x, lines.get(j).y);
            }
            endLine(false);
        }

        if(renderer.lights.enabled()){
            for(int i = 0; i < n - 1; i++){
                Drawf.light(lines.get(i).x, lines.get(i).y, lines.get(i+1).x, lines.get(i+1).y, 40f, e.color, 0.9f);
            }
        }
    }),

    boltFade = new Effect(80f, 1300f, e -> {
        if(!(e.data instanceof Seq)) return;
        Seq<Vec2> lines = e.data();
        for(int i = 2; i >= 0; i--){
            stroke(4.5f * (i / 2f + 1f) * e.fout());
            color(i == 0 ? Color.white : e.color);
            alpha((i == 2 ? 0.5f : 1f) * e.fout());

            beginLine();
            for(Vec2 p : lines){
                linePoint(p.x, p.y);
            }
            endLine(false);
        }

        if(renderer.lights.enabled()){
            for(int i = 0; i < lines.size - 1; i++){
                Drawf.light(lines.get(i).x, lines.get(i).y, lines.get(i+1).x, lines.get(i+1).y, 40f, e.color, 0.9f * e.fout());
            }
        }
    }),

    boltStrike = new Effect(80f, 100f, e -> {
        color(Color.white, e.color, e.fin());

        for(int i = 2; i >= 0; i--){
            float s = 4.5f * (i / 2f + 1f) * e.fout();
            color(i == 0 ? Color.white : e.color);
            alpha((i == 2 ? 0.5f : 1f) * e.fout());
            for(int j = 0; j < 3; j++){
                Drawf.tri(e.x, e.y, 2f * s, (s + 65f + Mathf.randomSeed(e.id - j, 95f)) * e.fout(), e.rotation + Mathf.randomSeedRange(e.id + j, 80f) + 180f);
            }
        }

        Draw.z(Layer.effect - 0.001f);
        stroke(3f * e.fout(), e.color);
        float r = 55f * e.finpow();
        Fill.light(e.x, e.y, circleVertices(r), r, Tmp.c4.set(e.color).a(0f), Tmp.c3.set(e.color).a(e.fout()));
        circle(e.x, e.y, r);
        if(renderer.lights.enabled()) Drawf.light(e.x, e.y, r * 3.5f, e.color, e.fout(0.5f));
    }).layer(Layer.effect + 0.001f),

    shootSmokeMiniTitan = new Effect(70.0F/3, (e) -> {
        rand.setSeed((long)e.id);

        for(int i = 0; i < 13; ++i) {
            v.trns(e.rotation + rand.range(30.0F/3), rand.random(e.finpow() * 40.0F/3));
            e.scaled(e.lifetime * rand.random(0.3F/3, 1.0F), (b) -> {
                Draw.color(e.color, Pal.lightishGray, b.fin());
                Fill.circle(e.x + v.x, e.y + v.y, b.fout() * 3.4F/3 + 0.3F/3);
            });
        }

    }),

    boltHit = new Effect(80f, 100f, e -> {
        color(Color.white, e.color, e.fin());

        for(int i = 2; i >= 0; i--){
            float s = 4.5f * (i / 2f + 1f) * e.fout();
            color(i == 0 ? Color.white : e.color);
            alpha((i == 2 ? 0.5f : 1f) * e.fout());
            for(int j = 0; j < 6; j++){
                Drawf.tri(e.x, e.y, 2f * s, (s + 35f + Mathf.randomSeed(e.id - j, 95f)) * e.fout(), Mathf.randomSeedRange(e.id + j, 360f));
            }
        }

        Draw.z(Layer.effect - 0.001f);
        stroke(3f * e.fout(), e.color);
        float r = 55f * e.finpow();
        Fill.light(e.x, e.y, circleVertices(r), r, Tmp.c4.set(e.color).a(0f), Tmp.c3.set(e.color).a(e.fout()));
        circle(e.x, e.y, r);
        if(renderer.lights.enabled()) Drawf.light(e.x, e.y, r * 3.5f, e.color, e.fout(0.5f));
    }).layer(Layer.effect + 0.001f),

    instSmallShoot = new Effect(24.0F/2, (e) -> {
        e.scaled(10.0F/2, (b) -> {
            Draw.color(Color.white, Pal.bulletYellowBack, b.fin());
            Lines.stroke(b.fout() * 3.0F/2 + 0.2F);
            Lines.circle(b.x, b.y, b.fin() * 50.0F/2);
        });
        Draw.color(Pal.bulletYellowBack);

        for(int i : Mathf.signs) {
            Drawf.tri(e.x, e.y, 13.0F/2 * e.fout(), 85.0F/2, e.rotation + 90.0F * (float)i);
            Drawf.tri(e.x, e.y, 13.0F/2 * e.fout(), 50.0F/2, e.rotation + 20.0F * (float)i);
        }

        Drawf.light(e.x, e.y, 180.0F/2, Pal.bulletYellowBack, 0.9F * e.fout());
    }),

    instSmallHit = new Effect(20.0F/2, 200.0F/2, (e) -> {
        Draw.color(Pal.bulletYellowBack);

        for(int i = 0; i < 2; ++i) {
            Draw.color(i == 0 ? Pal.bulletYellowBack : Pal.bulletYellow);
            float m = i == 0 ? 1.0F : 0.5F;

            for(int j = 0; j < 5; ++j) {
                float rot = e.rotation + Mathf.randomSeedRange((long)(e.id + j), 50.0F);
                float w = 23.0F/2 * e.fout() * m;
                Drawf.tri(e.x, e.y, w, (80.0F/2 + Mathf.randomSeedRange((long)(e.id + j), 40.0F/2)) * m, rot);
                Drawf.tri(e.x, e.y, w, 20.0F/2 * m, rot + 180.0F);
            }
        }

        e.scaled(10.0F/2, (c) -> {
            Draw.color(Pal.bulletYellow);
            Lines.stroke(c.fout() * 1 + 0.2F);
            Lines.circle(e.x, e.y, c.fin() * 30.0F/2);
        });
        e.scaled(12.0F/2, (c) -> {
            Draw.color(Pal.bulletYellowBack);
            Angles.randLenVectors((long)e.id, 25, 5.0F/2 + e.fin() * 80.0F/2, e.rotation, 60.0F/2, (x, y) -> Fill.square(e.x + x, e.y + y, c.fout() * 3.0F/2, 45.0F));
        });
    }),

    railSmallHit = new Effect(18.0F/2, 200.0F/2, (e) -> {
        Draw.color(Pal.orangeSpark);

        for(int i : Mathf.signs) {
            Drawf.tri(e.x, e.y, 10.0F/2 * e.fout(), 60.0F/2, e.rotation + 140.0F * (float)i);
        }

    }),

    instSmallTrail = new Effect(30.0F/2, (e) -> {
        for(int i = 0; i < 2; ++i) {
            Draw.color(i == 0 ? Pal.bulletYellowBack : Pal.bulletYellow);
            float m = i == 0 ? 1.0F : 0.5F;
            float rot = e.rotation + 180.0F;
            float w = 15.0F/2 * e.fout() * m;
            Drawf.tri(e.x, e.y, w, (30.0F/2 + Mathf.randomSeedRange((long)e.id, 15.0F/2)) * m, rot);
            Drawf.tri(e.x, e.y, w, 10.0F/2 * m, rot + 180.0F);
        }

        Drawf.light(e.x, e.y, 60.0F/2, Pal.bulletYellowBack, 0.6F * e.fout());
    }),

    instSmallBomb = new Effect(15.0F/2, 100.0F/2, (e) -> {
        Draw.color(Pal.bulletYellowBack);
        Lines.stroke(e.fout() * 4.0F/2);
        Lines.circle(e.x, e.y, 4.0F/2 + e.finpow() * 20.0F/2);

        for(int i = 0; i < 4; ++i) {
            Drawf.tri(e.x, e.y, 6.0F/2, 80.0F/2 * e.fout(), (float)(i * 90 + 45));
        }
        Draw.color();

        for(int i = 0; i < 4; ++i) {
            Drawf.tri(e.x, e.y, 3.0F/2, 30.0F/2 * e.fout(), (float)(i * 90 + 45));
        }

        Drawf.light(e.x, e.y, 150.0F/2, Pal.bulletYellowBack, 0.9F * e.fout());
    });
}

    