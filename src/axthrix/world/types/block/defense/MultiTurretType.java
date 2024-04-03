package axthrix.world.types.block.defense;

import arc.Core;
import arc.graphics.Pixmap;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.*;
import arc.math.geom.Vec2;
import arc.struct.EnumSet;
import arc.struct.Seq;
import axthrix.world.types.AxFaction;
import axthrix.world.types.weapontypes.BlockWeapon;
import axthrix.world.types.weapontypes.mounts.BlockWeaponMount;
import axthrix.world.util.AxStatValues;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.entities.TargetPriority;
import mindustry.entities.units.WeaponMount;
import mindustry.graphics.*;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.*;

import static mindustry.Vars.state;

public class MultiTurretType extends AxItemTurret {
    /** minimum range of any weapon; used for approaching targets. can be overridden by setting a value > 0. */
    public float range = -1;
    /** maximum range of any weapon */
    public float maxRange = -1f;
    /** if true, all weapons will attack the same target. */
    public boolean singleTarget = false;
    /** if true, this unit will be able to have multiple targets, even if it only has one mirrored weapon. */
    public boolean forceMultiTarget = false;
    public boolean canHeal = false, canAttack = true;
    /** All weapons that this unit will shoot with. */
    public Seq<BlockWeapon> weapons = new Seq<>();
    public BlockWeaponMount[] mounts = new BlockWeaponMount[0];
    /** Defines drawing behavior for this turret. */
    public boolean outlines = true, alwaysCreateOutline;

    public float dpsEstimate = -1;
    public Vec2 vel;

    public MultiTurretType(String name){
        super(name);

        update = true;
        solid = true;
        outlineIcon = true;
        attacks = true;
        this.vel = new Vec2();
        priority = TargetPriority.turret;
        group = BlockGroup.turrets;
        flags = EnumSet.of(BlockFlag.turret);
    }
    public boolean hasWeapons(){
        return weapons.size > 0;
    }

    public void setStats(){
        if(weapons.any()){
            stats.add(Stat.weapons, AxStatValues.blockWeapons(this, weapons));
        }
    }
    //never actually called; it turns out certain mods have custom weapons that do not need bullets.
    protected void validateWeapons(){
        for(int i = 0; i < weapons.size; i++){
            var wep = weapons.get(i);
            if(wep.bullet == Bullets.placeholder || wep.bullet == null){
                throw new RuntimeException("Unit: " + name + ": weapon #" + i + " ('" + wep.name + "') does not have a bullet defined. Make sure you have a bullet: (JSON) or `bullet = ` field in your unit definition.");
            }
        }
    }

    @Override
    public void init(){
        super.init();
        //assume slight range margin
        float margin = 4f;

        boolean skipWeapons = !weapons.contains(w -> !w.useAttackRange);

        //set up default range
        if(range < 0){
            range = Float.MAX_VALUE;
            for(BlockWeapon weapon : weapons){
                if(!weapon.useAttackRange && skipWeapons) continue;

                range = Math.min(range, weapon.range() - margin);
                maxRange = Math.max(maxRange, weapon.range() - margin);
            }
        }

        if(maxRange < 0){
            maxRange = Math.max(0f, range);

            for(BlockWeapon weapon : weapons){
                if(!weapon.useAttackRange && skipWeapons) continue;

                maxRange = Math.max(maxRange, weapon.range() - margin);
            }
        }

        //add mirrored weapon variants
        Seq<BlockWeapon> mapped = new Seq<>();
        for(BlockWeapon w : weapons){
            if(w.recoilTime < 0) w.recoilTime = w.reload;
            mapped.add(w);

            //mirrors are copies with X values negated
            if(w.mirror){
                BlockWeapon copy = w.copy();
                copy.flip();
                mapped.add(copy);

                //since there are now two weapons, the reload and recoil time must be doubled
                w.recoilTime *= 2f;
                copy.recoilTime *= 2f;
                w.reload *= 2f;
                copy.reload *= 2f;

                w.otherSide = mapped.size - 1;
                copy.otherSide = mapped.size - 2;
            }
        }
        this.weapons = mapped;

        weapons.each(BlockWeapon::init);

        canHeal = weapons.contains(w -> w.bullet.heals());

        canAttack = weapons.contains(w -> !w.noAttack);

        estimateDps();
    }

    public float estimateDps(){
        //calculate estimated DPS for one target based on weapons
        if(dpsEstimate < 0){
            dpsEstimate = weapons.sumf(BlockWeapon::dps);

            //suicide enemy
            if(weapons.contains(w -> w.bullet.killShooter)){
                //scale down DPS to be insignificant
                dpsEstimate /= 25f;
            }
        }

        return dpsEstimate;
    }
    @Override
    public void load(){
        super.load();

        weapons.each(BlockWeapon::load);
        region = Core.atlas.find(name);

        clipSize = Math.max(region.width * 2f, clipSize);
    }

    public void getRegionsToOutline(Seq<TextureRegion> out){
        for(BlockWeapon weapon : weapons){
            for(var part : weapon.parts){
                part.getOutlines(out);
            }
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);

        var toOutline = new Seq<TextureRegion>();
        getRegionsToOutline(toOutline);

        for(var region : toOutline){
            if(region instanceof TextureAtlas.AtlasRegion atlas){
                String regionName = atlas.name;
                Pixmap outlined = Pixmaps.outline(Core.atlas.getPixmap(region), outlineColor, outlineRadius);

                Drawf.checkBleed(outlined);

                packer.add(MultiPacker.PageType.main, regionName + "-outline", outlined);
            }
        }

        if(outlines){
            Seq<TextureRegion> outlineSeq = Seq.with(region);

            //note that mods with these regions already outlined will have *two* outlines made, which is... undesirable
            for(var outlineTarget : outlineSeq){
                if(!outlineTarget.found()) continue;

                makeOutline(MultiPacker.PageType.main, packer, outlineTarget, alwaysCreateOutline && region == outlineTarget, outlineColor, outlineRadius);
            }

            for(BlockWeapon weapon : weapons){
                if(!weapon.name.isEmpty() && (minfo.mod == null || weapon.name.startsWith(minfo.mod.name)) && (weapon.top || !packer.isOutlined(weapon.name) || weapon.parts.contains(p -> p.under))){
                    makeOutline(MultiPacker.PageType.main, packer, weapon.region, !weapon.top || weapon.parts.contains(p -> p.under), outlineColor, outlineRadius);
                }
            }
        }
    }
    /*public void drawWeapons(MultiTurretType mtt){

        for(BlockWeaponMount mount : mtt.mounts){
            mount.weapon.draw(mtt, mount);
        }

        Draw.reset();
    }

    public void drawWeaponOutlines(MultiTurretType mtt){

        for(BlockWeaponMount mount : mtt.mounts){
            if(!mount.weapon.top){
                //apply layer offset, roll it back at the end
                float z = Draw.z();
                Draw.z(z + mount.weapon.layerOffset);

                mount.weapon.drawOutline(mtt, mount);

                Draw.z(z);
            }
        }

        Draw.reset();
    }*/
    public void vel(Vec2 vel) {
        this.vel = vel;
    }
}
