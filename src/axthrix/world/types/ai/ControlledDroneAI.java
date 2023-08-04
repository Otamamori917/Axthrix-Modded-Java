package axthrix.world.types.ai;

import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import axthrix.world.types.abilities.DroneControlAbility;
import mindustry.entities.units.AIController;
import mindustry.gen.Call;
import mindustry.gen.PosTeam;
import mindustry.gen.Unit;

public class ControlledDroneAI extends AIController {
        Unit owner;

        public ControlledDroneAI(mindustry.gen.Unit owner) {
            this.owner = owner;
            rally();

        }

        Vec2 rallyPos = new Vec2();
        PosTeam posTeam = PosTeam.create();
        @Override
        public void updateUnit() {
        if(!owner.isValid()) {
        Call.unitDespawn(unit);
        }
        super.updateUnit();
        }
        @Override
        public void updateMovement() {
        rally();
        }
        void rally(Vec2 pos) {
        rallyPos = pos;
        }
        public void rally() {
                if (owner != null){
        Tmp.v2.set(owner.x, owner.y);
        moveTo(Tmp.v1.set(rallyPos).add(Tmp.v2).rotateAround(Tmp.v2, owner.rotation - 90), 2f, 0.6f);
        unit.rotation(Angles.moveToward(unit.rotation, owner.rotation, unit.type.rotateSpeed));
        }}
        }
