package axthrix.content;

import arc.graphics.Color;
import mindustry.content.Items;
import mindustry.type.Item;
import mindustry.type.Liquid;


public class AxResources {
    public static Item
            //components
    smallRDC,CSW,coolingAssembly,
            //fusion materials
    aqullium,esperillo,komainium,narcor,redNarcor,gellium,
            //alloys
    aquillo,steel,mesmeril,fermendalite,simulta,
            // other materials
    obsidian,diamond,sapphire,ceramic,
            //printing coils
    carbonCoil, copperCoil, tungstenCoil, silverCoil, leadCoil,
            //misc
    ingot,fossilizedIkatusa,
           //waste material
    stone,ice,dirt
    ;
    public static Liquid
            nothingness;

    ///natural material (actual elements within the periodic table only)
    public static Liquid
    /// gases
            xenon,iodineGas,argon,
    /// liquids
            mercury;
    public static Item
    iron,sulfur,cobalt,calcium,

    curium,

    iodine,

    ytterbium,

    arsenic,

    bismuth,
    //radioactive
    astatine,

    praseodymium,
    //samarium will be used in  magnets (in addition to cobalt), nuclear reactors, and specialized electronics related uses
    samarium,
    // used in entities with great ranges as Dysprosium is used in ships’ sonar systems.
    dysprosium,


    //highly unstable must be used immediately after creation (possible decay mechanic?)

    // created by smashing calcium and curium together
    livermorium,

    oganesson;

    public static void Lload()
    {
        xenon = new Liquid("xenon") {{
            gas = true;
            color = gasColor = Color.valueOf("972020");
            animationScaleGas = 4f;
            heatCapacity = 1.15f;
            effect = AxthrixStatus.execute;
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
    public static void Iload() {
        //pratically useless
        stone = new Item("stone") {{
            color = Color.gray;
            hideDetails = false;
        }};
        //components
        smallRDC = new Item("RDC") {{
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

        iron = new Item("iron"){{
           hideDetails = false;
           color = Color.valueOf("bfa1a1");
        }};
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
