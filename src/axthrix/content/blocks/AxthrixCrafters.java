package axthrix.content.blocks;

import arc.graphics.Color;
import arc.struct.Seq;
import axthrix.content.AxFactions;
import axthrix.content.AxItems;
import axthrix.content.AxLiquids;
import axthrix.content.units.AxthrixUnits;
import axthrix.world.types.block.LiquidDeposit;
import axthrix.world.types.block.effect.GeneratorCoreBlock;
import axthrix.world.types.block.effect.NanobotProjector;
import axthrix.world.types.block.production.*;
import axthrix.world.util.AxRecipe;
import axthrix.world.util.AxRecipeSelector;
import mindustry.content.*;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.Block;
import mindustry.world.draw.*;
import multicraft.*;

import static axthrix.content.blocks.PayloadAmmoBlocks.*;
import static mindustry.type.ItemStack.with;

public class AxthrixCrafters {
	public static Block
            heatGenerator,cryoForge,
			nanobotProjector,nanobotDome,nanobotRealm,
            lithicCore, cratonCore, deltaicCore, batholithCore,

    //multicrafters
    centrifugalAccelerator,componentPrinter,pCoilPress,

	//crafters
	coreCrafter,broiler,

	//seperators
	chemicalSeparator,liquidator,

	//payload ammo crafters

	caliberPress, caliberCrafter,

	liquidDeposit;



