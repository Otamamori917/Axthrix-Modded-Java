package ajmain.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static mindustry.Vars.*;

public class StatusEffects{
    public static StatusEffect turteI, turteII, turteIII;

    public static void load(){


        turteI = new StatusEffect("TurteI"){{
            color = Pal.heal;
            healthMultiplier = 1.25f;
            speedMultiplier = 0.80f;
        }};

        turteII = new StatusEffect("TurteII"){{
            color = Pal.heal;
            healthMultiplier = 2.50f;
            speedMultiplier = 0.40f;
        }}; 

        turteIII = new StatusEffect("TurteIII"){{
            color = Pal.heal;
            healthMultiplier = 5f;
            speedMultiplier = 0.20f;
        }};     
    }
}        