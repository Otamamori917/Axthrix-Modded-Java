package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.gen.*;
import mindustry.type.StatusEffect;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class SacrificeProtocolAbility extends Ability {
    public float healthThreshold = 0.2f;
    public float kamikazeDamage = 200f;
    public float kamikazeRadius = 40f;
    public float kamikazeRange = 200f;
    public float seekSpeed = 4f;
    public StatusEffect debuff = StatusEffects.blasted;
    public float debuffDuration = 180f;
    public Effect explosionEffect = Fx.massiveExplosion;
    public Color explosionColor = Color.orange;

    protected boolean triggered = false;
    protected boolean exploded = false;
    protected Teamc kamikazeTarget = null;
    protected float kamikazeTimer = 0f;
    protected float maxKamikazeDuration = 180f;
    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-sacrifice-protocol");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]"+Core.bundle.format("stat.aj-trigger")+": [white]<" + (int)(healthThreshold * 100f) + "% " + Core.bundle.format("stat.aj-health"));
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-explosion") +" "+ Stat.damage.localized() + ": [white]" + (int)kamikazeDamage);
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-explosion") +" "+ Stat.shootRange.localized() + ": [white]" + (int)(kamikazeRadius / 8f) +" "+ StatUnit.blocks.localized());
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-targeting") +" "+ Stat.shootRange.localized() + ": [white]" + (int)(kamikazeRange / 8f) +" "+ StatUnit.blocks.localized());
        t.row();
    }

    @Override
    public void update(Unit unit){
        if(exploded) return;

        if(!(unit.type instanceof DroneUnitType drone)) return;
        if(drone.isShield) return;

        if(!triggered && unit.healthf() < healthThreshold){
            triggered = true;

            kamikazeTarget = Units.closestEnemy(unit.team, unit.x, unit.y, kamikazeRange, u -> true);

            if(kamikazeTarget == null){
                explode(unit);
                return;
            }
        }

        if(triggered && kamikazeTarget != null){
            kamikazeTimer += Time.delta;

            float targetX = kamikazeTarget.getX();
            float targetY = kamikazeTarget.getY();

            float angle = Mathf.angle(targetX - unit.x, targetY - unit.y);

            unit.vel.trns(angle, seekSpeed);
            unit.rotation = Mathf.slerpDelta(unit.rotation, angle, 0.2f);

            if(unit.within(targetX, targetY, kamikazeRadius * 0.5f) || kamikazeTimer >= maxKamikazeDuration){
                explode(unit);
                return;
            }

            boolean targetDead = false;
            if(kamikazeTarget instanceof Unit targetUnit){
                targetDead = !targetUnit.isValid() || targetUnit.dead;
            }else if(kamikazeTarget instanceof Building targetBuilding){
                targetDead = !targetBuilding.isValid() || targetBuilding.dead;
            }

            if(targetDead){
                kamikazeTarget = Units.closestEnemy(unit.team, unit.x, unit.y, kamikazeRange, u -> true);
                if(kamikazeTarget == null){
                    explode(unit);
                }
            }
        }
    }

    protected void explode(Unit unit){
        if(exploded) return;
        exploded = true;

        explosionEffect.at(unit.x, unit.y, 0f, explosionColor);

        mindustry.entities.Damage.damage(unit.team, unit.x, unit.y, kamikazeRadius, kamikazeDamage, true, true);

        Units.nearby(null, unit.x, unit.y, kamikazeRadius, other -> {
            if(other.team != unit.team){
                other.apply(debuff, debuffDuration);
            }
        });
        unit.health = 0f;
        unit.dead = true;
        unit.kill();
    }

    public boolean isKamikazing(){
        return triggered;
    }
}