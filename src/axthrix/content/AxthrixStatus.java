package axthrix.content;

import arc.graphics.Color;
import axthrix.content.FX.AxthrixFfx;
import axthrix.content.FX.AxthrixFx;
import axthrix.world.types.abilities.ChainHealAbility;
import axthrix.world.types.statuseffects.*;
import mindustry.entities.abilities.ArmorPlateAbility;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.content.*;

public class AxthrixStatus {
    public static StatusEffect vindicationI, vindicationII, vindicationIII, nanodiverge, precludedX, precludedA, vibration, repent, finalStand,excert,chainExcert,slivered,

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
        slivered = new StatusEffectAbility("slivered"){{
            localizedName = "[#9d98ab]Sli[#8b8696]ver[#7c7887]ed";
            description = """
                          [#9d98ab]Dan[#8b8696]ger[#7c7887]ous [#9d98ab]ele[#8b8696]men[#7c7887]ts [#9d98ab]a[#8b8696]r[#7c7887]e [#9d98ab]co[#8b8696]at[#7c7887]ed [#9d98ab]on[#8b8696]t[#7c7887]o [#9d98ab]eff[#8b8696]ect[#7c7887]ed [#9d98ab]ta[#8b8696]rg[#7c7887]et [#9d98ab]wea[#8b8696]ken[#7c7887]ing [#9d98ab]th[#8b8696]ei[#7c7887]r [#9d98ab]ar[#8b8696]mo[#7c7887]r
                          [#9d98ab]c[#8b8696]a[#7c7887]n [#9d98ab]al[#8b8696]s[#7c7887]o [#9d98ab]pro[#8b8696]te[#7c7887]ct [#9d98ab]th[#8b8696]e[#7c7887]m [#9d98ab]un[#8b8696]de[#7c7887]r [#9d98ab]cer[#8b8696]ta[#7c7887]in                    [#9d98ab]circu[#8b8696]msta[#7c7887]nces
                          """;

            show = true;
            healthMultiplier = 0.1f;
            permanent = false;
            ability = new ArmorPlateAbility(){{
                healthMultiplier = 0.6f;
            }};
        }};
    }
}        