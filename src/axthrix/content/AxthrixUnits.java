package axthrix.content;

import arc.graphics.*;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import axthrix.world.types.ai.AttackDroneAI;
import axthrix.world.types.bulletypes.SpiralPattern;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.unittypes.MountUnitType;
import mindustry.entities.abilities.*;
import axthrix.world.types.abilities.*;
import axthrix.world.types.bulletypes.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.*;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.content.*;

import java.util.ArrayList;

import static mindustry.Vars.content;
import static mindustry.Vars.tilePayload;

public class AxthrixUnits {
    public static UnitType
    //Axthrix  |6 trees|
        //Ground
            //Assault Hovers |SubAtomic|
                quark, electron, baryon, hadron, photon,
                /*
                quark: duo helix 1 red 1 blue
                electron: bendy homing bullet
                baryon: Tri Helix, explode on contact
                hadron: one bullets that explodes into two on contact
                photon: wip
                */
            //Support Walkers |Protect|
                barrier, blockade, palisade, parapet, impediment,
            //Specialist Tanks |Energy/Gem|
               anagh,akshaj,amitojas,agnitejas,ayustejas,
        //Air
            //Assault Helicopters |Storm/Thunder|
                rai,zyran,tufani,styrmir,corentin,
            //Support Airships |safety|
                naji,haven,nagiah,abhayad,sosthenes,
            //Specialist Flying Mounts |carry|
                amos,aymoss,amalik,anuvaha,ambuvahini,
    //Raodon |3 trees|
        //Assault Walker |Power|
            asta,adira,allura,andrea,athena,
        //Support Tank  |Wealth|
            danu,dorit,duarte,dhanya,dhanashri,
        //Specialist aircraft |Fame|
            efim,estes,elmena,evdoxia,estanislao,
    //Ikatusa |undetermined|

    //Core Units |8 units|

