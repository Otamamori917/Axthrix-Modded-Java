package axthrix;

import arc.Events;
import arc.audio.Sound;
import arc.func.Func;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Time;
import axthrix.content.*;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.content.blocks.AxthrixTurrets;
import axthrix.content.blocks.PayloadAmmoBlocks;
import axthrix.content.units.*;
import axthrix.world.types.block.AxBlock;
import axthrix.world.types.block.Egg;
import axthrix.world.types.block.TemperatureFloor;
import axthrix.world.types.block.production.TemperatureCrafter;
import axthrix.world.types.bulletypes.AttachmentGrenadeBulletType;
import axthrix.world.types.bulletypes.GrabBulletType;
import axthrix.world.types.bulletypes.TemperatureBulletType;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.util.*;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.FileTreeInitEvent;
import mindustry.gen.Groups;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class AxthrixLoader extends Mod{
    public static Seq<BulletData> allBullets = new Seq<>();
    public static boolean funibullet = false;
    public static boolean screwStealthFlyers = false;
    //debug
    public static boolean amosPowerDebug = false;
    public static boolean amosLiquidDebug = false;
    public static boolean nado3dDebug = false;
    public static boolean showMessage = true;
    //visuals
    public static boolean showRevolverAmmo = true;
    public static boolean showPayloadCrafterIndicators = true;
    public static int payloadMenuOffsetX = 0;
    public static int payloadMenuOffsetY = 0;
    public static int nadoEffectDensity = 100;
    CustomUnitInfoBar unitInfoBar;
    TemperatureGaugeUI tempGauge;
    public AxthrixLoader(){
        super();
        Events.on(FileTreeInitEvent.class, e -> app.post(() -> {
            if(!headless){
                AxShaders.init();
            }
        }));
        Events.on(ClientLoadEvent.class, e -> loadSettings());

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


        funibullet = settings.getBool("aj-funni-disabled", false);
        amosPowerDebug = settings.getBool("aj-mount-power-debug", false);
        amosLiquidDebug = settings.getBool("aj-mount-liquid-debug", false);
        nado3dDebug = settings.getBool("aj-nado-3d-debug", false);
        showMessage = settings.getBool("aj-message-debug", true);
        screwStealthFlyers = settings.getBool("aj-screw-stealth", true);
    }
    @Override
    public void init(){

        /// UI INITIALIZATION

        unitInfoBar = new CustomUnitInfoBar();
        unitInfoBar.build();

        tempGauge = new TemperatureGaugeUI();
        tempGauge.build();


        /// SETTINGS LOADING

        showRevolverAmmo = settings.getBool("aj-revolver-ammo", true);
        nadoEffectDensity = settings.getInt("aj-nado-density", 100);
        showPayloadCrafterIndicators = settings.getBool("aj-pay-indicators", true);
        payloadMenuOffsetX = settings.getInt("aj-pay-X-offset", 0);
        payloadMenuOffsetY = settings.getInt("aj-pay-Y-offset", 0);






        /// CLIENT LOAD EVENTS

        Events.on(ClientLoadEvent.class, e -> {
            // Mod bundle setup
            if(!headless){
                Mods.LoadedMod aj = mods.locateMod("aj");
                Func<String, String> getModBundle = value -> bundle.get("mod." + aj.meta.name + "." + value);
            }

            // Chaos frag setup
            if(funibullet){
                Log.info("Chaos Frag is turned on \nMay crash if Apex or Apexus fires an Router");
                setupEveryBullets(null);
            }
        });


        /// UPDATE EVENTS (Run every frame)

        Events.run(EventType.Trigger.update, () -> {
            // Attachment grenade updates
            Groups.unit.each(AttachmentGrenadeBulletType::updateGrenades);

            // Grab bullet updates
            GrabBulletType.updateGrabs();

            // Temperature system updates
            if(!headless){
                TemperatureBulletType.updateTemperatureSystem();
            }


            // Temperature floor updates - apply temp to units and buildings on temperature floors
            if(state.isPlaying()){
                // Units on temperature floors
                Groups.unit.each(unit -> {
                    Tile tile = unit.tileOn();
                    if(tile != null && tile.floor() instanceof TemperatureFloor tempFloor){
                        float temp = tempFloor.temperaturePerSecond * Time.delta / 60f;
                        TemperatureBulletType.applyTemperatureUnit(unit, temp);

                        if(Mathf.chance(tempFloor.effectChance * Time.delta)){
                            if(tempFloor.temperaturePerSecond > 0){
                                Fx.fire.at(unit.x, unit.y);
                            }else{
                                Fx.freezing.at(unit.x, unit.y);
                            }
                        }
                    }
                });

                // Buildings on temperature floors
                Groups.build.each(building -> {
                    if(building.tile != null && building.tile.floor() instanceof TemperatureFloor tempFloor){
                        float temp = tempFloor.temperaturePerSecond * Time.delta / 60f;
                        TemperatureBulletType.applyTemperatureBuilding(building, temp);

                        if(Mathf.chance(tempFloor.effectChance * Time.delta * 0.3f)){ // Less frequent for buildings
                            if(tempFloor.temperaturePerSecond > 0){
                                Fx.fire.at(building.x, building.y);
                            }else{
                                Fx.freezing.at(building.x, building.y);
                            }
                        }
                    }
                });
            }
        });


        /// DRAW EVENTS (Run every frame during draw)

        Events.run(EventType.Trigger.draw, () -> {
            // Attachment grenade drawing
            Groups.unit.each(AttachmentGrenadeBulletType::drawGrenades);
            // Grab bullet drawing
            GrabBulletType.drawGrabs();
        });


        /// ENTITY DAMAGE EVENTS

        // Building heat damage amplification
        Events.on(EventType.BuildDamageEvent.class, event -> {
            if(event.source == null) return; // Skip non-bullet damage

            // Heat damage amplification
            float tempMult = TemperatureBulletType.getTemperatureDamageMultiplier(event.build);
            float baseDamage = event.source.damage;
            float extraDamage = (baseDamage * tempMult) - baseDamage;

            if(TemperatureBulletType.debugLogging && extraDamage > 0){
                Log.info("[DamageEvent] Block @ (pos: @): Base damage: @, Temp mult: @x, Extra damage: @",
                        event.build.block.name, event.build.pos(), baseDamage, tempMult, extraDamage);
            }

            if(extraDamage > 0){
                event.build.damage(extraDamage);

                if(TemperatureBulletType.debugLogging){
                    Log.info("[DamageEvent] Block @ (pos: @): Total extra damage applied: @",
                            event.build.block.name, event.build.pos(), extraDamage);
                }
            }
        });

        // Unit damage events
        Events.on(EventType.UnitDamageEvent.class, event -> {
            // Shield drone pierce negation
            if(event.unit.type instanceof DroneUnitType droneType && droneType.isShield){
                if(event.bullet != null && event.bullet.type.pierce){
                    event.bullet.type.fragOnAbsorb = false;
                    event.bullet.absorb();
                }
            }

            // Temperature damage multiplier & resistance reduction
            if(event.bullet == null) return; // Skip non-bullet damage

            // Heat damage amplification
            float tempMult = TemperatureBulletType.getTemperatureDamageMultiplier(event.unit);
            float baseDamage = event.bullet.damage;
            float extraDamage = (baseDamage * tempMult) - baseDamage;

            if(TemperatureBulletType.debugLogging && extraDamage > 0){
                Log.info("[DamageEvent] Unit @ (ID: @): Base damage: @, Temp mult: @x, Extra damage: @",
                        event.unit.type.name, event.unit.id, baseDamage, tempMult, extraDamage);
            }

            // Cold resistance reduction
            float temp = TemperatureBulletType.getTemperatureUnit(event.unit);
            if(temp < 0){
                float effectiveMult = TemperatureBulletType.getEffectiveHealthMultiplier(event.unit);
                if(effectiveMult < event.unit.healthMultiplier){
                    float compensationMult = event.unit.healthMultiplier / effectiveMult;
                    float resistanceDamage = baseDamage * (compensationMult - 1f);
                    extraDamage += resistanceDamage;

                    if(TemperatureBulletType.debugLogging){
                        Log.info("[DamageEvent] Unit @ (ID: @): Resistance bypass - Health mult: @, Effective: @, Extra damage: @",
                                event.unit.type.name, event.unit.id, event.unit.healthMultiplier, effectiveMult, resistanceDamage);
                    }
                }
            }

            // Apply total extra damage
            if(extraDamage > 0){
                event.unit.damage(extraDamage);

                if(TemperatureBulletType.debugLogging){
                    Log.info("[DamageEvent] Unit @ (ID: @): Total extra damage applied: @",
                            event.unit.type.name, event.unit.id, extraDamage);
                }
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
        AxthrixWeathers.load();
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


        Events.on(ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
            BaseDialog dialog = new BaseDialog(bundle.get("menu.aj-menu.title"));
            dialog.cont.add(bundle.get("menu.aj-menu.message")).row();
            dialog.cont.image(atlas.find("aj-welcome-icon")).pad(20f).row();
            dialog.cont.button("okay", dialog::hide).size(100f, 50f);
            if (showMessage){
                dialog.show();
            } else {
                dialog.hide();
            }
        }));
    }
    private void loadSettings(){
        ui.settings.addCategory(bundle.get("setting.aj-title"), "aj-settings-icon", t -> {
            t.checkPref("aj-funni-disabled", false);// stay must refresh
            t.checkPref("aj-screw-stealth", false);//ignore
            t.pref(new AxUtil.Separator(20)); //any "debug" stay must refesh
            t.checkPref("aj-message-debug", true);
            t.checkPref("aj-mount-power-debug", false);
            t.checkPref("aj-mount-liquid-debug", false);
            t.checkPref("aj-nado-3d-debug", false);
            t.pref(new AxUtil.Separator(20)); //everything below i want to update instantly
            // Revolver ammo - instant update
            t.checkPref("aj-revolver-ammo", true, b -> {
                showRevolverAmmo = b; // Update your static variable
            });

            // Nado density - instant update
            t.sliderPref("aj-nado-density", 100, 20, 100, 20, i -> {
                nadoEffectDensity = i; // Update your static variable
                return i == -1 ? "@aj-nado-density.def" : i + "%";
            });

            // Payload indicators - instant update
            t.checkPref("aj-pay-indicators", true, b -> {
                showPayloadCrafterIndicators = b; // Update your static variable
            });

            // X offset - instant update
            t.sliderPref("aj-pay-X-offset", 0, -500, 0, 1, i -> {
                payloadMenuOffsetX = i; // Update your static variable immediately
                return i + "px";
            });

            // Y offset - instant update
            t.sliderPref("aj-pay-Y-offset", 0, -250, 250, 1, i -> {
                payloadMenuOffsetY = i; // Update your static variable immediately
                return i + "px";
            });
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
