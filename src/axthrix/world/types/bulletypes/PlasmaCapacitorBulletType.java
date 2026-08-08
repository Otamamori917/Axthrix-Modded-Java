package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.part.DrawPart;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;

public class PlasmaCapacitorBulletType extends BasicBulletType {
    public float chargeDuration = 70f;
    public float heatSiphonPerFrame = 1.8f;
    public float damagePerHeatStored = 5.2f;
    public float radiusPerHeatStored = 1.4f;

    public float baseLaunchSpeed = 10f;
    public float chargedLaunchSpeed = 6f;
    public float spawnOffset = -6f;

    public Effect siphonEffect = Fx.fire;

    public PlasmaCapacitorBulletType() {
        super(0.01f, 20f);
        lifetime = 180f;
        hitSize = 12f;
        despawnEffect = Fx.none;
        hitEffect = Fx.massiveExplosion;
        pierce = true;
        pierceBuilding = true;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        for (DrawPart part : parts) {
            part.load(null);
        }
    }

    @Override
    public void update(Bullet b) {
        if (b.owner instanceof Unit owner) {
            if (b.data == null) {
                b.data = new float[]{0f, 0f, 0f};
            }

            float[] state = (float[]) b.data;
            boolean isLaunched = state[2] > 0.5f;
            float currentHeat = TemperatureLogic.getTemperatureUnit(owner);

            if (!isLaunched) {
                if (currentHeat > 0.01f && b.time < chargeDuration) {
                    float siphonAmount = Math.min(currentHeat, heatSiphonPerFrame * arc.util.Time.delta);
                    TemperatureLogic.applyTemperatureUnit(owner, -siphonAmount);

                    state[0] += siphonAmount * damagePerHeatStored;
                    state[1] += siphonAmount * radiusPerHeatStored;

                    b.hitSize = Mathf.clamp(12f + (state[0] * 0.05f), 12f, 48f);
                    currentHeat = TemperatureLogic.getTemperatureUnit(owner);

                    if (Mathf.chance(0.35f)) {
                        float spawnRadius = Mathf.random(14f, 45f);
                        float randomAngle = Mathf.random(360f);
                        float px = b.x + Mathf.cosDeg(randomAngle) * spawnRadius;
                        float py = b.y + Mathf.sinDeg(randomAngle) * spawnRadius;
                        float inwardAngle = Angles.angle(px, py, b.x, b.y);

                        siphonEffect.at(px, py, inwardAngle, Color.valueOf("de9458"));
                    }
                }

                if (b.time >= chargeDuration || currentHeat <= 0.01f) {
                    state[2] = 1f;

                    b.rotation(owner.rotation);

                    float chargeFraction = Mathf.clamp(b.time / chargeDuration);
                    float finalLaunchSpeed = Mathf.lerp(baseLaunchSpeed, chargedLaunchSpeed, chargeFraction);
                    b.vel.set(finalLaunchSpeed, 0f).setAngle(owner.rotation);

                    owner.unapply(StatusEffects.unmoving);
                    Fx.lightningShoot.at(b.x, b.y, b.rotation());
                } else {
                    b.vel.set(0f, 0f);
                    b.set(owner.x + Mathf.cosDeg(owner.rotation) * (owner.hitSize + spawnOffset), owner.y + Mathf.sinDeg(owner.rotation) * (owner.hitSize + spawnOffset));
                }
            }
        }

        super.update(b);
    }
    @Override
    public void draw(Bullet b) {
        super.draw(b);

        if (parts.size > 0 && b.owner instanceof Unit owner) {
            float[] state = b.data instanceof float[] ? (float[]) b.data : new float[]{0f, 0f, 0f};
            float maxPotentialBonusDamage = 100f * damagePerHeatStored;

            var param = DrawPart.params.set(
                    0f,
                    state[2] > 0.5f ? 1f : 0f,
                    maxPotentialBonusDamage > 0.01f ? Mathf.clamp(state[0] / maxPotentialBonusDamage) : 0f,
                    TemperatureLogic.getTemperature(owner) / 100f,
                    0f,
                    Mathf.clamp(b.time / chargeDuration),
                    b.x, b.y, b.rotation()
            );
            DrawPart.params.life = b.fin();

            for (DrawPart part : parts) {
                part.draw(param);
            }
        }
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        explodePayload(b);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        super.hitTile(b, build, x, y, initialHealth, direct);
        explodePayload(b);
    }

    private void explodePayload(Bullet b) {
        float bonusDamage = 0f;
        float bonusRadius = 0f;

        if (b.data instanceof float[] state) {
            bonusDamage = state[0];
            bonusRadius = state[1];
        }

        float finalSplashRadius = Mathf.clamp(24f + bonusRadius, 24f, 180f);
        float finalTotalDamage = b.damage + bonusDamage;

        mindustry.entities.Damage.damage(b.team, b.x, b.y, finalSplashRadius, finalTotalDamage, true, true);
        Fx.blastExplosion.at(b.x, b.y);
        b.remove();
    }
}
