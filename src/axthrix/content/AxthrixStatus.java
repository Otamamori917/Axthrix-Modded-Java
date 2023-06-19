package axthrix.content;

import mindustry.graphics.*;
import mindustry.type.*;

public class AxthrixStatus {
    public static StatusEffect vindicationI, vindicationII, vindicationIII, nanodiverge, precludedX, precludedA, vibration;

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
            color = Pal.heal;
            speedMultiplier = 0.90f;
            reloadMultiplier = 0.80f;
            damage = 0.2f;
            transitionDamage = 20f;
            init(() -> {
                opposite(unmoving); 
            });
        }};


        
        precludedX = new StatusEffect("precludedX"){{
            color = Pal.remove;
            speedMultiplier = 0.01f;
            buildSpeedMultiplier = 0f;
            reloadMultiplier = 4f;
            damageMultiplier = 4f;
            init(() -> {
                opposite(precludedA);
            });
        }};

        precludedA = new StatusEffect("precludedA"){{
            color = Pal.remove;
            speedMultiplier = 4f;
            buildSpeedMultiplier = 4f;
            disarm = true;
            init(() -> {
                opposite(precludedX);
            });
        }};        
    }
}        