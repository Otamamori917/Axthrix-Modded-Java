package axthrix.content;

import arc.Core;
import arc.graphics.Color;
import axthrix.content.FX.AxthrixFfx;
import axthrix.content.FX.AxthrixFx;
import axthrix.world.types.abilities.ChainHealAbility;
import axthrix.world.types.abilities.SilveringWeakness;
import axthrix.world.types.statuseffects.*;
import mindustry.entities.Effect;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.units.StatusEntry;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.meta.Stat;

import java.util.HashMap;

import static arc.math.Interp.pow10Out;
import static axthrix.content.AxthrixSounds.*;

public class AxthrixStatus {
    public static StatusEffect
            melting,
            vindicationI, vindicationII, vindicationIII, nanodiverge,
            precludedX, precludedA,
            Thundering,Lightning,
            finalStand,ReapAndSow,
            excert,chainExcert,
            slivered,
            unrepair,grayRepair,repair,
            gravicalSlow,
            minigame,


    //visual statuses
    standFx,ReapFx,bFx
            ;

    public static void load(){
        bFx = new StatusEffect("b-trigger-vfx") {{
            applyEffect = AxthrixFx.unitBreakdown;
            show = false;
        }};
        standFx = new StatusEffect("stand-trigger-vfx") {{
            color = Color.blue;
            applyEffect = AxthrixFfx.circleOut(180, 30, 4,Layer.blockOver,Color.blue);
            parentizeApplyEffect = true;
            show = false;
        }};

        ReapFx = new StatusEffect("reap-vfx") {{
            color = Color.valueOf("972020");
            applyEffect = new MultiEffect(
                AxthrixFfx.circleOut(30, 30, 4,Layer.blockOver,Color.valueOf("972020")),
                AxthrixFfx.circleOut(35, 35, 5,Layer.blockOver,Color.valueOf("972020")),
                AxthrixFfx.circleOut(40, 40, 6,Layer.blockOver,Color.valueOf("972020")),
                AxthrixFfx.circleOut(45, 45, 7,Layer.blockOver,Color.valueOf("972020"))
            );
            parentizeApplyEffect = true;
            show = false;
        }};

        melting = new StackStatusEffect("melting"){{
            color = Color.valueOf("ff2a00");
            damage = 0.01f;
            charges = 25;
            effect = AxthrixFx.ahhimaLiquidNow;
        }};

        vindicationI = new StatusEffect("vindicationI"){{
            color = Pal.heal;
            healthMultiplier = 1.25f;
            speedMultiplier = 0.80f;
            init(() -> {
                opposite(nanodiverge);
            });
        }};

        vindicationII = new StatusEffect("vindicationII"){{
            color = Pal.heal;
            healthMultiplier = 2.50f;
            speedMultiplier = 0.40f;
            init(() -> {
                opposite(nanodiverge);
            });
        }}; 

        vindicationIII = new StatusEffect("vindicationIII"){{
            color = Pal.heal;
            healthMultiplier = 5f;
            speedMultiplier = 0.20f;
            init(() -> {
                opposite(nanodiverge);
            });
        }};

        nanodiverge = new StatusEffect("nanodiverge"){{
            color = Pal.heal;
            speedMultiplier = 0.80f;
            reloadMultiplier = 0.80f;
            damage = 0.4f;
            transitionDamage = 8f;
            init(() -> {
                opposite(vindicationI);
                opposite(vindicationII);
                opposite(vindicationIII);   
            });
        }};

        unrepair = new StackStatusEffect("unrepair"){{
            color = Color.valueOf("6d6f7c");
            healthMultiplier = .998f;
            charges = 300;
        }};
        gravicalSlow = new StackStatusEffect("gravical-slow"){{
            color = Color.purple;
            speedMultiplier = buildSpeedMultiplier = .985f;
            charges = 50;
        }};

        grayRepair = new StatusEffect("gray-repair"){
            @Override
            public void update(Unit unit, StatusEntry entry){
                {
                    color = Color.valueOf("80ffb8");
                }
                if(unit.hasEffect(unrepair)){
                    StackStatusEffect.stackRemove(unit,4,unrepair);
                    AxthrixFfx.circleOut(120, 30, 4,Layer.blockOver,Color.valueOf("80ffb8")).at(unit.x,unit.y);
                    if(unit.health+(unit.maxHealth() / 6.66f) >= unit.maxHealth() || unit.health == unit.maxHealth()){
                        unit.health = unit.maxHealth();
                        unit.unapply(this);
                    }else{
                        unit.health = unit.health + (unit.maxHealth() / 6.66f);
                        AxthrixFfx.circleOut(180, 30, 4,Layer.blockOver,Color.valueOf("80ffb8")).at(unit.x,unit.y);
                        unit.unapply(this);
                    }

                }else{
                    if(unit.health+(unit.maxHealth() / 10f) >= unit.maxHealth() || unit.health == unit.maxHealth()){
                        unit.health = unit.maxHealth();
                        unit.unapply(this);
                    }else{
                        unit.health = unit.health + (unit.maxHealth() / 10f);
                        AxthrixFfx.circleOut(180, 15, 4,Layer.blockOver,Color.valueOf("80ffb8")).at(unit.x,unit.y);
                        unit.unapply(this);
                    }

                }
            }
            public void setStats(){
                super.setStats();
                stats.add(new Stat("gray-repair-repair"),"[stat]10% ~ 15%");
                stats.add(new Stat("gray-repair-restore"),"[stat]25%");
            }
        };
        repair = new StatusEffect("repair"){
            @Override
            public void update(Unit unit, StatusEntry entry){
                {
                    color = Color.green;
                }
                if(unit.health+(unit.maxHealth() / 12.5f) >= unit.maxHealth() || unit.health == unit.maxHealth()){
                    unit.health = unit.maxHealth();
                    unit.unapply(this);
                }else{
                    unit.health = unit.health + (unit.maxHealth() / 12.5f);
                    AxthrixFfx.circleOut(180, 16, 2,Layer.blockOver,Color.green).at(unit.x,unit.y);
                    unit.unapply(this);
                }

            }
            public void setStats(){
                super.setStats();
                stats.add(new Stat("gray-repair-repair"),"[stat]8%");
            }
        };

        
        precludedX = new StatusEffect("precludedX"){{
            color = Pal.remove;
            speedMultiplier = 0.01f;
            buildSpeedMultiplier = 0f;
            reloadMultiplier = 6f;
            init(() -> {
                opposite(precludedA);
            });
        }};

        precludedA = new StatusEffect("precludedA"){{
            color = Pal.remove;
            speedMultiplier = 5f;
            buildSpeedMultiplier = 5f;
            reloadMultiplier = 0.01f;
            init(() -> {
                opposite(precludedX);
            });
        }};
        minigame = new StatusEffect("minigame"){

            boolean once = false;
            boolean once1 = false;
            boolean once2 = false;
            public final HashMap<Unit, Float> delay = new HashMap<>();
            public final HashMap<Unit, Float> delay2 = new HashMap<>();
            public final HashMap<Unit, Float> delay3 = new HashMap<>();
            public final HashMap<Unit, Float> deathTimer = new HashMap<>();
            public final HashMap<Unit, Float> minigame = new HashMap<>();
            public BaseDialog dia1;
            public BaseDialog dia2;


            @Override
            public void update(Unit unit, StatusEntry entry){
                if (!delay.containsKey(unit)){
                    delay.put(unit,0f);
                }
                if (!delay2.containsKey(unit)){
                    delay2.put(unit,500f);
                }
                if (!delay3.containsKey(unit)){
                    delay3.put(unit,0f);
                }
                if (!deathTimer.containsKey(unit)){
                    deathTimer.put(unit,240f);
                }
                if (!minigame.containsKey(unit)){
                    minigame.put(unit,0f);
                }
                if(!once){
                    PandemoniumScreaming.play();
                    once = true;
                }
                if(!unit.isPlayer()){
                    unit.health = 0;
                    unit.dead = true;
                    unit.unapply(this);
                } else {
                    if(delay.get(unit) >= 240){
                        //Log.info("X:"+Core.input.mouseX()+"    Y:"+Core.input.mouseY());
                        if (!once1){
                            PandemoniumScreaming.stop();
                            BaseDialog dialog = new BaseDialog(Core.bundle.get("menu.aj-minigame.title"));
                            dialog.cont.add(Core.bundle.get("menu.aj-minigame.message")).padTop(10).row();
                            dialog.cont.image(Core.atlas.find("aj-white-line")).padTop(10).row();
                            dialog.cont.image(Core.atlas.find("aj-safe-circle")).pad(16f).row();
                            dia1 = dialog;
                            if (!dialog.isShown()){
                                dialog.show();
                                PandemoniumMinigameTheme.play();
                            }

                            once1 = true;
                        }
                        if(!(deathTimer.get(unit) <= 0)){
                            if(Core.input.mouseX() > 720 && Core.input.mouseX() < 815 && Core.input.mouseY() > 350 && Core.input.mouseY() < 460){
                                if(deathTimer.get(unit) >= 240){
                                    deathTimer.replace(unit,240f);
                                }else{
                                    deathTimer.replace(unit,deathTimer.get(unit)+1);
                                }
                            }else{
                                deathTimer.replace(unit,deathTimer.get(unit)-1);
                            }
                            //Log.info("timeLeft:"+deathTimer.get(unit));
                        }else{
                            if(dia1 != null){dia1.hide();}
                            PandemoniumMinigameTheme.stop();
                            if(!once2){
                                BaseDialog dialog = new BaseDialog(Core.bundle.get("menu.aj-minigame.title"));
                                dialog.cont.image(Core.atlas.find("aj-pandi")).row();
                                dia2 = dialog;
                                if (!dialog.isShown()){
                                    PandemoniumScreaming.play(2);
                                    dialog.show();
                                }
                                once2 = true;
                            }
                            if(delay3.get(unit) >= 120){
                                unit.health = 0;
                                unit.dead = true;
                            }
                            delay3.replace(unit,delay3.get(unit)+1);
                        }
                        if(minigame.get(unit) >= 950){
                            //Log.info("shake:"+delay2.get(unit));
                            if(delay2.get(unit) >= 500){
                                MetalCrash.play(1);
                                Effect.shake(1, 50, unit.x,unit.y);
                                delay2.replace(unit,0f);
                            }
                            delay2.replace(unit,delay2.get(unit)+1);
                        }
                        //Log.info("timer"+minigame.get(unit));
                        if(minigame.get(unit) >= 11260){
                            once2 = once1 = once = false;
                            if(dia1 != null){dia1.hide();}
                            if(dia2 != null){dia2.hide();}
                            delay.remove(unit);
                            delay2.remove(unit);
                            delay3.remove(unit);
                            deathTimer.remove(unit);
                            minigame.remove(unit);
                            dia1 = dia2 =null;
                            unit.unapply(this);

                        }
                        if(minigame.get(unit) != null){
                            minigame.replace(unit,minigame.get(unit)+1);
                        }
                    }
                    if(delay.get(unit) != null) {
                        delay.replace(unit, delay.get(unit) + 1);
                    }
                }
                if(unit.dead || !unit.hasEffect(this)){
                    once2 = once1 = once = false;
                    PandemoniumMinigameTheme.stop();
                    PandemoniumScreaming.stop();
                    if(dia1 != null){dia1.hide();}
                    if(dia2 != null){dia2.hide();}
                    delay.remove(unit);
                    delay2.remove(unit);
                    delay3.remove(unit);
                    deathTimer.remove(unit);
                    minigame.remove(unit);
                    dia1 = dia2 =null;
                    unit.unapply(this);
                }
            }
            {
            color = Color.black;
            speedMultiplier = 0f;
            buildSpeedMultiplier = 0f;
            reloadMultiplier = 0f;
            permanent = true;
        }};

        Thundering = new StackStatusEffect("thundering"){{
            tickFx = new ParticleEffect(){{
                lifetime = 50;
                particles = 3;
                length = 12;
                baseLength = 2;
                interp = pow10Out;
                colorFrom = Color.valueOf("576A7399");
                colorTo = Color.valueOf("576A734E");
                sizeFrom = 0;
                sizeTo = 3;
                layer = Layer.blockOver;
            }};
            color = Color.yellow;
            speedMultiplier = 0.975f;
            charges = 15;
        }};

        Lightning = new FragStatusEffect("lightning"){{
            fragBullets = 4;
            fragBullet = new LightningBulletType(){{
                lightningLength = 4;
                lightningColor = Color.orange.cpy().add(Color.maroon);
                damage = 10f;
            }};
        }};

        finalStand = new StatusEffectTrigger("final-stand"){{
            activationStatusFx = standFx;
            activationThreshold = 4f;
            activationResistanceTime = 180f;
            permanent = true;
        }};
        ReapAndSow = new StatusEffectTrigger("reap-and-sow"){{
            activationStatusFx = ReapFx;
            color = Color.valueOf("972020");
            Reap = true;
            activationThreshold = 5f;
            permanent = false;
        }};

        excert = new StatusEffectAbility("excert") {{
            permanent = false;
        }};
        chainExcert = new StatusEffectAbility("chain-excert") {{
            ability = new ChainHealAbility(excert,60,35,8*8);
            ((StatusEffectAbility)excert).ability = new ChainHealAbility(this,60,35,8*8);
            permanent = false;
        }};
        slivered = new StatusEffectAbility("slivered"){{
            show = true;
            permanent = false;
            ability = new SilveringWeakness(){{
                healthReduction = 0.7f;
                maxPentalyTime = 240;
            }};
        }};
    }
}        