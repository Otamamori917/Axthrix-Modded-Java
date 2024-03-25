package axthrix.content.FX;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Interp;
import arc.struct.IntMap;
import mindustry.entities.Effect;


import java.util.Arrays;

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
}














