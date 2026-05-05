package axthrix.world.types.unittypes;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.struct.ObjectMap;
import axthrix.world.types.ai.WallBreakerAI;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;

public class WallBreakerUnitType extends AmmoLifeTimeUnitType {

    public ObjectMap<Unit, Building> tetherBuilding = new ObjectMap<>();
    public ObjectMap<Unit, Float> drillTime = new ObjectMap<>();
    public float maxDrillTime;

    public WallBreakerUnitType(String name) {
        super(name);
        useUnitCap = false;
    }

    @Override
    public void update(Unit unit) {
        Building tether = tetherBuilding.get(unit);
        if (tether == null || !tether.isValid() || tether.team != unit.team) {
            if (unit.isAdded()) {
                Call.unitDespawn(unit.self());
            }
        }
    }



    @Override
    public void draw(Unit unit) {
        super.draw(unit);
        if (!(unit.controller() instanceof WallBreakerAI ai)) return;
        if (unit != Vars.control.input.selectedUnit()) return;
        Building home = tetherBuilding.get(unit);
        if (home == null) return;

        // Green line to spawner
        Lines.stroke(3f, Pal.gray);
        Lines.line(unit.x, unit.y, home.x, home.y);
        Lines.stroke(1f, Color.green);
        Lines.line(unit.x, unit.y, home.x, home.y);

        // Yellow line to target tile if mining
        if (ai.targetTile != null && ai.state == WallBreakerAI.State.mining) {
            Lines.stroke(3f, Pal.gray);
            Lines.line(unit.x, unit.y, ai.targetTile.worldx(), ai.targetTile.worldy());
            Lines.stroke(1f, Color.yellow);
            Lines.line(unit.x, unit.y, ai.targetTile.worldx(), ai.targetTile.worldy());
        }

        Draw.color();
        Lines.stroke(1f);
    }
}