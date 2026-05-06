package axthrix.world.types.perks;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Posc;
import mindustry.gen.Unit;
import mindustry.world.blocks.defense.turrets.Turret;

public class BulletPerk extends Perk {

    // ---- Configuration ----

    /**
     * The bullet type to spawn when the perk activates.
     */
    public BulletType bullet;

    /**
     * Glow radius drawn around the weapon when charged.
     */
    public float glowRadius = 18f;

    /** Color of the weapon glow when charged. */
    public Color glowColor = Color.valueOf("ff9944");

    // ---- Constructor ----

    public BulletPerk() {
        name = "Bullet";
        hitsPerStack = 2;
        maxStacks = 1;
        minRange = 50f * 8f; // 50 tiles in world units (8 units per tile)
        decaysOnMiss = true;
        decaysOverTime = false;
        consumesOnActivate = true;
    }

    // ---- Perk Callbacks ----

    @Override
    public void onStack(Unit unit, Turret.TurretBuild turret) {
    }

    @Override
    public void onMaxStack(Unit unit, Turret.TurretBuild turret, float targetX, float targetY) {
        if(bullet == null) return;

        float shooterX, shooterY;
        mindustry.game.Team team;
        Posc owner;

        if(unit != null) {
            shooterX = unit.x;
            shooterY = unit.y;
            team = unit.team;
            owner = unit;
        } else if(turret != null) {
            shooterX = turret.x;
            shooterY = turret.y;
            team = turret.team;
            owner = turret;
        } else {
            return;
        }

        float angle = Mathf.angle(targetX - shooterX, targetY - shooterY);

        // Spawn the perk projectile — matches the pattern used in HarpoonBulletType
        bullet.create(owner, team, shooterX, shooterY, angle);
    }

    // ---- Draw ----

    @Override
    public void draw(float x, float y, float rotation) {
        float progress = getProgress();
        if(progress <= 0f) return;

        float alpha = progress * 0.50f;
        float radius = glowRadius * (0.4f + 0.6f * progress);
        float breathe = 1f + 0.08f * Mathf.sin(Time.time * 4f);

        // Orange glow — grows with charge progress
        Draw.color(glowColor, alpha);
        Fill.circle(x, y, radius * breathe);

        // Pulsing ring at full charge
        if(progress >= 0.999f) {
            float ringAlpha = 0.75f + 0.25f * Mathf.sin(Time.time * 7f);
            Draw.color(glowColor, ringAlpha);
            Lines.stroke(1.8f);
            Lines.circle(x, y, radius * breathe);

            // Second inner ring
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
        copy.glowRadius = glowRadius;
        copy.glowColor = glowColor;
        return copy;
    }
}