    //Legends |undetermined|
        //yin and yang tree
            spate, influx,
//testing
    test1, testDrone
            ;
    public static void load(){
        quark = new AxUnitType("quark") {{
            localizedName = "Quark";
            constructor = UnitEntity::create;
            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
            range = 8 * 26;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            parts.add(
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
            }});

            weapons.add(new Weapon(){{
                mirror = false;
                minWarmup = 0.8f;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootY = 2f;
                shoot = new SpiralPattern(1f, 2){{
                    shots = 3;
                }};
                bullet = new BasicBulletType(3.5f, 30){{
                    width = 1;
                    height = 1;
                    lifetime = 80;
                    keepVelocity = false;
                    trailColor = backColor = lightColor = Color.valueOf("683b3d");
                    frontColor = Color.valueOf("de9458");
                    trailLength = 12;
                    trailChance = 0f;
                    trailWidth = 0.7f;
                    despawnEffect = hitEffect = Fx.none;
                }};
            }});
        }};
        //support walkers
        barrier = new AxUnitType("barrier"){{
           outlineColor = Pal.darkOutline;           
           speed = 0.55f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           canBoost = true;
           boostMultiplier = 2.5f;
           constructor = MechUnit::create;
           factions.add(AxFactions.axthrix);
           weapons.add(new Weapon("puw"){{
                shootSound = Sounds.sap;
                shootY = 2f;
                x = 0f;
                y = 0f;
                mirror = false;
                top = false;
                reload = 200;
                heatColor = Pal.heal;
                immunities.add(AxthrixStatus.nanodiverge);
                bullet = new BasicBulletType(){{
                    homingRange = 40f;
                    homingPower = 4f;
                    homingDelay = 5f;
                    width = 0.5f;
                    height = 0.5f;
                    damage = 2;
                    lifetime = 40;
                    speed = 3;
                    healPercent = 1;
                    collidesTeam = true;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.heal;
                    trailLength = 4;
                    trailWidth = 0.5f;
                    status = AxthrixStatus.nanodiverge;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});    

            abilities.add(new ForceFieldAbility(20f, 0.1f, 100f, 40f * 6));
        }};

        blockade = new AxUnitType("blockade"){{
           outlineColor = Pal.darkOutline;
           armor = 5f;
           speed = 0.7f;
           hitSize = 11f;
           health = 650;
           buildSpeed = 2f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;
           factions.add(AxFactions.axthrix);

            abilities.add(new ShieldArcAbility(){{
                region = "aj-blockade-shield";
                radius = 30f;
                angle = 45f;
                y = -24f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 6f;
                whenShooting = false;
            }});

            weapons.add(new Weapon("aj-nano-launcher"){{
                shootSound = Sounds.blaster;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                x = 6;
                y = 0;
                shootX = 2f;
                shootY = -1f;
                mirror = true;
                top = false;
                reload = 40;
                inaccuracy = 5;
                heatColor = Pal.heal;
                immunities.add(AxthrixStatus.nanodiverge);
                parts.add(
                new RegionPart("-shell"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    mirror = false;
                    under = false;
                    moveX = 2f;
                    moves.add(new PartMove(PartProgress.recoil, -1f, 1f, -25f)); 
                }},
                new RegionPart("-bar"){{
                    progress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    layerOffset = -0.5f;
                    mirror = false;
                    under = true;
                    moveX = 2f;
                }});

                bullet = new BasicBulletType(){{
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("nano-missile"){{
                        targetAir = false;
                        speed = 2.3f;
                        maxRange = 6f;
                        lifetime = 60f * 1.4f;
                        outlineColor = Pal.darkOutline;
                        engineColor = trailColor = Pal.heal;
                        engineLayer = Layer.effect;
                        health = 45;
                        loopSoundVolume = 0.1f;

                        weapons.add(new Weapon(){{
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            shootOnDeath = true;
                            bullet = new ExplosionBulletType(80f, 25f){{
                                shootEffect = Fx.massiveExplosion;
                                fragBullets = 20;
                                fragBullet = new BasicBulletType(5.5f, 50){{
                                    homingRange = 40f;
                                    homingPower = 4f;
                                    homingDelay = 5f;
                                    width = 0.5f;
                                    height = 0.5f;
                                    damage = 1;
                                    lifetime = 40;
                                    speed = 1;
                                    healPercent = 1;
                                    collidesTeam = true;
                                    trailEffect = Fx.none;
                                    trailInterval = 3f;
                                    trailParam = 4f;
                                    trailColor = Pal.heal;
                                    trailLength = 4;
                                    trailWidth = 0.5f;
                                    status = AxthrixStatus.nanodiverge;
                                    backColor = Pal.heal;
                                    frontColor = Color.white;
                                }};
                            }};
                        }});
                    }};    
                }};
            }});
        }}; 

        palisade = new AxUnitType("palisade"){{
           outlineColor = Pal.darkOutline;
           armor = 12f;
           speed = 0.8f;
           hitSize = 13;
           health = 4050;
           buildSpeed = 3f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;
           factions.add(AxFactions.axthrix);

            abilities.add(new ShieldArcAbility(){{
                region = "aj-palisade-shield";
                radius = 35f;
                y = -24f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 800f;
                width = 8f;
                whenShooting = false;
            }});

            weapons.add(new Weapon("aj-recursor"){{
                shootStatus = AxthrixStatus.vindicationI;
                shootStatusDuration = 250f;
                shootSound = Sounds.shockBlast;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                x = 8f;
                y = 0.5f;
                shootX = 4f;
                shootY = -2f;
                mirror = true;
                recoil = 5f;
                top = false;
                reload = 120;
                inaccuracy = 50;
                shoot.shots = 60;
                shoot.shotDelay = 0;
                heatColor = Pal.heal;
                immunities.add(AxthrixStatus.nanodiverge);
                parts.add(
                new RegionPart("-pin"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = Pal.heal;
                    mirror = false;
                    under = true;
                    moveX = 0f; 
                }},
                new RegionPart("-barrel"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = Pal.heal;
                    mirror = false;
                    under = false;
                    moveX = 3f;
                    moveY = -1f;
                    moveRot = -15f;
                    children.add(new RegionPart("-mount"){{
                        progress = PartProgress.warmup;
                        mirror = false;
                        under = true;
                        layerOffset = -2f;
                        moveY = 0f;
                        moveX = 0f;
                    }});
                }});

                bullet = new BasicBulletType(2f, 9){{
                    homingRange = 40f;
                    homingPower = 4f;
                    homingDelay = 5f;
                    width = 0.5f;
                    height = 0.5f;
                    damage = 8;
                    lifetime = 20;
                    speed = 3;
                    healPercent = 1;
                    collidesTeam = true;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.heal;
                    trailLength = 4;
                    trailWidth = 0.5f;
                    status = AxthrixStatus.nanodiverge;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});
        }}; 

        parapet = new AxUnitType("parapet"){{
           outlineColor = Pal.darkOutline;
           armor = 17f;           
           speed = 0.70f;
           hitSize = 24f;
           health = 8600;
           buildSpeed = 4f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;
           factions.add(AxFactions.axthrix);

            abilities.add(new ShieldArcAbility(){{
                region = "aj-parapet-shield";
                radius = 30f;
                angle = 100f;
                y = -22f;
                regen = 0.6f;
                cooldown = 200f;
                max = 1000f;
                width = 10f; 
                whenShooting = false;           
            }});

            weapons.add(new Weapon("aj-hammer-shotgun"){{
                shootSound = Sounds.shockBlast;
                shootStatus = AxthrixStatus.vindicationII;
                shootStatusDuration = 450f;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                top = false;
                x = 12;
                y = 0;
                mirror = true;
                alternate = false;
                reload = 220;
                inaccuracy = 50;
                shoot.shots = 80;
                shoot.shotDelay = 1;
                heatColor = Pal.heal;
                immunities.add(AxthrixStatus.nanodiverge);
                parts.add(
                new RegionPart("-blade"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    mirror = false;
                    under = false;
                    moveX = 2f;
                    moves.add(new PartMove(PartProgress.recoil, -1f, 1f, -25f));
                    children.add(new RegionPart("-piston"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.warmup;
                        heatColor = Pal.heal;
                        mirror = false;
                        under = false;
                        moveY = 2f;
                        moveX = 0f;
                        moves.add(new PartMove(PartProgress.recoil, 0f, -4f, 0f));
                    }}); 
                }});

                bullet = new BasicBulletType(2f, 9){{
                    homingRange = 40f;
                    homingPower = 4f;
                    homingDelay = 5f;
                    width = 0.5f;
                    height = 0.5f;
                    damage = 18;
                    lifetime = 40;
                    speed = 3;
                    healPercent = 1;
                    collidesTeam = true;
                    trailEffect = Fx.none;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.heal;
                    trailLength = 4;
                    trailWidth = 0.5f;
                    status = AxthrixStatus.nanodiverge;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});
            weapons.add(new Weapon("aj-trombone"){{
                shootSound = Sounds.plasmaboom;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                top = true;
                x = 8;
                y = -3f;
                mirror = true;
                reload = 80;
                inaccuracy = 10;
                shoot.shots = 3;
                shoot.shotDelay = 5;
                immunities.add(AxthrixStatus.vibration);
                parts.add(
                new RegionPart("-shell"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    mirror = true;
                    under = false;
                    moveX = -1.5f;
                    moveY = -2f;
                    moveRot = -15f;
                    moves.add(new PartMove(PartProgress.recoil, 1f, -2f, -5f));
                    children.add(new RegionPart("-bar"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.recoil;
                        heatColor = Pal.heal;
                        mirror = false;
                        under = true;
                        moveY = 0f;
                        moveX = 0f;
                        layerOffset = -1f;
                    }});
                    children.add(new RegionPart("-piston"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.recoil;
                        heatColor = Pal.heal;
                        mirror = false;
                        under = true;
                        moveY = 1.5f;
                        moveX = 0f;
                        moves.add(new PartMove(PartProgress.recoil, 0f, -3.5f, 0f));
                    }}); 
                }});
                bullet = new SonicBulletType(){{
                    damage = 150;
                    width = 12f;
                    height = 6f;
                }};
            }});        
        }}; 
        impediment = new AxUnitType("impediment"){{
           outlineColor = Pal.darkOutline;
           armor = 25f;           
           speed = 0.60f;
           health = 14460;
           buildSpeed = 4f;
           constructor = MechUnit::create;
           factions.add(AxFactions.axthrix);

            abilities.add(new ShieldArcAbility(){{
                region = "aj-impediment-shield";
                radius = 40f;
                angle = 100f;
                y = -22f;
                regen = 0.6f;
                cooldown = 200f;
                max = 1000f;
                width = 14f;
                whenShooting = false;           
            }});

            abilities.add(new NanobotStormAbility());

            weapons.add(new Weapon("aj-tuba"){{
                shootSound = Sounds.plasmaboom;
                shootStatus = AxthrixStatus.vindicationIII;
                shootStatusDuration = 200f;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                top = true;
                x = 16;
                y = 0f;
                shootX = -24f;
                shootY = 12f;
                mirror = true;
                reload = 80;
                inaccuracy = 10;
                shoot.shots = 4;
                shoot.shotDelay = 5;
                immunities.add(AxthrixStatus.vibration);
                layerOffset = 0.2f;
                parts.add(
                new RegionPart("-arm"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup.delay(0.6f);
                    heatColor = Pal.heal;
                    mirror = false;
                    under = false;
                    x = -17f;
                    moveX = -1.5f;
                    moveY = -2f;
                    moveRot = -15f;
                    moves.add(new PartMove(PartProgress.recoil, 0f,  1f, -5f));
                    children.add(new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        rotateSpeed = -5;
                        color = Pal.heal;
                        sides = 8;
                        hollow = true;
                        stroke = 0f;
                        strokeTo = 1.6f;
                        radius = 6f;
                        layer = Layer.effect;
                        y = -5;
                        x = -22;
                    }});
                    children.add(new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        rotateSpeed = 4;
                        color = Pal.heal;
                        sides = 6;
                        hollow = true;
                        stroke = 0f;
                        strokeTo = 1.6f;
                        radius = 4f;
                        layer = Layer.effect;
                        y = -5;
                        x = -22;
                    }});
                    children.add(new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        rotateSpeed = -5;
                        color = Pal.heal;
                        sides = 20;
                        hollow = true;
                        stroke = 0f;
                        strokeTo = 1.6f;
                        radius = 9f;
                        layer = Layer.effect;
                        y = -5;
                        x = -22;
                    }});
                    children.add(new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        color = Pal.heal;
                        sides = 8;
                        hollow = true;
                        shapes = 5;
                        stroke = 0f;
                        strokeTo = 4f;
                        radius = 1f;
                        haloRadius = 7f;
                        haloRotateSpeed = 1;
                        layer = Layer.effect;
                        y = -5;
                        x = -22;
                    }});
                    children.add(new RegionPart("-plate"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.warmup.delay(0.6f);
                        heatColor = Pal.heal;
                        mirror = false;
                        under = true;
                        moveY = 2f;
                        moveX = -11f;
                        children.add(new RegionPart("-wing"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.warmup.delay(0.6f);
                            heatColor = Pal.heal;
                            mirror = false;
                            under = true;
                            moveY = -3f;
                            moveX = -6f;
                            moveRot = -10f;
                            moves.add(new PartMove(PartProgress.recoil, 0f, -2f, -10f));
                        }});
                    }});
                }});
                bullet = new SonicBulletType(){{
                    damage = 200;
                    width = 9f;
                    height = 4.5f;
                    shrinkY = -0.6f;
                    shrinkX = -1.8f;
                 }};
            }});
            weapons.add(new Weapon("aj-pod"){{
                shootCone = 360f;
                shootSound = Sounds.blaster;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                baseRotation = 180f;
                top = false;
                x = 0;
                y = -1;
                shootY = -6f;
                mirror = false;
                reload = 1020;
                inaccuracy = 40;
                shoot.shots = 60;
                shoot.shotDelay = 2;
                heatColor = Pal.heal;
                layerOffset = -2f;
                immunities.add(AxthrixStatus.nanodiverge);
                

                bullet = new BasicBulletType(){{
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("nano-swarmer"){{
                        targetAir = true;
                        speed = 4f;
                        maxRange = 14f;
                        lifetime = 120f * 1.6f;
                        outlineColor = Pal.darkOutline;
                        engineColor = trailColor = Pal.heal;
                        engineLayer = Layer.effect;
                        health = 45;
                        loopSoundVolume = 0.1f;

                        weapons.add(new Weapon(){{
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            shootOnDeath = true;
                            bullet = new ExplosionBulletType(120f, 85f){{
                                shootEffect = Fx.massiveExplosion;
                            }};
                        }});
                    }};    
                }};
            }});    
        }};
        //Special Flying Mount
        //amos,aymoss,amalik,anuvaha,ambuvahini,
        amos = new MountUnitType("amos")
        {{
            localizedName = "[#a52ac7]Amos";
            description = """
                          [#a52ac7]Can pick up and use any 1x1 Item Turret.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 600;
            armor = 2;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            flying = true;
            hitSize = 2*2;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 300;
            itemOffsetY = 6;
            speed = 20f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (1 * 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        aymoss = new MountUnitType("aymoss")
        {{
            localizedName = "[#a52ac7]Aymoss";
            description = """
                          [#a52ac7]Can pick up and use any 2x2 Item Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 1800;
            armor = 5;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            flying = true;
            hitSize = 4*4;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 600;
            itemOffsetY = 6;
            speed = 18f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (2 * 2);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        amalik = new MountUnitType("amalik")
        {{
            localizedName = "[#a52ac7]Amalik";
            description = """
                          [#a52ac7]Can pick up and use any 3x3 Item Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 5400;
            armor = 8;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            flying = true;
            hitSize = 6*6;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 1200;
            itemOffsetY = 6;
            speed = 16f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (3 * 3);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        anuvaha = new MountUnitType("anuvaha")
        {{
            localizedName = "[#a52ac7]Anuvaha";
            description = """
                          [#a52ac7]Can pick up and use any 4x4 Item Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 16200;
            armor = 11;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            flying = true;
            hitSize = 8*8;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 2400;
            itemOffsetY = 6;
            speed = 14f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (4 * 4);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        ambuvahini = new MountUnitType("ambuvahini")
        {{
            localizedName = "[#a52ac7]Ambuvahini";
            description = """
                          [#a52ac7]Can pick up and use any 5x5 Item Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 32500;
            armor = 26;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            flying = true;
            hitSize = 10*10;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 4800;
            itemOffsetY = 6;
            speed = 12f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (5 * 5);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        //legends
        //yin and yang tree
        spate = new UnitType("spate"){{
           outlineColor = Pal.darkOutline;
           flying = true;           
           speed = 2f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           constructor = UnitEntity::create;
           weapons.add(new Weapon("puw"){{
                shootY = 2f;
                x = 1f;
                y = 0f;
                mirror = true;
                reload = 10;
                top = false;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }});  

            abilities.add(new SStatusFieldAbility(AxthrixStatus.precludedA, 160f, 140f, 100f){{
                atNotShoot = true;
            }});
        }};
        influx = new UnitType("influx"){{
           outlineColor = Pal.darkOutline;         
           speed = 2f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           faceTarget = false;
           crushDamage = 500f;
           constructor = TankUnit::create;
           weapons.add(new Weapon("puw"){{
                rotate = true;
                rotateSpeed = 3;
                shootY = 2f;
                x = 1f;
                y = 0f;
                mirror = false;
                reload = 10;
                top = true;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }});  

            abilities.add(new SStatusFieldAbility(AxthrixStatus.precludedX, 160f, 140f, 100){{
                onShoot = true;
            }});
        }};
        testDrone = new UnitType("testdrone")
        {{
            localizedName = "[#a52ac7]TEST DRONE";

            constructor = PayloadUnit::create;
            health = 2000;
            playerControllable = false;
            logicControllable = false;
            allowedInPayloads = false;
            armor = 2;
            faceTarget = true;
            flying = true;
            hitSize = 2*2;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 300;
            itemOffsetY = 6;
            speed = 20f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (1 * 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
            weapons.add(new Weapon("aj-nano-launcher"){{
                shootSound = Sounds.blaster;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                x = 6;
                y = 0;
                shootX = 2f;
                shootY = -1f;
                mirror = true;
                top = false;
                reload = 40;
                inaccuracy = 5;
                heatColor = Pal.heal;
                immunities.add(AxthrixStatus.nanodiverge);
                parts.add(
                        new RegionPart("-shell"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.warmup;
                            heatColor = Pal.heal;
                            mirror = false;
                            under = false;
                            moveX = 2f;
                            moves.add(new PartMove(PartProgress.recoil, -1f, 1f, -25f));
                        }},
                        new RegionPart("-bar"){{
                            progress = PartProgress.warmup;
                            heatColor = Pal.heal;
                            layerOffset = -0.5f;
                            mirror = false;
                            under = true;
                            moveX = 2f;
                        }});

                bullet = new BasicBulletType(){{
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = content.unit("aj-nano-missile");
                }};
            }});
        }};
        test1 = new UnitType("test1")
        {{
            localizedName = "[#a52ac7]TEST";

            constructor = PayloadUnit::create;
            health = 6000;
            armor = 2;
            faceTarget = true;
            flying = true;
            hitSize = 2*2;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 300;
            itemOffsetY = 6;
            speed = 20f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (1 * 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
            for(float i : Mathf.signs) {
                abilities.add(
                        new DroneControlAbility() {{

                    rallyPos.add(new Vec2(38f *i, 8f));
                    rallyPos.add(new Vec2(20f * i, 20f));
                    spawnX = 48 / 4f * i;
                    spawnY = 7 / -4f;
                    unitSpawn = testDrone;
                    constructTime = 60 * 5f;
                    setController(returnOwner());
                }}
                    );
            }
        }};
    }
}        