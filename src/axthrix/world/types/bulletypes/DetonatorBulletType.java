package axthrix.world.types.bulletypes;

import arc.graphics.Color;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Unit;

public class DetonatorBulletType extends BasicBulletType {
    public AttachmentGrenadeBulletType grenadeType;
    public Effect detonateEffect = Fx.reactorExplosion;
    public boolean damageOnHit = true;
    public float baseHitDamage = 50f;

    public DetonatorBulletType(){
        // Fast sniper shot
        speed = 15f;
        lifetime = 40f;
        damage = 50f;

        // Visual
        width = 8f;
        height = 12f;
        shrinkY = 0f;

        hitEffect = Fx.hitBulletColor;
    }

    @Override
    public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float health){
        super.hitEntity(b, entity, health);

        // Check if we hit a unit with grenades
        if(entity instanceof Unit && grenadeType != null){
            Unit target = (Unit)entity;
            int grenadeCount = AttachmentGrenadeBulletType.getGrenadeCount(target);

            if(grenadeCount > 0){
                // Detonate all grenades on the target
                AttachmentGrenadeBulletType.detonateGrenades(target, grenadeType);
                detonateEffect.at(target.x, target.y, Color.orange);
            }else if(damageOnHit){
                // No grenades, just deal base damage
                target.damage(baseHitDamage);
            }
        }
    }
}