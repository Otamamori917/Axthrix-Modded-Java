package axthrix.world.types.bulletypes.bulletpatterntypes;

import arc.math.*;
import arc.util.*;
import mindustry.entities.pattern.ShootPattern;
/** NOTE when using this bullet pattern use "WeaponHelix" as it's weapon type for units also note weapons with this pattern cannot rotate */
public class SpiralPattern extends ShootPattern {
    public float scl = 2f, mag = 1.5f, offset = Mathf.PI;

    public SpiralPattern(){
        shots = 2;
    }

    public SpiralPattern(float scl, float mag){
        this();
        this.scl = scl;
        this.mag = mag;
        offset = scl * Mathf.halfPi;
    }
    public void shoot(int totalShots, BulletHandler handler, @Nullable Runnable barrelIncrementer){
        for(int i = 0; i < shots; i++){
            float off = offset + i * Mathf.PI2 * scl / shots;
            handler.shoot(Mathf.cos(off, scl, scl * mag), 0, 0, firstShotDelay + shotDelay * i,
                b -> b.moveRelative(0f, Mathf.sin(b.time + off, scl, mag)));
        }
    }
}