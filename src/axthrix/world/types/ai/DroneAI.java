package axthrix.world.types.ai;


import arc.math.*;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.util.*;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.part.*;
import mindustry.entities.units.*;
import mindustry.gen.Teamc;
import mindustry.type.Weapon;

public class DroneAI extends AIController {
	public float getRotation() {
		if (!(unit.type instanceof DroneUnitType u) || !(u.tetherUnit.type.abilities.peek() instanceof DroneSpawnAbility abl)) return 0f;
		if (u.tetherUnit.mounts().length > 0) {
			WeaponMount first = u.tetherUnit.mounts()[0];
			DrawPart.params.set(first.warmup, first.reload / u.tetherUnit.type().weapons.first().reload, first.smoothReload, first.heat, first.recoil, first.charge, u.tetherUnit.x(), u.tetherUnit.y(), u.tetherUnit.rotation());
		} else {
			DrawPart.params.set(0, 0, 0, 0, 0, 0, u.tetherUnit.x(), u.tetherUnit.y(), u.tetherUnit.rotation());
		}
		return Mathf.lerp(
			   u.tetherUnit.rotation + abl.startAng,
                u.tetherUnit.rotation + abl.endAng,
			abl.ShootProg(u.tetherUnit)
		);
	}

	@Override
	public void updateMovement() {
		if ( (unit.type instanceof DroneUnitType u) && (u.tetherUnit.type.abilities.peek() instanceof DroneSpawnAbility abl)) {
			unit.set(Tmp.v1.add(Mathf.lerp(u.tetherUnit.x+abl.startX,u.tetherUnit.x+abl.endX,abl.ShootProg(u.tetherUnit)),Mathf.lerp(u.tetherUnit.y+abl.startY,u.tetherUnit.y+abl.endY,abl.ShootProg(u.tetherUnit))));
			unit.rotation(getRotation());
		}
	}

	public void updateWeapons(){
		if((unit.type instanceof DroneUnitType u) && u.tetherUnit != null && !u.tetherUnit.dead && unit.type.canAttack){

			Vec2 aimVec = Predict.intercept(vec , new Vec2(u.tetherUnit.aimX, u.tetherUnit.aimY), unit.type.weapons.first().bullet.speed);
			if(!u.tetherUnit.isShooting) aimVec = Predict.intercept(vec, unit, unit.speed());
			/*I don't know which one worked so have all of them*/
			unit.aimLook(aimVec); unit.lookAt(aimVec); unit.aim(aimVec);
			unit.isShooting = u.tetherUnit.isShooting();

			for(var mount : unit.mounts) {
				Weapon weapon = mount.weapon;
				//let uncontrollable weapons do their own thing
				if (!weapon.aiControllable) {
					mount.rotate = false;
					continue;
				}
				if (weapon.noAttack) continue;

				Vec2 to = Predict.intercept(vec, aimVec, weapon.bullet.speed);
				mount.aimX = to.x;
				mount.aimY = to.y;
				mount.shoot = u.tetherUnit.isShooting;
			}
		} else{
			super.updateWeapons();
			if(!unit.isShooting && unit().hasWeapons()){
				/*shoot range regardless if it's the target & there's enemies nearby*/
				Teamc check = Units.closestTarget(unit.team, unit.x, unit.y, unit.range(), u -> u.checkTarget(unit.type.targetAir, unit.type.targetGround));
				unit.isShooting = check != null;
			}
		}
	}

}
