package axthrix;

import arc.*;
import arc.func.Func;
import arc.util.*;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.content.blocks.AxthrixTurrets;
import axthrix.content.blocks.PayloadAmmoBlocks;
import axthrix.content.units.*;
import axthrix.world.types.bulletypes.InfFragBulletType;
import axthrix.world.util.StackWorldState;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import axthrix.world.util.*;
import arc.audio.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.world.blocks.defense.turrets.*;

import static arc.Core.*;
import static axthrix.content.blocks.PayloadAmmoBlocks.funniBullet;
import static mindustry.Vars.*;
import axthrix.content.*;

import static arc.Core.app;
import static mindustry.Vars.headless;

public class AxthrixLoader extends Mod{
    public static Seq<BulletData> allBullets = new Seq<>();
    public static boolean funibullet = false;
    public AxthrixLoader(){
        super();
        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            if(!headless){
                AxShaders.init();
                //AxSounds.load();
            }
        }));
        Events.on(ClientLoadEvent.class, e -> {
            loadSettings();
        });

        // Check if funni bullet is enabled
        funibullet = settings.getBool("aj-funni-disabled", false);
    }
    @Override
    public void init(){
        Events.on(ClientLoadEvent.class, e -> {
            if(!headless){
                Mods.LoadedMod aj = mods.locateMod("aj");
                Func<String, String> getModBundle = value -> bundle.get("mod." + aj.meta.name + "." + value);
            }

            if(funibullet){
                setupEveryBullets(null); //It having a chance to frag into itself will be really funny
            }
        });
    }
    @Override
    public void loadContent(){
        Log.info("Loading Axthrix content");
        StackWorldState.load();
        AxFactions.load();
        AxStats.load();
        //AxItems.load();
        AxthrixStatus.load();
        //AxLiquids.load();
        AxthrixDrones.load();
        AxthrixUnits.load();
        LegendUnits.load();
        RaodonUnits.load();
        //AxthrixBlocks.load();
        PayloadAmmoBlocks.load();
        AxthrixCrafters.load();
        //AxthrixPower.load();
        AxthrixTurrets.load();
        AxPlanets.load();
        //AxSectorPresets.load();
        AxthrixTechTree.load();
        Log.info("Axthrix Content Loaded. :)");


        Events.on(ClientLoadEvent.class, e -> {
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog(Core.bundle.get("menu.aj-menu.title"));
                dialog.cont.add(Core.bundle.get("menu.aj-menu.message")).row();
                dialog.cont.image(Core.atlas.find("aj-welcome-icon")).pad(20f).row();
                dialog.cont.button("okay", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }
    private void loadSettings(){
        ui.settings.addCategory(bundle.get("setting.aj-title"), "aj-settings-icon", t -> {
            t.checkPref("aj-funni-disabled", false);
        });
    }
    public static boolean checkKillShooter(BulletType b){
        if(b == null || b == Bullets.damageLightning || b == Bullets.damageLightningGround) return false;
        return b.killShooter ||
                checkKillShooter(b.fragBullet) ||
                checkKillShooter(b.intervalBullet) ||
                checkKillShooter(b.lightningType) ||
                b.spawnBullets.contains(AxthrixLoader::checkKillShooter);
    }

    public static void setupEveryBullets(Turret base){
        content.units().each(u -> u.weapons.each(w -> w.bullet != null, w -> {
            BulletType bul = w.bullet;
            BulletData data = new BulletData(bul, w.shootSound, bul.shootEffect, bul.smokeEffect, w.shake, bul.lifetime);
            if(!allBullets.contains(data)){
                allBullets.add(data);
            }
        }));
        content.blocks().each(b -> b instanceof Turret, b -> {
            if(b != base){
                if(b instanceof LaserTurret block && block.shootType != null){
                    BulletType bul = block.shootType;
                    Effect fshootEffect = block.shootEffect == null ? bul.shootEffect : block.shootEffect;
                    Effect fsmokeEffect = block.smokeEffect == null ? bul.smokeEffect : block.smokeEffect;
                    BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime + block.shootDuration, true);
                    allBullets.add(data);
                }else if(b instanceof PowerTurret block && block.shootType != null){
                    BulletType bul = block.shootType;
                    Effect fshootEffect = block.shootEffect == null ? bul.shootEffect : block.shootEffect;
                    Effect fsmokeEffect = block.smokeEffect == null ? bul.smokeEffect : block.smokeEffect;
                    BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime);
                    allBullets.add(data);
                }else if(b instanceof ItemTurret block){
                    for(BulletType bul : block.ammoTypes.values()){
                        Effect fshootEffect = block.shootEffect == null ? bul.shootEffect : block.shootEffect;
                        Effect fsmokeEffect = block.smokeEffect == null ? bul.smokeEffect : block.smokeEffect;
                        BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime);
                        allBullets.add(data);
                    }
                }else if(b instanceof LiquidTurret block){
                    for(BulletType bul : block.ammoTypes.values()){
                        Effect fshootEffect = block.shootEffect == null ? bul.shootEffect : block.shootEffect;
                        Effect fsmokeEffect = block.smokeEffect == null ? bul.smokeEffect : block.smokeEffect;
                        BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime);
                        allBullets.add(data);
                    }
                }else if(b instanceof PayloadAmmoTurret block){
                    for(BulletType bul : block.ammoTypes.values()){
                        Effect fshootEffect = block.shootEffect == null ? bul.shootEffect : block.shootEffect;
                        Effect fsmokeEffect = block.smokeEffect == null ? bul.smokeEffect : block.smokeEffect;
                        BulletData data = new BulletData(bul, block.shootSound, fshootEffect, fsmokeEffect, block.shake, bul.lifetime);
                        allBullets.add(data);
                    }
                }
            }
        });

        allBullets.sort(b -> AxUtil.bulletDamage(b.bulletType, b.lifetime));
    }

    public static class BulletData{
        public BulletType bulletType;
        public Sound shootSound;
        public Effect shootEffect, smokeEffect;
        public float shake, lifetime;
        public boolean continuousBlock;

        public BulletData(BulletType bulletType, Sound shootSound, Effect shakeEffect, Effect smokeEffect, float shake, float lifetime, boolean continuous){
            this.bulletType = bulletType;
            this.shootSound = shootSound;
            this.shootEffect = shakeEffect;
            this.smokeEffect = smokeEffect;
            this.shake = shake;
            this.lifetime = lifetime;
            this.continuousBlock = continuous;
        }

        public BulletData(BulletType bulletType, Sound shootSound, Effect shootEffect, Effect smokeEffect, float shake, float lifetime){
            this(bulletType, shootSound, shootEffect, smokeEffect, shake, lifetime, false);
        }

        @Override
        public boolean equals(Object obj){
            return obj instanceof BulletData o &&
                    bulletType == o.bulletType &&
                    shootSound == o.shootSound &&
                    shootEffect == o.shootEffect &&
                    smokeEffect == o.smokeEffect &&
                    shake == o.shake &&
                    lifetime == o.lifetime &&
                    continuousBlock == o.continuousBlock;
        }
    }
}
