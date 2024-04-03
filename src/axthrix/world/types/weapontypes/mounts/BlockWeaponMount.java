package axthrix.world.types.weapontypes.mounts;

import arc.util.Nullable;
import axthrix.world.types.block.defense.MultiTurretType;
import axthrix.world.types.weapontypes.BlockWeapon;
import mindustry.audio.SoundLoop;
import mindustry.gen.Bullet;
import mindustry.gen.Teamc;
import mindustry.type.Weapon;

public class BlockWeaponMount {
    /** weapon associated with this mount */
    public final BlockWeapon blockWeapon;
    /** reload in frames; 0 means ready to fire */
    public float reload;
    /** rotation relative to the unit this mount is on */
    public float rotation;
    /** weapon recoil */
    public float recoil;
    /** weapon barrel recoil */
    public @Nullable float[] recoils;
    /** destination rotation; do not modify! */
    public float targetRotation;
    /** current heat, 0 to 1*/
    public float heat;
    /** lerps to 1 when shooting, 0 when not */
    public float warmup;
    /** is the weapon actively charging */
    public boolean charging;
    /** counts up to 1 when charging, 0 when not */
    public float charge;
    /** lerps to reload time */
    public float smoothReload;
    /** aiming position in world coordinates */
    public float aimX, aimY;
    /** whether to shoot right now */
    public boolean shoot = false;
    /** whether to rotate to face the target right now */
    public boolean rotate = false;
    /** extra state for alternating weapons */
    public boolean side;
    /** total bullets fired from this mount */
    public int totalShots;
    /** counter for which barrel bullets have been fired from; used for alternating patterns */
    public int barrelCounter;
    /** current bullet for continuous weapons */
    public @Nullable Bullet bullet;
    /** sound loop for continuous weapons */
    public @Nullable SoundLoop sound;
    /** current target; used for autonomous weapons and AI */
    public @Nullable Teamc target;
    /** retarget counter */
    public float retarget = 0f;
    public MultiTurretType weapon;

    public BlockWeaponMount(BlockWeapon weapon){
        this.blockWeapon = weapon;
        this.rotation = weapon.baseRotation;
    }
}
