package axthrix.world.types.ai;

import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.entities.units.AIController;

public class AttackDroneAI extends ControlledDroneAI{
    Unit owner;
        public AttackDroneAI(Unit owner){
            super(owner);
        }
        @Override
        public void updateMovement() {
        if(owner.isShooting){
        if (unit.hasWeapons()) {
        posTeam.set(owner.aimX, owner.aimY);

        if (unit.type.circleTarget) {
        circleAttack(120f);
        } else {
        moveTo(posTeam, unit.type.range * 0.75f);
        unit.lookAt(posTeam);
        }
        }
        } else {
        rally();
        }
        }
        @Override
        public Teamc target(float x, float y,float range, boolean air, boolean ground) {
            if(!owner.isValid() && !owner.isShooting){return null;} else return posTeam;
        }
        @Override
        public boolean shouldShoot() {
            if (owner.isShooting) return true; else return false;
        }
        }
