package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.ObjectMap;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;

public class GrabBulletType extends BulletType {
    // Static map to track grabbed units
    private static final ObjectMap<Integer, GrabData> grabbedUnits = new ObjectMap<>();

    public float grabDuration = 180f; // How long to hold (3 seconds)
    public float holdDamage = 5f; // Damage per tick while held
    public float holdDamageInterval = 10f; // Apply damage every X ticks

    public float maxSizeRatio = 1.5f; // Max target size vs attacker size
    public boolean canGrabFlying = false; // Can grab flying units

    public Effect grabEffect = Fx.hitBulletColor;
    public Effect holdEffect = Fx.none;
    public float holdEffectInterval = 15f; // Play effect every X ticks
    public Effect failEffect = Fx.hitBulletSmall; // Effect when grab fails
    public Color grabColor = Color.red;

    public GrabBulletType(){
        speed = 0.01f; // Nearly instant
        lifetime = 5f;
        damage = 50f; // Initial bite damage
        collides = collidesAir = collidesGround = true;
        keepVelocity = false;
        hittable = false;
    }

    @Override
    public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float health){
        super.hitEntity(b, entity, health);

        // Check if we hit a unit
        if(entity instanceof Unit && b.owner instanceof Unit){
            Unit target = (Unit)entity;
            Unit attacker = (Unit)b.owner;

            // Can't grab friendly units or self
            if(target.team == attacker.team || target == attacker) return;

            // Can't grab flying units (unless enabled)
            if(!canGrabFlying && target.isFlying()){
                failEffect.at(target.x, target.y);
                return;
            }

            // Can't grab units that are too big
            if(target.hitSize > attacker.hitSize * maxSizeRatio){
                failEffect.at(target.x, target.y);
                return;
            }

            // Check if already grabbed
            if(isGrabbed(target)) return;

            // Grab the unit
            grabUnit(target, attacker, b.team);
        }
    }

    public void grabUnit(Unit target, Unit attacker, mindustry.game.Team team){
        GrabData data = new GrabData();
        data.target = target;
        data.attacker = attacker;
        data.team = team;
        data.grabTimer = grabDuration;
        data.holdDamage = this.holdDamage;
        data.holdDamageInterval = this.holdDamageInterval;
        data.holdEffect = this.holdEffect;
        data.holdEffectInterval = this.holdEffectInterval;
        data.initialX = target.x;
        data.initialY = target.y;

        grabbedUnits.put(target.id, data);

        // Initial bite damage
        target.damage(damage);
        grabEffect.at(target.x, target.y, grabColor);
    }

    public static void updateGrabs(){
        // Update all grabbed units
        for(var entry : grabbedUnits){
            GrabData data = entry.value;
            Unit target = data.target;
            Unit attacker = data.attacker;

            // Check if either unit is invalid
            if(!target.isValid() || !attacker.isValid()){
                grabbedUnits.remove(entry.key);
                continue;
            }

            // Only update if game not paused
            if(!Vars.state.isPaused()){
                data.grabTimer -= Time.delta;
                data.damageTimer += Time.delta;
                data.effectTimer += Time.delta;

                // Hold unit at attacker's position (slightly in front)
                float holdAngle = attacker.rotation;
                Tmp.v1.trns(holdAngle, attacker.hitSize / 2f + target.hitSize / 2f);
                target.x = attacker.x + Tmp.v1.x;
                target.y = attacker.y + Tmp.v1.y;

                // Force target to face attacker
                target.rotation = Mathf.slerpDelta(target.rotation, holdAngle + 180f, 0.2f);

                // Cancel target's velocity
                target.vel.set(0, 0);

                // Apply hold damage over time
                if(data.damageTimer >= data.holdDamageInterval){
                    target.damage(data.holdDamage);
                    data.damageTimer = 0f;
                }

                // Play hold effect periodically
                if(data.effectTimer >= data.holdEffectInterval){
                    data.holdEffect.at(target.x, target.y);
                    data.effectTimer = 0f;
                }

                // Release when timer expires
                if(data.grabTimer <= 0){
                    releaseUnit(target);
                    grabbedUnits.remove(entry.key);
                }
            }
        }
    }

    public static void drawGrabs(){
        for(var entry : grabbedUnits.values()){
            Unit target = entry.target;
            Unit attacker = entry.attacker;

            if(!target.isValid() || !attacker.isValid()) continue;

            Draw.z(Layer.flyingUnit + 1);

            // Draw jaw/grip lines
            Draw.color(Color.red, 0.6f);
            Lines.stroke(2f);

            // Draw multiple "teeth" lines connecting attacker to target
            for(int i = 0; i < 4; i++){
                float angle = (360f / 4f) * i;
                Tmp.v1.trns(attacker.rotation + angle, attacker.hitSize / 3f);
                float x1 = attacker.x + Tmp.v1.x;
                float y1 = attacker.y + Tmp.v1.y;

                Tmp.v2.trns(attacker.rotation + angle, target.hitSize / 3f);
                float x2 = target.x + Tmp.v2.x;
                float y2 = target.y + Tmp.v2.y;

                Lines.line(x1, y1, x2, y2);
            }

            // Draw struggle indicator
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
        if(data != null){
            // Optional: apply knockback when released
            float angle = Mathf.angle(data.attacker.x - unit.x, data.attacker.y - unit.y);
            Tmp.v1.trns(angle, 3f);
            unit.vel.add(Tmp.v1);

            Fx.hitBulletColor.at(unit.x, unit.y, Color.red);
        }
        grabbedUnits.remove(unit.id);
    }

    public static class GrabData {
        public Unit target;
        public Unit attacker;
        public mindustry.game.Team team;
        public float grabTimer;
        public float damageTimer = 0f;
        public float effectTimer = 0f;
        public float initialX, initialY;
        public float holdDamage;
        public float holdDamageInterval;
        public Effect holdEffect;
        public float holdEffectInterval;
        public float grabDuration;
    }
}
