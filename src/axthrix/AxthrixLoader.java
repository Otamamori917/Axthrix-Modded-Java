package axthrix;

import arc.*;
import arc.func.Func;
import arc.graphics.g2d.Draw;
import arc.util.*;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.content.blocks.AxthrixTurrets;
import axthrix.content.blocks.PayloadAmmoBlocks;
import axthrix.content.units.*;
import axthrix.world.types.bulletypes.InfFragBulletType;
import axthrix.world.util.StackWorldState;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.graphics.Shaders;
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
import static mindustry.Vars.*;
import axthrix.content.*;

import static arc.Core.app;
import static mindustry.Vars.headless;

public class AxthrixLoader extends Mod{
    public static Seq<BulletData> allBullets = new Seq<>();
    public static boolean funibullet = false;
    public static boolean screwStealthFlyers = false;
    //debug
    public static boolean amosPowerDebug = false;
    public static boolean amosLiquidDebug = false;
    public static boolean nado3dDebug = false;
    public static boolean showMessage = true;
    //options
    public static boolean showRevolverAmmo = true;
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

        /*Events.run(EventType.Trigger.drawOver, () -> {
                    if (Vars.renderer.animateShields && PvShaders.nullisAura != null)
                        Draw.drawRange(PvLayers.voidLayer, 0.1f, () -> {
                            if (!Vars.renderer.effectBuffer.isBound())
                                Vars.renderer.effectBuffer.begin(Color.clear);
                        }, () ->
                        {
                            Vars.renderer.effectBuffer.end();
                            Vars.renderer.effectBuffer.blit(Shaders);
                            if (Vars.renderer.effectBuffer.isBound())
                                Vars.renderer.effectBuffer.endBind();
                        });
                });    */


        // Check if funni bullet is enabled
        funibullet = settings.getBool("aj-funni-disabled", false);
        amosPowerDebug = settings.getBool("aj-mount-power-debug", false);
        amosLiquidDebug = settings.getBool("aj-mount-liquid-debug", false);
        nado3dDebug = settings.getBool("aj-nado-3d-debug", false);
        showMessage = settings.getBool("aj-message-debug", true);
        showRevolverAmmo = settings.getBool("aj-revolver-ammo", true);
        screwStealthFlyers = settings.getBool("aj-screw-stealth", true);
    }
    @Override
    public void init(){
        Events.on(ClientLoadEvent.class, e -> {
            if(!headless){
                Mods.LoadedMod aj = mods.locateMod("aj");
                Func<String, String> getModBundle = value -> bundle.get("mod." + aj.meta.name + "." + value);
            }

            if(funibullet){
                Log.info("Chaos Frag is turned on \nMay crash if Apex or Apexus fires an Router");
                setupEveryBullets(null); //It having a chance to frag into itself will be really funny
            }
        });
    }
    @Override
    public void loadContent(){
        Log.info("Loading Axthrix content");
        StackWorldState.load();
        MountWorldState.load();
        //DroneWorldState.load();
        AxFactions.load();
        AxStats.load();
        AxthrixSounds.LoadSounds();
        AxItems.load();
        AxthrixStatus.load();
        AxLiquids.load();
        AxthrixEnvironment.load();
        AxthrixDrones.load();
        AxthrixUnits.load();
        LegendUnits.load();
        IkatusaUnits.load();
        RaodonUnits.load();
        //AxthrixBlocks.load();
        PayloadAmmoBlocks.load();
        AxthrixCrafters.load();
        //AxthrixPower.load();
        AxthrixTurrets.loadAxthrix();
        AxthrixTurrets.loadRaodon();
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
                if (showMessage){
                    dialog.show();
                } else {
                    dialog.hide();
                }
            });
        });
    }
    private void loadSettings(){
        ui.settings.addCategory(bundle.get("setting.aj-title"), "aj-settings-icon", t -> {
            t.checkPref("aj-funni-disabled", false);
            t.checkPref("aj-screw-stealth", false);
            t.checkPref("aj-message-debug", true);
            t.checkPref("aj-mount-power-debug", false);
            t.checkPref("aj-mount-liquid-debug", false);
            t.checkPref("aj-nado-3d-debug", false);
            t.checkPref("aj-revolver-ammo", true);
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
