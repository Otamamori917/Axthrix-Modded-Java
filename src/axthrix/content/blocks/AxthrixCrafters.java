package axthrix.content.blocks; 

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.*;
import mindustry.entities.effect.*;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.draw.*;
import mindustry.world.meta.Attribute;
import multicraft.*;

import static mindustry.Vars.tilesize;
import static mindustry.type.ItemStack.with;

public class UAWBlocksProduction {
	public static Block placeholder,

    //multicrafters
    centrifugal-accelerator;

    public static void load() {

        centrifugal-accelerator = new MultiCrafter("centrifugal-accelerator") {{
			requirements(Category.crafting, with(
				Items.lead, 45,
				Items.graphite, 30,
				Items.thorium, 20
			));
			size = 2;

			hasItems = true;
			hasLiquids = true;
			updateEffectChance = 0.02f;

			menu = detailed;

			resolvedRecipes = Seq.with(
				// Fluid to Gel
				new Recipe() {{
					input = new IOEntry(
						Seq.with(ItemStack.with(Items.sand, 2)),
						Seq.with(LiquidStack.with(Liquids.cryofluid, 0.25f)),
						0.45f
					);
					output = new IOEntry(
						Seq.with(ItemStack.with(Items.titanium, 1)),
						Seq.with()
					);
					craftTime = 45f;
				}},
				// Gel to fluid
				new Recipe() {{
					input = new IOEntry(
						Seq.with(ItemStack.with(Items.titanium, 1)),
						Seq.with(),
						0.45f
					);
					output = new IOEntry(
						Seq.with(),
						Seq.with(LiquidStack.with(Liquids.cryofluid, 0.25f))
					);
					craftTime = 45f;
				}}
			);


			drawer = new DrawMulti(
				new DrawRegion("-bottom"),
				new DrawLiquidTile() {{
					padding = 8 * px;
				}},
				new DrawBubbles() {{
					color = UAWPal.cryoFront;
				}},
				new DrawDefault()
			);
		}};
        

    }
}        


