package axthrix.world.types.bulletypes;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.content.FX.AxthrixFx;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.*;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.bullet.LaserBulletType;
import mindustry.entities.bullet.PointBulletType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.StatusEffect;

public class GluonBulletType extends PointBulletType {
    ///how far to attach and how far a unit has to move to deal snap damage
    public float maxUnitTetherDistance = 20f;
    ///force applied to a unit per second
    public float pullStrength = 0.5f;
    public static Seq<Tether> activeTethers = new Seq<>();

    /// how far to create fence lines
    public float maxBuildingTetherDistance = 160;
    ///  max fences created per hit
    public int maxBuildingsPerHit = 5;
    /// how often fences deal snap damage to those attached and inside
    public float snapInterval = 100;
    public static Seq<BuildingTether> activeBuildingTethers = new Seq<>();

    /// how long unit tether/building fences last
    public float tetherDuration = 600;
    /// damage deal on snap
    public float snapDamage = 100;
    public GluonBulletType() {
        super();
        damage = 40f;
        speed = 200f;
        trailEffect = AxthrixFx.gluonTrail;
        shootEffect = AxthrixFx.gluonFireAdvanced;
        hitEffect = AxthrixFx.gluonHit;
        despawnEffect = AxthrixFx.gluonHit;
        hitColor = Pal.sapBullet;


    }

    @Override
    public void hit(Bullet b, float x, float y) {
        super.hit(b, x, y);
        hitSound.at(x, y, 1.1f);

        // 1. Try to find a Unit first
        Unit struck = Units.closestEnemy(b.team, x, y, 20f, u -> true);

        if (struck == null) {
            // 2. No unit? Find the building at the exact impact point
            Building struckB = Vars.world.build(Mathf.floor(x / Vars.tilesize), Mathf.floor(y / Vars.tilesize));
            if (struckB == null) return;


            Seq<Building> nearbyBuildings = new Seq<>();
            Vars.indexer.eachBlock(null, x, y, maxBuildingTetherDistance, other -> other != struckB, nearbyBuildings::add);

            if (nearbyBuildings.size > 0) {
                nearbyBuildings.shuffle(); // Safe random selection
                int count = Math.min(nearbyBuildings.size, maxBuildingsPerHit);

                for (int i = 0; i < count; i++) {
                    Building other = nearbyBuildings.get(i);
                    if (!activeBuildingTethers.contains(t -> t.links(struckB, other))) {
                        activeBuildingTethers.add(new BuildingTether(b, struckB, other,maxBuildingTetherDistance, snapDamage, tetherDuration,snapInterval));
                        // Success Log
                        Log.info("Tether Created between " + struckB.block.name + " and " + other.block.name);
                    }
                }
            }
        } else {
            // 3. Unit Cluster Logic
            Units.nearbyEnemies(b.team, x, y, maxUnitTetherDistance, other -> {
                if (other != struck && !activeTethers.contains(t -> t.links(struck, other))) {
                    activeTethers.add(new Tether(b, struck, other, maxUnitTetherDistance + struck.hitSize, snapDamage, pullStrength, tetherDuration, status));
                }
            });
        }
    }



    public static class Tether {
        public Unit unitA, unitB;
        public Bullet bullet;
        public StatusEffect status;
        public float maxDist, damage, pullStr, duration;
        protected float curTick;

        public Tether(Bullet bullet, Unit a, Unit b, float dist, float dmg, float pStr, float dura, StatusEffect status) {
            this.bullet = bullet;
            this.unitA = a;
            this.unitB = b;
            this.maxDist = dist;
            this.damage = dmg;
            this.pullStr = pStr;
            this.duration = dura;
            this.status = status;
            this.curTick = 0;
        }

        public boolean links(Unit a, Unit b) {
            return (unitA == a && unitB == b) || (unitA == b && unitB == a);
        }

