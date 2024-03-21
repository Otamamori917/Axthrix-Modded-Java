package axthrix.world.types.unittypes;

import axthrix.world.types.entities.CptrUnitEntity;
import axthrix.world.util.*;
import axthrix.world.types.parts.Propeller;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.math.*;
import arc.struct.Seq;
import mindustry.gen.Unit;
import mindustry.graphics.*;

public class CopterUnitType extends AxUnitType {
	public final Seq<Propeller> propeller = new Seq<>();

	public float spinningFallSpeed = 0;
	public float rotorDeathSlowdown = 0.01f;
	public float fallSmokeX = 0f, fallSmokeY = -5f, fallSmokeChance = 0.1f;

	public CopterUnitType(String name) {
		super(name);
		engineSize = 0;
		constructor = CptrUnitEntity::new;
	}

	@Override
	public void drawSoftShadow(Unit unit, float alpha) {
		float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
		Draw.z(z - 3f);
		super.drawSoftShadow(unit, alpha);
	}

	// Drawing Rotors
	public void drawPropeller(Unit unit) {
		float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
		applyColor(unit);
		if (unit instanceof CptrUnitEntity cptr) {
			for (Propeller.PropellerMount mount : cptr.propellers) {
				Propeller propeller = mount.propeller;
				float rx = unit.x + Angles.trnsx(unit.rotation - 90, propeller.x, propeller.y);
				float ry = unit.y + Angles.trnsy(unit.rotation - 90, propeller.x, propeller.y);
				float rotorScl = Draw.scl * propeller.rotorSizeScl;
				float rotorTopScl = Draw.scl * propeller.rotorTopSizeScl;

				for (int i = 0; i < propeller.bladeCount; i++) {
					float angle = (i * 360f / propeller.bladeCount + mount.rotorRotation) % 360;
					float blurAngle = (i * 360f / propeller.bladeCount + (mount.rotorRotation * propeller.rotorBlurSpeedMultiplier)) % 360;

					// region Normal Rotor
					Draw.z(z + propeller.rotorLayer);
					Draw.alpha(propeller.bladeBlurRegion.found() ? 1 - (cptr.rotorSpeedScl / 0.8f) : 1);
					Draw.rect(
							propeller.bladeOutlineRegion, rx, ry,
							propeller.bladeOutlineRegion.width * rotorScl,
							propeller.bladeOutlineRegion.height * rotorScl,
						angle
					);
					Draw.mixcol(Color.white, unit.hitTime);
					Draw.rect(propeller.bladeRegion, rx, ry,
							propeller.bladeRegion.width * rotorScl,
							propeller.bladeRegion.height * rotorScl,
						angle
					);
					// endregion Normal Rotor

					// Double Rotor
					if (propeller.doubleRotor) {
						Draw.rect(
								propeller.bladeOutlineRegion, rx, ry,
								propeller.bladeOutlineRegion.width * rotorScl * -Mathf.sign(false),
								propeller.bladeOutlineRegion.height * rotorScl,
							-angle
						);
						Draw.mixcol(Color.white, unit.hitTime);
						Draw.rect(propeller.bladeRegion, rx, ry,
								propeller.bladeRegion.width * rotorScl * -Mathf.sign(false),
								propeller.bladeRegion.height * rotorScl,
							-angle
						);
					}
					Draw.reset();

					// Blur Rotor
					if (propeller.bladeBlurRegion.found()) {
						Draw.z(z + propeller.rotorLayer);
						Draw.alpha(cptr.rotorSpeedScl * propeller.rotorBlurAlphaMultiplier * (cptr.dead() ? cptr.rotorSpeedScl * 0.5f : 1));
						Draw.rect(
								propeller.bladeBlurRegion, rx, ry,
								propeller.bladeBlurRegion.width * rotorScl,
								propeller.bladeBlurRegion.height * rotorScl,
							-blurAngle
						);

						// Double Rotor Blur
						if (propeller.doubleRotor) {
							Draw.rect(
									propeller.bladeBlurRegion, rx, ry,
									propeller.bladeBlurRegion.width * rotorScl * -Mathf.sign(false),
									propeller.bladeBlurRegion.height * rotorScl,
								blurAngle
							);
						}
						Draw.reset();
					}

					Draw.reset();

					// Rotor Top
					if (propeller.drawRotorTop) {
						Draw.z(z + propeller.rotorLayer + 0.001f);
						Draw.rect(
								propeller.topRegionOutline, rx, ry,
								propeller.topRegionOutline.width * rotorTopScl,
								propeller.topRegionOutline.height * rotorTopScl,
							unit.rotation - 90);
						Draw.mixcol(Color.white, unit.hitTime);
						Draw.rect(
								propeller.topRegion, rx, ry,
								propeller.topRegion.width * rotorTopScl,
								propeller.topRegion.height * rotorTopScl,
							unit.rotation - 90
						);
					}
					Draw.reset();
				}
			}
		}
	}

	@Override
	public void createIcons(MultiPacker packer) {
		super.createIcons(packer);
		// Helicopter Rotors
		for (Propeller propeller : propeller) {
			liner.outlineRegion(packer, propeller.bladeRegion, outlineColor, propeller.name + "-outline", outlineRadius);
			liner.outlineRegion(packer, propeller.topRegion, outlineColor, propeller.name + "-top-outline", outlineRadius);
		}
	}

	@Override
	public void draw(Unit unit) {
		float z = unit.elevation > 0.5f ? (lowAltitude ? Layer.flyingUnitLow : Layer.flyingUnit) : groundLayer + Mathf.clamp(hitSize / 4000f, 0, 0.01f);
		super.draw(unit);
		Draw.z(z);
		drawPropeller(unit);
	}

	@Override
	public void load() {
		super.load();
		propeller.each(Propeller::load);
	}

}
