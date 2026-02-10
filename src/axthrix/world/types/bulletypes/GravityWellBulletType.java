package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.AxthrixSounds;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;

public class GravityWellBulletType extends BasicBulletType {
    public float gravityRadius = 24f;
    //force at which a unit is pulled by
    public float pullStrength = 0.05f;
    public float gravityDuration = 180f;
    // Damage per tick while in field
    public float continuousDamage = 5f;
    // Extra damage multiplier for flying units
    public float flyingDamageMultiplier = 2f;
    public String craterSprite = "aj-gravity-well";

    public GravityWellBulletType() {
        speed = 100f;
        damage = 0f;
        lifetime = 20f;
        width = 0f;
        height = 0f;
        shrinkY = 0f;
        shrinkX = 0f;
        reflectable = absorbable = false;
        collides = false;
        hitSize = 8f;
        splashDamage = 60f;
        splashDamageRadius = 40f;

        frontColor = Color.valueOf("bf92f9");
        backColor = Color.valueOf("6d56bf");

        despawnEffect = Fx.none;
        hitEffect = Fx.massiveExplosion;
        hitSound = AxthrixSounds.slam;
    }

    @Override
    public void hit(Bullet b, float x, float y) {
        super.hit(b, x, y);

        Team bulletTeam = b.team;

        Effect gravityField = new Effect(gravityDuration, gravityRadius * 2f, e -> {

            float pulse = Mathf.absin(Time.time, 2f, 1f);

            if (!craterSprite.isEmpty()) {
                Draw.z(Layer.floor);
                Draw.color(Color.white);
                Draw.alpha((1f - e.fin()));
                Draw.rect(craterSprite, e.x, e.y, gravityRadius * 2f, gravityRadius * 2f);
                Draw.z(Layer.effect);
                Draw.reset();
            }

            Draw.color(Color.valueOf("6d56bf"));
            Draw.alpha(0.6f * (1f - e.fin()) * (0.6f + 0.4f * pulse));
            Fill.circle(e.x, e.y, gravityRadius * 0.4f * (1f + 0.15f * pulse));

            Draw.color(Color.valueOf("bf92f9"));
            Draw.alpha(0.8f * (1f - e.fin()) * pulse);
            Fill.circle(e.x, e.y, gravityRadius * 0.2f * (1f + 0.2f * pulse));


            Draw.color(Color.valueOf("bf92f9"));
            Draw.alpha(0.4f * (1f - e.fin()) * pulse);
            Lines.stroke(2f);
            Lines.circle(e.x, e.y, gravityRadius * 0.6f * (1f + 0.05f * pulse));

            Draw.reset();

            Groups.unit.each(unit -> {
                if (unit.team != bulletTeam && unit.within(e.x, e.y, gravityRadius)) {
                    float angle = Angles.angle(unit.x, unit.y, e.x, e.y);
                    float dst = Mathf.dst(unit.x, unit.y, e.x, e.y);
                    float strength = pullStrength * (1f - dst / gravityRadius);


                    unit.vel.add(Tmp.v1.trns(angle, strength));

                    if (unit.isFlying()) {
                        unit.apply(StatusEffects.unmoving, gravityDuration/2);
                        if (Mathf.chance(0.05)) {
                            unit.damage(continuousDamage * flyingDamageMultiplier);
                        }
                    } else {

                        if (Mathf.chance(0.05)) {
                            unit.damage(continuousDamage);
                        }
                    }
                }
            });
        });

        gravityField.at(x, y);
    }
}