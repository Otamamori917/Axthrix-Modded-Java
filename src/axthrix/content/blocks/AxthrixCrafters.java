package axthrix.content.blocks;

import arc.struct.Seq;
import axthrix.content.AxFactions;
import axthrix.world.types.block.production.AxMulticrafter;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.Block;
import multicraft.*;

import static mindustry.type.ItemStack.with;

public class AxthrixCrafters {
	public static Block placeholder,

    //multicrafters
    centrifugalAccelerator;

    public static void load() {
		centrifugalAccelerator = new AxMulticrafter("centrifugal-accelerator")
		{{
			faction = Seq.with(AxFactions.ikatusa).with(AxFactions.axthrix);
			requirements(Category.crafting, with(Items.copper,1));
			localizedName = "Centrifugal Accelerator";
			size = 4;
			resolvedRecipes = Seq.with(
					new Recipe(
							//IOEntry input
							new IOEntry(
									//item input
									Seq.with(),
									//Liquid input
									Seq.with(LiquidStack.with(Liquids.water, 0.33f))
							),
							//IOEntry output
							new IOEntry(
									Seq.with(),
									//output fluids, again, it can be empty
									Seq.with(LiquidStack.with(Liquids.nitrogen, 0.165))
							),
							//float craftTime in ticks
							60f
					),
			new Recipe(
					//IOEntry input
					new IOEntry(
							//item input
							Seq.with(),
							//Liquid input
							Seq.with(LiquidStack.with(Liquids.nitrogen, 0.165f,  Liquids.water, 0.165f))
					),
					//IOEntry output
					new IOEntry(
							Seq.with(),
							//output fluids, again, it can be empty
							Seq.with(LiquidStack.with(Liquids.slag, 0.165))
					),
					//float craftTime in ticks
					60f
			)
					);
			craftEffect = Fx.bubble;
		}};

    }
}