        public boolean update() {
            if(!Vars.state.isPaused()) curTick += Time.delta;

            if (!unitA.isValid() || !unitB.isValid()) {
                triggerSnap(unitA);
                triggerSnap(unitB);
                return true;
            }

            if (unitA.dst(unitB) > maxDist) {
                triggerSnap(unitA);
                triggerSnap(unitB);
                return true;
            }

            if (curTick >= duration) {
                triggerExpiry();
                return true;
            }

            unitA.apply(status, 10f);
            unitB.apply(status, 10f);

            triggerPull(unitA, unitB);
            triggerPull(unitB, unitA);

            return false;
        }

        private void triggerPull(Unit pulled, Unit target) {
            float angle = Angles.angle(pulled.x, pulled.y, target.x, target.y);
            float dst = Mathf.dst(pulled.x, pulled.y, target.x, target.y);

            float stretch = dst / maxDist;
            float strength = pullStr * stretch * Time.delta;

            pulled.vel.add(Tmp.v1.trns(angle, strength/60));

            pulled.vel.scl(0.98f);
        }

        private void triggerSnap(Unit unit) {
            if (unit == null || !unit.isAdded()) return;

            // High-damage snap: using bullet for team ownership
            Damage.damage(bullet == null ? null : bullet.team, unit.x, unit.y, unit.hitSize * 4, damage);

            // Violent visual effects
            Fx.sapExplosion.at(unit.x, unit.y);
            AxthrixFx.gluonHit.at(unit.x, unit.y, 0, Pal.sapBulletBack);
        }

        private void triggerExpiry() {
            float angle = Angles.angle(unitA.x, unitA.y, unitB.x, unitB.y);
            AxthrixFx.gluonDissipate.at(unitA.x, unitA.y, angle);
            AxthrixFx.gluonDissipate.at(unitB.x, unitB.y, angle + 180f);
        }


        public void draw() {
            if (!unitA.isValid() || !unitB.isValid()) return;

            Draw.z(Layer.power);
            float angle = Angles.angle(unitA.x, unitA.y, unitB.x, unitB.y);
            float dist = unitA.dst(unitB);
            float stretch = dist / maxDist;

            // Unstable Gluon pulse: faster and whiter when stretched
            float speed = 2f + (stretch * 4f);
            float pulse = Mathf.absin(Time.time, speed, 0.4f);
            Color col = Tmp.c1.set(Pal.sapBulletBack).lerp(Color.white, pulse);

            // Visual Tension: Line gets thinner but jitters more as it stretches
            float jitterScl = 1.5f + (stretch * 3.5f);
            float stroke = (1.4f + pulse) * (1f - stretch * 0.4f);
            int segments = (int)(dist / 4f);

            Draw.color(col);
            Lines.stroke(stroke);
            Lines.beginLine();
            for(int i = 0; i <= segments; i++){
                float progress = (float)i / segments;
                float px = Mathf.lerp(unitA.x, unitB.x, progress);
                float py = Mathf.lerp(unitA.y, unitB.y, progress);

                if(i > 0 && i < segments){
                    // High-speed jitter using randomSeedRange
                    float offset = Mathf.randomSeedRange((long)(i + Time.time * 2), jitterScl);
                    px += Mathf.cosDeg(angle + 90) * offset;
                    py += Mathf.sinDeg(angle + 90) * offset;
                }
                Lines.linePoint(px, py);
            }
            Lines.endLine();

            // Glow caps at the units
            for(int i = 0; i < 2; i++) {
                float m = (i == 0 ? 1f : 0.5f);
                Draw.color(i == 0 ? Pal.sapBulletBack : Color.white);
                float headScl = 1f + Mathf.randomSeedRange((long)Time.time, 0.2f);

                Drawf.tri(unitA.x, unitA.y, 4f * m, (10f * headScl) * m, angle);
                Drawf.tri(unitB.x, unitB.y, 4f * m, (10f * headScl) * m, angle + 180f);
            }

            Drawf.light(unitA.x, unitA.y, unitB.x, unitB.y, 30f * (1f + pulse), Pal.sapBulletBack, 0.6f);
            Draw.reset();
        }
    }

