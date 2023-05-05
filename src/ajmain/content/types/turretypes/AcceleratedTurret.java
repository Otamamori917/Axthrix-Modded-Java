package ajmain.content.types.turretypes;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.bullet.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class AcceleratedTurret extends ItemTurret{
    public float acceleratedDelay = 120, acceleratedBonus = 1.5f;
    public int acceleratedSteps = 1;
    public float burnoutDelay = 240, cooldownDelay = 120;
    public boolean burnsOut = true;
        public float barX, barY, barStroke, barLength;
    public float barWidth = 1.5f, barHeight = 0.75f;

    public AcceleratedTurret(String name){
        super(name);
    }
    

     drawer = new DrawTurret(){
            TextureRegion barrel, barrelOutline;

            @Override
            public void getRegionsToOutline(Block block, Seq<TextureRegion> out){
                super.getRegionsToOutline(block, out);
                out.add(barrel);
            }

            @Override
            public void load(Block block){
                super.load(block);

                barrel = Core.atlas.find(block.name + "-barrel");
                barrelOutline = Core.atlas.find(block.name + "-barrel-outline");
            }

            @Override
            public void drawTurret(Turret block, TurretBuild build){
                if(!(build instanceof AcceleratedTurretBuild m)) return;

                Vec2 v = Tmp.v1;

                Draw.z(Layer.turret- 0.01f);
                Draw.rect(outline, build.x + m.recoilOffset.x, build.y + m.recoilOffset.y, build.drawrot());
                for(int i = 0; i < 4; i++){
                    Draw.z(Layer.turret - 0.01f);
                    v.trns(m.rotation - 90f, barWidth * Mathf.cosDeg(m.spin - 90 * i), barHeight * Mathf.sinDeg(m.spin - 90 * i)).add(m.recoilOffset);
                    Draw.rect(barrelOutline, m.x + v.x, m.y + v.y, m.drawrot());
                    Draw.z(Layer.turret - 0.005f - Mathf.sinDeg(m.spin - 90 * i) / 1000f);
                    Draw.rect(barrel, m.x + v.x, m.y + v.y, m.drawrot());
                    if(m.heats[i] > 0.001f){
                        Drawf.additive(heat, heatColor.write(Tmp.c1).a(m.heat), m.x + v.x, m.y + v.y, m.drawrot(), Layer.turretHeat);
                    }
                }

                Draw.z(Layer.turret);
                super.drawTurret(block, build);

                if(m.boostf() > 0.0001f){
                    Draw.color(m.barColor());
                    Lines.stroke(barStroke);
                    for(int i = 0; i < 2; i++){
                        v.trns(m.drawrot(), barX * Mathf.signs[i], barY).add(m.recoilOffset);
                        Lines.lineAngle(m.x + v.x, m.y + v.y, m.rotation, barLength * Mathf.clamp(m.boostf()), false);
                    }
                }
            }

            @Override
            public void drawHeat(Turret block, TurretBuild build){
                //Don't
            }
        };
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("aj-firerate-bonus", (AcceleratedTurretBuild entity) -> new Bar(
            () -> Core.bundle.format("bar.aj-firerate-bonus", Strings.autoFixed(entity.accelBoost * 100f, 2)),
            () -> entity.accelCount > acceleratedSteps ? Pal.remove : Pal.techBlue,
            entity::boostf
        ));
    }

    public class AcceleratedTurretBuild extends ItemTurretBuild{
        public float accelBoost, accelCounter;
        public int accelCount;

        @Override
        public void updateTile(){
            super.updateTile();

            if(accelCount > acceleratedSteps){
                accelCounter += edelta();
                if(accelCounter >= cooldownDelay){
                    accelCount = 0;
                    accelBoost = 1;
                    accelCounter %= cooldownDelay;
                }
            }else if(isShooting()){
                accelCounter += edelta(); 
                if(accelCount < acceleratedSteps && accelCounter >= acceleratedDelay){
                    accelBoost += (acceleratedBonus - 1);
                    accelCount++;
                    accelCounter %= acceleratedDelay;
                }else if(burnsOut && accelCounter >= burnoutDelay){
                    accelBoost = 0;
                    accelCount++;
                    accelCounter %= burnoutDelay;
                }
            }else{
                accelCount = 0;
                accelCounter = 0;
                accelBoost = 1;
            }
        }

        @Override
        protected void updateReload(){
            float multiplier = hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
            reloadCounter += delta() * multiplier * accelBoost * baseReloadSpeed();

            reloadCounter = Math.min(reloadCounter, reload);
        }
        
        public float boostf(){
            if(accelCount > acceleratedSteps) return 1 - (accelCounter / cooldownDelay);
            return Mathf.clamp((float)accelCount / acceleratedSteps);
        }
    }
}