    public static void load() {
		// Power generator
		heatGenerator = new TemperaturePowerGenerator("heat-generator"){{
			size = 2;
			minTemperature = 50f;
			maxTemperature = 100f;
			powerProduction = 15f;
			requiresHeat = true;
			effectResistanceHeat = 0.2f;
			accumulationResistanceCold = 0.4f;
			requirements(Category.power, with(
					Items.copper, 75,
					Items.lead, 100));
		}};

        // Crafter
		cryoForge = new TemperatureCrafter("cryo-forge"){{
			size = 3;
			minTemperature = 40f;
			maxTemperature = 100f;
			requiresHeat = false; // Needs cold
			effectResistanceCold = 0;
			accumulationResistanceHeat = 0.4f;

			consumeItems(ItemStack.with(AxItems.tungstenCoil, 8, AxItems.iodine, 4, AxItems.silverCoil,10));
			outputItem = new ItemStack(AxItems.coolingAssembly, 2);
			craftTime = 120f;
			requirements(Category.crafting, with(
					Items.copper, 75,
					Items.lead, 100));

		}};
		lithicCore = new GeneratorCoreBlock("lithic-core"){{
			size = 3;
			health = 3500;

			powerProduction = 12f / 60f;
			costRate = 235f;


			addBoost(AxItems.sulfur, 16f);
			faction = Seq.with(AxFactions.axthrix);

			unitType = AxthrixUnits.shale;
			itemCapacity = 10000;
			requirements(Category.effect, with(
					Items.copper, 75,
					Items.lead, 100));

		}};
		nanobotProjector = new NanobotProjector("nanobot-projector"){{
			size = 2;
			health = 280;
			range = 10*8f;
			damage = 5;
			buildingDamageMultiplier = 0.10f;
			baseTickRate = 30f;
			itemTickRate = 60f;
			liquidBoost = 3f;
			healAmount = 2.5f;
			healPercent = 0.09f;
			bulletSpeedBonus = 1.02f;
			bulletSlowdown = 0.995f;
			statusDuration = baseTickRate;
			efficiencyBoost = 1.25f;
			nanobotSize = 0.4f;
			nanobotSpeed = 2.5f;
			nanobotCount = 12;
			stackPenalty = 0.8f;

			faction = Seq.with(AxFactions.axthrix);

			requirements(Category.effect, with(
					Items.copper, 75,
					Items.lead, 100));

			consumePower(1.5f);
			consumeItem(AxItems.sulfur, 1);
			consumeLiquid(AxLiquids.mercury, 0.2f).boost();
		}};
		nanobotDome = new NanobotProjector("nanobot-dome"){{
			size = 3;
			health = 380;
			range = 18*8f;
			damage = 8;
			buildingDamageMultiplier = 0.10f;
			baseTickRate = 25f;
			itemTickRate = 40f;
			liquidBoost = 2.5f;
			healAmount = 4f;
			healPercent = 0.1f;
			bulletSpeedBonus = 1.03f;
			bulletSlowdown = 0.992f;
			statusDuration = baseTickRate;
			efficiencyBoost = 1.45f;
			nanobotSize = 0.6f;
			nanobotSpeed = 3f;
			nanobotCount = 18;
			stackPenalty = 0.8f;

			faction = Seq.with(AxFactions.axthrix);

			requirements(Category.effect, with(
					Items.copper, 75,
					Items.lead, 100));

			consumePower(4.25f);
			consumeItem(AxItems.sulfur, 1);
			consumeLiquid(AxLiquids.mercury, 0.4f).boost();
		}};
		nanobotRealm = new NanobotProjector("nanobot-realm"){{
			size = 5;
			health = 890;
			range = 45*8f;
			damage = 18;
			buildingDamageMultiplier = 0.05f;
			baseTickRate = 20f;
			itemTickRate = 40f;
			liquidBoost = 2f;
			healAmount = 3f;
			healPercent = 0.1f;
			bulletSpeedBonus = 1.05f;
			bulletSlowdown = 0.990f;
			statusDuration = baseTickRate;
			efficiencyBoost = 1.8f;
			nanobotSize = 0.9f;
			nanobotSpeed = 2f;
			nanobotCount = 28;
			stackPenalty = 0.8f;

			faction = Seq.with(AxFactions.axthrix);

			requirements(Category.effect, with(
					Items.copper, 75,
					Items.lead, 100));

			consumePower(8f);
			consumeItem(AxItems.sulfur, 2);
			consumeLiquid(AxLiquids.mercury, 0.8f).boost();
		}};
		centrifugalAccelerator = new AxMulticrafter("centrifugal-accelerator")
		{{
			faction = Seq.with(AxFactions.axthrix);
			requirements(Category.crafting, with(Items.copper,1));
			size = 4;
			selector = AxRecipeSelector.Simple;
			resolvedRecipes = Seq.with(
					new AxRecipe(
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
			new AxRecipe(
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

		componentPrinter = new AxMulticrafter("component-printer")
		{{
			faction = Seq.with(AxFactions.axthrix);
			requirements(Category.crafting, with(Items.copper,1));
			selector = AxRecipeSelector.Simple;
			size = 2;
			resolvedRecipes = Seq.with(
					new AxRecipe(
							//IOEntry input
							new IOEntry(
									//item input
									Seq.with(ItemStack.with(AxItems.carbonCoil, 8, AxItems.silverCoil, 1)),
									//Liquid input
									Seq.with()
							),
							//IOEntry output
							new IOEntry(
									Seq.with(ItemStack.with(AxItems.CSW, 2)),
									//output fluids, again, it can be empty
									Seq.with()
							),
							//float craftTime in ticks
							120f
				    )//,
//
//					new AxRecipe(
//							//IOEntry input
//							new IOEntry(
//									//item input
//									Seq.with(ItemStack.with(AxItems.tungstenCoil, 2, AxItems.copperCoil, 4, AxItems.silverCoil,1)),
//									//Liquid input
//									Seq.with(LiquidStack.with(Liquids.cryofluid, 0.265f))
//							),
//							//IOEntry output
//							new IOEntry(
//									Seq.with(ItemStack.with(AxItems.coolingAssembly, 1)),
//									//output fluids, again, it can be empty
//									Seq.with()
//							),
//							//float craftTime in ticks
//							260f
//					)
			);
			craftEffect = Fx.bubble;
		}};
		pCoilPress = new AxMulticrafter("p-coil-press")
		{{
			faction = Seq.with(AxFactions.axthrix);
			requirements(Category.crafting, with(Items.copper,1));
			selector = AxRecipeSelector.Simple;
			size = 1;
			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawRegion("-spinner", 1, true),
					new DrawDefault());
			resolvedRecipes = Seq.with(
					new AxRecipe(
							//IOEntry input
							new IOEntry(
									//item input
									Seq.with(ItemStack.with(AxItems.aqullium, 2)),
									//Liquid input
									Seq.with()
							),
							//IOEntry output
							new IOEntry(
									Seq.with(ItemStack.with(AxItems.carbonCoil, 2, AxItems.tungstenCoil, 1, AxItems.ingot, 1)),
									//output fluids, again, it can be empty
									Seq.with()
							),
							//float craftTime in ticks
							20f
					),
					new AxRecipe(
							//IOEntry input
							new IOEntry(
									//item input
									Seq.with(ItemStack.with(AxItems.esperillo, 2)),
									//Liquid input
									Seq.with()
							),
							//IOEntry output
							new IOEntry(
									Seq.with(ItemStack.with(AxItems.copperCoil, 2, AxItems.ingot, 4)),
									//output fluids, again, it can be empty
									Seq.with()
							),
							//float craftTime in ticks
							20f
					),
					new AxRecipe(
							//IOEntry input
							new IOEntry(
									//item input
									Seq.with(ItemStack.with(AxItems.komainium, 2)),
									//Liquid input
									Seq.with()
							),
							//IOEntry output
							new IOEntry(
									Seq.with(ItemStack.with(AxItems.leadCoil, 2, AxItems.silverCoil, 1, AxItems.ingot, 1)),
									//output fluids, again, it can be empty
									Seq.with()
							),
							//float craftTime in ticks
							20f
					)
			);
			craftEffect = Fx.bubble;
		}};

		coreCrafter  = new AxGenericCrafter("core-crafter"){{
			faction = Seq.with(AxFactions.axthrix);
			hasItems = hasLiquids = hasPower = true;

			size = 3;
			craftTime = 1200f;
			itemCapacity = 20;
			buildCostMultiplier = 0.5f;
			craftEffect = Fx.pulverizeMedium;

			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawLiquidTile(AxLiquids.xenon),
					new DrawLiquidTile(AxLiquids.mercury, 10f),
					new DrawDefault()
			);
			consumePower(800f/60f);
			outputItem = new ItemStack(AxItems.RDC, 1);
			researchCost = with(Items.copper, 1);
			requirements(Category.crafting, with(Items.copper, 1));
			consumeLiquids(LiquidStack.with(AxLiquids.xenon, 0.2f, AxLiquids.iodineGas, 0.15));
			consumeItems(ItemStack.with(AxItems.CSW, 2, AxItems.coolingAssembly, 1,AxItems.dysprosium,4));
		}};

		chemicalSeparator = new AxSeparator("chemical-separator"){{
			faction = Seq.with(AxFactions.axthrix);

			requirements(Category.crafting, with(Items.copper, 1));
			results = with(
					AxItems.sulfur, 8,
					AxItems.iodine, 5,
					AxItems.samarium, 2,
					AxItems.curium, 2,
					AxItems.dysprosium, 1
			);

			hasPower = true;
			craftTime = 150f;
			size = 3;
			itemCapacity = 40;

			consumePower(8f);
			consumeItem(AxItems.ingot, 4);

			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawLiquidTile(),
					new DrawRegion("-spinner", 3, true),
					new DrawDefault());
		}};

		liquidator = new AxGenericCrafter("liquidator"){{
			faction = Seq.with(AxFactions.axthrix);
			requirements(Category.crafting, with(Items.copper, 1));
			size = 3;


            researchCostMultiplier = 1.2f;
			craftTime = 10f;
			rotate = true;
			invertFlip = true;
			itemCapacity = 16;

			liquidCapacity = 50f;

			consumeItem(AxItems.ingot,3);
			consumePower(2f);

			drawer = new DrawMulti(
					new DrawRegion("-bottom"),
					new DrawBubbles(AxLiquids.xenon.color.cpy().add(AxLiquids.mercury.color)){{
						sides = 10;
						recurrence = 3f;
						spread = 6;
						radius = 1.5f;
						amount = 20;
					}},
					new DrawRegion(),
					new DrawGlowRegion(){{
						alpha = 0.7f;
						color = Color.lightGray;
						glowIntensity = 0.3f;
						glowScale = 6f;
					}}
			);

			ambientSound = Sounds.loopCircuit;
			ambientSoundVolume = 0.08f;

			regionRotated1 = 3;
			outputLiquids = LiquidStack.with(AxLiquids.xenon, 16f / 60, AxLiquids.mercury, 15f / 60);
			liquidOutputDirections = new int[]{1, 3};
		}};
		broiler = new AxGenericCrafter("broiler"){{
			hasLiquids = hasPower =  outputsLiquid =  consumesPower = true;

			size = 2;
			health = 100;
			craftTime = 4f;

			consumePower(10f);
			consumeItem(AxItems.iodine, 2);
			outputLiquid = new LiquidStack(AxLiquids.iodineGas, 12.5f/60f);
			drawer = new DrawMulti(new DrawDefault(), new DrawLiquidRegion());
			requirements(Category.liquid, with(Items.copper,1));
		}};

		caliberPress = new PayloadProducer("caliber-press"){{
			requirements(Category.crafting, with(
					Items.copper, 75,
					Items.lead, 100,
					Items.titanium, 100,
					Items.silicon, 80
			));

			size = 3;
			ambientSound = Sounds.loopMachine;
			recipes(
					empty1mCaliber
			);
			recipes.each(r -> r.centerBuild = true);
			setRecipeProductionStats();
		}};

		caliberCrafter = new PayloadProducer("caliber-crafter"){{
			requirements(Category.crafting, with(
					Items.copper, 350,
					Items.lead, 250,
					Items.silicon, 220,
					Items.plastanium, 160,
					Items.thorium, 110
			));

			size = 3;
			hideDetails = false;
			ambientSound = Sounds.loopMachine;
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
		liquidDeposit = new LiquidDeposit("liquid-deposit") {{
			liquidCapacity = 200;
			size = 2;
			requirements(Category.liquid, with(
					Items.copper, 75,
					Items.lead, 100,
					Items.titanium, 100,
					Items.silicon, 80
			));
		}};
    }
}

