package axthrix.world.types.abilities;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.entities.abilities.Ability;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class DamageDistributionAbility extends Ability {
    public float range = 80f;
    /// damage distributed to network
    public float sharePercent = 0.5f;
    public Color linkColor = Color.valueOf("84f491");
    public float linkWidth = 0.5f;

    protected float lastHealth = -1f;

    @Override
    public String localized(){
        return Core.bundle.format("ability.aj-damage-distribution");
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.range.localized() + ": [white]" + (int)(range / 8f) +" "+ StatUnit.blocks.localized());
        t.row();
        t.add("[lightgray]"+Core.bundle.format("stat.aj-damage-shared")+" [white]" + (int)(sharePercent * 100f) + "%");
        t.row();
    }

    @Override
    public void update(Unit unit){
        if(!(unit.type instanceof DroneUnitType)) return;

        if(lastHealth < 0) lastHealth = unit.health;
        float damageTaken = lastHealth - unit.health;
        if(damageTaken > 0.01f && !unit.dead()){
            Seq<Unit> nearbyDrones = new Seq<>();
            mindustry.gen.Groups.unit.each(other -> {
                if(other != unit &&
                        other.type instanceof DroneUnitType &&
                        other.team == unit.team &&
                        unit.within(other, range)){
                    nearbyDrones.add(other);
                }
            });

            if(nearbyDrones.size > 0){
                float sharedDamage = damageTaken * sharePercent;
                float damagePerDrone = sharedDamage / nearbyDrones.size;
                unit.heal(sharedDamage);
                for(Unit drone : nearbyDrones){
                    drone.damage(damagePerDrone);
                }
            }
        }

        lastHealth = unit.health;
    }

    @Override
    public void draw(Unit unit){
        if(!(unit.type instanceof DroneUnitType)) return;

        Draw.z(Layer.blockOver);
        Draw.color(linkColor);
        Lines.stroke(linkWidth);

        // Draw links to nearby drones
        mindustry.gen.Groups.unit.each(other -> {
            if(other != unit &&
                    other.type instanceof DroneUnitType &&
                    other.team == unit.team &&
                    unit.within(other, range)){

                // Pulse effect
                float alpha = 0.3f + Mathf.absin(Time.time + unit.id * 3f, 3f, 0.3f);
                Draw.alpha(alpha);
                Lines.line(unit.x, unit.y, other.x, other.y);
            }
        });

        Draw.reset();
    }
}