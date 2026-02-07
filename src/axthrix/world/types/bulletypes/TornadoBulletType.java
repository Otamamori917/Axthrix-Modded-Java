package axthrix.world.types.bulletypes;

import arc.audio.Sound;
import arc.graphics.Color;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Nullable;
import axthrix.AxthrixLoader;
import axthrix.content.AxthrixSounds;
import axthrix.content.AxthrixStatus;
import axthrix.world.util.importedcode.DrawPseudo3d;
import blackhole.entities.abilities.BlackHoleAbility;
import blackhole.entities.bullet.BlackHoleBulletType;
import blackhole.entities.effect.SwirlEffect;
import blackhole.graphics.BHDrawf;
import blackhole.graphics.BHLayer;
import blackhole.graphics.BlackHoleRenderer;
import blackhole.utils.BlackHoleUtils;
import mindustry.Vars;
import mindustry.audio.SoundLoop;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;

import java.util.HashMap;

public class TornadoBulletType extends BulletType {
    public float width;
    public float height;
    public Effect swirlEffect;

    public float damageRadius;
    public float suctionRadius;
    public boolean repel;
    public float force;
    public float scaledForce;
    public float bulletForce;
    public float scaledBulletForce;
    public float bulletDamage;
    @Nullable
    public Color[] colors;
    public float growTime;
    public float shrinkTime;
    public boolean counterClockwise;
    public Sound loopSound;
    public float loopSoundVolume;
    public TornadoBulletType(float life,float Height,float Width){
        hittable = false;
        pierceBuilding = true;
        pierce = true;
        laserAbsorb = false;
        hitSound = Sounds.shield;
        despawnEffect = Fx.none;
        shootEffect = Fx.none;
        hitEffect = Fx.none;
        smokeEffect = Fx.none;
        knockback = 4;
        impact = true;
        status = AxthrixStatus.unrepair;
        swirlEffect = Fx.none;
        statusDuration = 1000*1000;
        keepVelocity = false;
        reflectable = false;
        absorbable = false;
        pierceArmor = true;
        removeAfterPierce = false;
        speed = 1f;
        damageRadius = 10;
        suctionRadius = 200;
        growTime = 100;
        shrinkTime = -1;

        scaledBulletForce = 2;
        bulletForce = 1f;

        scaledForce = 300;
        force = 20;

        lifetime = life;
        height = Height;
        width = Width;
        swirlEffect = new SwirlEffect();


        damageRadius = 6.0F;
        suctionRadius = 160.0F;
        bulletDamage = 0.0F;
        colors = new Color[]{Color.white,Color.lightGray,Color.darkGray,Color.black};
        counterClockwise = false;
        loopSound = Sounds.spellLoop;
        loopSoundVolume = 2.0F;
        hittable = absorbable = false;
        collides = false;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = Fx.none;
        layer = BHLayer.end + 0.01F;
    }

    public void init() {
        super.init();

        if (shrinkTime < 0.0F) {
            shrinkTime = swirlEffect.lifetime;
        }

        Effect var2 = swirlEffect;
        if (var2 instanceof SwirlEffect) {
            SwirlEffect s = (SwirlEffect)var2;
            if (s.maxDst <= 0.0F) {
                s.clip = Math.max(s.clip, width * 2.0F);
            }
        }

    }

    public float continuousDamage() {
        return damage / 2.0F * 60.0F;
    }

    public void init(Bullet b) {
        super.init(b);
        if (loopSound != null) {
            b.data = new SoundLoop(loopSound, loopSoundVolume);
        }

    }

    public void update(Bullet b) {
        if (b.timer(1, 2.0F)) {
            float fout = fout(b);
            BlackHoleUtils.blackHoleUpdate(b.team, b, damageRadius * fout, suctionRadius * fout, b.damage, pierceArmor, buildingDamageMultiplier, bulletDamage * damageMultiplier(b), repel, force, scaledForce, bulletForce, scaledBulletForce);
        }

        if (!Vars.headless) {
            Object var3 = b.data;
            if (var3 instanceof SoundLoop) {
                SoundLoop loop = (SoundLoop)var3;
                loop.update(b.x, b.y, b.isAdded(), fout(b));
            }
        }

        Groups.bullet.each(eb -> {
            float fout = fout(b);
            float d = Mathf.dst(eb.x, eb.y, b.x, b.y);
            if (d < (damageRadius * fout) && eb.type instanceof BlackHoleBulletType) {
                if (eb.team != b.team){
                    b.absorb();
                }
            }
            if (d < (suctionRadius * fout) && !(eb.type instanceof TornadoBulletType)) {
                eb.keepAlive(true);
                if (eb.team != b.team && d < (damageRadius * fout)){
                    BulletType bulletcpy = eb.type.copy();
                    bulletcpy.pierce = false;
                    bulletcpy.pierceBuilding = false;
                    bulletcpy.removeAfterPierce = true;
                    bulletcpy.fragOnHit = true;

                    bulletcpy.create(b,b.team,eb.x, eb.y,Mathf.random(eb.rotation(), b.rotation()));

                    eb.type.fragOnAbsorb = false;
                    eb.absorb();
                }
            }
        });
        Groups.unit.each(eu -> {
            float fout = fout(b);
            float d = Mathf.dst(eu.x, eu.y, b.x, b.y);
            for (int i = 0; i < eu.abilities.length; i++) {
                if (d < (damageRadius * fout) && eu.type.abilities.get(i) instanceof BlackHoleAbility) {
                    if (eu.team != b.team){
                        b.absorb();
                    }
                }
            }
        });

        super.update(b);
    }

    public void draw(Bullet b) {
        DrawPseudo3d.TORNADO(swirlEffect,counterClockwise,width,height,b,colors[0],colors[1],colors[2],colors[3],0.5f,0.5f);
        if (AxthrixLoader.nado3dDebug) {
            DrawPseudo3d.slantTube(b.x, b.y, b.lastX, b.lastY, width, height,colors[0],colors[1],colors[2],colors[3]);
        }
    }

    public void drawLight(Bullet b) {
    }

    public float fout(Bullet b) {
        return Interp.sineOut.apply(Mathf.curve(b.time, 0.0F, growTime) - Mathf.curve(b.time, b.lifetime - shrinkTime, b.lifetime));
    }

    public void despawned(Bullet b) {
        if (despawnHit) {
            hit(b);
        } else {
            createUnits(b, b.x, b.y);
        }

        if (!fragOnHit) {
            createFrags(b, b.x, b.y);
        }

        despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);
        despawnSound.at(b);
    }

    public void removed(Bullet b) {
        super.removed(b);
        Object var3 = b.data;
        if (var3 instanceof SoundLoop) {
            SoundLoop loop = (SoundLoop)var3;
            loop.stop();
        }

    }
}




