package axthrix.content.FX;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.IntMap;
import arc.util.Tmp;
import mindustry.content.Items;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;


import java.util.Arrays;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
import static arc.scene.actions.Actions.alpha;
import static mindustry.Vars.renderer;
import static mindustry.content.Fx.rand;

public class AxthrixFfx{
	public static final IntMap<Effect> same = new IntMap<>();

	public static int hash(String m, Color c){
		return Arrays.hashCode(new int[]{m.hashCode(), c.hashCode()});
	}
	
	public static Effect get(String m, Color c, Effect effect){
		int hash = hash(m, c);
		Effect or = same.get(hash);
		if(or == null)same.put(hash, effect);
		return or == null ? effect : or;
	}


		public static Effect lightningPart(float xv, float yv, float thick,Color color){
		return new Effect(4F, 300.0F, (e) -> {

			float var14 = xv;
			float ty = yv;
			float dst = Mathf.dst(e.x, e.y, var14, ty);
			Tmp.v1.set(xv,yv).sub(e.x, e.y).nor();
			float normx = Tmp.v1.x;
			float normy = Tmp.v1.y;
			float range = 6.0F;
			int links = Mathf.ceil(dst / range);
			float spacing = dst / (float)links;
			Lines.stroke(thick * e.fout());
			Draw.color(color, e.color, e.fin());
			Lines.beginLine();
			Lines.linePoint(e.x, e.y);
			rand.setSeed(e.id);

			for(int i = 0; i < links; ++i) {
				float nx;
				float ny;
				if (i == links - 1) {
					nx = var14;
					ny = ty;
				} else {
					float len = (float)(i + 1) * spacing;
					Tmp.v1.setToRandomDirection(rand).scl(range / 4.0F);
					nx = xv + normx * len + Tmp.v1.x;
					ny = yv + normy * len + Tmp.v1.y;
				}
				Lines.linePoint(nx, ny);
			}
			Lines.endLine();
		}).followParent(true).rotWithParent(true);
	}

	public static Effect staticShock(float radius) {
		return new Effect(80f, 100f, e -> {
			color(Color.white, e.color, e.fin());

			for(int i = 2; i >= 0; i--){
				float s = 4.5f * (i / 2f + 1f) * e.fout();
				color(i == 0 ? Color.white : e.color);
				Draw.alpha((i == 2 ? 0.5f : 1f) * e.fout());
				for(int j = 0; j < 3; j++){
					Drawf.tri(e.x, e.y, 2f * s, (s + radius + Mathf.randomSeed(e.id - j, radius*2)) * e.fout(), e.rotation + Mathf.randomSeedRange(e.id + j, 80f) + 180f);
				}
			}

			Draw.z(Layer.effect - 0.001f);
			stroke(3f * e.fout(), e.color);
			float r = radius * e.finpow();
			Fill.light(e.x, e.y, circleVertices(r), r, Tmp.c4.set(e.color).a(0f), Tmp.c3.set(e.color).a(e.fout()));
			circle(e.x, e.y, r);
			if(renderer.lights.enabled()) Drawf.light(e.x, e.y, r * 3.5f, e.color, e.fout(0.5f));
		}).layer(Layer.effect + 0.001f);
	}
	
	public static Effect circleOut(float lifetime, float radius, float thick,Color color){
		return new Effect(lifetime, radius * 2f, e -> {
			Draw.color(color, Color.white, e.fout() * 0.7f);
			Lines.stroke(thick * e.fout());
			Lines.circle(e.x, e.y, radius * e.fin(Interp.pow3Out));
		});
	}

	//apexus trail effects
	public static Effect energyRoundRadiate(Color color){
		return new Effect(40f,e -> {
			color(color);
			float waves = 3f;
			float x1 = e.x + Mathf.sin(e.rotation / 180 * Mathf.pi) * Mathf.sin(e.fin() * waves * Mathf.pi) * 8;
			float y1 = e.y - Mathf.cos(e.rotation / 180 * Mathf.pi) * Mathf.sin(e.fin() * waves * Mathf.pi) * 9;
			float x2 = e.x - Mathf.sin(e.rotation / 180 * Mathf.pi) * Mathf.sin(e.fin() * waves * Mathf.pi) * 8;
			float y2 = e.y + Mathf.cos(e.rotation / 180 * Mathf.pi) * Mathf.sin(e.fin() * waves * Mathf.pi) * 9;
			float fin = (e.time + 1) / e.lifetime;
			float xn = e.x - Mathf.cos(e.rotation / 180 * Mathf.pi) * 10;
			float yn = e.y - Mathf.sin(e.rotation / 180 * Mathf.pi) * 6;
			float xn1 = xn + Mathf.sin(e.rotation / 180 * Mathf.pi) * Mathf.sin(fin * waves * Mathf.pi) * 14;
			float yn1 = yn - Mathf.cos(e.rotation / 180 * Mathf.pi) * Mathf.sin(fin * waves * Mathf.pi) * 6;
			float xn2 = xn - Mathf.sin(e.rotation / 180 * Mathf.pi) * Mathf.sin(fin * waves * Mathf.pi) * 14;
			float yn2 = yn + Mathf.cos(e.rotation / 180 * Mathf.pi) * Mathf.sin(fin * waves * Mathf.pi) * 6;
			stroke(e.fout() * 2);
			line(x1, y1, xn1, yn1);
			line(x2, y2, xn2, yn2);
			line(x1, y2, xn1, yn2);
			line(x2, y1, xn2, yn1);
		});
	}
	public static Effect solidRoundRadiate(Color color){
		return new Effect(50f,e -> {
			color(color);
			float waves = 4f;
			float x1 = e.x + Mathf.sin(e.rotation/180*Mathf.pi) * Mathf.sin(e.fin()*waves *Mathf.pi) * 6;
			float y1 = e.y - Mathf.cos(e.rotation/180*Mathf.pi) * Mathf.sin(e.fin()*waves *Mathf.pi) * 6;
			float x2 = e.x - Mathf.sin(e.rotation/180*Mathf.pi) * Mathf.sin(e.fin()*waves *Mathf.pi) * 6;
			float y2 = e.y + Mathf.cos(e.rotation/180*Mathf.pi) * Mathf.sin(e.fin()*waves *Mathf.pi) * 12;
			float fin = (e.time+1)/e.lifetime;
			float xn = e.x - Mathf.cos(e.rotation/180*Mathf.pi) * 8;
			float yn = e.y - Mathf.sin(e.rotation/180*Mathf.pi) * 10;
			float xn1 = xn + Mathf.sin(e.rotation/180*Mathf.pi) * Mathf.sin(fin*waves *Mathf.pi) * 12;
			float yn1 = yn - Mathf.cos(e.rotation/180*Mathf.pi) * Mathf.sin(fin*waves *Mathf.pi) * 12;
			float xn2 = xn - Mathf.sin(e.rotation/180*Mathf.pi) * Mathf.sin(fin*waves *Mathf.pi) * 12;
			float yn2 = yn + Mathf.cos(e.rotation/180*Mathf.pi) * Mathf.sin(fin*waves *Mathf.pi) * 6;
			stroke(e.fout()*3);
			line(x1, y1, xn1, yn1);
			line(x2, y2, xn2, yn2);
		});
	}
}














