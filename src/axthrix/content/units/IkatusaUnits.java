package axthrix.content.units;

import arc.graphics.Color;
import axthrix.content.AxItems;
import axthrix.content.FX.AxthrixFfx;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.block.Egg;
import axthrix.world.types.block.defense.JormungandrNest;
import axthrix.world.types.bulletypes.GrabBulletType;
import axthrix.world.types.entities.comp.LeggedWaterEntity;
import axthrix.world.types.unittypes.IkatusaUnitType;
import axthrix.world.types.unittypes.JormungandrUnitType;
import axthrix.world.types.unittypes.LeggedWaterUnit;
import mindustry.content.Blocks;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.ExplosionBulletType;
import mindustry.gen.ElevationMoveUnit;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class IkatusaUnits {
    public static UnitType
            // Jelly - Iyiminin
            YoungJelly, JuvenileJelly, AdultJelly, ElderJelly,

    // Stopper - Mlombala
    YoungStopper, JuvenileStopper, AdultStopper,

    // Crocit - Rakdos
    YoungCrocit, FemaleAdultCrocit, MaleAdultCrocit,

    // Snake - Gandr
    // Vorryn (Hatchling) → Vorryn-Kath (Juvenile) → Keth-Vorryn (Adult) → Jormungandr (Elder)
    GandrHatchling, GandrJuvenile, GandrAdult, GandrElder;

    public static Block
            // Jelly
            SpawnJelly,
    // Stopper
    EggStopper,
    // Crocit
    EggCrocit,
    // Snake
    GandrEgg,
            GandrNestYoung,
            GandrNestJuvenile,
            GandrNestAdult,
            GandrNestElder;

    public static void load() {

        ElderJelly = new IkatusaUnitType("iyiminin-elder") {{
            localizedName = "Elder Iyiminin";
            description = ""; constructor = ElevationMoveUnit::create;
            finalStage = true; growthTime = 3280; maturityTime = 1;
            cooldown = 1200; asexual = true; spawnAmount = 2; spawnAmountRand = 1;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{ killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80,2,Layer.blockOver,Color.valueOf("481257")); }};
            }});
        }};

        AdultJelly = new IkatusaUnitType("iyiminin-adult") {{
            localizedName = "Iyiminin"; description = ""; constructor = ElevationMoveUnit::create;
            nextStage = ElderJelly; growthTime = 2880; maturityTime = 1; cooldown = 800;
            asexual = true; spawnAmount = 3; spawnAmountRand = 3;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{ killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80,2,Layer.blockOver,Color.valueOf("481257")); }};
            }});
        }};

        JuvenileJelly = new IkatusaUnitType("iyiminin-juvenile") {{
            localizedName = "Juvenile Iyiminin"; description = "";
            nextStage = AdultJelly; growthTime = maturityTime = 860;
            constructor = ElevationMoveUnit::create;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{ killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80,2,Layer.blockOver,Color.valueOf("481257")); }};
            }});
        }};

        YoungJelly = new IkatusaUnitType("iyiminin-young") {{
            localizedName = "Young Iyiminin"; description = "";
            nextStage = JuvenileJelly; growthTime = maturityTime = 650;
            constructor = ElevationMoveUnit::create;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{ killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80,2,Layer.blockOver,Color.valueOf("481257")); }};
            }});
        }};

        SpawnJelly = new Egg("iyiminin-egg") {{
            size = 1; spawnAmount = 2; spawnAmountRand = 6; growthTime = 440;
            nextStage = YoungJelly;
            ((IkatusaUnitType)ElderJelly).egg = this;
            ((IkatusaUnitType)AdultJelly).egg = this;
            attributes.addAll(Blocks.water,Blocks.deepwater,Blocks.deepTaintedWater,
                    Blocks.sandWater,Blocks.darksandWater,Blocks.darksandTaintedWater);
            requirements(Category.units, with(AxItems.fossilizedIkatusa, 10));
        }};

        MaleAdultCrocit = new LeggedWaterUnit("rakdosm") {{
            localizedName = "Male Rakdos"; description = "";
            constructor = LeggedWaterEntity::create;
            legCount = 4; boostUsesNaval = naval = true;
            showLegsOnDeepLiquid = showLegsOnLiquid = false;
            finalStage = true; growthTime = 12500; ismale = true;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;

            preyTypes.add(
                    YoungJelly,
                    JuvenileJelly,
                    AdultJelly,
                    ElderJelly
            );
            weapons.add(new Weapon(){{
                mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new GrabBulletType(){{
                    holdDistance = 8.5f;
                    grabDuration = 205f; damage = 10f; maxSizeRatio = 1.5f;
                    canGrabFlying = false; failEffect = Fx.smoke;
                    grabEffect = Fx.blastExplosion; grabColor = Color.valueOf("ff0000");
                }};
            }});
        }};

        FemaleAdultCrocit = new LeggedWaterUnit("rakdosf") {{
            localizedName = "Female Rakdos"; description = "";
            constructor = LeggedWaterEntity::create;
            legCount = 4; boostUsesNaval = naval = true;
            finalStage = true; growthTime = 12500;
            oppositeGender = MaleAdultCrocit;
            ((IkatusaUnitType)MaleAdultCrocit).oppositeGender = this;
            showLegsOnDeepLiquid = showLegsOnLiquid = false;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;
            preyTypes.add(
                    YoungJelly,
                    JuvenileJelly,
                    AdultJelly
            );
            weapons.add(new Weapon(){{
                mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new GrabBulletType(){{
                    holdDistance = 8;
                    grabDuration = 200f; damage = 10f; maxSizeRatio = 1.5f;
                    canGrabFlying = false; failEffect = Fx.smoke;
                    grabEffect = Fx.blastExplosion; grabColor = Color.valueOf("ff0000");
                }};
            }});
        }};

        YoungCrocit = new LeggedWaterUnit("rakdos-young") {{
            localizedName = "Young Rakdos"; description = "";
            constructor = LeggedWaterEntity::create;
            legCount = 4; boostUsesNaval = naval = true; canDrown = false;
            showLegsOnDeepLiquid = showLegsOnLiquid = false;
            maleStage = MaleAdultCrocit; femaleStage = FemaleAdultCrocit;
            growthTime = 6500;
            flying = false; speed = 8.3f/7.5f; drag = 0.13f;
            hitSize = 10f; health = 275; armor = 3;
            accel = 0.6f; rotateSpeed = 3.3f; faceTarget = true; hovering = true;
            preyTypes.add(
                    YoungJelly,
                    JuvenileJelly
            );
            weapons.add(new Weapon(){{
                mirror = false; x = 0; y = 0; reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving; shootStatusDuration = 15;
                bullet = new GrabBulletType(){{
                    holdDistance = 5;
                    grabDuration = 180f; damage = 10f; maxSizeRatio = 1.5f;
                    canGrabFlying = false; failEffect = Fx.smoke;
                    grabEffect = Fx.blastExplosion; grabColor = Color.valueOf("ff0000");
                }};
            }});
        }};

        EggCrocit = new Egg("rakdos-egg") {{
            size = 1; spawnAmount = 3; spawnAmountRand = 2; growthTime = 1840;
            nextStage = YoungCrocit;
            ((IkatusaUnitType)FemaleAdultCrocit).egg = this;
            attributes.add(AxthrixEnvironment.crimsonSandDeepFloor);
            requirements(Category.units, with(AxItems.fossilizedIkatusa, 10, Items.sand, 10));
        }};

        GandrElder = new JormungandrUnitType("gandr-elder") {{
            localizedName = "Jormungandr";
            description = "An ancient serpent of immense size. Rarely seen, never forgotten.";
            constructor = ElevationMoveUnit::create;
            finalStage = true;
            canNest = true;
            isHatchling = false;
            stageTime = 14400f;
            resourceProgressValue = 30f;
            nestSeekRange = 280f;
            eggBatchCount = 3;
            eggSpread = 50f;
            bodySegments = 28; segmentSpacing = 10f;
            lungeRange = 120f;
            flying = false; speed = 0.7f; drag = 0.13f;
            hitSize = 22f; health = 1200; armor = 12;
            accel = 0.4f; rotateSpeed = 1.5f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 45f;
                bullet = new ExplosionBulletType(120,100){{ killShooter = false;
                    status = StatusEffects.corroded; statusDuration = 240f; }};
            }});
        }};

        // ---- Adult (Keth-Vorryn) ----

        GandrAdult = new JormungandrUnitType("gandr-adult") {{
            localizedName = "Keth-Vorryn";
            description = "A mature Vorryn serpent. Highly venomous — armour offers little protection.";
            constructor = ElevationMoveUnit::create;
            nextStage = (JormungandrUnitType) GandrElder;
            chanceToAge = 0.15f;
            canNest = true;
            isHatchling = false;
            stageTime = 10800f;
            resourceProgressValue = 60f;
            eggBatchCount = 2;
            eggSpread = 40f;
            bodySegments = 8; segmentSpacing = 7f;
            lungeRange = 95f;
            flying = false; speed = 0.9f; drag = 0.13f;
            hitSize = 14f; health = 500; armor = 7;
            accel = 0.5f; rotateSpeed = 2.0f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 55f;
                bullet = new ExplosionBulletType(70,75){{ killShooter = false;
                    status = StatusEffects.corroded; statusDuration = 120f; }};
            }});
        }};

        // ---- Juvenile (Vorryn-Kath) ----

        GandrJuvenile = new JormungandrUnitType("gandr-juvenile") {{
            localizedName = "Vorryn-Kath";
            description = "A growing Vorryn serpent. Beginning to show its venom.";
            constructor = ElevationMoveUnit::create;
            nextStage = (JormungandrUnitType) GandrAdult;
            canNest = true;
            isHatchling = false;
            stageTime = 7200f;
            resourceProgressValue = 50f;
            bodySegments = 5; segmentSpacing = 6f;
            lungeRange = 75f;
            flying = false; speed = 1.1f; drag = 0.13f;
            hitSize = 9f; health = 260; armor = 3;
            accel = 0.58f; rotateSpeed = 2.6f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 63f;
                bullet = new ExplosionBulletType(40,50){{ killShooter = false;
                    status = StatusEffects.corroded; statusDuration = 45f; }};
            }});
        }};

        // ---- Hatchling (Vorryn) ----

        GandrHatchling = new JormungandrUnitType("gandr-hatchling") {{
            localizedName = "Vorryn";
            description = "A freshly hatched Vorryn serpent. Small, skittish, but already venomous.";
            constructor = ElevationMoveUnit::create;
            nextStage = (JormungandrUnitType) GandrJuvenile;
            canNest = true;
            isHatchling = true;
            stageTime = 3600f;
            resourceProgressValue = 40f;
            bodySegments = 3; segmentSpacing = 5f;
            lungeRange = 55f;
            minScale = 0.2f;
            flying = false; speed = 1.25f; drag = 0.13f;
            hitSize = 5f; health = 130; armor = 0;
            accel = 0.64f; rotateSpeed = 3.2f; faceTarget = true; hovering = true;
            weapons.add(new Weapon(){{ mirror = false; x = 0; y = 0; reload = 75f;
                bullet = new ExplosionBulletType(15,25){{ killShooter = false;
                    status = StatusEffects.corroded; statusDuration = 15f; }};
            }});
        }};
        GandrNestYoung = new JormungandrNest.JormungandrNestYoung("gandr-nest-young");
        ((JormungandrUnitType) GandrHatchling).nestBlock = GandrNestYoung;

        GandrNestJuvenile = new JormungandrNest.JormungandrNestJuvenile("gandr-nest-juvenile");
        ((JormungandrUnitType) GandrJuvenile).nestBlock = GandrNestJuvenile;

        GandrNestAdult = new JormungandrNest.JormungandrNestAdult("gandr-nest-adult");
        ((JormungandrUnitType) GandrAdult).nestBlock = GandrNestAdult;

        GandrNestElder = new JormungandrNest.JormungandrNestElder("gandr-nest-elder");
        ((JormungandrUnitType) GandrElder).nestBlock = GandrNestElder;

        GandrEgg = new Egg("gandr-egg") {{
            size           = 1;
            spawnAmount    = 6;
            spawnAmountRand = 0;
            growthTime     = 720f;
            badEggChance   = 0.30f;
            nextStage      = GandrHatchling;
            attributes.addAll(Blocks.stone, Blocks.metalFloor);
        }};

        ((JormungandrUnitType) GandrAdult).eggBlock  = GandrEgg;
        ((JormungandrUnitType) GandrElder).eggBlock  = GandrEgg;
    }
}