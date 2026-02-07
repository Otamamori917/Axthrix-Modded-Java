package axthrix.world.types.block.defense;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import axthrix.world.types.AxFaction;
import axthrix.world.util.AxStats;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Predict;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Groups;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.world.blocks.defense.turrets.PointDefenseTurret;
import mindustry.world.blocks.defense.turrets.ReloadTurret;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValues;

import static mindustry.Vars.state;

public class ProjectileDefenceTurret extends ReloadTurret {
    public final int timerTarget;
    public float retargetTime;
    public TextureRegion baseRegion;
    public BulletType shootType;
    public Sound shootSound;
    public float shootCone;

    public boolean predictTarget = true;

    public Seq<AxFaction> faction = new Seq<>();
    public boolean blackListFactions = false;

    public ProjectileDefenceTurret(String name) {
        super(name);
        timerTarget = timers++;
        retargetTime = 0F;
        shootCone = 5.0F;
        rotateSpeed = 20.0F;
        reload = 30.0F;
        coolantMultiplier = 2.0F;
    }

    /*public TextureRegion[] icons() {
        return new TextureRegion[]{baseRegion, region};
    }*/


    public void setStats() {
        super.setStats();
        stats.add(Stat.reload, 60.0F / reload, StatUnit.perSecond);
        if(faction.any()){
            stats.add(AxStats.faction, Core.bundle.get("team." +  faction.peek().name));
        }
        stats.add(Stat.ammo, StatValues.ammo(ObjectMap.of(new Object[]{this, shootType})));

    }

    public class ProjectileDefenceTurretBuild extends ReloadTurret.ReloadTurretBuild {
        @Nullable
        public Bullet target;

        float dest;

        public void updateTile() {
            if (timer(timerTarget, retargetTime)) {
                target = (Bullet)Groups.bullet.intersect(x - range, y - range, range * 2.0F, range * 2.0F).min((b) -> b.team != team && b.type().hittable, (b) -> b.dst2(this));
            }

            if (target != null && !target.isAdded()) {
                target = null;
            }

            if (coolant != null) {
                updateCooling();
            }

            if (target != null && target.within(this, range) && target.team != team && target.type() != null && target.type().reflectable) {
                /*TODO make its more reliable at hitting fast but short lifetime bullets (example would be reign)
                Vec2 offset = Tmp.v1.setZero();
                offset.set(target.deltaX(), target.deltaY()).scl(target.lifetime / Time.delta);*/
                if (predictTarget) {
                    dest = angleTo(Predict.intercept(x,y,target.x,target.y, target.deltaX, target.deltaY, shootType.speed));
                } else {
                    dest = angleTo(target);
                }
                rotation = Angles.moveToward(rotation, dest, rotateSpeed * edelta());
                reloadCounter += edelta();

                if (Angles.within(rotation, dest, shootCone) && reloadCounter >= reload) {

                    shootType.create(this, x, y, rotation);
                    shootSound.at(x + Tmp.v1.x, y + Tmp.v1.y);
                    reloadCounter = 0.0F;
                }
            }

        }

        public boolean shouldConsume() {
            return super.shouldConsume() && target != null;
        }

        /*public void draw() {
            Draw.rect(baseRegion, x, y);
            Drawf.shadow(region, x - (float)size / 2.0F, y - (float)size / 2.0F, rotation - 90.0F);
            Draw.rect(region, x, y, rotation - 90.0F);
        }*/

        public void write(Writes write) {
            super.write(write);
            write.f(rotation);
        }

        public void read(Reads read, byte revision) {
            super.read(read, revision);
            rotation = read.f();
        }
    }
}
