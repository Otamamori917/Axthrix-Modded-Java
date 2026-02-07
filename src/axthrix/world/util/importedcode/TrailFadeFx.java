package axthrix.world.util.importedcode;


import mindustry.entities.Effect;
import mindustry.graphics.Layer;

import static mindustry.Vars.state;

public class TrailFadeFx{
    public static Effect


    heightTrailFade = new Effect(400f, e -> {
        if(!(e.data instanceof HeightTrail trail)) return;
        //lifetime is how many frames it takes to fade out the trail
        e.lifetime = trail.length * 1.4f;

        if(!state.isPaused()){
            trail.shorten();
        }
        trail.drawCap(e.color, e.rotation);
        trail.draw(e.color, e.rotation);
    }).layer(Layer.flyingUnit + 1.9f);
}
