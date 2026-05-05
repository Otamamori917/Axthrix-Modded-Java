package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.*;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.util.draw.AxDrawf;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

import static mindustry.Vars.ui;

public class DroneSpawnAbility extends Ability {
    public DroneUnitType drone;
    public float spawnTime = 510;
    public Effect spawnEffect;
    public boolean parentizeEffects;
    public int droneSlot = 0;
    public int number = 0;
    public boolean spawnOnShoot = false; // New: spawn only when tether shoots

    public HashMap<Unit, Unit> aliveUnit = new HashMap<>();
    public HashMap<Unit, Float> warmup = new HashMap<>();
    public float timer;
    protected boolean hasKamikaze = false;
    protected SacrificeProtocolAbility kamikazeAbility = null;
    public float dY;
    public float dX;
    public float dRot;
    public float moveY;
    public float moveX;
    public float moveRot;

    public DroneSpawnAbility(DroneUnitType unit, float spawTime, float spawX, float spawY) {
        spawnEffect = Fx.spawn;
        drone = unit;
        spawnTime = spawTime;
        dY = spawY;
        dX = spawX;
    }

    public DroneSpawnAbility() {
        spawnEffect = Fx.spawn;
    }

    public void addStats(Table t) {
        t.add("[lightgray]" + Stat.buildTime.localized() + ": [white]" + Strings.autoFixed(spawnTime / 60.0F, 2) + " " + StatUnit.seconds.localized());
        t.row();

        if(spawnOnShoot){
            t.add("[lightgray]"+Core.bundle.format("stat.aj-spawns-on-shoot"));
            t.row();
        }

        t.table( u -> {
            u.image(drone.uiIcon).scaling(Scaling.fit).left();
            u.table(in -> {
                in.add(drone.localizedName).row();
                if (Core.settings.getBool("console")) in.add("[lightgray]" +drone.name + "[]");
            }).center().pad(10f).growX();
            u.button("?", Styles.flatBordert, () -> ui.content.show(drone)).right().growY().visible(drone::unlockedNow).size(40f);
        });
    }

    public void update(Unit unit) {

        if (!aliveUnit.containsKey(unit)){
            aliveUnit.put(unit, null);
        }
        if (!warmup.containsKey(unit)){
            warmup.put(unit, 0f);
        }else{
            warmup.replace(unit, Mathf.lerpDelta(warmup.get(unit), unit.isShooting ? 1.0F : 0.0F, 0.1f));
        }


        // Check if unit stopped shooting and we're in spawn-on-shoot mode
        if(spawnOnShoot && !unit.isShooting && aliveUnit.get(unit) != null && aliveUnit.get(unit).isValid()){
            Unit droneUnit = aliveUnit.get(unit);




            for(Ability ability : droneUnit.abilities){
                if(ability instanceof SacrificeProtocolAbility kamikaze){
                    hasKamikaze = true;
                    kamikazeAbility = kamikaze;
                    break;
                }
            }

            if(hasKamikaze && kamikazeAbility != null){
                // Damage drone to just below threshold to trigger kamikaze
                // Calculate damage needed: current health - (maxHealth * threshold) + 1
                float thresholdHealth = droneUnit.maxHealth * kamikazeAbility.healthThreshold;
                float damageNeeded = droneUnit.health - thresholdHealth + 1f;

                if(damageNeeded > 0){
                    droneUnit.damage(damageNeeded);
                }
                // The SacrificeProtocolAbility will handle the rest on its next update
            }else{
                // No kamikaze, just despawn
                Call.unitDespawn(droneUnit);
                aliveUnit.replace(unit, null);
            }

            // Reset timer
            timer = 0f;
        }

        // Only increment timer if:
        // - Not spawn-on-shoot mode, OR
        // - Spawn-on-shoot mode AND unit is shooting
        if(!spawnOnShoot || unit.isShooting){
            timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team);
        }

        // Spawn conditions
        boolean shouldSpawn = timer >= spawnTime &&
                Units.canCreate(unit.team, drone) &&
                canReplace(unit);

        // For spawn-on-shoot, also require unit to be shooting
        if(spawnOnShoot){
            shouldSpawn = shouldSpawn && unit.isShooting;
        }

        if (shouldSpawn){
            float offsetX = getPoscMath(warmup.get(unit), dX, moveX);
            float offsetY = getPoscMath(warmup.get(unit), dY, moveY);
            Tmp.v1.set(offsetX, offsetY).rotate(unit.rotation - 90f);
            float worldX = unit.x + Tmp.v1.x;
            float worldY = unit.y + Tmp.v1.y;
            float rotation = unit.rotation + getRotShooter(unit, warmup.get(unit), dRot, moveRot, drone.isShield);

            spawnEffect.at(worldX, worldY, 0.0F, parentizeEffects ? unit : null);

            Unit u = drone.create(unit.team);

            if (!drone.tetherUnit.containsKey(u)){
                drone.tetherUnit.put(u, unit);
            }else{
                drone.tetherUnit.replace(u, unit);
            }

            aliveUnit.replace(unit, u);
            aliveUnit.get(unit).set(worldX, worldY);
            aliveUnit.get(unit).rotation = rotation;

            Events.fire(new EventType.UnitCreateEvent(aliveUnit.get(unit), (Building)null, unit));

            if (!Vars.net.client()) {
                aliveUnit.get(unit).add();
            }

            if(!drone.droneSlot.containsKey(aliveUnit.get(unit))){
                drone.droneSlot.put(aliveUnit.get(unit), droneSlot);
            }else{
                drone.droneSlot.replace(aliveUnit.get(unit), droneSlot);
            }

            timer = 0.0F;
        }
    }

    public void draw(Unit unit) {
        if (Units.canCreate(unit.team, drone) && canReplace(unit) && warmup.containsKey(unit)) {
            // Don't show construction preview if spawn-on-shoot and not shooting
            if(spawnOnShoot && !unit.isShooting) return;

            Draw.draw(Draw.z(), () -> {
                float offsetX = getPoscMath(warmup.get(unit), dY, moveY);
                float offsetY = getPoscMath(warmup.get(unit), dX, moveX);

                Tmp.v1.set(offsetX, offsetY).rotate(unit.rotation);

                float worldX = unit.x + Tmp.v1.x;
                float worldY = unit.y + Tmp.v1.y;

                float rotation = unit.rotation + getRotShooter(unit, warmup.get(unit), dRot, moveRot, drone.isShield);

                AxDrawf.materialize(
                        worldX,
                        worldY,
                        drone.fullIcon,
                        unit.team.color,
                        rotation - 90f,
                        0.1f,
                        timer / spawnTime,
                        -timer / 4f
                );
            });
        }
    }

    public boolean canReplace(Unit unit){
        return aliveUnit.get(unit) == null || !aliveUnit.get(unit).isValid() || aliveUnit.get(unit).team != unit.team;
    }

    public float getPoscMath(float partProgress, float startVal, float endVal){
        return endVal * partProgress + startVal;
    }

    public float getRotShooter(Unit unit,float partProgress, float droneRotST, float droneRotED, boolean nonRotate){
        if(!nonRotate){
            return ((droneRotED * (1.0f - partProgress)) + unit.rotation) * partProgress + (droneRotST * (1.0f - partProgress));
        }else{
            return droneRotED * partProgress + droneRotST;
        }
    }

    public String localized() {
        return Core.bundle.format(number <= 1 ? "ability.aj-dronespawn" : "ability.aj-dronespawn-numbered", number <= 1 ? new Object[]{drone.localizedName} : new Object[]{drone.localizedName,number});
    }
}