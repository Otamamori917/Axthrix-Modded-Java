package axthrix.content.blocks;

import axthrix.world.types.block.production.MultiCrafter;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class AxthrixCrafters {
	public static Block placeholder,

    //multicrafters
    centrifugalAccelerator;

    public static void load() {
		centrifugalAccelerator = new MultiCrafter("centrifugal-accelerator")
		{{
			requirements(Category.crafting, with(Items.copper,1));
			localizedName = "Centrifugal Accelerator";
			size = 4;
			newConsumer(Liquids.nitrogen);
			outputLiquid(Liquids.nitrogen,10f/60f);
			consumeLiquid(Liquids.water,20f/60f);
			newConsumer(Liquids.slag);
			consumeLiquid(Liquids.water,10f/60f);
			consumeLiquid(Liquids.nitrogen,10f/60f);
			outputLiquid(Liquids.slag,10f/60f);
			craftEffect = Fx.bubble;
		}};

    }
}

