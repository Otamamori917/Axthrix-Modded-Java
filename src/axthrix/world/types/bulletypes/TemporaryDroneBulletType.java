package axthrix.world.types.bulletypes;

import arc.math.Mathf;
import arc.util.*;
import mindustry.content.Fx;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.*;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.types.ai.TemporaryDroneAI;

public class TemporaryDroneBulletType extends BulletType {
    public DroneUnitType droneType;
    public float droneLifetime = 420f;
    public float moveRangeMultiplier = 1.3f;
    public float spawnOffset = 22f;

    public TemporaryDroneBulletType(DroneUnitType droneType) {
        this.droneType = droneType;
        speed = 0f;
        lifetime = 5f;
        collides = false;
        hitEffect = Fx.none;
        despawnEffect = Fx.none;
    }

    @Override
    public void init(Bullet b) {
        if (droneType == null) return;

        Teamc shooter = b.owner instanceof Teamc t ? t : null;
        float aimX = b.aimX;
        float aimY = b.aimY;

        float spawnX = b.x + Mathf.random(-spawnOffset, spawnOffset);
        float spawnY = b.y + Mathf.random(-spawnOffset, spawnOffset);

        Unit drone = droneType.create(b.team);
        if (drone == null) return;

        drone.set(spawnX, spawnY);
        drone.rotation = b.rotation();

        if (shooter instanceof Unit unitOwner) {
            droneType.tetherUnit.put(drone, unitOwner);
        } else if (shooter instanceof Building buildOwner) {
            droneType.tetherBuilding.put(drone, buildOwner);
        }

        drone.add();

        drone.controller(new TemporaryDroneAI(shooter, aimX, aimY, moveRangeMultiplier, droneLifetime));

        Fx.spawn.at(spawnX, spawnY);
    }
}