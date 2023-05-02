package ajmain.content;

import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.content.*;

public class StatusA {
    public static StatusEffect vindicationI, vindicationII, vindicationIII, precludedX, precludedA;

    public static void load(){


        vindicationI = new StatusEffect("vindicationI"){{
            color = Pal.heal;
            healthMultiplier = 1.25f;
            speedMultiplier = 0.80f;
        }};

        vindicationII = new StatusEffect("vindicationII"){{
            color = Pal.heal;
            healthMultiplier = 2.50f;
            speedMultiplier = 0.40f;
        }}; 

        vindicationIII = new StatusEffect("vindicationIII"){{
            color = Pal.heal;
            healthMultiplier = 5f;
            speedMultiplier = 0.20f;
        }};


        
        precludedX = new StatusEffect("precludedX"){{
            color = Color.valueOf("8B4000");
            speedMultiplier = 0.01f;
            buildSpeedMultiplier = 0f;
            reloadMultiplier = 4f;
            damageMultiplier = 4f;
            init(() -> {
                opposite(precludedA);
            });
        }};

        precludedA = new StatusEffect("precludedA"){{
            color = Color.valueOf("8B4000");
            speedMultiplier = 4f;
            buildSpeedMultiplier = 4f;
            disarm = true;
            init(() -> {
                opposite(precludedX);
            });
        }};        
    }
}        