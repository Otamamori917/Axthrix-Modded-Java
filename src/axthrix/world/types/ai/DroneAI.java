package axthrix.world.types.ai;


import arc.math.geom.Vec2;
import arc.util.Tmp;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.content.Blocks;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.Teamc;
import mindustry.type.Weapon;

public class DroneAI extends AIController {

	@Override
	public void updateMovement() {
		if ((unit.type instanceof DroneUnitType u) && u.tetherUnit.get(unit) != null) {
			if (u.tetherUnit.get(unit).type.abilities.get(u.droneSlot.get(unit)) instanceof DroneSpawnAbility abl) {
				unit.set(Tmp.v1.set(abl.getPoscMath(abl.warmup.get(u.tetherUnit.get(unit)),u.tetherUnit.get(unit).x+abl.dX,abl.moveX), abl.getPoscMath(abl.warmup.get(u.tetherUnit.get(unit)),u.tetherUnit.get(unit).y+abl.dY,abl.moveY)));
				unit.rotation(abl.getRotShooter(u.tetherUnit.get(unit),abl.warmup.get(u.tetherUnit.get(unit)),abl.dRot,abl.moveRot,u.isShield));
			}
		}
	}
	@Override
	public void updateWeapons(){
		if((unit.type instanceof DroneUnitType u) && u.tetherUnit.get(unit) != null && !u.tetherUnit.get(unit).dead && !u.isShield){

			Vec2 aimVec = Predict.intercept(vec , new Vec2(u.tetherUnit.get(unit).aimX, u.tetherUnit.get(unit).aimY), unit.type.weapons.first().bullet.speed);
			if(!u.tetherUnit.get(unit).isShooting) aimVec = Predict.intercept(vec, unit, unit.speed());
			unit.aimLook(aimVec); unit.lookAt(aimVec); unit.aim(aimVec);
			unit.isShooting = u.tetherUnit.get(unit).isShooting();

			for(var mount : unit.mounts) {
				Weapon weapon = mount.weapon;
				if (!weapon.aiControllable) {
					mount.rotate = false;
					continue;
				}
				if (weapon.noAttack) continue;

				Vec2 to = Predict.intercept(vec, aimVec, weapon.bullet.speed);
				mount.aimX = to.x;
				mount.aimY = to.y;
				mount.shoot = u.tetherUnit.get(unit).isShooting;
			}
		} else{
			super.updateWeapons();
			if(!unit.isShooting && unit().hasWeapons()){
				Teamc check = Units.closestTarget(unit.team, unit.x, unit.y, unit.range(), u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround));
				unit.isShooting = check != null;
			}
		}
	}

}

