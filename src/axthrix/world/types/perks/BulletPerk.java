package axthrix.world.types.perks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import axthrix.world.types.block.defense.PerkTurretType;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;
/// a perk "bullet" when activated shoots a special bullet instead of the one in the base type
public class BulletPerk extends Perk {

    public BulletType bullet;

    /**
     * Reload multiplier applied to the shot immediately after the perk shot fires.
     * < 1.0 = faster reload (e.g. 0.4 = reload in 40% of normal time).
     * > 1.0 = slower reload (e.g. 2.0 = reload takes twice as long).
     * 1.0 = no change.
     * Range: > 0. Must not be 0 or negative.
     */
    public float postPerkReloadMultiplier = 1f;

    public float glowRadius = 18f;
    public Color glowColor = Color.valueOf("ff9944");

    public BulletPerk() {
        name = "Bullet";
        hitsPerStack = 2;
        maxStacks = 1;
        minRange = 50f * 8f;
        decaysOnMiss = true;
        decaysOverTime = false;
        consumesOnActivate = true;
    }

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret, PerkStateData s) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, PerkStateData s, float targetX, float targetY) {
        s.pendingShot = true;
    }

    @Override
    public BulletType getPendingBullet() {
        return bullet;
    }

    @Override
    public void onPendingShotFired(Turret.TurretBuild turret, PerkStateData s) {
        if(postPerkReloadMultiplier != 1f && turret instanceof PerkTurretType.PerkTurretTypeBuild build) {
            build.pendingReloadMultiplier = Math.max(0.01f, postPerkReloadMultiplier);
        }
    }

    @Override
    public void draw(float x, float y, float rotation, PerkStateData s) {
        if(s.smoothProgress <= 0.01f) return;

        float alpha = s.smoothProgress * 0.50f;
        float radius = glowRadius * (0.4f + 0.6f * s.smoothProgress);
        float breathe = 1f + 0.08f * Mathf.sin(Time.time * 4f);

        Draw.color(glowColor, alpha);
        Fill.circle(x, y, radius * breathe);

        if(s.smoothProgress >= 0.8f) {
            float ringAlpha = ((s.smoothProgress - 0.8f) / 0.2f) * (0.75f + 0.25f * Mathf.sin(Time.time * 7f));
            Draw.color(glowColor, ringAlpha);
            Lines.stroke(1.8f);
            Lines.circle(x, y, radius * breathe);

            Draw.color(arc.graphics.Color.white, ringAlpha * 0.35f);
            Lines.stroke(0.9f);
            Lines.circle(x, y, radius * breathe * 0.7f);
        }

        Draw.reset();
    }

    @Override
    public Perk copy() {
        BulletPerk copy = new BulletPerk();
        copyBaseTo(copy);
        copy.bullet = bullet;
        copy.postPerkReloadMultiplier = postPerkReloadMultiplier;
        copy.glowRadius = glowRadius;
        copy.glowColor = glowColor;
        return copy;
    }
}