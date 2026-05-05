package axthrix.world.types.bulletypes;

import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.world.util.logics.NanobotLogic;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Bullet;
import mindustry.gen.Hitboxc;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;

public class NanobotBulletType extends BasicBulletType {

    // Flying nanobot stats
    public boolean activeWhileFlying = false;
    public float flyingRange = 40f;
    public float flyingNanoDamage = 2f;
    public float flyingNanoHealPercent = 0.5f;
    public float flyingNanoHealAmount = 1f;
    public float flyingTickRate = 5f;
    public int flyingNanobotCount = 10;
    public float flyingNanobotOrbitSpeed = 3f;
    public float flyingNanobotMoveSpeed = 0.05f;
    public float flyingNanobotSize = 0.8f;
    public float flyingNanobotSpread = 1f;
    public float flyingStackPenalty = 0.8f;

    // Linger cloud nanobot stats
    public boolean lingersOnHit = false;
    public float lingerDuration = 120f;
    public float lingerRange = 60f;
    public float lingerNanoDamage = 2f;
    public float lingerNanoHealPercent = 0.5f;
    public float lingerNanoHealAmount = 1f;
    public float lingerTickRate = 5f;
    public int lingerNanobotCount = 12;
    public float lingerNanobotOrbitSpeed = 3f;
    public float lingerNanobotMoveSpeed = 0.05f;
    public float lingerNanobotSize = 0.8f;
    public float lingerStackPenalty = 0.8f;
    public float cloudFadeDuration = 30f;
    public float smokeInterval = 20f; // ticks between smoke puffs

    // Bullet drawing
    public boolean drawDefaultBullet = true;

    // Soft cap settings
    public int softCapClouds = 5;
    public float softCapWindow = 10f;
    public float softCapDurationPenalty = 0.05f;
    public float softCapMinPenalty = 0.03f;

    // Internal cloud tracking
    private float spawnWindowTimer = 0f;
    private int spawnsInWindow = 0;
    public final Seq<NanobotCloud> clouds = new Seq<>();

    public NanobotBulletType(float speed, float damage) {
        super(speed, damage);
        hitEffect = despawnEffect = Fx.none;
    }

    public NanobotBulletType() {
        super();
    }

    private NanobotLogic.NanobotParams buildFlyingParams(float x, float y, Team team) {
        NanobotLogic.NanobotParams p = new NanobotLogic.NanobotParams();
        p.x = x;
        p.y = y;
        p.damage = flyingNanoDamage;
        p.healAmount = flyingNanoHealAmount;
        p.healPercent = flyingNanoHealPercent;
        p.range = flyingRange;
        p.tickRate = flyingTickRate;
        p.buildingDamageMultiplier = buildingDamageMultiplier;
        p.bulletSpeedBonus = 1f;
        p.bulletSlowdown = 1f;
        p.efficiencyBoost = 1f;
        p.stackPenalty = flyingStackPenalty;
        p.status = status;
        p.statusDuration = statusDuration;
        p.color = backColor != null ? backColor : Pal.heal;
        p.nanobotCount = flyingNanobotCount;
        p.nanobotSize = flyingNanobotSize;
        p.nanobotOrbitSpeed = flyingNanobotOrbitSpeed;
        p.nanobotMoveSpeed = flyingNanobotMoveSpeed;
        p.fadeMultiplier = 1f;
        p.spreadMultiplier = flyingNanobotSpread;
        p.team = team;
        return p;
    }

    private NanobotLogic.NanobotParams buildLingerParams(float x, float y, Team team) {
        NanobotLogic.NanobotParams p = new NanobotLogic.NanobotParams();
        p.x = x;
        p.y = y;
        p.damage = lingerNanoDamage;
        p.healAmount = lingerNanoHealAmount;
        p.healPercent = lingerNanoHealPercent;
        p.range = lingerRange;
        p.tickRate = lingerTickRate;
        p.buildingDamageMultiplier = buildingDamageMultiplier;
        p.bulletSpeedBonus = 1f;
        p.bulletSlowdown = 1f;
        p.efficiencyBoost = 1f;
        p.stackPenalty = lingerStackPenalty;
        p.status = status;
        p.statusDuration = statusDuration;
        p.color = backColor != null ? backColor : Pal.heal;
        p.nanobotCount = lingerNanobotCount;
        p.nanobotSize = lingerNanobotSize;
        p.nanobotOrbitSpeed = lingerNanobotOrbitSpeed;
        p.nanobotMoveSpeed = lingerNanobotMoveSpeed;
        p.fadeMultiplier = 1f;
        p.spreadMultiplier = 1f;
        p.team = team;
        return p;
    }

    private float getPenaltyPerCloud() {
        if (clouds.size <= softCapClouds) return softCapDurationPenalty;
        float excess = (float)(clouds.size - softCapClouds) / softCapClouds;
        float t = Math.min(excess, 1f);
        return softCapDurationPenalty + (softCapMinPenalty - softCapDurationPenalty) * t;
    }

    private void applyDurationPenalty() {
        if (clouds.size <= softCapClouds) return;
        float penaltyPerCloud = getPenaltyPerCloud();

        for (NanobotCloud cloud : clouds) {
            float remaining = cloud.duration - cloud.elapsedTimer;
            cloud.duration -= remaining * penaltyPerCloud;
            if (cloud.elapsedTimer >= cloud.duration) {
                cloud.duration = cloud.elapsedTimer + 1f;
            }
        }
    }

