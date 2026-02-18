package axthrix.world.util;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.entities.part.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.PayloadAmmoTurret.*;
import mindustry.world.draw.*;

public class DrawIPayloadAmmo extends RegionPart{
    public PartProgress matProgress = PartProgress.warmup;
    public boolean materialize = true, fade = true;
    public float layer = Layer.turret - 0.005f;
    public Building parent;

    public DrawIPayloadAmmo() {
        suffix = "aj-nothing";
        outline = false;
    }

    @Override
    public void draw(PartParams partParams){
        PayloadTurretBuild tb = (PayloadTurretBuild)parent;

        UnlockableContent pAmmo = tb.currentAmmo();
        if(pAmmo == null) return;

        float tProgress = tb.progress();
        var params = DrawPart.params.set(parent.warmup(), 1f - tProgress, 1f - tProgress, tb.heat, tb.curRecoil, tb.charge, tb.x + tb.recoilOffset.x, tb.y + tb.recoilOffset.y, tb.rotation);


        float rx = params.x + Tmp.v1.x, ry = params.y + Tmp.v1.y, rot = params.rotation - 90f + rotation;
        if(materialize){
            float matProg = matProgress.getClamp(params);;
            Draw.draw(layer, () -> {
                AxDrawf.materialize(rx, ry, pAmmo.fullIcon, tb.team.color, rot, 0.1f, matProg, -Time.time / 4f);
            });
        }else{
            Draw.z(layer);
            if(fade) Draw.alpha(matProgress.getClamp(params));
            Draw.rect(pAmmo.fullIcon, rx, ry, rot);
            Draw.alpha(1f);
        }
        Draw.scl();
    }
}
