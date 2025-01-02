package axthrix.world.types.weapontypes;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import axthrix.world.types.weapontypes.mounts.RevolverWeaponMount;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.type.Weapon;


public class RevolverWeapon extends Weapon {

    public int maxCartridges = 1;


    public Effect reloadCartridgesEffect = Fx.none;
    public Sound reloadCartridgesSound = Sounds.none;
    public float reloadCartridges = 1f;



    public TextureRegion cartridgesRegion;

    public Effect shootCartridgesEffect = Fx.none;
    public Sound shootCartridgesSound = Sounds.none;
    public float shotCartridges = 1f;
    public int numOfReloadCartridges = 1;


    public TextureRegion nonCartridgesRegion;
    public Effect nonCartridgesShootEffect = Fx.none;
    public Sound nonCartridgesShootSound = Sounds.none;

    @Override
    public void load() {
        super.load();
        cartridgesRegion = Core.atlas.find("cartridges");

        nonCartridgesRegion = Core.atlas.find("non-cartridges");
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

        RevolverWeaponMount rwm = (RevolverWeaponMount) mount;
        if (unit.isPlayer()) drawCartridgeses(rwm, unit.x, unit.y + unit.hitSize);
    }

    public void drawCartridgeses(RevolverWeaponMount rwm, float x, float y) {
        float len = (cartridgesRegion.width / 4f * 0.5f) * (maxCartridges - 0.5f);
        float _x = x - len / 2f + (cartridgesRegion.width / 4f * 0.5f) / 2f;
        float _y = y;

        for (int i = 0; i < maxCartridges; i++) {
            if (rwm.cartridges > i) {
                drawCartridges(cartridgesRegion, _x, _y);
            } else {
                drawCartridges(nonCartridgesRegion, _x, _y);
            }

            _x += cartridgesRegion.width / 4f * 0.5f;
        }
    }

    private void drawCartridges(TextureRegion tr, float x, float y) {
        Draw.z(Layer.end);
        Draw.scl(0.5f);
        Draw.rect(tr, x, y);
    }

    @Override
    public void update(Unit unit, WeaponMount mount) {
        super.update(unit, mount);
        RevolverWeaponMount rwm = (RevolverWeaponMount) mount;
        if (rwm.reloadCartridges <= 0) reloadCartridges(unit, rwm);
        rwm.reloadCartridges = Math.max(rwm.reloadCartridges - Time.delta * unit.reloadMultiplier, 0);
    }

    public void sinit() {
        mountType = RevolverWeaponMount::new;
    }

    @Override
    protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
        RevolverWeaponMount rwm = (RevolverWeaponMount) mount;

        if (rwm.cartridges < shotCartridges) {
            nonCartridgesShoot(unit, shootX, shootY, rotation);
            return;
        }

        cartridgesShoot(unit, rwm, shootX, shootY, rotation);
    }

    public void reloadCartridges(Unit unit, RevolverWeaponMount mount) {
        if (mount.cartridges >= maxCartridges) return;
        mount.reloadCartridges = reloadCartridges;
        mount.cartridges += numOfReloadCartridges;

        reloadCartridgesEffect.at(unit.x, unit.y + unit.hitSize / 2f);
        reloadCartridgesSound.at(unit);
    }

    protected void nonCartridgesShoot(Unit unit, float shootX, float shootY, float rotation) {
        nonCartridgesShootEffect.at(shootX, shootY, rotation);
        nonCartridgesShootSound.at(unit);
    }

    protected void cartridgesShoot(Unit unit, RevolverWeaponMount mount, float shootX, float shootY, float rotation) {
        mount.cartridges -= shotCartridges;

        shootCartridgesEffect.at(shootX, shootY, rotation);
        shootCartridgesSound.at(unit);

        mount.reloadCartridges = reloadCartridges;

        super.shoot(unit, mount, shootX, shootY, rotation);
    }
}
