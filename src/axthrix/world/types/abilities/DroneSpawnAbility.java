package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.*;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
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
    public HashMap<Unit, Unit> aliveUnit = new HashMap<>();
    public HashMap<Unit, Float> warmup = new HashMap<>();
    protected float timer;
    public float dX;
    public float dY;
    public float dRot;
    public float moveX;
    public float moveY;
    public float moveRot;

    public DroneSpawnAbility(DroneUnitType unit, float spawTime, float spawX, float spawY) {
        spawnEffect = Fx.spawn;
        drone = unit;
        spawnTime = spawTime;
        dX = spawX;
        dY = spawY;
    }

    public DroneSpawnAbility() {
        spawnEffect = Fx.spawn;
    }

    public void addStats(Table t) {
        t.add("[lightgray]" + Stat.buildTime.localized() + ": [white]" + Strings.autoFixed(spawnTime / 60.0F, 2) + " " + StatUnit.seconds.localized());
        t.row();
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
        if (unit.dead()){
            aliveUnit.remove(unit);
        }
        if (!aliveUnit.containsKey(unit)){
            aliveUnit.put(unit,null);
        }
        if (!warmup.containsKey(unit)){
            warmup.put(unit,0f);
        }else{
            warmup.replace(unit,Mathf.lerpDelta(warmup.get(unit), unit.isShooting ? 1.0F : 0.0F, 0.1f));
        }
        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team);
        if (timer >= spawnTime && Units.canCreate(unit.team, drone) && canReplace(unit)){

            spawnEffect.at(getPoscMath(warmup.get(unit),unit.x+dX,moveX),getPoscMath(warmup.get(unit),unit.y+dY,moveY), 0.0F, parentizeEffects ? unit : null);
            Unit u = drone.create(unit.team);
            if (!drone.tetherUnit.containsKey(u)){
                drone.tetherUnit.put(u,unit);
            }else{
                drone.tetherUnit.replace(u,unit);
            }
            aliveUnit.replace(unit,u);
            aliveUnit.get(unit).set(getPoscMath(warmup.get(unit),unit.x+dX,moveX),getPoscMath(warmup.get(unit),unit.y+dY,moveY));
            aliveUnit.get(unit).rotation = getPoscMath(warmup.get(unit),dRot,moveRot);
            Events.fire(new EventType.UnitCreateEvent(aliveUnit.get(unit), (Building)null, unit));
            if (!Vars.net.client()) {
                aliveUnit.get(unit).add();
            }
            if(!drone.droneSlot.containsKey(aliveUnit.get(unit))){
                drone.droneSlot.put(aliveUnit.get(unit),droneSlot);
            }else{
                drone.droneSlot.replace(aliveUnit.get(unit),droneSlot);
            }
            timer = 0.0F;
        }
    }

    public void draw(Unit unit) {
        if (Units.canCreate(unit.team, drone) && canReplace(unit)) {
            Draw.draw(Draw.z(), () -> {
                Drawf.construct(getPoscMath(warmup.get(unit),unit.x+dX,moveX),getPoscMath(warmup.get(unit),unit.y+dY,moveY), drone.fullIcon, (getRotShooter(unit,warmup.get(unit),dRot,moveRot,drone.isShield)- 90), timer / spawnTime, 1.0F, timer);
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
        return Core.bundle.format("ability.aj-dronespawn", new Object[]{drone.localizedName});
    }
}

