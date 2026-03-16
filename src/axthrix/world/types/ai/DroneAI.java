package axthrix.world.types.ai;


import arc.graphics.g2d.Draw;
import arc.math.Angles;
import arc.math.geom.Vec2;
import arc.util.Tmp;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.abilities.SacrificeProtocolAbility;
import axthrix.world.types.unittypes.DroneUnitType;
import mindustry.content.UnitTypes;
import mindustry.entities.Predict;
import mindustry.entities.Units;
import mindustry.entities.units.AIController;
import mindustry.gen.Teamc;
import mindustry.type.Weapon;
import mindustry.type.weapons.BuildWeapon;

public class DroneAI extends AIController {
	@Override
	public void updateMovement() {
		if(unit.type instanceof DroneUnitType u){
			for(var ability : unit.abilities){
				if(ability instanceof SacrificeProtocolAbility sac && sac.isKamikazing()){
					return;
				}
			}
		}
		if ((unit.type instanceof DroneUnitType u) && u.tetherUnit.get(unit) != null) {
			var un = u.tetherUnit.get(unit);

			if(un.type.abilities.get(u.droneSlot.get(unit)) instanceof DroneSpawnAbility abl) {
				var ablWarmup = abl.warmup.get(un);

				float offsetX = abl.getPoscMath(ablWarmup, abl.dY, abl.moveY);
				float offsetY = abl.getPoscMath(ablWarmup, abl.dX, abl.moveX);

				Tmp.v1.set(offsetX, offsetY).rotate(un.rotation);

				unit.set(un.x + Tmp.v1.x, un.y + Tmp.v1.y);

				if(u.isShield){
					// Shield drones: always use formation rotation
					float relativeRotation = abl.getRotShooter(un, ablWarmup, abl.dRot, abl.moveRot, true);
					unit.rotation = un.rotation + relativeRotation;
				}else{
					// Weapon drones: formation rotation normally, override when shooting
					if(!un.isShooting){
						float relativeRotation = abl.getRotShooter(un, ablWarmup, abl.dRot, abl.moveRot, false);
						unit.rotation = un.rotation + relativeRotation;
					}else{
						// When shooting: match tether rotation
						unit.rotation = un.rotation;
					}
				}
			}
		}
	}

	@Override
	public void updateWeapons(){
		if(unit.type instanceof DroneUnitType u){
			for(var ability : unit.abilities){
				if(ability instanceof SacrificeProtocolAbility sac && sac.isKamikazing()){
					unit.isShooting = false;
					return;
				}
			}
		}
		if((unit.type instanceof DroneUnitType u) && u.tetherUnit.get(unit) != null && !u.tetherUnit.get(unit).dead){
			var un = u.tetherUnit.get(unit);

			if(unit.type.weapons.size > 0){
				for(var mount : unit.mounts) {
					Weapon weapon = mount.weapon;
					if(weapon instanceof BuildWeapon bw){
						if(un.activelyBuilding() && unit.buildPlan() != null){
							unit.buildPlan().block = un.buildPlan().block;
							mount.aimX = un.buildPlan().drawx();
							mount.aimY = un.buildPlan().drawy();;

							float z = Draw.z(),
									rotation = unit.rotation - 90,
									weaponRotation  = rotation + (bw.rotate ? mount.rotation : 0),
									wx = unit.x + Angles.trnsx(rotation, bw.x, bw.y) + Angles.trnsx(weaponRotation, 0, -mount.recoil),
									wy = unit.y + Angles.trnsy(rotation, bw.x, bw.y) + Angles.trnsy(weaponRotation, 0, -mount.recoil),
									px = wx + Angles.trnsx(weaponRotation, bw.shootX, bw.shootY),
									py = wy + Angles.trnsy(weaponRotation, bw.shootX, bw.shootY);

							unit.drawBuildingBeam(px, py);
							Draw.z(z);
						}

					}
				}
			}

			if(u.isShield){
				// Shield drones: just sync shooting state
				unit.isShooting = un.isShooting();
				return;
			}

			// Weapon drones
			if(un.isShooting){
				// When shooting: override rotation and aim at tether's target
				unit.rotation = un.rotation;
				unit.aimX = un.aimX;
				unit.aimY = un.aimY;
			}

			unit.isShooting = un.isShooting();

			// Update weapons
			for(var mount : unit.mounts) {
				Weapon weapon = mount.weapon;
				if (!weapon.aiControllable) {
					mount.rotate = false;
					continue;
				}
				if (weapon.noAttack) continue;

				if(un.isShooting){
					// Aim weapon at tether's target with prediction
					Vec2 aimVec = new Vec2(un.aimX, un.aimY);
					Vec2 to = Predict.intercept(unit, aimVec, weapon.bullet.speed);

					mount.aimX = to.x;
					mount.aimY = to.y;
				}

				mount.shoot = un.isShooting;
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