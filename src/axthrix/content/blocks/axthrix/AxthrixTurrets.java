package axthrix.content.blocks.axthrix;

import axthrix.content.AxFactions;

import axthrix.content.AxthrixStatus;
import axthrix.world.types.bulletypes.*;
import axthrix.world.types.bulletypes.bulletpatterntypes.SpiralPattern;
import axthrix.world.types.block.defense.AcceleratedTurret;
import axthrix.world.types.block.defense.AxItemTurret;
import mindustry.content.*;
import mindustry.entities.UnitSorts;
import mindustry.gen.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.MissileUnitType;
import mindustry.world.*;
import mindustry.world.draw.*;

import static mindustry.type.ItemStack.*;

public class AxthrixTurrets{
    public static Block

    //Bendy miniguns

    kramola, razdor, smuta,

    //Rocket Artillery

    kisten,

    //shotguns

    fragmentation,

    //special

    nado, aratiri;

    public static void load(){
        kramola = new AcceleratedTurret("kramola"){{
            localizedName = "Kramola";
            description = """
                          Homing Minigun MK1
                          """;
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom variables
            acceleratedDelay = 60f;
            acceleratedBonus = 1.1f;
            acceleratedSteps = 10;
            burnoutDelay = 300f;
            cooldownDelay = 300f;
            cooldownTime = 120f;

            buildCostMultiplier = 0.1f;
            size = 2;
            scaledHealth = 420f;
            reload = 10f;
            range = 360f;
            maxAmmo = 200;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            faction.add(AxFactions.axthrix);
            coolant = consumeCoolant(0.2f);
            shoot = new SpiralPattern(1, 2);
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 15f;
                    homingPower = 4f;
                    homingRange = 200;
                    homingDelay = 40f;
                    width = 2f;
                    height = 4f;
                    hitSize = 2f;
                    lifetime = 100f;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.tungstenShot;
                    trailLength = 5;
                    trailWidth = 1f;
                }}
            );
            inaccuracy = 0f;
        }};




        razdor = new AcceleratedTurret("razdor"){{
            localizedName = "Kramola";
            description = """
                          Homing Minigun MK2
                          """;
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom variables
            acceleratedDelay = 60f;
            acceleratedBonus = 1.1f;
            acceleratedSteps = 10;
            burnoutDelay = 300f;
            cooldownDelay = 300f;
            cooldownTime = 120f;

            buildCostMultiplier = 0.1f;
            size = 3;
            scaledHealth = 420f;
            reload = 10f;
            range = 360f;
            maxAmmo = 300;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            faction.add(AxFactions.axthrix);
            coolant = consumeCoolant(0.2f);
            shoot = new SpiralPattern(2.5f, 1.5f){{
                shots = 3;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 30f;
                    homingPower = 4f;
                    homingRange = 200;
                    homingDelay = 40f;
                    width = 3f;
                    height = 6f;
                    hitSize = 3f;
                    lifetime = 100f;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.tungstenShot;
                    trailLength = 5;
                    trailWidth = 1f;
                }}
            );
            inaccuracy = 0f;
        }};


        smuta = new AcceleratedTurret("smuta"){{
            localizedName = "Kramola";
            description = """
                          Homing Minigun MK3
                          """;
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom variables
            acceleratedDelay = 60f;
            acceleratedBonus = 1.1f;
            acceleratedSteps = 10;
            burnoutDelay = 300f;
            cooldownDelay = 300f;
            cooldownTime = 120f;

            buildCostMultiplier = 0.1f;
            size = 4;
            scaledHealth = 420f;
            reload = 10f;
            range = 360f;
            maxAmmo = 400;
            ammoPerShot = 2;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            faction.add(AxFactions.axthrix);
            coolant = consumeCoolant(0.2f);
            shoot = new SpiralPattern(2.75f, 1.75f){{
                shots = 4;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 45f;
                    homingPower = 4f;
                    homingRange = 200;
                    homingDelay = 40f;
                    width = 4f;
                    height = 8f;
                    hitSize = 4f;
                    lifetime = 100f;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.tungstenShot;
                    trailLength = 5;
                    trailWidth = 1f;
                }}
            );
            inaccuracy = 0f;

        }};

        kisten = new AxItemTurret("kisten"){{
            localizedName = "Kisten";
            description = """
                          Biblically Accurate Rocket Launcher
                          """;
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));

            buildCostMultiplier = 0.1f;
            size = 4;
            scaledHealth = 320f;
            reload = 600f;
            range = 760f;
            maxAmmo = 20;
            ammoPerShot = 10;
            recoil = 3f;
            shootCone = 45;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.mediumCannon;
            minWarmup = 0.94f;
            shootWarmupSpeed = 0.05f;
            faction.add(AxFactions.axthrix);
            shoot = new SpiralPattern(4f, 2f){{
                shots = 10;
            }};
            ammo(
                Items.pyratite, new MissileBulletType(4f, 100){{
                    damage = 200f;
                    makeFire = true;
                    homingPower = 3f;
                    homingRange = 80;
                    homingDelay = 60f;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    trailEffect = Fx.burning;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.darkFlame;
                    status = StatusEffects.burning;
                    trailLength = 16;
                    trailWidth = 2f;
                    backColor = Pal.darkFlame;
                    frontColor = Pal.techBlue;
                }},

                Items.titanium, new MissileBulletType(4f, 100){{
                    damage = 250f;
                    homingPower = 3f;
                    homingRange = 80;
                    homingDelay = 60f;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    trailEffect = Fx.freezing;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.techBlue;
                    status = StatusEffects.freezing;
                    trailLength = 16;
                    trailWidth = 2f;
                    reloadMultiplier = 1.4f;
                    backColor = Pal.techBlue;
                    frontColor = Pal.techBlue;
                }},

                Items.surgeAlloy, new MissileBulletType(4f, 100){{
                    damage = 300f;
                    homingPower = 3f;
                    homingRange = 80;
                    homingDelay = 60f;
                    lightning = 2;
                    lightningLength = 15;
                    lightningLengthRand = 10;
                    lightningDamage = 50;
                    lightningColor = Pal.surge;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    trailEffect = Fx.lightning;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.surge;
                    status = StatusEffects.shocked;
                    trailLength = 16;
                    trailWidth = 2f;
                    backColor = Pal.surge;
                    frontColor = Pal.techBlue;
                }},

                Items.metaglass, new MissileBulletType(4f, 100){{
                    damage = 150f;
                    homingPower = 3f;
                    homingRange = 80;
                    homingDelay = 60f;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    knockback = 7.5f;
                    trailEffect = Fx.unitDust;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.tungstenShot;
                    trailLength = 16;
                    trailWidth = 2f;
                    reloadMultiplier = 1.8f;
                    backColor = Pal.tungstenShot;
                    frontColor = Pal.techBlue;
                }},

                Items.thorium, new MissileBulletType(4f, 100){{
                    damage = 600f;
                    homingPower = 0.01f;
                    homingRange = 20;
                    homingDelay = 120f;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    trailEffect = Fx.sapped;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.sap;
                    status = StatusEffects.sapped;
                    trailLength = 16;
                    trailWidth = 2f;
                    reloadMultiplier = 0.9f;
                    backColor = Pal.sap;
                    frontColor = Pal.techBlue;
                }}
            );
            inaccuracy = 0f;
            coolant = consumeCoolant(0.2f);
            recoilTime = 400f;

            drawer = new DrawTurret("crystalized-"){{
                parts.add(new RegionPart("-blade"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.techBlue;
                    moveRot = -18f;
                    moveX = 4f;
                    moveY = 5f;
                    mirror = true;
                    under = true;
                    heatLayerOffset = 1.2f;
                    layerOffset = 1;
                    outlineLayerOffset = 0.8f;

                    children.add(new RegionPart("-joint"){{
                        progress = PartProgress.warmup.delay(0.6f);
                        heatProgress = PartProgress.recoil;
                        heatColor = Pal.techBlue;
                        mirror = true;
                        under = true;
                        moveRot = -4f;
                        moveY = 1f;
                        moveX = 2f;

                        moves.add(new PartMove(PartProgress.recoil, -1f, 6f, -20f));
                        children.add(new RegionPart("-plate"){{
                            progress = PartProgress.warmup.delay(0.6f);
                            heatProgress = PartProgress.recoil;
                            heatColor = Pal.techBlue;
                            mirror = true;
                            under = true;
                            moveRot = -6f;
                            moveY = 1.5f;
                            moveX = 2.5f;

                            moves.add(new PartMove(PartProgress.recoil, -2f, 6f, -40f));
                            children.add(new RegionPart("-wing"){{
                                progress = PartProgress.warmup;
                                heatProgress = PartProgress.warmup.add(-0.2f).add(p -> Mathf.sin(9f, 0.2f) * p.warmup);
                                heatColor = Pal.techBlue;
                                mirror = true;
                                under = true;
                                moveRot = 40f;
                                x = 8f;
                                moveY = 3f;
                                moveX = 9.5f;

                                moves.add(new PartMove(PartProgress.recoil, 2f, 9f, 90f));      
                            }}); 

                            children.add(new RegionPart("-wing"){{
                                progress = PartProgress.warmup;
                                heatProgress = PartProgress.warmup.add(-0.2f).add(p -> Mathf.sin(9f, 0.2f) * p.warmup);
                                heatColor = Pal.techBlue;
                                mirror = true;
                                under = true;
                                moveRot = 40f;
                                x = 8f;
                                moveY = -2f;
                                moveX = 5f;

                                moves.add(new PartMove(PartProgress.recoil, 0f, 8f, 70f));      
                            }});     
                        }});
                    }});
                }}, 
                new RegionPart("-mid"){{
                    progress = PartProgress.recoil;
                    heatProgress = PartProgress.recoil;
                    heatColor = Pal.techBlue;
                    mirror = false;
                    under = true;
                    moveY = -5f;
                    heatLayerOffset = 1.2f;
                    layerOffset = 0.9f;
                    outlineLayerOffset = 0.7f;
                    children.add(new RegionPart("-missile") {{
                        progress = PartProgress.reload.curve(Interp.pow2In);

                        colorTo = new Color(1f, 1f, 1f, 0f);
                        color = Color.white;
                        mixColorTo = Pal.accent;
                        mixColor = new Color(1f, 1f, 1f, 0f);
                        outline = false;
                        under = true;

                        layerOffset = -0.1f;
                        outlineLayerOffset = -0.2f;

                        moves.add(new PartMove(PartProgress.warmup.inv(), 0f, -4f, 0f));
                    }});
                }});
            }};
        }};

        aratiri = new AcceleratedTurret("aratiri"){{
            localizedName = "Aratiri";
            description = """ 
                          ThunderBolt Minigun 
                          """;
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 20f;
            acceleratedBonus = 3f;
            acceleratedSteps = 20;
            burnoutDelay = 1600f;
            cooldownDelay = 400f;

            buildCostMultiplier = 0.1f;
            size = 5;
            scaledHealth = 820f;
            reload = 400f;
            range = 500f;
            maxAmmo = 1000;
            ammoPerShot = 10;
            consumeAmmoOnce = false;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.shootBig;
            faction.add(AxFactions.axthrix);
            coolant = consumeCoolant(0.2f);
            ammo(
                Items.surgeAlloy, new BoltBulletType(500, Pal.surge){{
                    boltLength = 30;
                    boltLengthRand = 20;    
                }}
            );
            inaccuracy = 0f;
        }};
        /*apexus = new SingularPayloadAmmoTurret("apexus"){{
            localizedName = "Apexus";
            description = """ 
                          Engineered by Axthrix, Apexus is a large Payload cannon. 
                          Has a variety of Payload ammunition types, this formidable weapon unleashes devastating high velocity 1m rounds. 
                          Axthrix's precision craftsmanship shines through in every aspect of Apexus.
                          """;
            requirements(Category.turret, with(
                    Items.copper, 150,
                    Items.graphite, 300,
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            ammo(
                    basic1mCaliber, new BasicBulletType(){{
                        speed = 20f;
                        drag = 0.5f;
                        knockback = pierceCap = 4;
                        damage = 600;
                        splashDamageRadius = 40;
                        splashDamage = 400;
                        impact = pierce = pierceBuilding = true;
                        buildingDamageMultiplier = 0.3f;
                        laserAbsorb = false;
                        shootEffect = Fx.shootPayloadDriver;
                        smokeEffect = Fx.shootBigSmoke2;
                        hitEffect = despawnEffect = Fx.bigShockwave;
                        destroyEffect = Fx.blockExplosionSmoke;
                    }}
            );

            size = 5;
            hideDetails = false;
            scaledHealth = 180;
            reload = 2f * 60f;
            setWarmupTime(1.5f);
            shootCone = 1f;
            shootY = 0f;
            range = 800f;
            recoil = 0.5f;
            rotateSpeed = 0.9f;
            shootSound = Sounds.missileLaunch;
            shootEffect = Fx.shootBig;
            smokeEffect = Fx.shootSmokeMissile;

            unitSort = UnitSorts.strongest;

            coolant = consumeCoolant(0.2f);
            limitRange();
            setUsers();
        }};*/
    }
}