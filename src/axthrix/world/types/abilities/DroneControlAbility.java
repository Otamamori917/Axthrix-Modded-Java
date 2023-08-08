package axthrix.world.types.abilities;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Time;
import axthrix.world.types.ai.AttackDroneAI;
import axthrix.world.types.ai.ControlledDroneAI;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.UnitTypes;
import mindustry.entities.Effect;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.AIController;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.type.UnitType;
import mindustry.game.EventType.UnitCreateEvent;

import java.util.ArrayList;



public class DroneControlAbility extends Ability {
        public UnitType unitSpawn = UnitTypes.avert;
        public float constructTime = 60f;
        public float spawnX = 0f;
        public float spawnY = 0f;
        public Effect spawnEffect = Fx.spawn;
        public boolean parentizeEffects = false;
        public ArrayList<Vec2> rallyPos = new ArrayList<Vec2>();
        public float layer = Layer.flyingUnitLow - 0.01f;
        public float rotation = 0f;
        public boolean autoRelease = true;
        public int droneCount = 2;
        protected float timer = 0f;
        ArrayList<Unit> units = new ArrayList<Unit>();

        public Ability copy(){
        ArrayList<Unit> units;
                return super.copy();
        }
        Unit returnableUnit;
        public void setController(Unit owner, String controller){
                if (controller == "AttackDroneAI"){
        AIController ai = new AttackDroneAI(owner);
        }else{AIController ai = new ControlledDroneAI(owner);}}
        @Override
        public void update(Unit unit) {
            Log.info(constructTime);
            Log.info(timer);
                        for (int i = 0; i < units.size(); i++) {
                                if (!units.get(i).isValid()) {
                                        units.remove(i);
                                }
                                if (units.size() < droneCount) {

                                        if (timer > constructTime) {


                                                if (autoRelease || unit.isShooting) {

                                                        float x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX);
                                                        float y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX);
                                                        returnableUnit = unit;
                                                        if (parentizeEffects) {
                                                            spawnEffect.at(x, y, 0f, unit);
                                                        }
                                                        Unit unitSpawned = this.unitSpawn.create(unit.team);
                                                        unitSpawned.set(x, y);
                                                        unitSpawned.rotation = unit.rotation + rotation;
                                                        this.units.add(0, unitSpawned);
                                                        unitSpawned.controller(new ControlledDroneAI(unit));

                                                        Events.fire(new UnitCreateEvent(unitSpawned, null, unit));
                                                        if(!Vars.net.client()){
                                                            unitSpawned.add();
                                                        }

                                                        timer %= constructTime;
                                                }

                                        } else timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team);
                                }
                        }



        {
            if (units.size() < droneCount) Draw.draw(layer, () -> {
                float x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX);
                float y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX);

                if (timer <= constructTime) Drawf.construct(
                    x, y, unitSpawn.fullIcon,
                    unit.rotation - 90 + rotation,
                    timer / constructTime,
                    1f, timer
                );
            else Draw.rect(unitSpawn.fullIcon, x, y, unit.rotation - 90 + rotation);
            });
        }
    }
    public Unit returnOwner(){
                return returnableUnit;
        }
}