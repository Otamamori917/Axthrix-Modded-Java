package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import axthrix.world.types.unittypes.AmmoLifeTimeUnitType;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Units;
import mindustry.entities.abilities.Ability;
import mindustry.entities.part.DrawPart;
import mindustry.entities.part.RegionPart;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

import static mindustry.Vars.ui;

public class DroneSpawnAbility extends Ability {
    public UnitType drone;
    public float spawnTime = 510;
    public Effect spawnEffect;
    public boolean parentizeEffects;
    public HashMap<Unit, Unit> aliveUnit = new HashMap<>();
    protected float timer;
    public float dX;
    public float dY;
    public float dRot;
    public float moveX;
    public float moveY;
    public float moveRot;

    public DroneSpawnAbility(UnitType unit, float spawnTime, float spawnX, float spawnY) {
        spawnEffect = Fx.spawn;
        drone = unit;
        spawnTime = spawnTime;
        spawnX = spawnX;
        spawnY = spawnY;
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
        if (drone instanceof DroneUnitType du){
            du.tetherUnitID = unit.id;
        }
        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team);
        if (timer >= spawnTime && Units.canCreate(unit.team, drone) && canReplace(unit)){

            spawnEffect.at(getPoscMath(unit,unit.x+dX,moveX), getPoscMath(unit,unit.y+dY,moveY), 0.0F, parentizeEffects ? unit : null);
            Unit u = drone.create(unit.team);
            u.set(getPoscMath(unit,unit.x+dX,moveX), getPoscMath(unit,unit.y+dY,moveY));
            u.rotation = getPoscMath(unit,unit.rotation+dRot,moveRot);
            Events.fire(new EventType.UnitCreateEvent(u, (Building)null, unit));
            if (!Vars.net.client()) {
                u.add();
            }
            aliveUnit.replace(unit,u);
            timer = 0.0F;
        }
    }

    public void draw(Unit unit) {
        if (Units.canCreate(unit.team, drone) && canReplace(unit)) {
            Draw.draw(Draw.z(), () -> {
                Drawf.construct(getPoscMath(unit,unit.x+dX,moveX), getPoscMath(unit,unit.y+dY,moveY), drone.fullIcon, (getPoscMath(unit,unit.rotation+dRot,moveRot)), timer / spawnTime, 1.0F, timer);
            });
        }

    }
    public boolean canReplace(Unit unit){
        return aliveUnit.get(unit) == null || !aliveUnit.get(unit).isValid() || aliveUnit.get(unit).team != unit.team;
    }
    public float getPoscMath(Unit unit, float startVal, float endVal){
        if(unit.type.parts.first() instanceof RegionPart rp) {
            return endVal * rp.progress.get(DrawPart.params) + startVal;
        }
        return 0;
    }

    public String localized() {
        return Core.bundle.format("ability.aj-dronespawn", new Object[]{drone.localizedName});
    }
}

