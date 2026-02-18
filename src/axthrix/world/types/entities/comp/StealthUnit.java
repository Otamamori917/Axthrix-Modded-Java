package axthrix.world.types.entities.comp;

import arc.util.Time;
import axthrix.content.units.AxthrixUnits;
import axthrix.world.types.abilities.AfterBurnAbility;
import mindustry.gen.UnitEntity;

import axthrix.world.types.unittypes.CnSUnitType;

public class StealthUnit extends UnitEntity {
  public float vulnerabilityFrame;

  public boolean cloaked() {return vulnerabilityFrame <= 0f;}

  @Override public String toString() {
    return "StealthUnit#" + id;
  }
  @Override public CnSUnitType type() {
    return (CnSUnitType) super.type();
  }
  @Override
  public int classId() {
    return AxthrixUnits.classID(getClass());
  }

  @Override
  public void update() {
    super.update();
    if (isShooting()) vulnerabilityFrame = type().vulnerabilityTime;
    if (vulnerabilityFrame > 0) vulnerabilityFrame -= Time.delta;
  }
}
