package axthrix.content.units;

import arc.graphics.Color;
import axthrix.content.AxItems;
import axthrix.content.FX.AxthrixFfx;
import axthrix.content.blocks.AxthrixCrafters;
import axthrix.content.blocks.AxthrixEnvironment;
import axthrix.world.types.block.Egg;
import axthrix.world.types.unittypes.IkatusaUnitType;
import axthrix.world.types.unittypes.LeggedWaterUnit;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.StatusEffects;
import mindustry.entities.bullet.ExplosionBulletType;
import mindustry.gen.ElevationMoveUnit;
import mindustry.gen.LegsUnit;
import mindustry.gen.UnitWaterMove;
import mindustry.graphics.Layer;
import mindustry.type.Category;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.world.Block;
import mindustry.world.meta.Attribute;

import java.awt.geom.Ellipse2D;

import static mindustry.type.ItemStack.with;

public class IkatusaUnits {
    public static UnitType
    //Ikatusa |undetermined| + 1 TX

        //Jelly - Iyiminin
        YoungJelly, JuvenileJelly, AdultJelly, ElderJelly,

        //Stopper - Mlombala Juvenile(male 40%/female 60%) Adult (male/female)
        YoungStopper, JuvenileStopper, AdultStopper,

        //Crocit - Rakdos Adult (male 45%/female 55%)
        YoungCrocit, FemaleAdultCrocit, MaleAdultCrocit;

    public static Block
            //Jelly - Iyiminin
            SpawnJelly,
            //Stopper - Mlombala
            EggStopper,
            //Crocit - Rakdos Adult (male 45%/female 55%)
            EggCrocit


            ;
    public static void load() {
        ElderJelly = new IkatusaUnitType("iyiminin-elder", true) {{
            localizedName = "Elder Iyiminin";
            description = """
                         
                          """;

            finalStage = true;
            growthTime = 3280;
            maturityTime = 1;
            cooldown = 1200;
            asexual = true;
            spawnAmount = 2;
            spawnAmountRand = 1;

            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        AdultJelly = new IkatusaUnitType("iyiminin-adult", true) {{
            localizedName = "Iyiminin";
            description = """
                         
                          """;

            nextStage = ElderJelly;
            growthTime = 2880;
            maturityTime = 1;
            cooldown = 800;
            asexual = true;
            spawnAmount = 3;
            spawnAmountRand = 3;

            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        JuvenileJelly = new IkatusaUnitType("iyiminin-juvenile", true) {{
            localizedName = "Juvenile Iyiminin";
            description = """
                         
                          """;


            nextStage = AdultJelly;
            growthTime = maturityTime = 860;


            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        YoungJelly = new IkatusaUnitType("iyiminin-young", true) {{
            localizedName = "Young Iyiminin";
            description = """
                         
                          """;

            nextStage = JuvenileJelly;
            growthTime = maturityTime = 650;

            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        SpawnJelly = new Egg("iyiminin-egg") {{
            size = 1;
            spawnAmount = 2;
            spawnAmountRand = 6;
            growthTime = 440;
            nextStage = YoungJelly;
            ((IkatusaUnitType)ElderJelly).egg = this;
            ((IkatusaUnitType)AdultJelly).egg = this;
            attributes.addAll(Blocks.water,Blocks.deepwater,Blocks.deepTaintedWater,Blocks.sandWater,Blocks.darksandWater,Blocks.darksandTaintedWater);
            requirements(Category.units, with(
                    AxItems.fossilizedIkatusa, 10
            ));
        }};

        MaleAdultCrocit = new LeggedWaterUnit("rakdosm") {{
            localizedName = "Rakdos";
            description = """
                         
                          """;
            constructor = UnitWaterMove::create;

            finalStage = true;
            growthTime = 12500;
            ismale = true;

            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                       x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        FemaleAdultCrocit = new LeggedWaterUnit("rakdos") {{
            localizedName = "Rakdos";
            description = """
                         
                          """;
            constructor = UnitWaterMove::create;

            finalStage = true;
            growthTime = 12500;
            oppositeGender = MaleAdultCrocit;
            ((IkatusaUnitType)MaleAdultCrocit).oppositeGender = this;

            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                       x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        YoungCrocit = new LeggedWaterUnit("rakdos-young") {{
            localizedName = "Young Rakdos";
            description = """
                         
                          """;
            constructor = UnitWaterMove::create;

            maleStage = MaleAdultCrocit;
            femaleStage = FemaleAdultCrocit;
            growthTime = 6500;

            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            /*parts.add(
                    new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 18f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 10;
                        hollow = true;
                        shapes = 2;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 2f;
                        haloRadius = 9f;
                        haloRotateSpeed = 4;
                        layer = Layer.effect;
                        y = 0;
                       x = 0;
                    }});*/

            weapons.add(new Weapon(){{
                mirror = false;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = 15;
                bullet = new ExplosionBulletType(100,80){{
                    killShooter = false;
                    shootEffect = AxthrixFfx.circleOut(10,80, 2,Layer.blockOver,Color.valueOf("481257"));
                }};
            }});
        }};

        EggCrocit = new Egg("rakdos-egg") {{
            size = 1;
            spawnAmount = 3;
            spawnAmountRand = 2;
            growthTime = 1840;
            nextStage = YoungCrocit;
            ((IkatusaUnitType)FemaleAdultCrocit).egg = this;
            attributes.add(AxthrixEnvironment.crimsonSandDeepFloor);
            requirements(Category.units, with(
                    AxItems.fossilizedIkatusa, 10, Items.sand, 10
            ));
        }};
    }
}