    private void spawnCloud(Bullet b) {
        if (!lingersOnHit) return;

        spawnsInWindow++;
        if (spawnsInWindow > softCapClouds) return;

        NanobotCloud cloud = new NanobotCloud();
        cloud.x = b.x;
        cloud.y = b.y;
        cloud.duration = lingerDuration;
        cloud.elapsedTimer = 0f;
        cloud.tickTimer = 0f;
        cloud.smokeTimer = 0f;
        cloud.dying = false;
        cloud.team = b.team;
        cloud.params = buildLingerParams(b.x, b.y, b.team);

        NanobotLogic.initNanobots(cloud, cloud.x, cloud.y, lingerNanobotCount);
        clouds.add(cloud);

        applyDurationPenalty();
    }

    private float getSpreadForLifetime(Bullet b) {
        float fin = b.fin();
        if (fin < 0.2f) {
            return Mathf.lerp(0f, flyingNanobotSpread, fin / 0.2f);
        } else if (fin > 0.8f) {
            return Mathf.lerp(flyingNanobotSpread, 0f, (fin - 0.8f) / 0.2f);
        }
        return flyingNanobotSpread;
    }

    @Override
    public void init(Bullet b) {
        super.init(b);
        if (activeWhileFlying) {
            NanobotLogic.initNanobots(b, b.x, b.y, flyingNanobotCount);
        }
    }

    @Override
    public void update(Bullet b) {
        super.update(b);

        if (activeWhileFlying && NanobotLogic.getNanobots(b) != null) {
            NanobotLogic.NanobotParams p = buildFlyingParams(b.x, b.y, b.team);
            p.fadeMultiplier = b.fout(0.2f);
            p.spreadMultiplier = getSpreadForLifetime(b);
            NanobotLogic.updateNanobots(b, p, b.time, false, () -> {});
        }
    }

    @Override
    public void draw(Bullet b) {
        if (drawDefaultBullet) super.draw(b);

        if (activeWhileFlying && NanobotLogic.getNanobots(b) != null) {
            NanobotLogic.NanobotParams p = buildFlyingParams(b.x, b.y, b.team);
            p.fadeMultiplier = b.fout(0.2f);
            p.spreadMultiplier = getSpreadForLifetime(b);
            NanobotLogic.drawNanobots(b, p);
        }
    }

    @Override
    public void drawTrail(Bullet b) {
        if (drawDefaultBullet) super.drawTrail(b);
    }

    @Override
    public void hitEntity(Bullet b, Hitboxc entity, float health) {
        super.hitEntity(b, entity, health);
        if (entity instanceof Unit) {
            spawnCloud(b);
        }
    }

    @Override
    public void hitTile(Bullet b, Building build, float x, float y, float initialHealth, boolean direct) {
        super.hitTile(b, build, x, y, initialHealth, direct);
        spawnCloud(b);
    }

    @Override
    public void despawned(Bullet b) {
        super.despawned(b);
        if (lingersOnHit && b.collided.isEmpty()) spawnCloud(b);
        if (activeWhileFlying) NanobotLogic.removeNanobots(b);
    }

    @Override
    public void removed(Bullet b) {
        super.removed(b);
        if (activeWhileFlying) NanobotLogic.removeNanobots(b);
    }

    public void updateClouds() {
        if (Vars.state.isPaused()) return;

        spawnWindowTimer += Time.delta;
        if (spawnWindowTimer >= softCapWindow) {
            spawnWindowTimer = 0f;
            spawnsInWindow = 0;
        }

        for (int i = clouds.size - 1; i >= 0; i--) {
            NanobotCloud cloud = clouds.get(i);
            cloud.elapsedTimer += Time.delta;
            cloud.tickTimer += Time.delta;
            cloud.smokeTimer += Time.delta;

            float remaining = cloud.duration - cloud.elapsedTimer;
            if (remaining <= cloudFadeDuration) {
                cloud.dying = true;
                cloud.params.fadeMultiplier = Mathf.clamp(remaining / cloudFadeDuration);
            }

            cloud.params.x = cloud.x;
            cloud.params.y = cloud.y;

            if (!cloud.dying) {
                NanobotLogic.updateNanobots(cloud, cloud.params, cloud.tickTimer, false, () -> {
                    cloud.tickTimer = 0f;
                });

                // Periodic smoke — only while not fading, random offset so clouds dont sync
                if (cloud.smokeTimer >= smokeInterval) {
                    cloud.smokeTimer = 0f;
                    float smokeX = cloud.x + Mathf.range(lingerRange * 0.1f);
                    float smokeY = cloud.y + Mathf.range(lingerRange * 0.1f);
                    Fx.smeltsmoke.at(smokeX, smokeY, 0f, cloud.params.color);
                }
            }

            if (cloud.elapsedTimer >= cloud.duration) {
                NanobotLogic.removeNanobots(cloud);
                clouds.remove(i);
            }
        }
    }

    public void drawClouds() {
        for (NanobotCloud cloud : clouds) {
            NanobotLogic.drawNanobots(cloud, cloud.params);
        }
    }

    public static class NanobotCloud {
        public float x, y;
        public float elapsedTimer;
        public float tickTimer;
        public float smokeTimer;
        public float duration;
        public boolean dying = false;
        public Team team;
        public NanobotLogic.NanobotParams params;
    }
}