    public static class BuildingTether {
        public Building buildA, buildB;
        public BulletType type;
        public Team team;
        public float snap;
        public float maxDist, duration, curTick = 0, snapTick = 0;

        public BuildingTether(Bullet bullet, Building a, Building b,float dist, float dmg, float dura,float snapf) {
            this.type = new LaserBulletType(){{
                damage = dmg;
                hitEffect = AxthrixFx.gluonHit;
                laserEffect = Fx.none;
                pierceArmor = pierceBuilding = pierce = true;
                absorbable = laserAbsorb = reflectable = hittable = false;
                pierceCap = 100;
                removeAfterPierce = false;
                largeHit = true;
                width = sideWidth = -1;
                length = a.dst(b);
            }
                @Override
                public void hitEntity(Bullet b, mindustry.gen.Hitboxc entity, float health) {
                    super.hitEntity(b, entity, health);

                    if(entity instanceof Building build && build == buildB) b.remove();
                }
                @Override
                public void draw(Bullet b){}
            };
            this.maxDist = dist;
            this.snap = snapf;
            this.team = bullet.team; // Save the team
            this.buildA = a; this.buildB = b;
            this.duration = dura;
        }

        public boolean links(Building b1, Building b2) {
            return (buildA == b1 && buildB == b2) || (buildA == b2 && buildB == b1);
        }

        public boolean update() {
            if(!Vars.state.isPaused()) {
                curTick += Time.delta;
                snapTick += Time.delta;
            }

            if (!buildA.isValid() || !buildB.isValid() || curTick >= duration) return true;

            if (snapTick >= snap) {
                float rot = buildA.angleTo(buildB);
                snapTick = 0;
                type.create(buildA,team,buildA.x,buildA.y,rot);
            }
            return false;
        }

        public void draw() {
            if (!buildA.isValid() || !buildB.isValid()) return;

            Draw.z(Layer.power);
            float angle = Angles.angle(buildA.x, buildA.y, buildB.x, buildB.y);
            float dist = buildA.dst(buildB);
            float stretch = dist / maxDist;

            // Unstable Gluon pulse: faster and whiter when stretched
            float speed = 2f + (stretch * 4f);
            float pulse = Mathf.absin(Time.time, speed, 0.4f);
            Color col = Tmp.c1.set(Pal.sapBulletBack).lerp(Color.white, pulse);

            // Visual Tension: Line gets thinner but jitters more as it stretches
            float jitterScl = 1.5f + (stretch * 3.5f);
            float stroke = (1.4f + pulse) * (1f - stretch * 0.4f);
            int segments = (int)(dist / 4f);

            Draw.color(col);
            Lines.stroke(stroke);
            Lines.beginLine();
            for(int i = 0; i <= segments; i++){
                float progress = (float)i / segments;
                float px = Mathf.lerp(buildA.x, buildB.x, progress);
                float py = Mathf.lerp(buildA.y, buildB.y, progress);

                if(i > 0 && i < segments){
                    // High-speed jitter using randomSeedRange
                    float offset = Mathf.randomSeedRange((long)(i + Time.time * 2), jitterScl);
                    px += Mathf.cosDeg(angle + 90) * offset;
                    py += Mathf.sinDeg(angle + 90) * offset;
                }
                Lines.linePoint(px, py);
            }
            Lines.endLine();

            // Glow caps at the units
            for(int i = 0; i < 2; i++) {
                float m = (i == 0 ? 1f : 0.5f);
                Draw.color(i == 0 ? Pal.sapBulletBack : Color.white);
                float headScl = 1f + Mathf.randomSeedRange((long)Time.time, 0.2f);

                Drawf.tri(buildA.x, buildA.y, 4f * m, (10f * headScl) * m, angle);
                Drawf.tri(buildB.x, buildB.y, 4f * m, (10f * headScl) * m, angle + 180f);
            }

            Drawf.light(buildA.x, buildA.y, buildB.x, buildB.y, 30f * (1f + pulse), Pal.sapBulletBack, 0.6f);
            Draw.reset();
        }

    }
}
