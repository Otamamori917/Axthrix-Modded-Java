package axthrix.world.types.bulletypes;

import arc.graphics.*;
import arc.math.*;
import axthrix.world.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;

public class BoltBulletType extends BulletType {
    public Color color1;
    public int boltLength = 40;
    public int boltLengthRand = 0;
    public float orbRadius = 11f;

    public BoltBulletType(float damage, Color c1){
        super(0.0001f, damage);
        this.damage = damage;
        color1 = c1;

        lifetime = 1;
        despawnEffect = Fx.none;
        hitEffect = Fx.hitLancer;
        keepVelocity = false;
        hittable = false;
        //for stats
        status = StatusEffects.shocked;
    }

    @Override
    public float calculateRange(){
        return (boltLength + boltLengthRand/2f) * 15f;
    }

    @Override
    public float estimateDPS(){
        return super.estimateDPS() * Math.max(boltLength / 10f, 1);
    }

    @Override
    public void draw(Bullet b){
    }

    @Override
    public void init(Bullet b){
        Bolt.create(b.team, color1, damage, b.x, b.y, b.rotation(), boltLength + Mathf.random(boltLengthRand));
    }
}
