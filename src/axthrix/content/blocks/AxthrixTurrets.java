package axthrix.content.blocks;

import axthrix.AxthrixLoader;
import axthrix.content.AxFactions;

import axthrix.content.AxthrixStatus;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.types.block.defense.PayloadAcceleratedTurret;
import axthrix.world.types.block.defense.PayloadTurretType;
import axthrix.world.types.bulletypes.*;
import axthrix.world.types.bulletypes.bulletpatterntypes.SpiralPattern;
import axthrix.world.types.block.defense.AcceleratedTurret;
import axthrix.world.types.block.defense.AxItemTurret;
import axthrix.world.util.AxUtil;
import blackhole.entities.bullet.BlackHoleBulletType;
import mindustry.content.*;
import mindustry.entities.UnitSorts;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.pattern.ShootAlternate;
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

    //Bendy miniguns

    kramola, razdor, smuta,

    //Rocket Artillery

    kisten,

    //shotguns

    fragmentation,

    //special

    nado, aratiri,

    multitest,

    //payload
    apex,//Small apexus? but autocannon possibly
    apexus;

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
            localizedName = "Razdor";
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
            localizedName = "Smuta";
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
        apex = new PayloadAcceleratedTurret("apex"){{
            localizedName = "Apex";
            description = """ 
                          """;
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
                        lifetime = AxUtil.GetRange(15f,75);
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
                            lifetime = AxUtil.GetRange(3f,5);
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
                        lifetime = AxUtil.GetRange(15f,75);
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
                        lifetime = AxUtil.GetRange(15f,75);
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
                        intervalDelay = AxUtil.GetRange(15f,100 / 8);
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
                        lifetime = AxUtil.GetRange(20f,75);
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
                        lifetime = AxUtil.GetRange(15,75);
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
                        lifetime = AxUtil.GetRange(10,75);
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
                ammoTypes.put(funniBullet, new InfFragBulletType(){{
                    var CoLor = Color.gray;
                    lifetime = AxUtil.GetRange(15f,75);
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
            acceleratedSteps = 150;
            acceleratedDelay = 1;
            burnoutDelay = 300;
            cooldownDelay = 800;
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

            coolant = consumeCoolant(0.2f);
            setUsers();
        }};
        apexus = new PayloadTurretType("apexus"){{
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
            faction.add(AxFactions.axthrix);
                ammo(
                        basic1mCaliber, new BasicBulletType(){{
                            var CoLor = Items.copper.color;
                            lifetime = AxUtil.GetRange(20f,100);
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
                                lifetime = AxUtil.GetRange(6f,5);
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
                            lifetime = AxUtil.GetRange(20f,100);
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
                            lifetime = AxUtil.GetRange(20f,100);
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
                            intervalDelay = AxUtil.GetRange(20f,25);
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
                            lifetime = AxUtil.GetRange(30f,100);
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
                            lifetime = AxUtil.GetRange(20,100);
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
                            lifetime = AxUtil.GetRange(15,100);
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
                ammoTypes.put(funniBullet, new InfFragBulletType(){{
                    var CoLor = Color.gray;
                    lifetime = AxUtil.GetRange(20f,100);
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
}
