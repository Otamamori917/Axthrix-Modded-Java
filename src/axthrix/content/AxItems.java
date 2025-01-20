package axthrix.content;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.type.Item;


public class AxItems {
    public static Item
            //components
    RDC,CSW,coolingAssembly,
            //fusion materials
    aqullium,esperillo,komainium,
            //normal material
    sulfur,curium,iodine,samarium,dysprosium,
            //alloys
    aquillo,
            //printing coils
    carbonCoil, copperCoil, tungstenCoil, silverCoil, leadCoil,
            //misc
    ingot, fossilizedIkatusa
    ;
    public static void load() {
        //components
        RDC = new Item("RDC") {{
            color = Color.purple;
            hideDetails = false;
        }};
        CSW = new Item("CSW") {{
            color = Color.gray;
            hideDetails = false;
        }};
        coolingAssembly = new Item("cooling-assembly") {{
            color = Color.cyan;
            hideDetails = false;
        }};
        //fusion materials
        aqullium = new Item("aqullium") {{
            color = Color.valueOf("628a59");
            hideDetails = false;
        }};
        esperillo = new Item("esperillo") {{
            color = Color.valueOf("ac3232");
            hideDetails = false;
        }};
        komainium = new Item("komainium") {{
            color = Color.valueOf("9bb6ac");
            hideDetails = false;
        }};
        //normal material
        sulfur = new Item("sulfur") {{
            color = Color.valueOf("c1815b");
            hideDetails = false;
        }};
        curium = new Item("curium") {{
            color = Color.valueOf("e0d6d0");
            hideDetails = false;
        }};
        iodine = new Item("iodine") {{
            color = Color.valueOf("a288ac");
            hideDetails = false;
        }};
        samarium = new Item("samarium") {{
            color = Color.valueOf("fddc5c");
            hideDetails = false;
        }};
        dysprosium = new Item("dysprosium") {{
            color = Color.valueOf("687587");
            hideDetails = false;
        }};
        //alloys
        aquillo = new Item("aquillo") {{
            color = Color.valueOf("895f46");
            hideDetails = false;
        }};
        //printing coils
        carbonCoil = new Item("carbon-coil") {{
            color = Items.coal.color;
            hideDetails = false;
        }};
        copperCoil = new Item("copper-coil") {{
            color = Items.copper.color;
            hideDetails = false;
        }};
        tungstenCoil = new Item("tungsten-coil") {{
            color = Items.tungsten.color;
            hideDetails = false;
        }};
        silverCoil = new Item("silver-coil") {{
            color = Color.valueOf("bbb8b8");
            hideDetails = false;
        }};
        leadCoil = new Item("lead-coil") {{
            color = Items.lead.color;
            hideDetails = false;
        }};
        //misc
        fossilizedIkatusa = new Item("fossilized-ikatusa") {{

            color = Color.valueOf("43253c");
            hideDetails = false;
        }};
        ingot = new Item("ingot") {{
            color = Color.valueOf("49421f");
            hideDetails = false;
        }};
    }
}
