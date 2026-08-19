package axthrix;

import arc.Core;
import arc.Events;
import arc.audio.Sound;
import arc.func.Func;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Time;
import axthrix.content.*;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.content.blocks.AxthrixTurrets;
import axthrix.content.blocks.PayloadAmmoBlocks;
import axthrix.content.units.*;
import axthrix.world.types.abilities.ShieldPlateAbility;
import axthrix.world.types.block.env.TemperatureFloor;
import axthrix.world.types.bulletypes.*;
import axthrix.world.types.sea.managers.SubmergedUpdaterAndRenderer;
import axthrix.world.types.statuseffects.AccumStackStatusEffect;
import axthrix.world.types.unittypes.DroneUnitType;
import axthrix.world.util.*;
import axthrix.world.util.logics.TemperatureLogic;
import axthrix.world.util.save_states.AxthrixWorldState;
import axthrix.world.util.ui.AnimatedWelcomeDialog;
import axthrix.world.util.ui.CustomUnitInfoBar;
import axthrix.world.util.ui.MiniUnitScaleDebug;
import mindustry.Vars;
import mindustry.content.Bullets;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.FileTreeInitEvent;
import mindustry.game.Gamemode;
import mindustry.gen.Groups;
import mindustry.mod.Mod;
import mindustry.mod.Mods;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.SettingsMenuDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.*;

import static arc.Core.*;
import static axthrix.world.types.bulletypes.GluonBulletType.activeBuildingTethers;
import static axthrix.world.types.bulletypes.GluonBulletType.activeTethers;
import static mindustry.Vars.*;

public class AxthrixLoader extends Mod{
    public static Seq<BulletData> allBullets = new Seq<>();
    public static boolean funibullet = false;
    public static boolean MINI = false;
    //debug
    public static boolean amosPowerDebug = false;
    public static boolean amosLiquidDebug = false;
    public static boolean showMessage = true;
    // Visuals
    public static int tempUnit = 0; // 0: Celsius, 1: Fahrenheit, 2: Kelvin
    public static boolean followRealCaps = false;
    public static boolean showRevolverAmmo = true;
    public static boolean drawEnchancedShadows = true;
    public static boolean drawUnderwaterVoid = true;
    public static boolean drawUnderwaterSand = true;
    public static float hudButtonScale = 1f;
    public static boolean showMobileButtons = mobile;
    public static boolean showAdvanced = true;
    public static int customHudOffsetX = 0;
    public static int customHudOffsetY = 0;
    public static int nadoEffectDensity = 100;
    CustomUnitInfoBar unitInfoBar;

    public static KeyCode bindToggle = KeyCode.controlLeft;
    public static KeyCode bindActivate = KeyCode.altLeft;

    private static boolean rebindingToggle;
    private static boolean rebindingActivate;

    public static boolean toggleTapped() {
        return !mobile && Core.input.keyTap(bindToggle);
    }

    public static boolean activateTapped() {
        return !mobile && Core.input.keyTap(bindActivate);
    }

    public static boolean activateDown() {
        return !mobile && Core.input.keyDown(bindActivate);
    }

