package axthrix.content.blocks;

import arc.struct.Seq;
import axthrix.content.AxFactions;
import axthrix.world.types.block.production.AxMulticrafter;
import axthrix.world.types.block.production.PayloadProducer;
import mindustry.content.*;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.Block;
import multicraft.*;

import static axthrix.content.blocks.PayloadAmmoBlocks.*;
import static mindustry.type.ItemStack.with;

public class AxthrixCrafters {
	public static Block placeholder,

    //multicrafters
    centrifugalAccelerator,

	//payload ammo crafters

	caliberPress, caliberCrafter;

    public static void load() {
		centrifugalAccelerator = new AxMulticrafter("centrifugal-accelerator")
		{{
			faction = Seq.with(AxFactions.axthrix);
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

		caliberPress = new PayloadProducer("caliber-press"){{
			localizedName = "Caliber Shell Press";
			description = """
                          Presses materials into ammunition shells.
                          """;
			requirements(Category.crafting, with(
					Items.copper, 75,
					Items.lead, 100,
					Items.titanium, 100,
					Items.silicon, 80
			));

			size = 3;
			ambientSound = Sounds.machine;
			recipes(
					empty1mCaliber
			);
			recipes.each(r -> r.centerBuild = true);
			setRecipeProductionStats();
		}};

		caliberCrafter = new PayloadProducer("caliber-crafter"){{
			localizedName = "Caliber Crafter";
			description = """
                          Takes Caliber Shells and materials to make live Payload ammunition.
                          """;
			requirements(Category.crafting, with(
					Items.copper, 350,
					Items.lead, 250,
					Items.silicon, 220,
					Items.plastanium, 160,
					Items.thorium, 110
			));

			size = 3;
			hideDetails = false;
			ambientSound = Sounds.machine;
			liquidCapacity = 80f;
			recipes(
					basic1mCaliber,
					arcane1mCaliber,
					//solids
					frostbite1mCaliber,
					incendiary1mCaliber,
					quicksilver1mCaliber,
					//energy
					void1mCaliber,
					sonicwave1mCaliber,
					tempest1mCaliber
			);
			setRecipeProductionStats();
		}};
    }
}

