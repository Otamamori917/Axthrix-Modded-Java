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
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.graphics.Layer;

public class WarningBulletType extends BasicBulletType {
    public String WarningSprite = "aj-warning";
    public float spriteRadius = 24f;
    public float warningDuration = 120;

    public WarningBulletType() {
        speed = 100f;
        damage = 0f;
        lifetime = 20f;
        width = 0f;
        height = 0f;
        shrinkY = 0f;
        shrinkX = 0f;
        reflectable = absorbable = false;
        collides = false;
        splashDamage = splashDamageRadius = 0f;

        frontColor = Color.valueOf("bf92f9");
        backColor = Color.valueOf("6d56bf");

        despawnEffect = Fx.none;
        hitEffect = Fx.none;
    }

    @Override
    public void hit(Bullet b, float x, float y) {
        super.hit(b, x, y);

        Team bulletTeam = b.team;

        Effect warningField = new Effect(warningDuration, spriteRadius * 2f, e -> {

            float pulse = Mathf.absin(Time.time, 2f, 1f);

            if (!WarningSprite.isEmpty()) {
                Draw.z(Layer.floor);
                Draw.color(Color.white);
                Draw.alpha((1f - e.fin()));
                Draw.rect(WarningSprite, e.x, e.y, spriteRadius * 0.4f * (1f + 0.15f * pulse), spriteRadius * 0.4f * (1f + 0.15f * pulse));
                Draw.z(Layer.effect);
                Draw.reset();
            }

            Draw.reset();

            Groups.unit.each(unit -> {
                if (unit.team != bulletTeam && unit.within(e.x, e.y, spriteRadius)) {
                        unit.apply(StatusEffects.slow, warningDuration);
                }
            });
        });

        warningField.at(x, y);
    }
}

