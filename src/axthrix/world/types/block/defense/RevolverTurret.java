package axthrix.world.types.block.defense;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.util.*;
import axthrix.AxthrixLoader;
import axthrix.content.AxthrixSounds;
import axthrix.world.util.RevolverLogic;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.gen.Sounds;
import mindustry.graphics.*;
import mindustry.ui.*;

public class RevolverTurret extends AxItemTurret implements RevolverLogic {
    public int maxCartridges = 6;
    public Effect reloadCartridgesEffect = Fx.none;
    public Sound reloadCartridgesSound = AxthrixSounds.RevolverReload;
    public float cartridgeReloadTime = 1f;
    public boolean reloadIfNotFull = true;

    public TextureRegion cartridgesRegion;
    public Effect shootCartridgesEffect = Fx.none;
    public Sound shootCartridgesSound = Sounds.none;
    public float shotCartridges = 1f;
    public int numOfReloadCartridges = 6;

    public TextureRegion nonCartridgesRegion;
    public Effect nonCartridgesShootEffect = Fx.none;
    public Sound nonCartridgesShootSound = AxthrixSounds.RevolverEmpty;
    public float secondarySmoothReloadSpeed = 0.15f;

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

    public RevolverTurret(String name){
        super(name);
    }

    @Override
    public void load() {
        super.load();
        cartridgesRegion = Core.atlas.find("aj-cartridges");
        nonCartridgesRegion = Core.atlas.find("aj-non-cartridges");
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("aj-rounds", (RevolverTurretBuild entity) -> new Bar(
                () ->  (entity.cartridges > 0) ? Core.bundle.format("bar.aj-rounds", Strings.autoFixed(entity.cartridges, 20)) : Core.bundle.format("bar.aj-reload"),
                () ->  (entity.cartridges > 0) ? Pal.ammo : Pal.orangeSpark,
                () -> (entity.cartridges > 0) ? (float)entity.cartridges / maxCartridges : Math.abs((entity.reloadConCartridges / cartridgeReloadTime) - 1)
        ));
        removeBar("ammo");
    }

    public class RevolverTurretBuild extends ItemTurretBuild implements RevolverLogic.RevolverBuild {
        public int cartridges = maxCartridges;
        public float reloadConCartridges = 0;
        public float secondarySmoothReload;

        // Getters and setters for interface
        @Override public int getCartridges() { return cartridges; }
        @Override public void setCartridges(int value) { cartridges = value; }
        @Override public float getReloadConCartridges() { return reloadConCartridges; }
        @Override public void setReloadConCartridges(float value) { reloadConCartridges = value; }

        // From parent turret
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
        @Override public float getX() { return x; }
        @Override public float getY() { return y; }
        @Override public float getHitSize() { return block.size * Vars.tilesize; }

        public float progress() {
            return (cartridges > 0) ? 1 : Math.abs(((reloadConCartridges * 2) / cartridgeReloadTime) - 1);
        }

        @Override
        public void draw() {
            super.draw();

            // Draw cartridges above turret
            if(AxthrixLoader.showRevolverAmmo && unit.isPlayer()){
                Draw.z(Layer.flyingUnit); // Draw above most things
                drawCartridges(cartridges, x, y + block.size * Vars.tilesize / 2f + 4f);
                Draw.reset();
            }
        }

        @Override
        public void updateTile(){
            super.updateTile();
            secondarySmoothReload = Mathf.lerpDelta(secondarySmoothReload, (reloadConCartridges <= 0) ? 1.0F : 0.0F, secondarySmoothReloadSpeed);

            // Use shared revolver logic
            updateRevolverLogic(Time.delta, coolant.multiplier.get(this));
        }

        @Override
        public void reloadCartridges() {
            // Additional check for turret-specific reload behavior
            if (cartridges >= maxCartridges) return;
            if (cartridges != 0 && !reloadIfNotFull) return;
            if (ammo.size == 0) return; // Only reload if we have ammo

            // Use shared logic
            RevolverLogic.RevolverBuild.super.reloadCartridges();
        }

        @Override
        protected void shoot(BulletType type){
            tryShoot(shootX, shootY, rotation, () -> super.shoot(type));
        }

        public void addAmmo(int amount) {
            if (cartridges >= maxCartridges) return;
            cartridges += amount;
            if (cartridges > maxCartridges) cartridges = maxCartridges;

            reloadCartridgesEffect.at(this.x, this.y + this.block.size / 2f);
            reloadCartridgesSound.at(this);
        }
    }
}