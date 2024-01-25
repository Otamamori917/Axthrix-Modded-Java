package axthrix.world.types.weapontypes;

import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.Weapon;
/** For unit weapons that use "Spiral Bullet pattern" fixes a known bug with the bullet pattern */
public class WeaponHelix extends Weapon {
    /** whether bullets shouldn't angle away from the shooting point; only works when rotate = false. */
    public boolean shootForward = true;

    protected void bullet(Unit unit, WeaponMount mount, float xOffset, float yOffset, float angleOffset, Mover mover){
        if(!unit.isAdded()) return;

        mount.charging = false;
        float
            xSpread = Mathf.range(xRand),
            weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation),
            mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
            mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y),
            bulletX = mountX + Angles.trnsx(weaponRotation, this.shootX, this.shootY),
            bulletY = mountY + Angles.trnsy(weaponRotation, this.shootX, this.shootY);

        if(rotate || !shootForward){
            bulletX += Angles.trnsx(weaponRotation, xOffset + xSpread, yOffset);
            bulletY += Angles.trnsy(weaponRotation, xOffset + xSpread, yOffset);
        }
        float
            shootAngle = bulletRotation(unit, mount, bulletX, bulletY) + angleOffset,
            lifeScl = bullet.scaleLife ? Mathf.clamp(Mathf.dst(bulletX, bulletY, mount.aimX, mount.aimY) / bullet.range) : 1f,
            angle = angleOffset + shootAngle + Mathf.range(inaccuracy + bullet.inaccuracy);

        if(!(rotate || !shootForward)){
            bulletX += Angles.trnsx(weaponRotation, xOffset + xSpread, yOffset);
            bulletY += Angles.trnsy(weaponRotation, xOffset + xSpread, yOffset);
        }

        mount.bullet = bullet.create(unit, unit.team, bulletX, bulletY, angle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, mount.aimX, mount.aimY);
        handleBullet(unit, mount, mount.bullet);

        if(!continuous){
            shootSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
        }

        ejectEffect.at(mountX, mountY, angle * Mathf.sign(this.x));
        bullet.shootEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);
        bullet.smokeEffect.at(bulletX, bulletY, angle, bullet.hitColor, unit);

        unit.vel.add(Tmp.v1.trns(shootAngle + 180f, bullet.recoil));
        Effect.shake(shake, shake, bulletX, bulletY);
        mount.recoil = 1f;
        if(recoils > 0){
            mount.recoils[mount.barrelCounter % recoils] = 1f;
        }
        mount.heat = 1f;
        mount.totalShots++;
    }

}
