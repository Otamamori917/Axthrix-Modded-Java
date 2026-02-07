package axthrix.world.types.entities.comp;

import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.world.util.Extension;
import mindustry.gen.ElevationMoveUnit;
import mindustry.graphics.Layer;

/** @author EyeOfDarkness */
public class ExtensionUnit extends ElevationMoveUnit {
    Seq<Extension> extensions = new Seq<>();
    int group;
    float smoothProgress;

    int extensionsIdx = 0;
    float extensionReload = 0;

    @Override
    public void update(){
        float maxProg = 0f;

        smoothProgress = Math.max(Mathf.lerpDelta(smoothProgress, Math.min(maxProg, 2f), 0.25f), smoothProgress);
        if(smoothProgress > 1) smoothProgress = 1f;
        int ir = 0;
        if(smoothProgress >= (1f - 0.001f)){
            group++;
            smoothProgress = 0f;
        }

        super.update();

        for(Extension t : extensions){
            t.updateTargetPosition(aimX, aimY);
            t.update(this);
        }
        if(isShooting){
            Extension t = extensions.get(extensionsIdx);
            if(extensionReload <= 0f && t.canShoot()){
                t.shoot(aimX, aimY);

                extensionsIdx = (extensionsIdx + 1) % extensions.size;
                extensionReload = 140f / extensions.size + 3f;
            }
        }
        if(extensionReload > 0) extensionReload -= Time.delta;
    }

    @Override
    public void draw(){
        float z = !isAdded() ? Draw.z() : elevation > 0.5f ? (type.lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : type.groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
        Draw.z(z - 0.02f);
        type.applyColor(this);
        Draw.z(z);
        for(Extension t : extensions){
            t.draw(this);
        }
        Draw.reset();

        super.draw();
    }

    @Override
    public void add(){
        if(!isAdded()){
            for(int i = 0; i < 24; i++){
                Extension t = new Extension();
                t.set(this, Mathf.random(360f));
                extensions.add(t);
            }
        }
        super.add();
    }

    @Override
    public boolean serialize(){
        return false;
    }
}
