package axthrix.content.FX;

import arc.Core;
import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.IntMap;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.util.AxShaders;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.world.Block;

import java.util.Arrays;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.*;
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


	public static Effect lightningPart(float xv, float yv, float thick,float layer,float layerOffset,Color color){
		return new Effect(4F, 300.0F, (e) -> {

			float z = Draw.z();
			if(layer >= 1){
				Draw.z(layer + layerOffset);
			}else{
				Draw.z(z + layerOffset);
			}

            float dst = Mathf.dst(e.x, e.y, xv, yv);
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
					nx = xv;
					ny = yv;
				} else {
					float len = (float)(i + 1) * spacing;
					Tmp.v1.setToRandomDirection(rand).scl(range / 4.0F);
					nx = xv + normx * len + Tmp.v1.x;
					ny = yv + normy * len + Tmp.v1.y;
				}
				Lines.linePoint(nx, ny);
			}
			Lines.endLine();
			Draw.z(z);
		}).followParent(true).rotWithParent(true);
	}

	public static final Effect payloadCasing = (new Effect(80.0F, (e) -> {
		Draw.color(Pal.lightOrange, Pal.lightishGray, Pal.lightishGray, e.fin());
		Draw.alpha(e.fout(0.5F));
		float rot = Math.abs(e.rotation) + 90.0F;
		int i = -Mathf.sign(e.rotation);
		float len = (4.0F + e.finpow() * 9.0F) * (float)i;
		float lr = rot + Mathf.randomSeedRange((long)(e.id + i + 6), 20.0F * e.fin()) * (float)i;
		Draw.rect(Core.atlas.find("aj-empty-1m-caliber"), e.x + Angles.trnsx(lr, len) + Mathf.randomSeedRange((long)(e.id + i + 7), 3.0F * e.fin()), e.y + Angles.trnsy(lr, len) + Mathf.randomSeedRange((long)(e.id + i + 8), 3.0F * e.fin()), 16F, 14, rot + e.fin() * 50.0F * (float)i);
	})).layer(100.0F);

	public static final Effect LightPulse = (new Effect(280.0F, 100.0F, (e) -> {
		color(new Color(255F, 255F, 255F, 0.10F));
		stroke(e.fin() * 2.0F);
		circle(e.x, e.y, 4.0F + e.fout() * 100.0F);
		Fill.circle(e.x, e.y, e.fin() * 20.0F);
		color();
		for (int i = 0; i < 4; i++) {
			circle(e.x, e.y, i * 3.5F + e.fout() * 20.0F);
		}
		Fill.circle(e.x, e.y, e.fin() * 10.0F);
		Drawf.light(e.x, e.y, e.fin() * 20.0F, Pal.heal, 0.7F);
	})).followParent(true).rotWithParent(true);

	public static final Effect GoldShine = new Effect(10.0F, (e) -> {
		Draw.color(Color.gold);
		for (int i = 0; i < 4; i++) {
			Fill.square(e.x + Mathf.random(-i, i), e.y + Mathf.random(-i, i), e.fslope()/2, 45.0F * i);
		}
	});

	public static Effect lightningArc = new Effect(20f, e -> {
		Draw.z(29f);
		Draw.color(Color.cyan, Color.white, e.fin());
		Draw.alpha(1f - e.fin());

		Lines.stroke(2f * (1f - e.fin()));

		float x1 = e.x;
		float y1 = e.y;
		float x2 = e.data instanceof float[] ? ((float[])e.data)[0] : e.x;
		float y2 = e.data instanceof float[] ? ((float[])e.data)[1] : e.y;

		int segments = 8;
		for(int i = 0; i < segments; i++){
			float progress = (float)i / segments;
			float nextProgress = (float)(i + 1) / segments;

			Tmp.v1.set(x2, y2).sub(x1, y1);
			float perpAngle = Tmp.v1.angle() + 90f;

			float offset1 = Mathf.random(-3f, 3f);
			float offset2 = Mathf.random(-3f, 3f);

			Tmp.v2.set(x1, y1).lerp(x2, y2, progress).add(Tmp.v1.setAngle(perpAngle).scl(offset1));
			Tmp.v3.set(x1, y1).lerp(x2, y2, nextProgress).add(Tmp.v1.setAngle(perpAngle).scl(offset2));

			Lines.line(Tmp.v2.x, Tmp.v2.y, Tmp.v3.x, Tmp.v3.y);
		}
	});

	public static Effect lightArc = new Effect(25f, e -> {
		float x2 = e.data instanceof float[] ? ((float[])e.data)[0] : e.x;
		float y2 = e.data instanceof float[] ? ((float[])e.data)[1] : e.y;

		Draw.z(Layer.effect);


		Draw.color(Color.white);
		Draw.alpha(0.6f * e.fout());
		Lines.stroke(5f * e.fout());
		Lines.line(e.x, e.y, x2, y2);

		// 2. The Main Beam (Bright white/yellow core)
		Draw.color(Color.white, Color.lightGray, e.fin());
		Draw.alpha(e.fout());
		Lines.stroke(2f * e.fout());
		Lines.line(e.x, e.y, x2, y2);

		// 3. Impact Flares (Circles at start and end)
		Fill.circle(e.x, e.y, 4f * e.fout());
		Fill.circle(x2, y2, 6f * e.fout());

		// 4. "Photon" Particles along the line
		int particles = 6;
		for(int i = 0; i < particles; i++){
			float progress = (i / (float)particles);
			// Particles move slightly toward the target
			float px = Mathf.lerp(e.x, x2, (progress + e.fin()) % 1f);
			float py = Mathf.lerp(e.y, y2, (progress + e.fin()) % 1f);

			Draw.color(Color.white);
			Fill.square(px, py, 1.5f * e.fout(), 45f);
		}
	});

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
	
	public static Effect circleOut(float lifetime, float radius, float thick,float layer,Color color){
		return new Effect(lifetime, radius * 2f, e -> {
			float z = Draw.z();
			Draw.z(layer);
			Draw.color(color, Color.white, e.fout() * 0.7f);
			Lines.stroke(thick * e.fout());
			Lines.circle(e.x, e.y, radius * e.fin(Interp.pow3Out));
			Draw.z(z);
		});
	}


	public static Effect crystalShatter(Block block, Color iceColor) {
		float fullSize = block.size * 8f;

		return new Effect(40f, fullSize * 4f, e -> {
			float z = Draw.z();
			Draw.z(Layer.effect);

			// 1. THE FLASH: A quick white "impact" circle
			Draw.color(Color.white, iceColor, e.fin());
			Lines.stroke(2f * e.fout());
			Lines.poly(e.x, e.y, 6, fullSize * e.fin(Interp.pow3Out));

			// 2. THE GLASS PANES: 4 big, chunky shards
			for(int i = 0; i < 4; i++){
				float angle = (i * 90f) + Mathf.randomSeed(i, -20f, 20f);
				float move = e.fin(Interp.pow2Out) * 45f * block.size;
				float rotation = i * 45f + e.fin() * 100f;

				float px = e.x + Angles.trnsx(angle, move);
				float py = e.y + Angles.trnsy(angle, move);

				// Shard Color: Block color mixed with ice
				Draw.color(block.mapColor, iceColor, 0.4f);
				Draw.alpha(e.fout() * 0.5f); // Semi-transparent glass

				// Random jagged triangle
				Fill.poly(px, py, 3, fullSize * 0.6f, rotation);

				// Shard Shine: A white line on one edge of the glass
				Draw.color(Color.white);
				Draw.alpha(e.fout());
				Lines.stroke(1f);
				Lines.lineAngle(px, py, rotation, fullSize * 0.4f);
			}

			// 3. THE FROST DUST: Tiny "glitter" particles
			Draw.color(iceColor, Color.white, e.fin());
			for(int i = 0; i < 10; i++){
				float a = Mathf.randomSeed(i + 99, 360f);
				float d = e.fin(Interp.pow5Out) * 60f * block.size;
				float x = e.x + Angles.trnsx(a, d);
				float y = e.y + Angles.trnsy(a, d);

				Fill.rect(x, y, 1.5f, 1.5f, a); // Tiny square dust
			}

			Draw.z(z);
			Draw.reset();
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














