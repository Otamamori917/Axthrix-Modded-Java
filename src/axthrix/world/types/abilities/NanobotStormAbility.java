package axthrix.world.types.abilities;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.Table;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

import static axthrix.content.AxthrixStatus.*;
import static mindustry.Vars.*;

public class NanobotStormAbility extends Ability {


    private static final HashMap<Unit, Seq<Nanobot>> unitNanobots = new HashMap<>();

    public float damage = 4f;
    public float healAmount = 4f;
    public float healPercent = 1f;
    public float range = 120f;
    /// rate at which ability does anything
    public float tickRate = 1f;
    public float buildingDamageMultiplier = 0.25f;
    /// how much it speeds up allies bullets per tick
    public float bulletSpeedBonus = 1.02f;
    /// how much it slows enemy bullets per tick
    public float bulletSlowdown = 0.94f;

    /// block bonus to efficiency
    public float efficiencyBoost = 1.1f;

    /// how much less healing it does to a unit also with nanobot storm
    public float stackPenalty = 0.5f;

    public StatusEffect status = nanodiverge;
    public float statusDuration = 60f * 3f;

    public Effect healEffect = Fx.heal;
    public Effect damageEffect = Fx.hitLaserBlast;
    public Sound ambientSound = Sounds.loopFlux;
    public float soundVolume = 0.3f;

    public Color color = Pal.heal;
    public int nanobotCount = 25;
    public float nanobotSize = 0.5f;
    public float nanobotSpeed = 0.5f;

    public boolean useAmmo = true;

    protected float timer = 0f;
    protected float soundTimer = 0f;
    protected float boostEffectTimer = 0f;

    public NanobotStormAbility(){}

