package axthrix.world.types.weapontypes;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import axthrix.AxthrixLoader;
import axthrix.world.types.block.defense.RevolverTurret;
import axthrix.world.util.AxPartParms;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.Weapon;
import axthrix.world.util.RevolverLogic;

import static axthrix.content.AxthrixSounds.RevolverEmpty;
import static axthrix.content.AxthrixSounds.RevolverReload;

public class RevolverWeapon extends Weapon implements RevolverLogic {

    public int maxCartridges = 6;
    public Effect reloadCartridgesEffect = Fx.none;
    public Sound reloadCartridgesSound = RevolverReload;
    public float cartridgeReloadTime = 1f;

    public TextureRegion cartridgesRegion;
    public Effect shootCartridgesEffect = Fx.none;
    public Sound shootCartridgesSound = Sounds.none;
    public float shotCartridges = 1f;
    public int numOfReloadCartridges = 1;

    public TextureRegion nonCartridgesRegion;
    public Effect nonCartridgesShootEffect = Fx.none;
    public Sound nonCartridgesShootSound = RevolverEmpty;

    // Interface implementations
    @Override public int getMaxCartridges() { return maxCartridges; }
    @Override public Effect getReloadCartridgesEffect() { return reloadCartridgesEffect; }
    @Override public Sound getReloadCartridgesSound() { return reloadCartridgesSound; }
    @Override public float getCartridgeReloadTime() { return cartridgeReloadTime; }
    @Override public int getNumOfReloadCartridges() { return numOfReloadCartridges; }
    @Override public Effect getShootCartridgesEffect() { return shootCartridgesEffect; }
    @Override public Sound getShootCartridgesSound() { return shootCartridgesSound; }
    @Override public float getShotCartridges() { return shotCartridges; }
    @Override public Effect getNonCartridgesShootEffect() { return nonCartridgesShootEffect; }
    @Override public Sound getNonCartridgesShootSound() { return nonCartridgesShootSound; }
    @Override public TextureRegion getCartridgesRegion() { return cartridgesRegion; }
    @Override public TextureRegion getNonCartridgesRegion() { return nonCartridgesRegion; }

    @Override
    public void load() {
        super.load();
        cartridgesRegion = Core.atlas.find("aj-cartridges");
        nonCartridgesRegion = Core.atlas.find("aj-non-cartridges");
    }

    public RevolverWeapon() {
        super("");
        sinit();
    }

    public RevolverWeapon(String name) {
        super(name);
        sinit();
    }

    @Override
    public void draw(Unit unit, WeaponMount mount) {
        super.draw(unit, mount);

        if(parts.size > 0) {
            if (mount instanceof RevolverWeaponMount rw) {
                float progress = (rw.cartridges > 0) ? 1 : Math.abs(((rw.reloadConCartridges * 2) / cartridgeReloadTime) - 1);

                AxPartParms.axparams.set(
                        1f - progress,
                        0,
                        0
                );
            }
        }

        RevolverWeaponMount rwm = (RevolverWeaponMount) mount;
        if(AxthrixLoader.showRevolverAmmo && unit.isPlayer()){
            drawCartridges(rwm.getCartridges(), unit.x, unit.y + unit.hitSize);
        }
    }

    @Override
    public void update(Unit unit, WeaponMount mount) {
        super.update(unit, mount);
        RevolverWeaponMount rwm = (RevolverWeaponMount) mount;
        rwm.updateRevolverLogic(Time.delta, unit.reloadMultiplier);
    }

    public void sinit() {
        mountType = RevolverWeaponMount::new;
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        RevolverWeaponMount rwm = (RevolverWeaponMount) mount;
        rwm.tryShoot(shootX, shootY, rotation, () -> super.shoot(unit, mount, shootX, shootY, rotation));
    }

    public class RevolverWeaponMount extends WeaponMount implements RevolverLogic.RevolverBuild {
        public int cartridges = maxCartridges;
        public float reloadConCartridges = 0;

        public RevolverWeaponMount(Weapon weapon) {
            super(weapon);
        }

        // Getters and setters
        @Override public int getCartridges() { return cartridges; }
        @Override public void setCartridges(int value) { cartridges = value; }
        @Override public float getReloadConCartridges() { return reloadConCartridges; }
        @Override public void setReloadConCartridges(float value) { reloadConCartridges = value; }

        // From parent weapon
        @Override public int getMaxCartridges() { return maxCartridges; }
        @Override public float getCartridgeReloadTime() { return cartridgeReloadTime; }
        @Override public int getNumOfReloadCartridges() { return numOfReloadCartridges; }
        @Override public Effect getReloadCartridgesEffect() { return reloadCartridgesEffect; }
        @Override public Sound getReloadCartridgesSound() { return reloadCartridgesSound; }
        @Override public float getShotCartridges() { return shotCartridges; }
        @Override public Effect getShootCartridgesEffect() { return shootCartridgesEffect; }
        @Override public Sound getShootCartridgesSound() { return shootCartridgesSound; }
        @Override public Effect getNonCartridgesShootEffect() { return nonCartridgesShootEffect; }
        @Override public Sound getNonCartridgesShootSound() { return nonCartridgesShootSound; }

        // Position
        @Override public float getX() { return weapon.x; }
        @Override public float getY() { return weapon.y; }
        @Override public float getHitSize() { return 0; }
    }
}