package axthrix.world.types.ai;

import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.types.abilities.SacrificeProtocolAbility;
import mindustry.entities.Predict;
import mindustry.entities.units.AIController;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.Turret;

public class TemporaryDroneAI extends AIController {

    private final Teamc owner;
    private final float targetX, targetY;
    private final float rangeMult;
    private final float lifetime;

    private float timer = 0f;

    public TemporaryDroneAI(Teamc owner, float targetX, float targetY, float rangeMult, float lifetime) {
        this.owner = owner;
        this.targetX = targetX;
        this.targetY = targetY;
        this.rangeMult = rangeMult;
        this.lifetime = lifetime;
    }

    public float getRemainingLifetime() {
        Log.info(lifetime - timer);
        return lifetime - timer;
    }

    @Override
    public void updateUnit() {
        timer += Time.delta;

        if (timer >= lifetime) {
            unit.kill();
            return;
        }
        for(var ability : unit.abilities){
            if(ability instanceof SacrificeProtocolAbility sac && sac.isKamikazing()){
                return;
            }
        }


        // Safe validity check
        boolean ownerValid = false;
        if (owner instanceof Unit u) ownerValid = u.isValid();
        else if (owner instanceof Building b) ownerValid = b.isValid();

        if (!ownerValid) {
            unit.kill();
            return;
        }

        Vec2 targetPos = Tmp.v1.set(targetX, targetY);
        float range = unit.range() * rangeMult;

        float ownerX = owner.x();
        float ownerY = owner.y();

        // === MAIN LOGIC ===
        boolean ownerIsShooting = false;

        if (owner instanceof Unit u) {
            ownerIsShooting = u.isShooting();
        } else if (owner instanceof Building build) {
            // For turrets: check if they have a target
            ownerIsShooting = build instanceof Turret.TurretBuild tb && tb.hasAmmo();
        }

        // Move to target area when owner is shooting
        if (ownerIsShooting) {
            if (!unit.within(targetX, targetY, range - 15f)) {
                unit.lookAt(targetX, targetY);
                Tmp.v2.set(targetX - unit.x, targetY - unit.y).limit(unit.speed() * 0.95f);
                unit.moveAt(Tmp.v2);
            } else {
                unit.vel.setZero();
                unit.lookAt(targetX, targetY);
            }
        }
        // Otherwise stay close to owner
        else if (!unit.within(ownerX, ownerY, 45f)) {
            unit.lookAt(ownerX, ownerY);
            Tmp.v2.set(ownerX - unit.x, ownerY - unit.y).limit(unit.speed() * 0.85f);
            unit.moveAt(Tmp.v2);
        } else {
            unit.vel.setZero();
        }

        // Shooting
        boolean inRange = unit.within(targetX, targetY, unit.range());
        unit.isShooting = inRange && ownerIsShooting;

        if (unit.isShooting) {
            for (var mount : unit.mounts) {
                if (mount.weapon != null && mount.weapon.aiControllable && mount.weapon.bullet != null) {
                    Vec2 predicted = Predict.intercept(unit, targetPos, mount.weapon.bullet.speed);
                    mount.aimX = predicted.x;
                    mount.aimY = predicted.y;
                    mount.shoot = true;
                }
            }
        }
    }
}