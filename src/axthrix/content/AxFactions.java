package axthrix.content;

import arc.graphics.Color;
import axthrix.world.types.AxFaction;


public class AxFactions {
    public static AxFaction
        axthrix,raodon,ikatusa
            ;
    public static void load()
    {
        axthrix = new AxFaction("axthrix", Color.valueOf("#de9458")) {{
            description = "[#de9458]A faction with an incredible Technological Advantage";
        }};
        raodon = new AxFaction("raodon", Color.valueOf("#202020")) {{
            description = "[#595959]A faction with an incredible Military Advantage";
        }};
        ikatusa = new AxFaction("ikatusa", Color.valueOf("#481257")) {{
            description = "[#481257]A group of biomechanical organisms";
        }};
    }
}
