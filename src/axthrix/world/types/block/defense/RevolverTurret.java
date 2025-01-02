package axthrix.world.types.block.defense;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.*;
import arc.graphics.g2d.TextureRegion;
import arc.math.*;
import arc.util.*;
import axthrix.world.types.weapontypes.mounts.RevolverWeaponMount;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.*;
import mindustry.entities.part.DrawPart;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.defense.turrets.*;

//U rename it lol
public class RevolverTurret extends AxItemTurret{
    public int maxCartridges = 6;


    public Effect reloadCartridgesEffect = Fx.none;
    public Sound reloadCartridgesSound = Sounds.none;
    public float cartridgeReloadTime = 1f;
    public boolean reloadIfNotFull = true;



    public TextureRegion cartridgesRegion;

    public Effect shootCartridgesEffect = Fx.none;
    public Sound shootCartridgesSound = Sounds.none;
    public float shotCartridges = 1f;
    public int numOfReloadCartridges = 6;


    public TextureRegion nonCartridgesRegion;
    public Effect nonCartridgesShootEffect = Fx.none;
    public Sound nonCartridgesShootSound = Sounds.none;

    public RevolverTurret(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("aj-rounds", (RevolverTurretBuild entity) -> new Bar(
                () ->  (entity.cartridges > 0) ? Core.bundle.format("bar.aj-rounds" , Strings.autoFixed(entity.cartridges, 20)) : Core.bundle.format("bar.aj-reload"),
                () ->  (entity.cartridges > 0) ? Pal.ammo : Pal.orangeSpark,
                () -> (entity.cartridges > 0) ? (float)entity.cartridges / maxCartridges : Math.abs((entity.reloadConCartridges / cartridgeReloadTime) - 1)
        ));
    }

    public DrawPart.PartProgress cartridgeprogress() {
        if(buildType.get() instanceof RevolverTurretBuild entity){

            return new DrawPart.PartProgress() {
                @Override
                public float get(DrawPart.PartParams partParams) {
                    if (entity.cartridges != 0 && !reloadIfNotFull) return 0;

                    return Mathf.clamp(entity.reloadConCartridges / cartridgeReloadTime);
                }
            };
        }
        return null;
    }

    public class RevolverTurretBuild extends ItemTurretBuild {
        int cartridges = 1;
        float reloadConCartridges;



        @Override
        public void updateTile(){
            super.updateTile();
            if (reloadConCartridges <= 0) reloadCartridges();
            reloadConCartridges = Math.max(reloadConCartridges - Time.delta * coolant.multiplier.get(this), 0);
        }


        @Override
        protected void shoot(BulletType type){

            if (cartridges < shotCartridges) {
                nonCartridgesShootEffect.at(shootX, shootY, rotation);
                nonCartridgesShootSound.at(this);
                return;
            }

            cartridges -= shotCartridges;

            shootCartridgesEffect.at(shootX, shootY, rotation);
            shootCartridgesSound.at(this);

            reloadConCartridges = cartridgeReloadTime;

            super.shoot(type);
        }

        public void reloadCartridges() {
            if (cartridges >= maxCartridges) return;
            if (cartridges != 0 && !reloadIfNotFull) return;
            reloadConCartridges = cartridgeReloadTime;
            cartridges += numOfReloadCartridges;
            if (cartridges > maxCartridges) cartridges = maxCartridges;

            reloadCartridgesEffect.at(this.x, this.y + this.block.size / 2f);
            reloadCartridgesSound.at(this);
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