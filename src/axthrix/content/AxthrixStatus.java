package axthrix.content;

import arc.graphics.Color;
import axthrix.content.FX.AxthrixFfx;
import axthrix.content.FX.AxthrixFx;
import axthrix.world.types.abilities.ChainHealAbility;
import axthrix.world.types.statuseffects.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.content.*;

public class AxthrixStatus {
    public static StatusEffect vindicationI, vindicationII, vindicationIII, nanodiverge, precludedX, precludedA, vibration, repent, finalStand,excert,chainExcert,

    //visual statuses
    standFx,bFx
            ;

    public static void load(){
        bFx = new StatusEffect("b-trigger-vfx") {{
            applyEffect = AxthrixFx.unitBreakdown;
            show = false;
        }};
        standFx = new StatusEffect("stand-trigger-vfx") {{
            color = Color.blue;
            applyEffect = AxthrixFfx.circleOut(180, 30, 4,Color.blue);
            parentizeApplyEffect = true;
            show = false;
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

        vibration = new StatusEffect("vibration"){{
            color = Pal.lightishGray;
            speedMultiplier = 0.60f;
            reloadMultiplier = 0.80f;
            damage = 5f;
            transitionDamage = 20f;
            init(() -> {
                opposite(StatusEffects.unmoving); 
            });
        }};


        
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

        repent = new StackStatusEffect("repent"){{
            color = Color.yellow;
            reloadMultiplier = 1.5f;
            charges = 15;
            show = false;
        }};

        finalStand = new StatusEffectTrigger("final-stand"){{
            localizedName = "[blue]Final Stand";
            description = "Protects you at low HP\n[orange]Activation Threshold[] \n>|[lightgray]30%[]| HP.\n[orange]Activation Invincibility Duration[] \n>|[lightgray]3[]| Seconds.";
            activationStatusFx = standFx;
            activationThreshold = 4f;
            activationResistanceTime = 180f;
            permanent = true;
        }};

        excert = new StatusEffectAbility("excert") {{
            localizedName = "[green]Excert";
            permanent = false;
        }};
        chainExcert = new StatusEffectAbility("chain-excert") {{
            ability = new ChainHealAbility(excert,60,35,8*8);
            ((StatusEffectAbility)excert).ability = new ChainHealAbility(this,60,35,8*8);
            localizedName = "[green]Chain Excert";
            permanent = false;
        }};
    }
}        