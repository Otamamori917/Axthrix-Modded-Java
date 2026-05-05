package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.types.block.defense.TransformingTurret;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Building;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class GrabBulletType extends BulletType {
    private static final ObjectMap<Integer, GrabData> grabbedUnits = new ObjectMap<>();

    public float grabDuration = 180f;
    public float holdDamage = 5f;
    public float holdDamageInterval = 10f;
    public float holdDistance = 0f;

    public float maxSizeRatio = 1.5f;
    public boolean canGrabFlying = false;

    public Effect grabEffect = Fx.hitBulletColor;
    public Effect holdEffect = Fx.none;
    public float holdEffectInterval = 15f;
    public Effect failEffect = Fx.hitBulletSmall;
    public Color grabColor = Color.red;

    public GrabBulletType(){
        speed = 0.01f;
        lifetime = 5f;
        damage = 50f;
        collides = collidesAir = collidesGround = true;
        keepVelocity = false;
        hittable = false;
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health){
        super.hitEntity(b, entity, health);

        if(!(entity instanceof Unit target)) return;

        Unit attackerUnit = null;
        Building attackerBuilding = null;

        if(b.owner instanceof Unit u) {
            attackerUnit = u;
        } else if(b.owner instanceof Building build) {
            attackerBuilding = build;
        } else {
            return;
        }

        if(target.team == b.team) return;

        if(!canGrabFlying && target.isFlying()){
            failEffect.at(target.x, target.y);
            return;
        }

        float attackerSize = attackerUnit != null ? attackerUnit.hitSize : (attackerBuilding != null ? attackerBuilding.hitSize() : 8f);
        if(target.hitSize > attackerSize * maxSizeRatio){
            failEffect.at(target.x, target.y);
            return;
        }

        if(isGrabbed(target)) return;

        grabUnit(target, attackerUnit, attackerBuilding, b.team);
    }

    public void grabUnit(Unit target, @Nullable Unit attackerUnit, @Nullable Building attackerBuilding, mindustry.game.Team team){
        GrabData data = new GrabData();
        data.target = target;
        data.attackerUnit = attackerUnit;
        data.attackerBuilding = attackerBuilding;
        data.team = team;
        data.grabTimer = grabDuration;
        data.holdDamage = this.holdDamage;
        data.holdDamageInterval = this.holdDamageInterval;
        data.holdDistance = this.holdDistance;
        data.holdEffect = this.holdEffect;
        data.holdEffectInterval = this.holdEffectInterval;
        data.initialX = target.x;
        data.initialY = target.y;
        data.grabDuration = this.grabDuration;

        grabbedUnits.put(target.id, data);

        target.damage(damage);
        grabEffect.at(target.x, target.y, grabColor);
    }

    public static void updateGrabs(){
        for(var entry : grabbedUnits){
            GrabData data = entry.value;
            Unit target = data.target;

            if(!target.isValid()){
                grabbedUnits.remove(entry.key);
                continue;
            }

            Unit attacker = (data.attackerUnit != null && data.attackerUnit.isValid()) ? data.attackerUnit : null;
            Building attackerBuilding = data.attackerBuilding;

            if(attacker == null && (attackerBuilding == null || !attackerBuilding.isValid())){
                grabbedUnits.remove(entry.key);
                continue;
            }

            if(!Vars.state.isPaused()){
                data.grabTimer -= Time.delta;
                data.damageTimer += Time.delta;
                data.effectTimer += Time.delta;

                float holdAngle, targetX, targetY;

                if(attacker != null){
                    holdAngle = attacker.rotation;
                    float spacing = attacker.hitSize / 2f + target.hitSize / 2f + data.holdDistance;
                    Tmp.v1.trns(holdAngle, spacing);
                    targetX = attacker.x + Tmp.v1.x;
                    targetY = attacker.y + Tmp.v1.y;
                } else {
                    // Read rotation from TransformingTurretBuild if available,
                    // since standard buildings always return 0
                    if(attackerBuilding instanceof TransformingTurret.TransformingTurretBuild turret){
                        holdAngle = turret.rotation;
                    } else {
                        holdAngle = attackerBuilding.rotation;
                    }
                    float spacing = attackerBuilding.hitSize() / 2f + target.hitSize / 2f + data.holdDistance;
                    Tmp.v1.trns(holdAngle, spacing);
                    targetX = attackerBuilding.x + Tmp.v1.x;
                    targetY = attackerBuilding.y + Tmp.v1.y;
                }

                float moveSpeed = 8f;
                target.vel.set(
                        (targetX - target.x) * moveSpeed * Time.delta,
                        (targetY - target.y) * moveSpeed * Time.delta
                );

                target.vel.limit(attacker != null ? attacker.hitSize : attackerBuilding.hitSize());

                target.rotation = Mathf.slerpDelta(target.rotation, holdAngle + 180f, 0.2f);

                if(!Vars.net.client()){
                    if(data.damageTimer >= data.holdDamageInterval){
                        target.damage(data.holdDamage);
                        data.damageTimer = 0f;
                    }

                    if(data.grabTimer <= 0){
                        releaseUnit(target);
                        grabbedUnits.remove(entry.key);
                    }
                }

                if(data.effectTimer >= data.holdEffectInterval){
                    data.holdEffect.at(target.x, target.y);
                    data.effectTimer = 0f;
                }
            }
        }
    }

    public static void drawGrabs(){
        for(var entry : grabbedUnits.values()){
            Unit target = entry.target;
            Unit attackerUnit = entry.attackerUnit;
            Building attackerBuilding = entry.attackerBuilding;

            if(!target.isValid()) continue;

            Unit attacker = (attackerUnit != null && attackerUnit.isValid()) ? attackerUnit : null;
            Building attackerB = (attacker == null && attackerBuilding != null && attackerBuilding.isValid()) ? attackerBuilding : null;

            if(attacker == null && attackerB == null) continue;

            float attackerX = attacker != null ? attacker.x : attackerB.x;
            float attackerY = attacker != null ? attacker.y : attackerB.y;
            float attackerRot = attacker != null ? attacker.rotation : attackerB.rotation;

            Draw.z(Layer.flyingUnit + 1);

            Draw.color(Color.red, 0.6f);
            Lines.stroke(2f);

            for(int i = 0; i < 4; i++){
                float angle = (360f / 4f) * i;
                float dist = attacker != null ? attacker.hitSize : attackerB.hitSize();
                Tmp.v1.trns(attackerRot + angle, dist / 3f);
                float x1 = attackerX + Tmp.v1.x;
                float y1 = attackerY + Tmp.v1.y;

                Tmp.v2.trns(attackerRot + angle, target.hitSize / 3f);
                float x2 = target.x + Tmp.v2.x;
                float y2 = target.y + Tmp.v2.y;

                Lines.line(x1, y1, x2, y2);
            }

            float progress = entry.grabTimer / entry.grabDuration;
            Draw.color(Color.orange, progress);
            Lines.stroke(3f);
            Lines.circle(target.x, target.y, target.hitSize / 2f + 2f);

            Draw.reset();
        }
    }

    public static boolean isGrabbed(Unit unit){
        return grabbedUnits.containsKey(unit.id);
    }

    public static void releaseUnit(Unit unit){
        GrabData data = grabbedUnits.get(unit.id);
        if(data != null && !Vars.net.client()){
            float ax = data.attackerUnit != null ? data.attackerUnit.x : (data.attackerBuilding != null ? data.attackerBuilding.x : unit.x);
            float ay = data.attackerUnit != null ? data.attackerUnit.y : (data.attackerBuilding != null ? data.attackerBuilding.y : unit.y);

            float angle = Mathf.angle(ax - unit.x, ay - unit.y);
            Tmp.v1.trns(angle, 3f);
            unit.vel.add(Tmp.v1);

            Fx.hitBulletColor.at(unit.x, unit.y, Color.red);
        }
        grabbedUnits.remove(unit.id);
    }

    public static class GrabData {
        public Unit target;
        @Nullable public Unit attackerUnit;
        @Nullable public Building attackerBuilding;
        public mindustry.game.Team team;
        public float grabTimer;
        public float damageTimer = 0f;
        public float effectTimer = 0f;
        public float initialX, initialY;
        public float holdDamage;
        public float holdDamageInterval;
        public float holdDistance;
        public Effect holdEffect;
        public float holdEffectInterval;
        public float grabDuration;
    }
}