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

public class BulletPerk extends Perk {

    // ---- Configuration ----

    /**
     * The bullet type to fire when the perk activates.
     * Replaces the turret's next normal shot instead of firing immediately.
     */
    public BulletType bullet;

    /**
     * Reload speed multiplier applied to the shot immediately after the perk shot fires.
     * Values below 1.0 make the follow-up shot faster (e.g. 0.4 = reload takes 40% of normal time).
     * Set to 1.0 to disable. Range: > 0.
     */
    public float postPerkReloadMultiplier = 0.4f;

    /** Glow radius drawn around the turret when charged. Range: >= 0. */
    public float glowRadius = 18f;

    /** Color of the glow when charged. */
    public Color glowColor = Color.valueOf("ff9944");

    // ---- Constructor ----

    public BulletPerk() {
        name = "Bullet";
        hitsPerStack = 2;
        maxStacks = 1;
        minRange = 50f * 8f;
        decaysOnMiss = true;
        decaysOverTime = false;
        consumesOnActivate = true;
    }

    // ---- Perk Callbacks ----

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret) {}

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {
        // Queue the shot to replace the turret's next normal fire — do not fire immediately
        pendingShot = true;
    }

    // ---- Shot replacement ----

    @Override
    public BulletType getPendingBullet() {
        return bullet;
    }

    @Override
    public void onPendingShotFired(Turret.TurretBuild turret) {
        if(turret instanceof PerkTurretType.PerkTurretTypeBuild build) {
            build.pendingReloadMultiplier = postPerkReloadMultiplier;
        }
    }

    // ---- Draw ----

    @Override
    public void draw(float x, float y, float rotation) {
        // Use smoothProgress so the glow lerps in and out rather than snapping
        if(smoothProgress <= 0.01f) return;

        float alpha = smoothProgress * 0.50f;
        float radius = glowRadius * (0.4f + 0.6f * smoothProgress);
        float breathe = 1f + 0.08f * Mathf.sin(Time.time * 4f);

        Draw.color(glowColor, alpha);
        Fill.circle(x, y, radius * breathe);

        // Ring appears as smoothProgress approaches 1 (not a hard snap)
        if(smoothProgress >= 0.8f) {
            float ringAlpha = (smoothProgress - 0.8f) / 0.2f; // fade in over last 20%
            ringAlpha *= 0.75f + 0.25f * Mathf.sin(Time.time * 7f);
            Draw.color(glowColor, ringAlpha);
            Lines.stroke(1.8f);
            Lines.circle(x, y, radius * breathe);

            Draw.color(arc.graphics.Color.white, ringAlpha * 0.35f);
            Lines.stroke(0.9f);
            Lines.circle(x, y, radius * breathe * 0.7f);
        }

        Draw.reset();
    }

    // ---- Copy ----

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