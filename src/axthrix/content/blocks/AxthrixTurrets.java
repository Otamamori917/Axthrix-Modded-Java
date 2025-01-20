package axthrix.content.blocks;

import axthrix.AxthrixLoader;
import axthrix.content.AxFactions;

import axthrix.content.AxLiquids;
import axthrix.content.AxthrixStatus;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.types.block.defense.*;
import axthrix.world.types.bulletypes.*;
import axthrix.world.types.bulletypes.bulletpatterntypes.SpiralPattern;
import axthrix.world.util.AxUtil;
import blackhole.entities.bullet.BlackHoleBulletType;
import blackhole.entities.part.BlackHolePart;
import mindustry.content.*;
import mindustry.entities.UnitSorts;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;

import static axthrix.content.blocks.PayloadAmmoBlocks.*;
import static mindustry.type.ItemStack.*;

public class AxthrixTurrets{
    public static Block
    //Axthrix

    //Bendy miniguns

    kramola, razdor, smuta,

    //Artillery

    kisten,

    //shotguns

    fragmentation,

    //revolvers
    emily,corvo,nagual,lucifer,

    //special

    nado, aratiri, gravitation, morta,

    multitest,viper,

    //payload
    apex,//Small apexus? but autocannon possibly
    apexus,

    //Raodon
    venture,asmot,rektios;

