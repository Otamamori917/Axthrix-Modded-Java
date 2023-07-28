package axthrix.content;

import arc.graphics.Color;
import axthrix.world.types.AxFaction;


public class AxFactions {
    public static AxFaction
        axthrix,raodon,ikatusa
            ;
    public static void load()
    {
        axthrix = new AxFaction("axthrix", Color.valueOf("#57d87e")) {{
            description = "A faction with an incredible Technological Advantage";
        }};
        raodon = new AxFaction("raodon", Color.valueOf("#57d87e")) {{
            description = "A faction with an incredible Technological Advantage";
        }};
        ikatusa = new AxFaction("ikatusa", Color.valueOf("#57d87e")) {{
            description = "A faction with an incredible Technological Advantage";
        }};
    }
}
