package axthrix.content.FX;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.struct.IntMap;
import mindustry.content.Items;
import mindustry.entities.Effect;


import java.util.Arrays;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.line;
import static arc.graphics.g2d.Lines.stroke;

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