    public NanobotStormAbility(float damage, float healAmount, float range){
        this.damage = damage;
        this.healAmount = healAmount;
        this.range = range;
    }

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-nanobot-storm");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.damage.localized() + ": [white]" + Strings.autoFixed(damage * (60f / tickRate), 2) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]Building Damage: [white]" + Strings.autoFixed(damage * buildingDamageMultiplier * (60f / tickRate), 2) + " " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.healing.localized() + ": [white]" + Strings.autoFixed(healAmount * (60f / tickRate), 2) + " + " + Strings.autoFixed(healPercent, 1) + "% " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]" + Stat.range.localized() + ": [white]" + Strings.autoFixed(range / tilesize, 2) + " " + StatUnit.blocks.localized());
        t.row();

        // Calculate exponential bullet effect
        float allySpeedIncrease = Mathf.pow(bulletSpeedBonus, 60f / tickRate) - 1f;
        float enemySpeedDecrease = 1f - Mathf.pow(bulletSlowdown, 60f / tickRate);

        t.add("[lightgray]Ally Bullet Speed: [white]+" + (int)(allySpeedIncrease * 100f) + "% " + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]Enemy Bullet Slow: [white]-" + (int)(enemySpeedDecrease * 100f) + "% "  + StatUnit.perSecond.localized());
        t.row();
        t.add("[lightgray]Block Efficiency: [white]" + (int)(efficiencyBoost * 100f) + "%");
        t.row();
    }

    protected void initNanobots(Unit unit){
        if(!unitNanobots.containsKey(unit)){
            Seq<Nanobot> nanobots = new Seq<>();
            for(int i = 0; i < nanobotCount; i++){
                nanobots.add(new Nanobot(unit.x, unit.y));
            }
            unitNanobots.put(unit, nanobots);
        }
    }

    // Check if a unit has this ability
    protected boolean hasNanobotStorm(Unit unit){
        for(Ability ability : unit.abilities){
            if(ability instanceof NanobotStormAbility) return true;
        }
        return false;
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);

        // Initialize nanobots if not done yet
        if(!unitNanobots.containsKey(unit)){
            initNanobots(unit);
        }

        Seq<Nanobot> nanobots = unitNanobots.get(unit);
        if(nanobots == null) return;

        Draw.z(Layer.flyingUnit + 1);

        // Draw each nanobot
        for(Nanobot nano : nanobots){
            nano.update(unit, range, nanobotSpeed);

            // Draw nanobot glow
            Draw.color(color, nano.alpha);
            Fill.circle(nano.x, nano.y, nanobotSize * nano.scale);

            // Draw nanobot core
            Draw.color(Color.white, nano.alpha * 0.7f);
            Fill.circle(nano.x, nano.y, nanobotSize * nano.scale * 0.5f);

            // Draw trail if moving toward target
            if(nano.targetX != 0 || nano.targetY != 0){
                Draw.color(color, nano.alpha * 0.3f);
                Lines.stroke(1f);
                Lines.line(nano.x, nano.y,
                        Mathf.lerp(nano.x, nano.orbitX, 0.3f),
                        Mathf.lerp(nano.y, nano.orbitY, 0.3f));
            }
        }

        // Draw range indicator faintly
        Draw.color(color, 0.15f);
        Lines.stroke(1.5f);
        Lines.circle(unit.x, unit.y, range);

        Draw.z(Layer.effect);

        float scanProgress = boostEffectTimer / 90f; // 0 to 1 over 90 ticks
        float scanAngle = unit.rotation + (scanProgress * -360f); // Full rotation
        float scanWidth = 60f; // Width of scan beam in degrees

        Draw.color(color, 0.3f * (1f - scanProgress)); // Fade as it rotates
        Lines.stroke(2f);

        for(int i = 0; i < 3; i++){
            float offset = i * (scanWidth / 3f);
            float currentAngle = scanAngle + offset;
            float alpha = (1f - scanProgress) * (1f - i * 0.3f);

            Draw.alpha(alpha);
            Lines.arc(unit.x, unit.y, range, scanWidth / 360f, currentAngle);
        }

        Draw.color(color, 0.5f * (1f - scanProgress));
        Lines.stroke(1.5f);
        Tmp.v1.trns(scanAngle, range);
        Lines.line(unit.x, unit.y, unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);

        Draw.reset();
    }

    @Override
    public void update(Unit unit){
        // Initialize nanobots on first update
        if(!unitNanobots.containsKey(unit)){
            initNanobots(unit);
        }

        // Clean up dead units
        unitNanobots.entrySet().removeIf(entry -> !entry.getKey().isValid());

        timer += Time.delta;
        soundTimer += Time.delta;
        boostEffectTimer += Time.delta;

        // Play ambient sound
        if(soundTimer >= 30f){
            ambientSound.at(unit.x, unit.y, 1f, soundVolume);
            soundTimer = 0f;
        }


        // Apply block efficiency boost
        indexer.eachBlock(unit.team(), unit.x, unit.y, range, other -> other.team == unit.team, building -> {
            // Apply efficiency boost
            if(building.block.hasPower || building.block.hasLiquids){
                building.applyBoost(efficiencyBoost, 60f);

                // Send nanobots to building to show boost
                if(Mathf.chance(0.05f)){ // 5% chance per tick to send bots
                    assignNanobots(unit, building.x, building.y, true);
                }
            }
        });

        // Play boost effect periodically
        if(boostEffectTimer >= 90f){
            boostEffectTimer = 0f;
        }

        // Apply effects every tickRate
        if(timer >= tickRate && (!useAmmo || unit.ammo > 0 || !state.rules.unitAmmo)){
            timer = 0f;

            // Affect units in range
            Units.nearby(null, unit.x, unit.y, range, other -> {
                if(other == unit) return;

                if(other.team == unit.team){
                    // Heal allies
                    if(other.damaged()){
                        // Check if target also has nanobot storm
                        float healMultiplier = hasNanobotStorm(other) ? stackPenalty : 1f;

                        // Flat heal + percentage heal
                        float totalHeal = (healAmount + (other.maxHealth * healPercent / 100f)) * healMultiplier;
                        other.heal(totalHeal);

                        healEffect.at(other.x, other.y, 0f, color);

                        // Send nanobots toward ally
                        assignNanobots(unit, other.x, other.y, true);
                    }
                }else{
                    // Damage enemies
                    other.damage(damage * state.rules.unitDamage(unit.team));

                    // Apply status
                    other.apply(status, statusDuration);

                    damageEffect.at(other.x, other.y, 0f, color);

                    // Send nanobots toward enemy
                    assignNanobots(unit, other.x, other.y, false);
                }
            });

            // Affect buildings
            indexer.eachBlock(null, unit.x, unit.y, range, other -> true, building -> {
                if(building.team == unit.team){
                    // Heal ally buildings
                    if(building.damaged()){
                        // Flat heal + percentage heal
                        float totalHeal = healAmount + (building.maxHealth * healPercent / 100f);
                        building.heal(totalHeal);

                        Fx.healBlockFull.at(building.x, building.y, 0f, color, building.block);

                        // Send nanobots toward ally building
                        assignNanobots(unit, building.x, building.y, true);
                    }
                }else{
                    // Damage enemy buildings with reduced damage
                    building.damage(damage * buildingDamageMultiplier * state.rules.unitDamage(unit.team));
                    damageEffect.at(building.x, building.y, 0f, color);

                    // Send nanobots toward enemy building
                    assignNanobots(unit, building.x, building.y, false);
                }
            });

            // Affect bullets - exponential scaling per application
            Groups.bullet.intersect(unit.x - range, unit.y - range, range * 2, range * 2, bullet -> {
                if(bullet.within(unit.x, unit.y, range)){
                    if(bullet.team == unit.team){
                        // Speed up ally bullets exponentially
                        bullet.vel.scl(bulletSpeedBonus);

                        // Send nanobots to show speed boost
                        if(Mathf.chance(0.1f)){
                            assignNanobots(unit, bullet.x, bullet.y, true);
                        }
                    }else{
                        // Slow down enemy bullets exponentially
                        bullet.vel.scl(bulletSlowdown);

                        // Send nanobots to show slowdown
                        if(Mathf.chance(0.1f)){
                            assignNanobots(unit, bullet.x, bullet.y, false);
                            damageEffect.at(bullet.x, bullet.y, 0f, color);
                        }
                    }
                }
            });

            if(useAmmo && state.rules.unitAmmo){
                unit.ammo--;
            }
        }
    }

    // Assign some nanobots to fly toward a target
    protected void assignNanobots(Unit owner, float targetX, float targetY, boolean healing){
        Seq<Nanobot> nanobots = unitNanobots.get(owner);
        if(nanobots == null) return;

        int assigned = 0;
        int toAssign = Mathf.random(3, 6);

        for(Nanobot nano : nanobots){
            if(assigned >= toAssign) break;

            if(nano.targetX == 0 && nano.targetY == 0){
                nano.targetX = targetX;
                nano.targetY = targetY;
                nano.healing = healing;
                nano.targetTimer = 30f;
                assigned++;
            }
        }
    }

    @Override
    public void death(Unit unit){
        // Clean up nanobots when unit dies
        unitNanobots.remove(unit);
    }

    public static class Nanobot {
        public float x, y;
        public float orbitX, orbitY;
        public float targetX, targetY;
        public float angle;
        public float orbitRadius;
        public float verticalAngle; // Vertical angle for 3D spread
        public float alpha = 1f;
        public float scale = 1f;
        public boolean healing = false;
        public float targetTimer = 0f;

        public Nanobot(float spawnX, float spawnY){
            // Initialize at unit's spawn position
            x = spawnX;
            y = spawnY;

            // Random 3D position - spherical coordinates
            angle = Mathf.random(360f);
            verticalAngle = Mathf.random(360f); // Second angle for spherical distribution

            // Random radius within sphere (not just surface)
            // Use cube root for uniform volume distribution
            float randomFactor = Mathf.pow(Mathf.random(), 1f/3f);
            orbitRadius = Mathf.random(5f, 95f) * randomFactor;
        }

        public void update(Unit unit, float maxRange, float speed){
            // Update both angles for 3D-like movement
            angle += speed * Time.delta;
            verticalAngle += speed * Time.delta * 0.7f; // Different speed for variety

            // Randomly vary radius over time - bots move in and out
            if(Mathf.chance(0.02f)){
                float newRadius = Mathf.random(5f, maxRange * 0.95f);
                float randomFactor = Mathf.pow(Mathf.random(), 1f/3f);
                orbitRadius = Mathf.lerp(orbitRadius, newRadius * randomFactor, 0.1f);
            }

            // Calculate 3D position projected to 2D
            // Use both angles to create spherical distribution
            float horizontalRadius = orbitRadius * Mathf.cos(verticalAngle * Mathf.degreesToRadians);
            Tmp.v1.trns(angle, horizontalRadius);

            orbitX = unit.x + Tmp.v1.x;
            orbitY = unit.y + Tmp.v1.y;

            // If targeting something, fly toward it
            if(targetTimer > 0){
                targetTimer -= Time.delta;

                // Move toward target quickly
                x = Mathf.lerp(x, targetX, 0.3f);
                y = Mathf.lerp(y, targetY, 0.3f);

                // Constant size and visibility when working
                scale = 1.2f;
                alpha = 1f;

                // Reset target when timer expires
                if(targetTimer <= 0){
                    targetX = 0;
                    targetY = 0;
                }
            }else{
                // Normal orbit behavior
                x = Mathf.lerp(x, orbitX, 0.05f);
                y = Mathf.lerp(y, orbitY, 0.05f);

                // Vary size and alpha based on vertical angle (simulates depth)
                float depthFactor = (Mathf.sin(verticalAngle * Mathf.degreesToRadians) + 1f) / 2f;
                scale = 0.7f + depthFactor * 0.5f;
                alpha = 0.5f + depthFactor * 0.5f;
            }
        }
    }
}