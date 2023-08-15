package axthrix.content;

import axthrix.world.types.statuseffects.StatusEffectStack;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.content.*;

public class AxthrixStatus {
    public static StatusEffect vindicationI, vindicationII, vindicationIII, nanodiverge, precludedX, precludedA, vibration, repent;

    public static void load(){
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

        repent = new StatusEffectStack("repent"){{
            color = color.yellow;
            reloadMultiplier = 1.5f;
            charges = 15;
            show = false;
            staticStat();
        }};
    }
}        