    private static void startRebind(boolean toggle) {
        rebindingToggle = toggle;
        rebindingActivate = !toggle;
        ui.showInfoToast(bundle.get("setting.aj-rebind-press"), 2f);
    }

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
    }
    @Override
    public void init(){

        /// UI INITIALIZATION

        unitInfoBar = new CustomUnitInfoBar();
        unitInfoBar.build();

        /// SETTINGS LOADING

        funibullet = settings.getBool("aj-funni-disabled", false);
        MINI = settings.getBool("aj-mini", false);
        drawEnchancedShadows = settings.getBool("aj-enhanced-shadows", true);
        amosPowerDebug = settings.getBool("aj-mount-power-debug", false);
        amosLiquidDebug = settings.getBool("aj-mount-liquid-debug", false);
        showMessage = settings.getBool("aj-message-debug", true);
        showRevolverAmmo = settings.getBool("aj-revolver-ammo", true);
        drawUnderwaterVoid = settings.getBool("aj-draw-void", true);
        drawUnderwaterSand = settings.getBool("aj-draw-sand", true);
        showMobileButtons = settings.getBool("aj-show-mobile-buttons", mobile);
        hudButtonScale = settings.getInt("aj-hud-button-scale", 100) / 100f;
        tempUnit = settings.getInt("aj-temp-unit", 0);
        followRealCaps = settings.getBool("aj-follow-caps", false);
        nadoEffectDensity = settings.getInt("aj-nado-density", 100);
        showAdvanced = settings.getBool("aj-advanced-ui", true);
        customHudOffsetX = settings.getInt("aj-hud-X-offset", 4);
        customHudOffsetY = settings.getInt("aj-hud-Y-offset", -120);
        bindToggle = KeyCode.valueOf(settings.getString("aj-bind-toggle", KeyCode.controlLeft.name()));
        bindActivate = KeyCode.valueOf(settings.getString("aj-bind-activate", KeyCode.altLeft.name()));


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

        Events.run(EventType.Trigger.update, () -> {
            if (Core.input.keyDown(KeyCode.controlLeft) && Core.input.keyTap(KeyCode.f7) && Vars.state.rules.mode() == Gamemode.sandbox) {
                MiniUnitScaleDebug.open();
                ui.showInfoToast(Core.bundle.get("menu.mini-scaler-page-warning"), 20f);
            }

            if (rebindingToggle || rebindingActivate) {
                for (KeyCode k : KeyCode.all) {
                    if (k == KeyCode.mouseLeft || k == KeyCode.mouseRight || k == KeyCode.mouseMiddle) continue;
                    if (Core.input.keyTap(k)) {
                        if (rebindingToggle) {
                            bindToggle = k;
                            settings.put("aj-bind-toggle", k.name());
                        } else {
                            bindActivate = k;
                            settings.put("aj-bind-activate", k.name());
                        }
                        rebindingToggle = rebindingActivate = false;
                        ui.showInfoToast(k.toString(), 1.5f);
                        break;
                    }
                }
            }
        });

        Events.run(EventType.Trigger.update, () -> {
            // Attachment grenade updates
            Groups.unit.each(AttachmentGrenadeBulletType::updateGrenades);

            // Grab bullet updates
            GrabBulletType.updateGrabs();

            //Harpoon Bullet Updates
            HarpoonBulletType.update();

            if (Time.time % 60 == 0 && activeBuildingTethers.size > 0) {
                Log.info("Active Building Tethers: " + activeBuildingTethers.size);
            }

            // Tether bullet updates
            activeTethers.removeAll(t -> t.update());
            activeBuildingTethers.removeAll(t -> t.update());

            // Nanobot cloud updates
            content.units().each(u -> u.weapons.each(w -> {
                if (w.bullet instanceof NanobotBulletType nb) nb.updateClouds();
            }));

            // Temperature system updates
            if (!headless) {
                TemperatureLogic.updateTemperatureSystem();
            }

            // Temperature floor updates
            if (state.isPlaying()) {
                Groups.unit.each(unit -> {
                    Tile tile = unit.tileOn();
                    if (tile != null && tile.floor() instanceof TemperatureFloor tempFloor) {
                        float temp = tempFloor.temperaturePerSecond * Time.delta / 60f;
                        TemperatureLogic.applyTemperatureUnit(unit, temp);

                        if (Mathf.chance(tempFloor.effectChance * Time.delta)) {
                            if (tempFloor.temperaturePerSecond > 0) {
                                Fx.fire.at(unit.x, unit.y);
                            } else {
                                Fx.freezing.at(unit.x, unit.y);
                            }
                        }
                    }
                });

                Groups.build.each(TemperatureLogic::applyTemperatureFromFloors);
            }

        });


/// DRAW EVENTS (Run every frame during draw)

        SubmergedUpdaterAndRenderer.init();

        Events.run(EventType.Trigger.draw, () -> {
            // Attachment grenade drawing
            Groups.unit.each(AttachmentGrenadeBulletType::drawGrenades);

            // Grab bullet drawing
            GrabBulletType.drawGrabs();

            //Harpoon Bullet drawing
            HarpoonBulletType.draw();

            // Tether bullet drawing
            for (GluonBulletType.Tether t : activeTethers) t.draw();
            for (GluonBulletType.BuildingTether t : activeBuildingTethers) t.draw();

            // Nanobot cloud drawing
            content.units().each(u -> u.weapons.each(w -> {
                if (w.bullet instanceof NanobotBulletType nb) nb.drawClouds();
            }));

        });



        /// ENTITY DAMAGE EVENTS

        // Building heat damage amplification
        Events.on(EventType.BuildDamageEvent.class, event -> {
            if (event.source == null) return; // Skip non-bullet damage

            // Heat damage amplification
            float tempMult = TemperatureLogic.getTemperatureDamageMultiplier(event.build);
            float baseDamage = event.source.damage;
            float extraDamage = (baseDamage * tempMult) - baseDamage;

            if (TemperatureLogic.debugLogging && extraDamage > 0) {
                Log.info("[DamageEvent] Block @ (pos: @): Base damage: @, Temp mult: @x, Extra damage: @",
                        event.build.block.name, event.build.pos(), baseDamage, tempMult, extraDamage);
            }

            if (extraDamage > 0) {
                event.build.damage(extraDamage);

                if (TemperatureLogic.debugLogging) {
                    Log.info("[DamageEvent] Block @ (pos: @): Total extra damage applied: @",
                            event.build.block.name, event.build.pos(), extraDamage);
                }
            }
        });

        Events.on(EventType.UnitDamageEvent.class, ShieldPlateAbility::onUnitDamage);

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
            float tempMult = TemperatureLogic.getTemperatureDamageMultiplier(event.unit);
            float baseDamage = event.bullet.damage;
            float extraDamage = (baseDamage * tempMult) - baseDamage;

            if(TemperatureLogic.debugLogging && extraDamage > 0){
                Log.info("[DamageEvent] Unit @ (ID: @): Base damage: @, Temp mult: @x, Extra damage: @",
                        event.unit.type.name, event.unit.id, baseDamage, tempMult, extraDamage);
            }

            // Cold resistance reduction
            float temp = TemperatureLogic.getTemperatureUnit(event.unit);
            if(temp < 0){
                float effectiveMult = TemperatureLogic.getEffectiveHealthMultiplier(event.unit);
                if(effectiveMult < event.unit.healthMultiplier){
                    float compensationMult = event.unit.healthMultiplier / effectiveMult;
                    float resistanceDamage = baseDamage * (compensationMult - 1f);
                    extraDamage += resistanceDamage;

                    if(TemperatureLogic.debugLogging){
                        Log.info("[DamageEvent] Unit @ (ID: @): Resistance bypass - Health mult: @, Effective: @, Extra damage: @",
                                event.unit.type.name, event.unit.id, event.unit.healthMultiplier, effectiveMult, resistanceDamage);
                    }
                }
            }

            // Apply total extra damage
            if(extraDamage > 0){
                event.unit.damage(extraDamage);

                if(TemperatureLogic.debugLogging){
                    Log.info("[DamageEvent] Unit @ (ID: @): Total extra damage applied: @",
                            event.unit.type.name, event.unit.id, extraDamage);
                }
            }
        });
    }
    @Override
    public void loadContent(){
        Log.info("Loading Axthrix content");
        AxthrixWorldState.load();
        AxFactions.load();
        AxStats.load();
        AxthrixSounds.LoadSounds();
        AxResources.Iload();
        AxthrixStatus.load();
        AxthrixWeathers.load();
        AxResources.Lload();
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
        AxthrixTurrets.loadIkatusa();
        AxthrixTurrets.loadRaodon();
        AxthrixTurrets.loadDef();
        AxPlanets.load();
        //AxSectorPresets.load();
        AxthrixTechTree.load();
        Log.info("Axthrix Content Loaded. :)");


        Events.on(ClientLoadEvent.class, e -> Time.runTask(10f, () -> {
            if (!showMessage) return;

            Seq<AnimatedWelcomeDialog> dialogPool = new Seq<>();

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-mark-my-words.message", "mark-my-words", AxthrixSounds.MarkMyWords, 244) {{
                chanceToTrigger = 0.01f;
                targetAudioDurationSeconds = 24.450f;
                showSkipButton = false;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-phoenix.message", "phoenix", AxthrixSounds.Phoenix, 401) {{
                chanceToTrigger = 0.01f;
                targetAudioDurationSeconds = 40.333f;
                showSkipButton = false;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-aura-monster.message", "aura-monster", AxthrixSounds.AuraMonster, 102) {{
                chanceToTrigger = 0.001f;
                targetAudioDurationSeconds = 4.26f;
                loopAnimation = true;
                showSkipButton = true;
                delayBetweenLoops = 2f;
                cutsceneVolume = 5f;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-lotro.message", "lotro", AxthrixSounds.LOTRO, 120) {{
                chanceToTrigger = 0.01f;
                targetAudioDurationSeconds = 230.086f;
                isMusicMode = true;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-erza.message", "erza", AxthrixSounds.Erza, 120) {{
                chanceToTrigger = 0.01f;
                targetAudioDurationSeconds = 180.506f;
                isMusicMode = true;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-edge.message", "edge", AxthrixSounds.Edge, 120) {{
                chanceToTrigger = 0.01f;
                targetAudioDurationSeconds = 166.060f;
                isMusicMode = true;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-horizon.message", "horizon", AxthrixSounds.Horizon, 120) {{
                chanceToTrigger = 0.015f;
                targetAudioDurationSeconds = 179.487f;
                isMusicMode = true;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-intra.message", "intra", AxthrixSounds.Intraregios, 120) {{
                chanceToTrigger = 0.015f;
                targetAudioDurationSeconds = 206.080f;
                isMusicMode = true;
            }});

            dialogPool.add(new AnimatedWelcomeDialog("menu.aj-menu.title", "menu.aj-valence.message", "valence", AxthrixSounds.Valence, 120) {{
                chanceToTrigger = 0.001f;
                targetAudioDurationSeconds = 240.048f;
                isMusicMode = true;
            }});

            float totalRarityWeightAccumulator = 0f;
            for (AnimatedWelcomeDialog d : dialogPool) {
                totalRarityWeightAccumulator += d.chanceToTrigger;
            }

            float rolledRaritySelectorValue = arc.math.Mathf.random(0f, 1f);

            if (rolledRaritySelectorValue <= totalRarityWeightAccumulator) {
                float currentWeightRangeBracket = 0f;
                boolean matchFound = false;

                for (AnimatedWelcomeDialog d : dialogPool) {
                    currentWeightRangeBracket += d.chanceToTrigger;
                    if (rolledRaritySelectorValue <= currentWeightRangeBracket) {
                        d.chanceToTrigger = 1.0f;
                        d.show();
                        matchFound = true;
                        break;
                    }
                }
                if (matchFound) return;
            }
            BaseDialog fallbackDialog = new BaseDialog(bundle.get("menu.aj-menu.title"));
            fallbackDialog.cont.add(bundle.get("menu.aj-menu.message")).row();
            fallbackDialog.cont.image(atlas.find("aj-welcome-icon")).pad(20f).row();
            fallbackDialog.cont.button("okay", fallbackDialog::hide).size(100f, 50f);
            fallbackDialog.show();
        }));

    }
    private void loadSettings(){
        ui.settings.addCategory(bundle.get("setting.aj-title"), "aj-settings-icon", t -> {

            t.pref(new AxUtil.Separator(10));
            t.pref(new AxUtil.Header("Sandbox Shenanigans",20,true,true));
            t.checkPref("aj-funni-disabled", false);
            t.pref(new AxUtil.Separator(5));
            t.checkPref("aj-mini", false, b -> {
                MINI = b;
            });

            t.pref(new AxUtil.Separator(10));
            t.pref(new AxUtil.Header("Debug Elements",20,true,true));
            t.checkPref("aj-message-debug", true);
            t.pref(new AxUtil.Separator(5));
            t.checkPref("aj-mount-power-debug", false);
            t.checkPref("aj-mount-liquid-debug", false);

            t.pref(new AxUtil.Separator(10));
            t.pref(new AxUtil.Header("Temperature Elements",20,true,true));
            t.sliderPref("aj-temp-unit", 0, 0, 2, 1, i -> {
                tempUnit = i;
                return switch (i) {
                    case 1 -> "Fahrenheit";
                    case 2 -> "Kelvin";
                    default -> "Celsius";
                };
            });
            t.pref(new AxUtil.Separator(5));
            t.checkPref("aj-follow-caps", false, b -> {
                followRealCaps = b;
            });

            t.pref(new AxUtil.Separator(10));
            t.pref(new AxUtil.Header("Effects",20,true,true));
            t.checkPref("aj-revolver-ammo", true, b -> {
                showRevolverAmmo = b;
            });
            t.pref(new AxUtil.Separator(5));
            t.sliderPref("aj-nado-density", 100, 20, 100, 20, i -> {
                nadoEffectDensity = i;
                return i == -1 ? "@aj-nado-density.def" : i + "%";
            });
            t.pref(new AxUtil.Separator(4));
            t.pref(new AxUtil.Header("Underwater",1,true,false));
            t.checkPref("aj-draw-void", true, b -> {
                drawUnderwaterVoid = b;
            });
            t.checkPref("aj-draw-sand", true, b -> {
                drawUnderwaterSand = b;
            });
            t.checkPref("aj-enchanced-shadows", true, b -> {
                drawEnchancedShadows = b;
            });

            t.pref(new AxUtil.Separator(10));
            t.pref(new AxUtil.Header("Ui Elements",20,true,true));
            if(!mobile){
                t.checkPref("aj-show-mobile-buttons", false, b -> {
                    showMobileButtons = b;
                });
                t.pref(new AxUtil.Separator(4));
            }else{
                showMobileButtons = true;
            }
            t.sliderPref("aj-hud-button-scale", 100, 100, 150, 5, i -> {
                hudButtonScale = i / 100f;
                return i + "%";
            });
            t.pref(new AxUtil.Separator(10));
            t.checkPref("aj-advanced-ui", true, b -> {
                showAdvanced = b;
            });

            t.pref(new AxUtil.Separator(4));
            t.pref(new AxUtil.Header("Custom HUD Positioning Offsets", 1, true, false));
            t.sliderPref("aj-hud-X-offset", 4, -100, 500, 1, i -> {
                customHudOffsetX = i;
                return i + "px";
            });
            t.sliderPref("aj-hud-Y-offset", -120, -500, 250, 1, i -> {
                customHudOffsetY = i;
                return i + "px";
            });

            if (!mobile) {
                t.pref(new AxUtil.Separator(4));
                t.pref(new AxUtil.Header("Keybinds (Desktop)", 1, true, false));

                t.pref(new SettingsMenuDialog.SettingsTable.Setting("aj-bind-toggle") {
                    {
                        title = "setting.aj-bind-toggle";
                    }

                    @Override
                    public void add(SettingsMenuDialog.SettingsTable table) {
                        table.table(col -> {
                            col.left();
                            col.add(bundle.get(title)).left().wrap().width(Core.graphics.getWidth() > 400 ? 280f : 200f).row();
                            col.button(b -> b.label(() -> bindToggle.toString()).labelAlign(Align.center),
                                    Styles.defaultt,
                                    () -> startRebind(true)
                            ).width(200f).height(40f).left();
                        }).left().padTop(6f).padBottom(4f).growX().row();
                    }
                });

                t.pref(new SettingsMenuDialog.SettingsTable.Setting("aj-bind-activate") {
                    {
                        title = "setting.aj-bind-activate";
                    }

                    @Override
                    public void add(SettingsMenuDialog.SettingsTable table) {
                        table.table(col -> {
                            col.left();
                            col.add(bundle.get(title)).left().wrap().width(Core.graphics.getWidth() > 400 ? 280f : 200f).row();
                            col.button(b -> b.label(() -> bindActivate.toString()).labelAlign(Align.center),
                                    Styles.defaultt,
                                    () -> startRebind(false)
                            ).width(200f).height(40f).left();
                        }).left().padTop(6f).padBottom(4f).growX().row();
                    }
                });
            }

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
