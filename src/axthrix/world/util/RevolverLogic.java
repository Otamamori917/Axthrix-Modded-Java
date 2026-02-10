package axthrix.world.util;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;

import static axthrix.content.AxthrixSounds.RevolverEmpty;
import static axthrix.content.AxthrixSounds.RevolverReload;

public interface RevolverLogic {
    // Configuration getters
    int getMaxCartridges();
    Effect getReloadCartridgesEffect();
    Sound getReloadCartridgesSound();
    float getCartridgeReloadTime();
    int getNumOfReloadCartridges();

    Effect getShootCartridgesEffect();
    Sound getShootCartridgesSound();
    float getShotCartridges();

    Effect getNonCartridgesShootEffect();
    Sound getNonCartridgesShootSound();

    TextureRegion getCartridgesRegion();
    TextureRegion getNonCartridgesRegion();

    // Default method for drawing cartridges
    default void drawCartridges(int currentCartridges, float x, float y) {
        TextureRegion cartridgesRegion = getCartridgesRegion();
        TextureRegion nonCartridgesRegion = getNonCartridgesRegion();

        if(cartridgesRegion == null || nonCartridgesRegion == null) return;

        int maxCartridges = getMaxCartridges();
        float len = (cartridgesRegion.width / 4f * 0.5f) * (maxCartridges - 0.5f);
        float _x = x - len / 2f + (cartridgesRegion.width / 4f * 0.5f) / 2f;
        float _y = y;

        for (int i = 0; i < maxCartridges; i++) {
            if (currentCartridges > i) {
                drawCartridgeIcon(cartridgesRegion, _x, _y);
            } else {
                drawCartridgeIcon(nonCartridgesRegion, _x, _y);
            }
            _x += cartridgesRegion.width / 4f * 0.5f;
        }
    }

    default void drawCartridgeIcon(TextureRegion tr, float x, float y) {
        Draw.scl(0.5f);
        Draw.rect(tr, x, y);
        Draw.scl();
    }

    // Shared build/mount interface
    interface RevolverBuild {
        int getCartridges();
        void setCartridges(int value);
        float getReloadConCartridges();
        void setReloadConCartridges(float value);

        // From parent (turret/weapon)
        int getMaxCartridges();
        float getCartridgeReloadTime();
        int getNumOfReloadCartridges();
        Effect getReloadCartridgesEffect();
        Sound getReloadCartridgesSound();
        float getShotCartridges();
        Effect getShootCartridgesEffect();
        Sound getShootCartridgesSound();
        Effect getNonCartridgesShootEffect();
        Sound getNonCartridgesShootSound();

        // Position for effects
        float getX();
        float getY();
        float getHitSize();

        default void updateRevolverLogic(float delta, float coolantMultiplier) {
            if (getReloadConCartridges() <= 0) {
                reloadCartridges();
            }
            setReloadConCartridges(Math.max(getReloadConCartridges() - delta * coolantMultiplier, 0));
        }

        default void reloadCartridges() {
            if (getCartridges() >= getMaxCartridges()) return;

            setReloadConCartridges(getCartridgeReloadTime());
            setCartridges(getCartridges() + getNumOfReloadCartridges());

            if(getCartridges() > getMaxCartridges()) {
                setCartridges(getMaxCartridges());
            }

            getReloadCartridgesEffect().at(getX(), getY() + getHitSize() / 2f);
            getReloadCartridgesSound().at(getX(), getY());
        }

        default boolean tryShoot(float shootX, float shootY, float rotation, Runnable actualShoot) {
            if (getCartridges() < getShotCartridges()) {
                nonCartridgesShoot(shootX, shootY, rotation);
                return false;
            }

            cartridgesShoot(shootX, shootY, rotation, actualShoot);
            return true;
        }

        default void nonCartridgesShoot(float shootX, float shootY, float rotation) {
            getNonCartridgesShootEffect().at(shootX, shootY, rotation);
            getNonCartridgesShootSound().at(getX(), getY());
        }

        default void cartridgesShoot(float shootX, float shootY, float rotation, Runnable actualShoot) {
            setCartridges(getCartridges() - (int)getShotCartridges());

            getShootCartridgesEffect().at(shootX, shootY, rotation);
            getShootCartridgesSound().at(getX(), getY());

            setReloadConCartridges(getCartridgeReloadTime());

            actualShoot.run();
        }
    }
}