    public static void loadAxthrix(){
        kramola = new ItemAcceleratedTurret("kramola"){{
            outlineColor = Color.valueOf("#181a1b");
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom variables
            acceleratedDelay = 40f;
            acceleratedBonus = 1.8f;
            acceleratedSteps = 16;
            burnoutDelay = 240;
            cooldownDelay = 360f;



            cooldownTime = 120f;
            recoils = 2;
            minWarmup = 0.3f;
            size = 2;
            scaledHealth = 420f;
            reload = 60f;
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
            shoot = new ShootMulti(
                    new ShootBarrel(){{
                        barrels = new float[]{
                                -1.5f, 15, 0,
                                 1.5f, 15, 0
                        };
                    }},
                    new ShootHelix(){{
                        mag = 0.3f;
                        scl = 0.1f;
                    }});
            ammo(
                Items.titanium, new BasicBulletType(4f, 15){{
                    homingPower = 0.09f;
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
            drawer = new DrawTurret(){{
                parts.add(
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 4.5f;
                            moveY = 3f;
                            recoilIndex = 0;
                            layerOffset = 2;
                            moves.add(
                                    new PartMove(PartProgress.reload, 2.5f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), -2.5f, 0f, 0f)
                            );

                        }},
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 4.5f;
                            moveY = 3f;
                            recoilIndex = 1;
                            layerOffset = 2;
                            moves.add(
                                    new PartMove(PartProgress.reload, -2.5f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), 2.5f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-main"){{layerOffset = 2;}});

            }};

        }};




        razdor = new ItemAcceleratedTurret("razdor"){{
            outlineColor = Color.valueOf("#181a1b");
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom variables
            acceleratedDelay = 50f;
            acceleratedBonus = 1.8f;
            acceleratedSteps = 18;
            burnoutDelay = 300;
            cooldownDelay = 360f;

            cooldownTime = 120f;
            recoils = 3;
            minWarmup = 0.3f;
            size = 3;
            scaledHealth = 420f;
            reload = 60f;
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
            shoot = new ShootMulti(
                    new ShootBarrel(){{
                        barrels = new float[]{
                                -1.25f, 15, 0,
                                 0, 15, 0,
                                 1.25f, 15, 0
                        };
                    }},
                    new ShootHelix(){{
                        mag = 0.4f;
                        scl = 0.2f;
                    }});
            ammo(
                Items.titanium, new BasicBulletType(4f, 30){{
                    homingPower = 0.09f;
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
            drawer = new DrawTurret("crystalized-"){{
                parts.add(

                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = 0;
                            moveY = 6f;
                            recoilIndex = 0;
                            moves.add(
                                    new PartMove(PartProgress.reload, -2.75f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), 0f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = 1.375f;
                            moveY = 6f;
                            recoilIndex = 1;
                            moves.add(
                                    new PartMove(PartProgress.reload, -2.75f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), 1.375f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = -1.375f;
                            moveY = 6f;
                            recoilIndex = 2;
                            moves.add(
                                    new PartMove(PartProgress.reload, 2.25f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), -1.375f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-main"));

            }};
        }};


        smuta = new ItemAcceleratedTurret("smuta"){{
            outlineColor = Color.valueOf("#181a1b");
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom variables
            acceleratedDelay = 60f;
            acceleratedBonus = 1.8f;
            acceleratedSteps = 20;
            burnoutDelay = 360f;
            cooldownDelay = 360f;

            recoils = 4;
            minWarmup = 0.3f;
            size = 4;
            scaledHealth = 420f;
            reload = 60f;
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
            shoot = new ShootMulti(
                    new ShootBarrel(){{
                        barrels = new float[]{
                                -4, 18, 0,
                                -2, 18, 0,
                                2, 18, 0,
                                4, 18, 0
                        };
                    }},
                    new ShootHelix(){{
                        mag = 0.5f;
                        scl = 0.3f;
                    }});
            ammo(
                Items.titanium, new BasicBulletType(4f, 45){{
                    homingPower = 0.09f;
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
            drawer = new DrawTurret("crystalized-"){{
                parts.addAll(
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = -2.25f;
                            moveY = 6f;
                            recoilIndex = 3;
                            moves.add(
                                    new PartMove(PartProgress.reload, 4.5f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), -2.25f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = 2.25f;
                            moveY = 6f;
                            recoilIndex = 3;
                            moves.add(
                                    new PartMove(PartProgress.reload, -4.5f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), 2.25f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = 2.25f;
                            moveY = 6f;
                            recoilIndex = 3;
                            moves.add(
                                    new PartMove(PartProgress.reload, -4.5f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), -2.25f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-barrel"){{
                            progress = PartProgress.warmup;
                            y = 6;
                            x = -2.25f;
                            moveY = 6f;
                            recoilIndex = 3;
                            moves.add(
                                    new PartMove(PartProgress.reload, 4.5f, 0f, 0f),
                                    new PartMove(PartProgress.reload.inv(), 2.25f, 0f, 0f)
                            );
                        }},
                        new RegionPart("-main"));

            }};
        }};

        kisten = new AxItemTurret("kisten"){{
            outlineColor = Color.valueOf("#181a1b");
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
                    layerOffset = 2.2f;
                    outlineLayerOffset = -1f;

                    children.add(new RegionPart("-joint"){{
                        progress = PartProgress.warmup.delay(0.6f);
                        heatProgress = PartProgress.recoil;
                        heatColor = Pal.techBlue;
                        mirror = true;
                        under = true;
                        moveRot = -4f;
                        moveY = 1f;
                        moveX = 2f;
                        layerOffset = 2.2f;

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

                            layerOffset = 1.8f;

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

                                layerOffset = 1.8f;

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

                                layerOffset = 1.8f;

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
                    layerOffset = 2f;
                    children.add(new RegionPart("-missile") {{
                        progress = PartProgress.reload.curve(Interp.pow2In);

                        colorTo = new Color(1f, 1f, 1f, 0f);
                        color = Color.white;
                        mixColorTo = Pal.accent;
                        mixColor = new Color(1f, 1f, 1f, 0f);
                        outline = false;
                        under = true;



                        layerOffset = 1.8f;

                        moves.add(new PartMove(PartProgress.warmup.inv(), 0f, -4f, 0f));
                    }});
                }});
            }};
        }};
        aratiri = new PowerAcceleratedTurret("aratiri"){{
            outlineColor = Color.valueOf("#181a1b");
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 45f;
            acceleratedBonus = 2f;
            acceleratedSteps = 8;
            burnoutDelay = 300f;
            cooldownDelay = 720f;

            buildCostMultiplier = 0.1f;
            size = 5;
            scaledHealth = 820f;
            reload = 30f;
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
            inaccuracy = 0f;
            consumePower(10);
            shootType = new BoltBulletType(500, Pal.surge){{
                boltLength = 30;
                boltLengthRand = 20;
            }};
        }};
        gravitation = new AxItemTurret("gravitation"){{
            drawer = new DrawTurret("crystalized-");
            outlineColor = Color.valueOf("#181a1b");
            range = 8*90;
            size = 6;
            reload = 600;
            coolantMultiplier = 1.8f;
            requirements(Category.turret, with(
                    Items.surgeAlloy, 1500,
                    Items.phaseFabric, 3000,
                    Items.silicon, 3250,
                    Items.titanium, 3500
            ));
            faction.add(AxFactions.axthrix);
            coolant = consumeCoolant(1);
            ammoPerShot = 60;
            maxAmmo = 180;
            ammo(
                    Items.phaseFabric, new BlackHoleBulletType(){{
                        lifetime = 600;
                        speed = 2;
                        damage = bulletDamage = 0;
                        horizonRadius = 0;
                        lensingRadius = 0;
                        suctionRadius = 240;
                        scaledForce = 400;
                        force = 40;
                        scaleLife = true;
                        swirlEffect = Fx.none;
                        color = Color.purple;
                        fragBullets = 1;
                        trailInterval = 1;
                        trailLength = 60;
                        trailColor = Color.purple;

                        trailEffect = AxthrixFfx.circleOut(50,50, 10,Color.purple);
                        fragBullet = new BlackHoleBulletType(){{
                            lifetime = 400;
                            speed = 0;
                            damage = bulletDamage = 25;
                            lensingRadius = 180;
                            horizonRadius = 20;
                            damageRadius = 280;
                            suctionRadius = 680;
                            scaledForce = 800;
                            force = 100;
                            color = Color.purple;
                            fragBullets = 1;
                            fragBullet = new BlackHoleBulletType(){{
                                lifetime = 40;
                                repel = true;
                                speed = 0;
                                color = Color.purple;
                                damage = bulletDamage = 75;
                                horizonRadius = 0;
                                lensingRadius = 0;
                                suctionRadius = damageRadius = 200;
                                scaledForce = 2670;
                                force = 200;
                                swirlEffect = AxthrixFfx.circleOut(40,200, 20,Color.purple);
                            }};
                        }};

                        lightning = 1;
                        lightningType = new BlackHoleBulletType(){{
                            lifetime = 40;
                            repel = true;
                            speed = 0;
                            damage = bulletDamage = 150;
                            horizonRadius = 0;
                            lensingRadius = 0;
                            suctionRadius = damageRadius = 280;
                            scaledForce = 8000;
                            force = 600;
                            swirlEffect = AxthrixFfx.circleOut(40,280, 50,Color.purple);
                        }};
                        parts.add(

                            new BlackHolePart(){{
                                progress = PartProgress.constant(1f);
                                growProgress = PartProgress.constant(1f);
                                size = sizeTo = 4;
                                edge = edgeTo = 120;
                                x = 0;
                                y = 0;
                                color = Color.purple;
                            }},
                            new BlackHolePart(){{
                                progress = PartProgress.constant(1f);
                                growProgress = PartProgress.constant(1f);
                                size = sizeTo = 4;
                                edge = edgeTo = 120;
                                x = 0;
                                y = 0;
                                color = Color.purple;
                            }}
                        );
                    }}
            );
        }};

        morta = new AxPowerTurret("morta"){{
            outlineColor = Color.valueOf("#181a1b");
            range = 8*50;
            size = 4;
            reload = 280;
            inaccuracy =  0f;
            requirements(Category.turret, with(
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.axthrix);
            consumePower(405/60);
            health = 1300;
            shootType = new SheildArcBullet(240,"aj-morta-sheild"){{
                    lifetime = 240;
            }};
            drawer = new DrawTurret("crystalized-"){{
                /*parts.add(
                        new RegionPart("-shell-l"){{
                            progress = PartProgress.reload.inv();;
                            heatProgress = PartProgress.warmup;
                            heatColor = Pal.heal;
                            mirror = false;
                            under = false;
                            moveX = -1.5f;
                            moveY = -2f;
                            moveRot = -15f;
                            layerOffset = 2;
                            moves.add(new PartMove(PartProgress.reload, 1f, -2f, -5f));
                            children.add(new RegionPart("-piston-l"){{
                                progress = PartProgress.reload.inv();
                                heatProgress = PartProgress.recoil;
                                heatColor = Pal.heal;
                                mirror = false;
                                moveY = -1f;
                                moveX = 1f;
                                layerOffset = 2;
                                moves.add(new PartMove(PartProgress.reload, -1f, 1f, 0f));
                            }});
                        }},
                        new RegionPart("-shell-r"){{
                            progress = PartProgress.reload.inv();;
                            heatProgress = PartProgress.warmup;
                            heatColor = Pal.heal;
                            mirror = false;
                            under = false;
                            moveX = 1.5f;
                            moveY = -2f;
                            moveRot = 15f;
                            layerOffset = 2;
                            moves.add(new PartMove(PartProgress.reload, -1f, -2f, 5f));
                            children.add(new RegionPart("-piston-r"){{
                                progress = PartProgress.reload.inv();
                                heatProgress = PartProgress.recoil;
                                heatColor = Pal.heal;
                                mirror = false;
                                moveY = -1f;
                                moveX = -1f;
                                layerOffset = 2;
                                moves.add(new PartMove(PartProgress.reload, 1f, 1f, 0f));
                            }});
                        }},
                        new RegionPart("-main"){{layerOffset = 2;}});*/
            }};
        }};
        viper = new AxLiquidTurret("viper"){{
            outlineColor = Color.valueOf("#181a1b");
            range = AxUtil.GetRange(22,46);
            size = 2;
            reload = 1;
            requirements(Category.turret, with(
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.axthrix);
            health = 800;
            ammoPerShot = 2;
            maxAmmo = 6*25;
            shoot = new ShootBarrel(){{
                barrels = new float[]{
                        -4f, -8, 10,
                        -2.5f, -5, 5,
                        0f, -3, 0,
                        2.5f, -5, -5,
                        4f, -8, -10
                };
            }};
            ammo(
                    AxLiquids.iodineGas, new LiquidBulletType(){{
                        lifetime = 46;
                        speed = 22;
                        hitSize = 12;
                        damage = 10;
                        liquid = AxLiquids.iodineGas;
                        status = AxthrixStatus.unrepair;
                    }}
            );
            drawer = new DrawTurret("crystalized-")/*{{
                parts.add(
                        new RegionPart("-shell-r"){{
                            progress = PartProgress.recoil;
                            moveY = 0.5f;
                            layerOffset = 2;
                            moves.add(
                                    new PartMove(PartProgress.smoothReload, -1f, -1f, 15f)
                            );

                        }},
                        new RegionPart("-shell-l"){{
                            progress = PartProgress.recoil;
                            moveY = 0.5f;
                            layerOffset = 2;
                            moves.add(
                                    new PartMove(PartProgress.smoothReload, 1f, -1f, -15f)
                            );
                        }},
                        new RegionPart("-main"){{layerOffset = 2;}});

            }}*/;
        }};
        apex = new PayloadAcceleratedTurret("apex"){{
            outlineColor = Color.valueOf("#181a1b");
            requirements(Category.turret, with(
                    Items.copper, 150,
                    Items.graphite, 300,
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.axthrix);
            ammo(
                    basic1mCaliber, new BasicBulletType(){{
                        var CoLor = Items.copper.color;
                        lifetime = AxUtil.GetLifetime(15f,75);
                        speed = 15f;
                        knockback = pierceCap = 4;
                        damage = 100;
                        splashDamageRadius = 30;
                        splashDamage = 100;
                        impact = pierce = pierceBuilding = true;
                        buildingDamageMultiplier = 0.4f;
                        collidesAir = true;
                        collidesGround = true;
                        trailInterval = 0;
                        trailChance = Integer.MAX_VALUE;
                        bulletInterval = 3;
                        intervalBullets = 1;
                        trailLength = 40;
                        trailWidth = 2;
                        trailColor = lightColor = backColor = CoLor;
                        trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                        trailRotation = true;
                        fragBullets = 2;
                        fragBullet = intervalBullet = new FlakBulletType(){{
                            explodeDelay = 10f;
                            lifetime = AxUtil.GetLifetime(3f,5);
                            speed = 3f;
                            knockback = pierceCap = 4;
                            splashDamageRadius = explodeRange = 40;
                            splashDamage = 100;
                            scaledSplashDamage = true;
                            impact = pierce = pierceBuilding = true;
                            buildingDamageMultiplier = 0.4f;
                            collidesAir = true;
                            collidesGround = true;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailRotation = true;
                        }};
                    }},
                    frostbite1mCaliber, new BasicBulletType(){{
                        var CoLor = Items.titanium.color;
                        lifetime = AxUtil.GetLifetime(15f,75);
                        speed = 15f;
                        knockback = pierceCap = 4;
                        damage = 100;
                        splashDamageRadius = 30;
                        splashDamage = 100;
                        impact = pierce = pierceBuilding = true;
                        buildingDamageMultiplier = 0.4f;
                        collidesAir = true;
                        collidesGround = true;
                        trailInterval = 0;
                        trailChance = Integer.MAX_VALUE;
                        bulletInterval = 3;
                        intervalBullets = 2;
                        trailLength = 40;
                        trailWidth = 2;
                        trailColor = lightColor = backColor = CoLor;
                        trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                        trailRotation = true;
                        fragBullets = 1;
                        fragVelocityMax = 0;
                        fragBullet = new AfterShockBulletType(80, 50){{
                            splashDelay = 10;
                            splashAmount = 3;
                            status = StatusEffects.freezing;
                            statusDuration = 80;
                            buildingDamageMultiplier = 0.4f;
                            frontColor = CoLor.cpy().a(0.4f);
                            particleColor = bottomColor = backColor = CoLor;
                        }};
                    }},
                    incendiary1mCaliber, new BasicBulletType(){{
                        var CoLor = Items.pyratite.color;
                        lifetime = AxUtil.GetLifetime(15f,75);
                        speed = 15f;
                        knockback = pierceCap = 4;
                        damage = 200;
                        impact = pierce = pierceBuilding = true;
                        buildingDamageMultiplier = 0.4f;
                        collidesAir = true;
                        collidesGround = true;
                        trailInterval = 0;
                        trailChance = Integer.MAX_VALUE;
                        bulletInterval = 0.5f;
                        intervalDelay = AxUtil.GetLifetime(15f,100 / 8);
                        intervalBullets = 3;
                        trailLength = 40;
                        trailWidth = 2;
                        trailColor = lightColor = backColor = CoLor;
                        trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                        trailRotation = true;
                        fragBullets = 6;
                        fragBullet = intervalBullet = new FireBulletType(){{
                            speed = 6f;
                            radius = 5;
                            velMin = 0.8f;
                            velMax = 6;
                            hittable = false;
                            keepVelocity = false;
                            collidesAir = false;
                            hitEffect = Fx.hitFlameSmall;
                            despawnEffect = Fx.none;
                            trailColor = lightColor = backColor = CoLor;
                            fragRandomSpread = 0f;
                            fragSpread = 5f;
                            fragVelocityMin = 1f;
                            fragBullets = 10;
                            fragBullet = new BulletType(4.2f, 8f) {{
                                hitSize = 7f;
                                lifetime = 13f;
                                pierce = true;
                                pierceBuilding = true;
                                pierceCap = 2;
                                trailColor = lightColor = backColor = CoLor;
                                statusDuration = 60f * 4;
                                shootEffect = Fx.shootSmallFlame;
                                hitEffect = Fx.hitFlameSmall;
                                despawnEffect = Fx.none;
                                status = StatusEffects.melting;
                                keepVelocity = false;
                                hittable = false;
                            }};
                        }};
                    }},
                    quicksilver1mCaliber, new BasicBulletType(){{
                        var CoLor = Color.valueOf("8b8696");
                        lifetime = AxUtil.GetLifetime(20f,75);
                        speed = 20f;
                        knockback = 2;
                        pierceCap = 12;
                        damage = 100;
                        impact = pierce = pierceBuilding = true;
                        collidesAir = true;
                        collidesGround = true;
                        trailInterval = 0;
                        trailChance = Integer.MAX_VALUE;
                        bulletInterval = 3;
                        trailLength = 40;
                        trailWidth = 2;
                        trailColor = lightColor = backColor = CoLor;
                        trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                        trailRotation = true;
                        status = AxthrixStatus.slivered;
                        statusDuration = 100;
                        bulletInterval = 0.5f;
                        intervalBullets = 6;
                        fragBullets = 4;
                        fragBullet = intervalBullet = new BulletType(4.2f, 8f) {{
                            hitSize = 7f;
                            lifetime = 13f;
                            pierce = true;
                            pierceBuilding = true;
                            pierceCap = 2;
                            trailColor = lightColor = backColor = CoLor;
                            shootEffect = Fx.none;
                            hitEffect = Fx.none;
                            despawnEffect = Fx.none;
                            status = AxthrixStatus.slivered;
                            statusDuration = 300;
                            keepVelocity = false;
                            hittable = false;
                        }};
                    }},
                    //energy rounds
                    arcane1mCaliber, new BasicBulletType(){{
                        var CoLor = Items.phaseFabric.color;
                        lifetime = AxUtil.GetLifetime(15,75);
                        speed = 15f;
                        knockback = pierceCap = 4;
                        damage = 300;
                        impact = pierce = pierceBuilding = true;
                        buildingDamageMultiplier = 0.4f;
                        collidesAir = true;
                        collidesGround = true;
                        trailInterval = 0;
                        trailChance = Integer.MAX_VALUE;
                        bulletInterval = 3;
                        intervalBullets = 2;
                        trailLength = 40;
                        trailWidth = 2;
                        trailColor = lightColor = backColor = CoLor;
                        trailEffect = AxthrixFfx.energyRoundRadiate(CoLor);
                        trailRotation = true;
                        fragBullets = 4;
                        fragBullet = intervalBullet = new LaserBulletType(){{
                            colors = new Color[]{CoLor.cpy().a(0.4f), CoLor, Color.white};
                            damage = 38;
                            length = 24;
                        }};
                    }},
                    void1mCaliber, new BasicBulletType(){{
                        var CoLor = Color.valueOf("7b1c9b");
                        lifetime = AxUtil.GetLifetime(10,75);
                        speed = 10f;
                        damage = 300;
                        impact = pierce = pierceBuilding = true;
                        buildingDamageMultiplier = 0.3f;
                        collidesAir = true;
                        collidesGround = true;
                        trailInterval = 0;
                        trailChance = Integer.MAX_VALUE;
                        trailLength = 40;
                        trailWidth = 2;
                        trailColor = lightColor = backColor = CoLor;
                        trailEffect = AxthrixFfx.energyRoundRadiate(CoLor);
                        fragOnHit = false;
                        trailRotation = true;
                        fragBullets = 1;
                        fragVelocityMax = 0;
                        fragBullet = new BlackHoleBulletType(){{
                            buildingDamageMultiplier = 0.4f;
                            lifetime = 200;
                            damage = 50;
                            speed = 0;
                            horizonRadius = 5;
                            lensingRadius = 120;
                            damageRadius = 60;
                            suctionRadius = 240;
                            scaledForce = 1200;
                            force = 20;
                            color = CoLor;
                        }};
                    }}
            );
            if(AxthrixLoader.funibullet){
                ammoTypes.put(Blocks.router, new InfFragBulletType(){{
                    var CoLor = Color.gray;
                    lifetime = AxUtil.GetLifetime(15f,75);
                    speed = 15f;
                    knockback = 20;
                    pierceCap = Integer.MAX_VALUE;
                    damage = 100;
                    impact = pierce = pierceBuilding = true;
                    collidesAir = true;
                    collidesGround = true;
                    trailInterval = 0;
                    trailChance = Integer.MAX_VALUE;
                    trailLength = 40;
                    trailWidth = 2;
                    trailColor = lightColor = backColor = CoLor;
                    trailEffect = new MultiEffect(AxthrixFfx.solidRoundRadiate(CoLor),AxthrixFfx.energyRoundRadiate(CoLor));
                    trailRotation = true;
                    fragBullets = 12;
                }});
            }
            shoot = new ShootAlternate(10);
            maxAmmo = 48;
            size = 3;
            hideDetails = false;
            scaledHealth = 180;
            reload = 30f;
            acceleratedBonus = 1;
            acceleratedDelay = 1;
            burnoutDelay = 359;
            cooldownDelay = 600;
            setWarmupTime(1.5f);
            shootCone = 1f;
            shootY = 0f;
            range = 600f;
            recoil = 0.5f;
            rotateSpeed = 1.8f;
            shootSound = Sounds.largeCannon;
            shootEffect = Fx.shootPayloadDriver;
            smokeEffect = Fx.shootBigSmoke2;

            unitSort = UnitSorts.strongest;

            coolant = consumeCoolant(0.5f);
            setUsers();
        }};
        apexus = new PayloadTurretType("apexus"){{
            outlineColor = Color.valueOf("#181a1b");
            requirements(Category.turret, with(
                    Items.copper, 150,
                    Items.graphite, 300,
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.axthrix);
                ammo(
                        basic1mCaliber, new BasicBulletType(){{
                            var CoLor = Items.copper.color;
                            lifetime = AxUtil.GetLifetime(20f,100);
                            speed = 20f;
                            knockback = pierceCap = 4;
                            damage = 200;
                            splashDamageRadius = 40;
                            splashDamage = 200;
                            impact = pierce = pierceBuilding = true;
                            buildingDamageMultiplier = 0.3f;
                            collidesAir = true;
                            collidesGround = true;
                            trailInterval = 0;
                            trailChance = Integer.MAX_VALUE;
                            bulletInterval = 3;
                            intervalBullets = 2;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                            trailRotation = true;
                            fragBullets = 4;
                            fragBullet = intervalBullet = new FlakBulletType(){{
                                explodeDelay = 10f;
                                lifetime = AxUtil.GetLifetime(6f,5);
                                speed = 6f;
                                knockback = pierceCap = 4;
                                splashDamageRadius = explodeRange = 40;
                                splashDamage = 100;
                                scaledSplashDamage = true;
                                impact = pierce = pierceBuilding = true;
                                buildingDamageMultiplier = 0.3f;
                                collidesAir = true;
                                collidesGround = true;
                                trailLength = 40;
                                trailWidth = 2;
                                trailColor = lightColor = backColor = CoLor;
                                trailRotation = true;
                            }};
                        }},
                        frostbite1mCaliber, new BasicBulletType(){{
                            var CoLor = Items.titanium.color;
                            lifetime = AxUtil.GetLifetime(20f,100);
                            speed = 20f;
                            knockback = pierceCap = 4;
                            damage = 200;
                            splashDamageRadius = 40;
                            splashDamage = 200;
                            impact = pierce = pierceBuilding = true;
                            buildingDamageMultiplier = 0.3f;
                            collidesAir = true;
                            collidesGround = true;
                            trailInterval = 0;
                            trailChance = Integer.MAX_VALUE;
                            bulletInterval = 3;
                            intervalBullets = 2;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                            trailRotation = true;
                            fragBullets = 1;
                            fragVelocityMax = 0;
                            fragBullet = new AfterShockBulletType(100, 60){{
                                splashDelay = 20;
                                splashAmount = 6;
                                status = StatusEffects.freezing;
                                statusDuration = 80;
                                buildingDamageMultiplier = 0.3f;
                                frontColor = CoLor.cpy().a(0.4f);
                                particleColor = bottomColor = backColor = CoLor;
                            }};
                        }},
                        incendiary1mCaliber, new BasicBulletType(){{
                            var CoLor = Items.pyratite.color;
                            lifetime = AxUtil.GetLifetime(20f,100);
                            speed = 20f;
                            knockback = pierceCap = 4;
                            damage = 400;
                            impact = pierce = pierceBuilding = true;
                            buildingDamageMultiplier = 0.3f;
                            collidesAir = true;
                            collidesGround = true;
                            trailInterval = 0;
                            trailChance = Integer.MAX_VALUE;
                            bulletInterval = 0.5f;
                            intervalDelay = AxUtil.GetLifetime(20f,25);
                            intervalBullets = 6;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                            trailRotation = true;
                            fragBullets = 12;
                            fragBullet = intervalBullet = new FireBulletType(){{
                                speed = 6f;
                                radius = 5;
                                velMin = 0.8f;
                                velMax = 6;
                                hittable = false;
                                keepVelocity = false;
                                collidesAir = false;
                                hitEffect = Fx.hitFlameSmall;
                                despawnEffect = Fx.none;
                                trailColor = lightColor = backColor = CoLor;
                                fragRandomSpread = 0f;
                                fragSpread = 5f;
                                fragVelocityMin = 1f;
                                fragBullets = 10;
                                fragBullet = new BulletType(4.2f, 8f) {{
                                    hitSize = 7f;
                                    lifetime = 13f;
                                    pierce = true;
                                    pierceBuilding = true;
                                    pierceCap = 2;
                                    trailColor = lightColor = backColor = CoLor;
                                    statusDuration = 60f * 4;
                                    shootEffect = Fx.shootSmallFlame;
                                    hitEffect = Fx.hitFlameSmall;
                                    despawnEffect = Fx.none;
                                    status = StatusEffects.melting;
                                    keepVelocity = false;
                                    hittable = false;
                                }};
                            }};
                        }},
                        quicksilver1mCaliber, new BasicBulletType(){{
                            var CoLor = Color.valueOf("8b8696");
                            lifetime = AxUtil.GetLifetime(30f,100);
                            speed = 30f;
                            knockback =2;
                            pierceCap = 12;
                            damage = 200;
                            impact = pierce = pierceBuilding = true;
                            collidesAir = true;
                            collidesGround = true;
                            trailInterval = 0;
                            trailChance = Integer.MAX_VALUE;
                            bulletInterval = 3;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailEffect = AxthrixFfx.solidRoundRadiate(CoLor);
                            trailRotation = true;
                            status = AxthrixStatus.slivered;
                            statusDuration = 300;
                            bulletInterval = 0.5f;
                            intervalBullets = 6;
                            fragBullets = 10;
                            fragBullet = intervalBullet = new BulletType(4.2f, 8f) {{
                                hitSize = 7f;
                                lifetime = 13f;
                                pierce = true;
                                pierceBuilding = true;
                                pierceCap = 2;
                                trailColor = lightColor = backColor = CoLor;
                                shootEffect = Fx.none;
                                hitEffect = Fx.none;
                                despawnEffect = Fx.none;
                                status = AxthrixStatus.slivered;
                                statusDuration = 300;
                                keepVelocity = false;
                                hittable = false;
                            }};
                        }},
                        //energy rounds
                        arcane1mCaliber, new BasicBulletType(){{
                            var CoLor = Items.phaseFabric.color;
                            lifetime = AxUtil.GetLifetime(20,100);
                            speed = 20f;
                            knockback = pierceCap = 4;
                            damage = 600;
                            impact = pierce = pierceBuilding = true;
                            buildingDamageMultiplier = 0.3f;
                            collidesAir = true;
                            collidesGround = true;
                            trailInterval = 0;
                            trailChance = Integer.MAX_VALUE;
                            bulletInterval = 3;
                            intervalBullets = 2;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailEffect = AxthrixFfx.energyRoundRadiate(CoLor);
                            trailRotation = true;
                            fragBullets = 4;
                            fragBullet = intervalBullet = new LaserBulletType(){{
                                colors = new Color[]{CoLor.cpy().a(0.4f), CoLor, Color.white};
                                damage = 38;
                                length = 24;
                            }};
                        }},
                        void1mCaliber, new BasicBulletType(){{
                            var CoLor = Color.valueOf("7b1c9b");
                            lifetime = AxUtil.GetLifetime(15,100);
                            speed = 15f;
                            damage = 600;
                            impact = pierce = pierceBuilding = true;
                            buildingDamageMultiplier = 0.3f;
                            collidesAir = true;
                            collidesGround = true;
                            trailInterval = 0;
                            trailChance = Integer.MAX_VALUE;
                            trailLength = 40;
                            trailWidth = 2;
                            trailColor = lightColor = backColor = CoLor;
                            trailEffect = AxthrixFfx.energyRoundRadiate(CoLor);
                            fragOnHit = false;
                            trailRotation = true;
                            fragBullets = 1;
                            fragVelocityMax = 0;
                            fragBullet = new BlackHoleBulletType(){{
                                buildingDamageMultiplier = 0.3f;
                                lifetime = 400;
                                damage = 150;
                                speed = 0;
                                horizonRadius = 10;
                                lensingRadius = 240;
                                damageRadius = 120;
                                suctionRadius = 480;
                                scaledForce = 2400;
                                force = 40;
                                color = CoLor;
                            }};
                        }}
                );
            if(AxthrixLoader.funibullet){
                ammoTypes.put(Blocks.router, new InfFragBulletType(){{
                    var CoLor = Color.gray;
                    lifetime = AxUtil.GetLifetime(20f,100);
                    speed = 20f;
                    knockback = 20;
                    pierceCap = Integer.MAX_VALUE;
                    damage = 400;
                    impact = pierce = pierceBuilding = true;
                    collidesAir = true;
                    collidesGround = true;
                    trailInterval = 0;
                    trailChance = Integer.MAX_VALUE;
                    trailLength = 40;
                    trailWidth = 2;
                    trailColor = lightColor = backColor = CoLor;
                    trailEffect = new MultiEffect(AxthrixFfx.solidRoundRadiate(CoLor),AxthrixFfx.energyRoundRadiate(CoLor));
                    trailRotation = true;
                    fragBullets = 24;
                }});
            }
            maxAmmo = 12;
            size = 5;
            hideDetails = false;
            scaledHealth = 180;
            reload = 2f * 60f;
            setWarmupTime(1.5f);
            shootCone = 1f;
            shootY = 0f;
            range = 800f;
            recoil = 0.5f;
            rotateSpeed = 1.2f;
            shootSound = Sounds.largeCannon;
            shootEffect = Fx.shootPayloadDriver;
            smokeEffect = Fx.shootBigSmoke2;

            unitSort = UnitSorts.strongest;

            coolant = consumeCoolant(0.2f);
            setUsers();
        }};
        /*multitest = new MultiTurretType("multi"){{
            outlineColor = Color.valueOf("#181a1b");
            localizedName = "multi";
            description = """
                          WIP
                          """;
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));


            buildCostMultiplier = 0.1f;
            size = 2;
            scaledHealth = 420f;
            range = 360f;
            faction.add(AxFactions.axthrix);
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
                    }});
            weapons.add(new BlockWeapon("puw"){{
                shootY = 2f;
                x = 4f;
                y = 4f;
                mirror = true;
                reload = 10;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }},
            new BlockWeapon("puw"){{
                shootY = 2f;
                x = -4f;
                y = -4f;
                mirror = true;
                reload = 10;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }});
        }};*/

    }
    public static void loadRaodon(){
        asmot = new AxItemTurret("asmot"){{
            outlineColor = Color.valueOf("#181a1b");
            localizedName = "Asmot";
            description = """
                          Short range High damage Machinegun.
                          Great for point blank and high speed targets
                          Due to design has high spread and is very ammo hungry
                          [stat]External Storage recomended.
                          """;
            range = 8*23;
            size = 2;
            reload = 10;
            coolantMultiplier = 2f;
            requirements(Category.turret, with(
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.raodon);
            coolant = consumeCoolant(1);
            health = 800;
            ammoPerShot = 6;
            maxAmmo = 6*25;
            shoot = new ShootBarrel(){{
                barrels = new float[]{
                        -4f, -8, 20,
                        -2.5f, -5, 10,
                        0f, -3, 0,
                        2.5f, -5, -10,
                        4f, -8, -20
                };
            }};
            ammo(
                    Items.titanium, new BasicBulletType(){{
                        lifetime = 46;
                        speed = 4;
                        damage = 100;
                        trailInterval = 1;
                        trailLength = 60;
                        trailColor = frontColor = backColor = Color.darkGray;
                    }},
                    Items.silicon, new BasicBulletType(){{
                        lifetime = 46;
                        speed = 4;
                        damage = 70;
                        trailInterval = 1;
                        trailLength = 60;
                        homingDelay = 16;
                        homingRange = 8;
                        homingPower = 0.1f;
                        trailColor = frontColor = backColor = Color.darkGray;
                    }}
            );
            drawer = new DrawTurret("reinforced-"){{
                parts.add(
                        new RegionPart("-shell-r"){{
                            progress = PartProgress.recoil;
                            moveY = 0.5f;
                            layerOffset = 2;
                            moves.add(
                                    new PartMove(PartProgress.smoothReload, -1f, -1f, 15f)
                            );

                        }},
                        new RegionPart("-shell-l"){{
                            progress = PartProgress.recoil;
                            moveY = 0.5f;
                            layerOffset = 2;
                            moves.add(
                                    new PartMove(PartProgress.smoothReload, 1f, -1f, -15f)
                            );
                        }},
                        new RegionPart("-main"){{layerOffset = 2;}});

            }};
        }};

        rektios = new AxPowerTurret("rektios"){{
            outlineColor = Color.valueOf("#181a1b");
            range = 8*36;
            size = 3;
            reload = 280;
            inaccuracy = 2.5f;
            requirements(Category.turret, with(
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.raodon);
            consumePower(210/60);
            health = 1300;
            shoot.shots = 3;
            shoot.shotDelay = 1;
            shootType = new SonicBulletType(){{
                damage = 16;
                lifetime = 36;
                width = 20;
                height = 6;
            }};
            drawer = new DrawTurret("reinforced-"){{
                parts.add(
                        new RegionPart("-shell-l"){{
                            progress = PartProgress.reload.inv();;
                            heatProgress = PartProgress.warmup;
                            heatColor = Pal.heal;
                            mirror = false;
                            under = false;
                            moveX = -1.5f;
                            moveY = -2f;
                            moveRot = -15f;
                            layerOffset = 2;
                            moves.add(new PartMove(PartProgress.reload, 1f, -2f, -5f));
                            children.add(new RegionPart("-piston-l"){{
                                progress = PartProgress.reload.inv();
                                heatProgress = PartProgress.recoil;
                                heatColor = Pal.heal;
                                mirror = false;
                                moveY = -1f;
                                moveX = 1f;
                                layerOffset = 2;
                                moves.add(new PartMove(PartProgress.reload, -1f, 1f, 0f));
                            }});
                        }},
                        new RegionPart("-shell-r"){{
                            progress = PartProgress.reload.inv();;
                            heatProgress = PartProgress.warmup;
                            heatColor = Pal.heal;
                            mirror = false;
                            under = false;
                            moveX = 1.5f;
                            moveY = -2f;
                            moveRot = 15f;
                            layerOffset = 2;
                            moves.add(new PartMove(PartProgress.reload, -1f, -2f, 5f));
                            children.add(new RegionPart("-piston-r"){{
                                progress = PartProgress.reload.inv();
                                heatProgress = PartProgress.recoil;
                                heatColor = Pal.heal;
                                mirror = false;
                                moveY = -1f;
                                moveX = -1f;
                                layerOffset = 2;
                                moves.add(new PartMove(PartProgress.reload, 1f, 1f, 0f));
                            }});
                        }},
                        new RegionPart("-main"){{layerOffset = 2;}});
            }};
        }};

        lucifer = new RevolverTurret("lucifer"){{
            outlineColor = Color.valueOf("#181a1b");
            maxCartridges = numOfReloadCartridges = 20;
            cartridgeReloadTime = 400;
            reloadIfNotFull = false;
            shoot = new ShootBarrel(){{
                barrels = new float[]{
                        -2f, -8, 0,
                        -1.25f, -2.5f, 0,
                        0f, -3, 0,
                        1.25f, -2.5f, 0,
                        2f, -8, 0
                };
            }};
            range = 8*23;
            size = 3;
            reload = 10;
            coolantMultiplier = 2f;
            requirements(Category.turret, with(
                    Items.silicon, 325,
                    Items.titanium, 350
            ));
            faction.add(AxFactions.raodon);
            coolant = consumeCoolant(1);
            health = 800;
            ammoPerShot = 1;
            maxAmmo = 6*25;
            ammo(
                    Items.titanium, new BasicBulletType(){{
                        lifetime = 46;
                        speed = 4;
                        damage = 100;
                        trailInterval = 1;
                        trailLength = 60;
                        trailColor = frontColor = backColor = Color.darkGray;
                    }}

            );
            drawer = new DrawTurret("reinforced-"){{
                parts.add(
                        new RegionPart("-barrel"){{
                            progress = cartridgeprogress();
                            moveX = 1f;
                            moveRot = 10;
                            layerOffset = 2;
                        }},
                        new RegionPart("-back"){{
                            progress = cartridgeprogress();
                            moveX = 1f;
                            moveRot = -10;
                            layerOffset = 2;
                        }},
                        new RegionPart("-cylinder"){{
                            progress = cartridgeprogress();
                            moveX = -1f;
                            moveRot = -10;
                            layerOffset = 2;
                        }}
                        );

            }};
        }};
    }
}
