package ajmain.content;

import mindustry.graphics.*;
import mindustry.type.*;

public class StatusA {
    public static StatusEffect turteI, turteII, turteIII;

    public static void load(){


        turteI = new StatusEffect("turteI"){{
            color = Pal.heal;
            healthMultiplier = 1.25f;
            speedMultiplier = 0.80f;
        }};

        turteII = new StatusEffect("turteII"){{
            color = Pal.heal;
            healthMultiplier = 2.50f;
            speedMultiplier = 0.40f;
        }}; 

        turteIII = new StatusEffect("turteIII"){{
            color = Pal.heal;
            healthMultiplier = 5f;
            speedMultiplier = 0.20f;
        }};     
    }
}        