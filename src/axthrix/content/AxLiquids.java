package axthrix.content;

import arc.graphics.Color;
import mindustry.type.Liquid;

public class AxLiquids {
    public static Liquid
    nothingness
    ;
    public static void load()
    {
        nothingness = new Liquid("nothing") {{
            hidden = true;
            color = Color.black;
        }};
    }
}
