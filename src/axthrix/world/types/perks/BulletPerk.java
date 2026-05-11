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

/**
 * Fires a special bullet to replace the turret's next normal shot when max stacks is reached.
 * After the perk shot fires, optionally applies a reload speed boost to the following normal shot.
 *
 * Supports all TriggerModes via the Perk base.
 */
public class BulletPerk extends Perk {

    /**
     * The bullet type to fire when the perk activates.
     * Replaces the turret's next normal shot instead of firing immediately.
     */
    public BulletType bullet;

    /**
     * Reload speed multiplier applied to the shot immediately after the perk shot fires.
     * Values below 1.0 make the follow-up shot faster (e.g. 0.4 = 40% of normal reload time).
     * Set to 1.0 to disable. Range: > 0.
     */
    public float postPerkReloadMultiplier = 1f;

    /** Glow radius drawn around the turret barrel when charged. Range: >= 0. */
    public float glowRadius = 18f;

    /** Color of the glow when charged. */
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
    public void onStack(Unit unit, Turret.TurretBuild turret) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {
        pendingShot = true;
    }

    @Override
    public BulletType getPendingBullet() {
        return bullet;
    }

    @Override
    public void onPendingShotFired(Turret.TurretBuild turret) {
        if(postPerkReloadMultiplier < 1f && turret instanceof PerkTurretType.PerkTurretTypeBuild build) {
            build.pendingReloadMultiplier = postPerkReloadMultiplier;
        }
    }

    @Override
    public void draw(float x, float y, float rotation) {
        if(smoothProgress <= 0.01f) return;

        float alpha = smoothProgress * 0.50f;
        float radius = glowRadius * (0.4f + 0.6f * smoothProgress);
        float breathe = 1f + 0.08f * Mathf.sin(Time.time * 4f);

        Draw.color(glowColor, alpha);
        Fill.circle(x, y, radius * breathe);

        if(smoothProgress >= 0.8f) {
            float ringAlpha = ((smoothProgress - 0.8f) / 0.2f) * (0.75f + 0.25f * Mathf.sin(Time.time * 7f));
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