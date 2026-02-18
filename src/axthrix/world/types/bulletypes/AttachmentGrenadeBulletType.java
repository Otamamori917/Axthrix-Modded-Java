package axthrix.world.types.bulletypes;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.AxthrixSounds;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.ArtilleryBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class AttachmentGrenadeBulletType extends ArtilleryBulletType {

    private static final ObjectMap<Integer, GrenadeData> grenadeMap = new ObjectMap<>();

    public float attachDuration = 180f;
    public Effect attachEffect = Fx.hitBulletColor;
    public Color grenadeColor = Color.red;

    public float detonationDamage = 100f;
    public float detonationRadius = 40f;
    public Effect detonationEffect = Fx.blastExplosion;
    public Sound dentonationSound = AxthrixSounds.nadeblast;

    public AttachmentGrenadeBulletType(){

        speed = 4f;
        lifetime = 60f;
        damage = 10f;
        splashDamage = 20f;
        splashDamageRadius = 20f;
        collides = collidesAir = true;
        scaleLife = false;


        hitEffect = attachEffect;
        despawnEffect = Fx.none;
        sprite = "aj-nade";
        backSprite = "aj-nade-back";
    }



    @Override
    public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float health){
        super.hitEntity(b, entity, health);


        if(entity instanceof Unit){
            Unit target = (Unit)entity;


            float worldHitAngle = Mathf.angle(b.x - target.x, b.y - target.y);
            float localAngle = worldHitAngle - target.rotation;
            float hitDistance = Mathf.dst(b.x, b.y, target.x, target.y);

            attachGrenade(target, b.team, localAngle, hitDistance, b.rotation());
        }
    }

    public void attachGrenade(Unit target, mindustry.game.Team team, float localAngle, float distance, float bulletRotation){
        int unitId = target.id;

        GrenadeData data = grenadeMap.get(unitId);
        if(data != null){
            data.grenades.add(new GrenadeInstance(
                    localAngle, distance, bulletRotation,
                    this.backRegion, this.frontRegion,
                    this.width, this.height,
                    this.frontColor, this.backColor,
                    target.rotation
            ));
            data.grenadeTimer = attachDuration;
            data.team = team;
        }else{
            GrenadeData newData = new GrenadeData();
            newData.grenades.add(new GrenadeInstance(
                    localAngle, distance, bulletRotation,
                    this.backRegion, this.frontRegion,
                    this.width, this.height,
                    this.frontColor, this.backColor,
                    target.rotation
            ));
            newData.grenadeTimer = attachDuration;
            newData.team = team;
            grenadeMap.put(unitId, newData);
        }

        attachEffect.at(target.x, target.y, grenadeColor);
    }

    public static void updateGrenades(Unit unit){
        if(!unit.isValid()) {
            grenadeMap.remove(unit.id);
            return;
        }

        GrenadeData data = grenadeMap.get(unit.id);
        if(data != null){
            if(!Vars.state.isPaused()){
                data.grenadeTimer -= Time.delta;


                for(GrenadeInstance grenade : data.grenades){
                    if(data.grenadeTimer <= 30f && !grenade.falling){

                        grenade.falling = true;
                        grenade.fallTimer = 0f;
                    }

                    if(grenade.falling){
                        grenade.fallTimer += Time.delta;
                    }
                }


                if(data.grenadeTimer <= 0){
                    grenadeMap.remove(unit.id);
                }
            }
        }
    }

    public static void drawGrenades(Unit unit){
        GrenadeData data = grenadeMap.get(unit.id);

        if(data != null){
            Draw.z(Layer.flyingUnit + 1);

            for(GrenadeInstance grenade : data.grenades){

                float worldAngle = grenade.localAngle + unit.rotation;
                Tmp.v1.trns(worldAngle, grenade.distance);
                float baseX = unit.x + Tmp.v1.x;
                float baseY = unit.y + Tmp.v1.y;


                float fallProgress = grenade.falling ? Mathf.clamp(grenade.fallTimer / 30f) : 0f;


                float scale = 1f - fallProgress * 0.8f;


                float fallDistance = fallProgress * fallProgress * 5f;
                Tmp.v2.trns(worldAngle, fallDistance);
                float grenadeX = baseX + Tmp.v2.x;
                float grenadeY = baseY + Tmp.v2.y;

                float alpha = 1f - fallProgress;


                float grenadeRotation = grenade.bulletRotation + (unit.rotation - grenade.initialUnitRotation);


                float drawWidth = (grenade.width *4) * scale * Draw.scl;
                float drawHeight = (grenade.height *4) * scale * Draw.scl;


                if(grenade.backRegion != null && grenade.backRegion.found()){
                    Draw.color(grenade.backColor, alpha);
                    Draw.rect(grenade.backRegion, grenadeX, grenadeY,
                            drawWidth, drawHeight, grenadeRotation - 90f);
                }


                if(grenade.spriteRegion != null && grenade.spriteRegion.found()){
                    Draw.color(grenade.frontColor, alpha);
                    Draw.rect(grenade.spriteRegion, grenadeX, grenadeY,
                            drawWidth, drawHeight, grenadeRotation - 90f);
                }

                if(data.grenadeTimer < 60f && !grenade.falling){
                    float blink = Mathf.absin(Time.time, 4f, 1f);
                    Draw.color(Color.orange, alpha * blink * 0.5f);
                    Draw.rect(grenade.spriteRegion, grenadeX, grenadeY,
                            drawWidth * 1.2f, drawHeight * 1.2f, grenadeRotation - 90f);
                }
            }

            Draw.reset();
        }
    }

    public static int getGrenadeCount(Unit target){
        GrenadeData data = grenadeMap.get(target.id);
        return data != null ? data.grenades.size : 0;
    }

    public static mindustry.game.Team getGrenadeTeam(Unit target){
        GrenadeData data = grenadeMap.get(target.id);
        return data != null ? data.team : null;
    }

    public static void detonateGrenades(Unit target, AttachmentGrenadeBulletType grenadeType){
        GrenadeData data = grenadeMap.get(target.id);

        if(data != null){
            for(int i = 0; i < data.grenades.size; i++){
                grenadeType.detonationEffect.at(target.x, target.y, grenadeType.grenadeColor);
                grenadeType.dentonationSound.at(target.x, target.y);
            }

            float totalDamage = grenadeType.detonationDamage * data.grenades.size;
            target.damage(totalDamage);

            mindustry.entities.Damage.damage(
                    data.team,
                    target.x,
                    target.y,
                    grenadeType.detonationRadius,
                    totalDamage * 0.5f,
                    true,
                    true
            );
            grenadeMap.remove(target.id);
        }
    }

    public static class GrenadeInstance {
        public float localAngle;
        public float distance;
        public float bulletRotation;
        public float initialUnitRotation;
        public boolean falling = false;
        public float fallTimer = 0f;
        public TextureRegion spriteRegion;
        public TextureRegion backRegion;
        public float width, height;
        public Color frontColor, backColor;

        public GrenadeInstance(float localAngle, float distance, float bulletRotation,
                               TextureRegion back, TextureRegion sprite,
                               float width, float height,
                               Color frontColor, Color backColor,
                               float initialUnitRotation){
            this.localAngle = localAngle;
            this.distance = distance;
            this.bulletRotation = bulletRotation;
            this.backRegion = back;
            this.spriteRegion = sprite;
            this.width = width;
            this.height = height;
            this.frontColor = frontColor;
            this.backColor = backColor;
            this.initialUnitRotation = initialUnitRotation;
        }
    }

    public static class GrenadeData {
        public arc.struct.Seq<GrenadeInstance> grenades = new arc.struct.Seq<>();
        public float grenadeTimer = 0f;
        public mindustry.game.Team team;
    }
}