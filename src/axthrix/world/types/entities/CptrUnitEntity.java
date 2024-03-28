package axthrix.world.types.entities;

import axthrix.content.units.AxthrixUnits;
import axthrix.world.types.unittypes.*;
import axthrix.world.types.parts.Propeller;
import axthrix.world.types.parts.Propeller.PropellerMount;
import arc.math.*;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.gen.UnitEntity;
import mindustry.type.UnitType;

public class CptrUnitEntity extends UnitEntity {
	public PropellerMount[] propellers;
	public float rotorSpeedScl = 1f;

	@Override
	public String toString() {
		return "CptrUnit#" + id;
	}

	@Override
	public int classId() {
		return AxthrixUnits.classID(getClass());
	}

	/** @author GlennFolker#6881 */
	@Override
	public void setType(UnitType type) {
		super.setType(type);
		if (type instanceof CopterUnitType cptr) {
			propellers = new PropellerMount[cptr.propeller.size];
			for (int i = 0; i < propellers.length; i++) {
				Propeller propellerType = cptr.propeller.get(i);
				propellers[i] = new PropellerMount(propellerType);
			}
		}
	}

	@Override
	public void update() {
		super.update();
		CopterUnitType type = (CopterUnitType) this.type;
		float rX = x + Angles.trnsx(rotation - 90, type.fallSmokeX, type.fallSmokeY);
		float rY = y + Angles.trnsy(rotation - 90, type.fallSmokeX, type.fallSmokeY);

		// Slows down rotor when dying
		if (dead || health() <= 0) {
			rotation += Time.delta * (type.spinningFallSpeed * vel().len()) * Mathf.signs[id % 2];
			if (Mathf.chanceDelta(type.fallSmokeChance)) {
				Fx.fallSmoke.at(rX, rY);
				Fx.burning.at(rX, rY);
			}
			rotorSpeedScl = Mathf.lerpDelta(rotorSpeedScl, 0f, type.rotorDeathSlowdown);
		} else {
			rotorSpeedScl = Mathf.lerpDelta(rotorSpeedScl, 1f, type.rotorDeathSlowdown);
		}

		for (PropellerMount propeller : propellers) {
			propeller.rotorRotation += ((propeller.propeller.rotorSpeed * rotorSpeedScl) + propeller.propeller.minimumRotorSpeed) * Time.delta;
		}
		type.fallSpeed = 0.006f;
	}
}
