package axthrix.world.types.bulletypes;

import arc.math.*;
import arc.struct.Seq;
import arc.util.*;
import axthrix.*;
import axthrix.AxthrixLoader.BulletData;
import mindustry.ai.types.MissileAI;
import mindustry.ctype.ContentType;
import mindustry.entities.Mover;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.entities.bullet.BulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.world.blocks.ControlBlock;

import static mindustry.Vars.net;
import static mindustry.Vars.world;
import static mindustry.gen.Groups.bullet;

public class InfFragBulletType extends BasicBulletType {
    protected static Rand fragRand = new Rand();

    /** If true, allow kill shooter bullets to spawn. */
    public boolean allowKillShooter = false;

    {
        despawnHit = true;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        b.fdata = b.id;
    }

    @Override
    public void createFrags(Bullet b, float x, float y){
        Log.info("fragification");
        if((fragOnAbsorb || !b.absorbed)){
            fragRand.setSeed((long)b.fdata);
            for(int i = 0; i < fragBullets; i++){
                float len = Mathf.random(1f, 7f);
                float a = b.rotation() + Mathf.range(fragRandomSpread / 2) + fragAngle + ((i - fragBullets/2) * fragSpread);
                getBullet().create(b, x + Angles.trnsx(a, len), y + Angles.trnsy(a, len), a, Mathf.random(fragVelocityMin, fragVelocityMax), Mathf.random(fragLifeMin, fragLifeMax));
            }
            b.fdata = fragRand.seed0;
        }
    }

    protected BulletType getBullet(){
        BulletType type = AxthrixLoader.allBullets.random(fragRand).bulletType;
        if(allowKillShooter) return type;

        while(type.killShooter){
            type = AxthrixLoader.allBullets.random(fragRand).bulletType;
        }

        return type;
    }
}
