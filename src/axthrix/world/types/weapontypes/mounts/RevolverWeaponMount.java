package axthrix.world.types.weapontypes.mounts;

import axthrix.world.types.weapontypes.RevolverWeapon;
import mindustry.entities.units.WeaponMount;
import mindustry.type.Weapon;


public class RevolverWeaponMount extends WeaponMount {
    public int cartridges = 1;
    public float reloadCartridges = 1;
    public RevolverWeapon revolverWeapon;

    public RevolverWeaponMount(Weapon weapon) {
        super(weapon);
        revolverWeapon = (RevolverWeapon) weapon;
    }
}
