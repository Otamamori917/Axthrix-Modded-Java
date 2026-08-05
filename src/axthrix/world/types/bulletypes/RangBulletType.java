package axthrix.world.types.bulletypes;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.pooling.Pools;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;

public class RangBulletType extends TemperatureBulletType {
    public float baseHitSize = 4f;
    public float maxHeatHitSize = 16f;
    public float returnHeatAmount = 5f;
    public float homingTurnSpeed = 0.12f;
    public float returnTrigger = 0.35f, endTrigger = 0.95f;

    public RangBulletType() {
        super();
        pierce = true;
        pierceBuilding = true;
    }

    public RangBulletType(float speed, float damage) {
        super(speed, damage);
        pierce = true;
        pierceBuilding = true;
    }
    @Override
    public void update(Bullet b) {
        if (b.owner instanceof Unit owner) {
            float liveTemp = TemperatureLogic.getTemperatureUnit(owner);
            float alpha = Mathf.clamp(liveTemp / 100f);

            b.hitSize = Mathf.lerp(baseHitSize, maxHeatHitSize, alpha);

            if (b.time >= b.lifetime * returnTrigger) {
                if (!owner.dead && owner.isValid()) {
                    float targetAngle = Mathf.angle(owner.x - b.x, owner.y - b.y);
                    Vec2 targetVelocity = Pools.obtain(Vec2.class, Vec2::new);
                    targetVelocity.set(speed, 0f).setAngle(targetAngle);

                    b.vel.lerp(targetVelocity, homingTurnSpeed * arc.util.Time.delta);
                    Pools.free(targetVelocity);

                    float distToOwner = Mathf.dst(b.x, b.y, owner.x, owner.y);
                    if (b.time > b.lifetime * endTrigger && distToOwner <= owner.hitSize + b.hitSize) {
                        if (returnHeatAmount > 0.01f) {
                            TemperatureLogic.applyTemperatureUnit(owner, returnHeatAmount);
                        }
                        Fx.generatespark.at(b.x, b.y);
                        b.remove();
                        return;
                    }
                }
            }
        }

        super.update(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        if (entity == b.owner) return;
        super.hitEntity(b, entity, health);
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        if (build != null && b.owner instanceof Unit owner && build == owner.buildOn()) return;
        super.hitTile(b, build, x, y, initialHealth, direct);
    }
}

