package axthrix.content;

import arc.graphics.Color;
import axthrix.world.types.statuseffects.AxStatusEffect;
import mindustry.type.Liquid;

public class AxLiquids {
    public static Liquid
    nothingness,

    xenon,mercury,iodineGas
    ;
    public static void load()
    {
        xenon = new Liquid("xenon") {{
            gas = true;
            color = gasColor = Color.valueOf("972020");
            animationScaleGas = 4f;
            heatCapacity = 1.15f;
            effect = AxthrixStatus.ReapAndSow;
        }};
        iodineGas = new Liquid("iodine-gas") {{
            gas = true;
            color = gasColor = Color.valueOf("a288ac");
            animationScaleGas = 1f;
            temperature = 2.1f;
            heatCapacity = 1.35f;
            effect = AxthrixStatus.unrepair;
        }};
        mercury = new Liquid("mercury") {{
            color = Color.valueOf("b8bbd2");
            heatCapacity = 1.05f;
            viscosity = 0.85f;
            effect = AxthrixStatus.slivered;
        }};
    }
}
