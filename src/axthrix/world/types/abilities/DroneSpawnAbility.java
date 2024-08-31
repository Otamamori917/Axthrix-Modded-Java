package axthrix.world.types.abilities;

import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
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
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.type.UnitType;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import java.util.HashMap;

public class DroneSpawnAbility extends Ability {
    public UnitType drone;
    public float spawnTime = 60.0F;
    public Effect spawnEffect;
    public boolean parentizeEffects;
    public HashMap<Unit, Boolean> unitAlive = new HashMap<>();
    public HashMap<Unit, Unit> aliveUnit = new HashMap<>();
    protected float timer;
    public float startAng = 0f;
    public float endAng = 0f;
    public float startX = 0f;
    public float endX = 0f;
    public float startY = 0f;
    public float endY = 0f;

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
        t.add(drone.emoji() + " " + drone.localizedName);
    }

    public void update(Unit unit) {
        if (!aliveUnit.containsKey(unit)){
            aliveUnit.put(unit,null);
        }
        if (!unitAlive.containsKey(unit)){
            unitAlive.put(unit,false);
        }

        if (drone instanceof DroneUnitType du){
            du.tetherUnitID = unit.id;
        }
        if(aliveUnit.get(unit) == null){
            unitAlive.replace(unit,false);
        }
        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team);
        if (timer >= spawnTime && Units.canCreate(unit.team, drone) && !unitAlive.get(unit)) {

            spawnEffect.at(Mathf.lerp(unit.x+startX,unit.x+endX,ShootProg(unit)), Mathf.lerp(unit.y+startY,unit.y+endY,ShootProg(unit)), 0.0F, parentizeEffects ? unit : null);
            Unit u = drone.create(unit.team);
            u.set(Mathf.lerp(unit.x+startX,unit.x+endX,ShootProg(unit)), Mathf.lerp(unit.y+startY,unit.y+endY,ShootProg(unit)));
            u.rotation = unit.rotation + Mathf.lerp(startAng,endAng,ShootProg(unit));
            Events.fire(new EventType.UnitCreateEvent(u, (Building)null, unit));
            unitAlive.replace(unit,true);
            aliveUnit.put(unit,u);
            if (!Vars.net.client()) {
                u.add();
            }
            timer = 0.0F;
        }

    }

    public void draw(Unit unit) {
        if (Units.canCreate(unit.team, drone) && !unitAlive.get(unit)) {
            Draw.draw(Draw.z(), () -> {
                Drawf.construct(Mathf.lerp(unit.x+startX,unit.x+endX,ShootProg(unit)), Mathf.lerp(unit.y+startY,unit.y+endY,ShootProg(unit)), drone.fullIcon, (unit.rotation-90.0F)+Mathf.lerp(startAng,endAng,ShootProg(unit)), timer / spawnTime, 1.0F, timer);
            });
        }

    }
    public float ShootProg(Unit unit){
        if(unit.type.parts.first() instanceof RegionPart rp) {
            return rp.progress.get(DrawPart.params);
        }
        return 0f;
    }

    public String localized() {
        return Core.bundle.format("ability.dronespawn", new Object[]{drone.localizedName});
    }
}

