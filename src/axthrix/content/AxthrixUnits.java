package axthrix.content;

import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.Seq;
import arc.util.Tmp;
import axthrix.world.types.bulletypes.SpiralPattern;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.unittypes.MountUnitType;
import mindustry.entities.Effect;
import mindustry.entities.abilities.*;
import axthrix.world.types.abilities.*;
import axthrix.world.types.bulletypes.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.ExplosionEffect;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.WaveEffect;
import mindustry.entities.part.*;

import mindustry.entities.pattern.ShootAlternate;
import mindustry.entities.pattern.ShootHelix;
import mindustry.entities.pattern.ShootMulti;
import mindustry.entities.pattern.ShootSpread;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.content.*;

import java.util.ArrayList;

import static arc.graphics.g2d.Lines.stroke;
import static arc.scene.actions.Actions.color;
import static mindustry.Vars.content;
import static mindustry.Vars.tilePayload;

public class AxthrixUnits {
    public static UnitType
    //Axthrix  |6 trees|
        //Ground
            //Assault Hovers |SubAtomic|
                quark, electron, baryon, hadron, photon,
                /*
                quark: tri helix
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
                //TX
                arcalishion,
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
    test1
            ;
    public static void load(){
        quark = new AxUnitType("quark") {{
            localizedName = "Quark";
            constructor = ElevationMoveUnit::create;
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
            localizedName = "[green]Barrier";
            description = """
                          [green]Protects Allies with small but durable force field.
                          Barrier Fires A single Hostile Nanobot that deals DOT.
                          """;
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
            localizedName = "[green]Blockade";
            description = """
                          [green]Heals Allies and Deals Great Damage at Medium range.
                          Blockade Fires Missiles containing Nanobots in quick succession.
                          """;
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
                        immunities.add(AxthrixStatus.precludedA);
                        immunities.add(AxthrixStatus.precludedX);
                        immunities.add(AxthrixStatus.vindicationIII);
                        immunities.add(AxthrixStatus.vindicationII);
                        immunities.add(AxthrixStatus.vindicationI);

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
            localizedName = "[green]Palisade";
            description = """
                          [green]A Nimble Walker Deals Heavy Damage at close quarters.
                          Palisade Fires A Wave of hostile Nanobots.[]
                          [#800000]Slows Down And Gives Great resistance To itself and allies when attacking.
                          """;
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
            immunities.add(AxthrixStatus.nanodiverge);
            immunities.add(AxthrixStatus.vindicationII);
            immunities.add(AxthrixStatus.vindicationIII);

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
            abilities.add(new SStatusFieldAbility(AxthrixStatus.vindicationI, 400f, 360f, 30){{
                onShoot = true;
            }});

            weapons.add(new Weapon("aj-recursor"){{
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
            localizedName = "[green]Parapet";
            description = """
                          [green]A Dangerous Adversary known to fire hundreds of Nanobots that can cut though blocks like butter.
                          Parapet Fires A large Burst of hostile Nanobots.
                          Has two double shot missile launchers.[]
                          [#800000]Slows Down And Gives Great resistance To itself and allies when attacking.
                          """;
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
           immunities.add(AxthrixStatus.nanodiverge);
           immunities.add(AxthrixStatus.vindicationIII);
           immunities.add(AxthrixStatus.vindicationI);

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
            abilities.add(new SStatusFieldAbility(AxthrixStatus.vindicationII, 400f, 360f, 60){{
                onShoot = true;
            }});

            weapons.add(new Weapon("aj-hammer-shotgun"){{
                shootSound = Sounds.shockBlast;
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
            weapons.add(new Weapon("aj-burst"){{
                shootSound = Sounds.plasmaboom;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                top = true;
                layerOffset = 0.001f;
                x = 8.2f;
                y = -3f;
                rotate = true;
                mirror = true;
                reload = 80;
                inaccuracy = 10;
                shoot.shots = 2;
                shoot.shotDelay = 10;
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
                bullet = new BasicBulletType(){{
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("burst-missile"){{
                        targetAir = false;
                        speed = 3f;
                        maxRange = 6f;
                        lifetime = 60f * 1.6f;
                        outlineColor = Pal.darkOutline;
                        engineColor = trailColor = Pal.heal;
                        engineLayer = Layer.effect;
                        health = 65;
                        loopSoundVolume = 0.1f;
                        immunities.add(AxthrixStatus.precludedA);
                        immunities.add(AxthrixStatus.precludedX);
                        immunities.add(AxthrixStatus.vindicationIII);
                        immunities.add(AxthrixStatus.vindicationII);
                        immunities.add(AxthrixStatus.vindicationI);

                        weapons.add(new Weapon(){{
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            shootOnDeath = true;
                            bullet = new ExplosionBulletType(80f, 25f){{
                                shootEffect = new ExplosionEffect(){{
                                    lifetime = 28f;
                                    waveStroke = 6f;
                                    waveLife = 10f;
                                    waveRadBase = 7f;
                                    waveColor = Pal.heal;
                                    waveRad = 30f;
                                    smokes = 6;
                                    smokeColor = Color.white;
                                    sparkColor = Pal.heal;
                                    sparks = 6;
                                    sparkRad = 35f;
                                    sparkStroke = 1.5f;
                                    sparkLen = 4f;
                                }};
                            }};
                        }});
                    }};
                }};
            }});        
        }}; 
        impediment = new AxUnitType("impediment"){{
            localizedName = "[green]Impediment";
            description = """
                          [green]A Unit With a Monstrous Long range weaponry.
                          Impediment Fires 2 Long range Anti tank missiles.
                          Has a Short range Nanobot field for protection.[]
                          [#800000]Slows Down And Gives Great resistance To itself and allies when attacking.
                          """;
           outlineColor = Pal.darkOutline;
           armor = 25f;           
           speed = 0.60f;
           health = 14460;
           hitSize = 28;
           buildSpeed = 4f;
           constructor = MechUnit::create;
           factions.add(AxFactions.axthrix);
           immunities.add(AxthrixStatus.nanodiverge);
           immunities.add(AxthrixStatus.vindicationII);
           immunities.add(AxthrixStatus.vindicationI);
           abilities.add(new NanobotStormAbility());

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
            abilities.add(new SStatusFieldAbility(AxthrixStatus.vindicationIII, 400f, 360f, 90){{
                onShoot = true;
            }});

            parts.add(
            new RegionPart("-mount"){{
                progress = PartProgress.warmup;
                heatProgress = PartProgress.warmup.delay(0.6f);
                heatColor = Pal.heal;
                mirror = true;
                under = true;
                children.add(new RegionPart("-arm"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup.delay(0.6f);
                    heatColor = Pal.heal;
                    mirror = true;
                    under = false;
                    x = 0f;
                    moveX = 0;
                    moveY = -0.5f;
                    moveRot = 5f;
                    moves.add(new PartMove(PartProgress.recoil, 0f,  0, 0f));
                    children.add(new RegionPart("-plate"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.warmup.delay(0.6f);
                        heatColor = Pal.heal;
                        mirror = true;
                        under = true;
                        moveY = 0f;
                        moveX = -6f;
                        children.add(new RegionPart("-shell"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.warmup.delay(0.6f);
                            heatColor = Pal.heal;
                            mirror = true;
                            under = true;
                            moveY = -1f;
                            moveX = 0f;
                            moveRot = -10f;
                            moves.add(new PartMove(PartProgress.recoil, 0f, 0f, 0f));
                        }});
                    }});
                }});
            }});


            weapons.add(new Weapon("aj-bruh"){{
                shootSound = Sounds.laserblast;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                top = false;
                x = 0;
                y = 0f;
                shootX = 14f;
                shootY = 8;
                mirror = true;
                alternate = false;
                reload = 800;
                recoil = 0;
                inaccuracy = 6;
                parts.add(
                    new RegionPart("-missile") {{
                        progress = PartProgress.reload.curve(Interp.pow2In);
                        colorTo = new Color(1f, 1f, 1f, 0f);
                        color = Color.white;
                        mixColorTo = Pal.accent;
                        mixColor = new Color(1f, 1f, 1f, 0f);
                        outline = true;
                        under = true;
                        y = -4;
                        x = 2;
                        layerOffset = 9.02f;
                        outlineLayerOffset = 9.01f;
                        moves.add(
                                new PartMove(PartProgress.warmup.inv(), -3f, 0f, -90f),
                                new PartMove(PartProgress.warmup.inv().delay(0.3f), 4f, 1f, 45f),
                                new PartMove(PartProgress.warmup.inv().delay(0.5f), 1f, 1f, 45f)
                        );
                    }}
                );
                bullet = new BasicBulletType(){{
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("bruh-missile"){{
                        speed = 4.6f;
                        maxRange = 6f;
                        lifetime = 60f * 5.5f;
                        outlineColor = Pal.darkOutline;
                        engineColor = trailColor = Pal.heal;
                        engineLayer = Layer.effect;
                        engineSize = 3.1f;
                        engineOffset = 10f;
                        rotateSpeed = 0.25f;
                        trailLength = 18;
                        missileAccelTime = 50f;
                        lowAltitude = true;
                        loopSound = Sounds.missileTrail;
                        loopSoundVolume = 0.6f;
                        deathSound = Sounds.largeExplosion;
                        targetAir = true;
                        collidesAir = true;

                        fogRadius = 6f;

                        health = 2100;
                        immunities.add(AxthrixStatus.precludedA);
                        immunities.add(AxthrixStatus.precludedX);
                        immunities.add(AxthrixStatus.vindicationIII);
                        immunities.add(AxthrixStatus.vindicationII);
                        immunities.add(AxthrixStatus.vindicationI);

                        weapons.add(new Weapon(){{
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            deathExplosionEffect = Fx.massiveExplosion;
                            shootOnDeath = true;
                            shake = 10f;
                            bullet = new ExplosionBulletType(800f, 65f){{
                                hitColor = Pal.heal;
                                shootEffect = hitEffect = new Effect(50f, 100f, e -> {
                                    e.scaled(7f, b -> {
                                        color(Pal.heal, b.fout());
                                        Fill.circle(e.x, e.y, 65);
                                    });

                                    color(Pal.heal);
                                    stroke(e.fout() * 3f);
                                    Lines.circle(e.x, e.y, 65);

                                    int points = 20;
                                    float offset = Mathf.randomSeed(e.id, 360f);
                                    for(int i = 0; i < points; i++){
                                        float angle = i* 360f / points + offset;
                                        Drawf.tri(e.x + Angles.trnsx(angle, 65), e.y + Angles.trnsy(angle, 65), 6f, 50f * e.fout(), angle);

                                    }

                                    Fill.circle(e.x, e.y, 12f * e.fout());
                                    color(Pal.heal);
                                    Fill.circle(e.x, e.y, 6f * e.fout());
                                    Drawf.light(e.x, e.y, 65 * 1.6f, Pal.heal, e.fout());
                                });

                                collidesAir = true;

                                ammoMultiplier = 1f;
                                fragLifeMin = 0.1f;
                                fragBullets = 8;
                                fragBullet = new EmpBulletType(){{
                                    lightOpacity = 0.7f;
                                    unitDamageScl = 0.8f;
                                    healPercent = 20f;
                                    timeIncrease = 3f;
                                    timeDuration = 60f * 20f;
                                    powerDamageScl = 3f;
                                    damage = 60;
                                    hitColor = lightColor = Pal.heal;
                                    lightRadius = 70f;
                                    clipSize = 250f;
                                    shootEffect = Fx.hitEmpSpark;
                                    smokeEffect = Fx.shootBigSmoke2;
                                    lifetime = 30f;
                                    sprite = "circle-bullet";
                                    backColor = Pal.heal;
                                    frontColor = Color.white;
                                    width = height = 12f;
                                    shrinkY = 0f;
                                    speed = 2f;
                                    trailLength = 20;
                                    trailWidth = 6f;
                                    trailColor = Pal.heal;
                                    trailInterval = 3f;
                                    splashDamage = 70f;
                                    splashDamageRadius = 30;
                                    hitShake = 4f;
                                    trailRotation = true;
                                    status = StatusEffects.electrified;
                                }};
                            }};
                        }});

                        abilities.add(new MoveEffectAbility(){{
                            effect = Fx.missileTrailSmoke;
                            rotation = 180f;
                            y = -9f;
                            color = Color.grays(0.6f).lerp(Color.darkGray, 0.5f).a(0.4f);
                            interval = 7f;
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
            rotateSpeed = 9 / 7.5f;
            accel = 0.08f;
            drag = 0.014f;
            strafePenalty = 1;
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
            rotateSpeed = 8 / 7.5f;
            accel = 0.07f;
            drag = 0.015f;
            strafePenalty = 1;
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
            rotateSpeed = 7 / 7.5f;
            accel = 0.06f;
            drag = 0.016f;
            strafePenalty = 1;
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
            rotateSpeed = 6 / 7.5f;
            accel = 0.05f;
            drag = 0.017f;
            strafePenalty = 1;
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
            rotateSpeed = 5 / 7.5f;
            accel = 0.04f;
            drag = 0.018f;
            strafePenalty = 1;
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
        arcalishion = new MountUnitType("arcalishion")//Todo Might have weapons or abilities
        {{
            localizedName = "[#a52ac7]Arcalishion";
            description = """
                          [orange]|Teir Xalibur Unit|
                          (This Means This Is A Boss)[]
                          --------------------------------------------------
                          [#a52ac7]Can pick up and use any 6x6 Item Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 65000;
            armor = 39;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            flying = true;
            hitSize = 12*12;
            engineColor = Color.valueOf("a52ac7");
            itemCapacity = 10000;
            itemOffsetY = 20;
            speed = 8f / 7.5f;
            rotateSpeed = 4 / 7.5f;
            accel = 0.03f;
            drag = 0.019f;
            strafePenalty = 1;
            lowAltitude = true;
            pickupUnits = false;
            omniMovement = false;
            payloadCapacity = tilePayload * (6 * 6);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(-30,-70,14,-90),
                    new UnitEngine(30,-70,14,-90),

                    new UnitEngine(-70,-35,8,180+45),
                    new UnitEngine(70,-35,8,270+45),

                    new UnitEngine(-60,29,10,180+45),
                    new UnitEngine(60,29,10,270+45)
            );
        }};
        //legends
        //yin and yang tree
        spate = new UnitType("spate"){{//Todo Missile weapon
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
        influx = new UnitType("influx"){{//Todo Cannon weapon
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
        test1 = new UnitType("test1")
        {{
            localizedName = "[#800000]TEST";

            constructor = UnitEntity::create;
            health = 6000;
            armor = 2;
            faceTarget = true;
            flying = true;
            hitSize = 2*2;
            engineColor = Color.valueOf("95abd9");
            itemCapacity = 300;
            itemOffsetY = 6;
            speed = 20f / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = false;
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
            weapons.add(new Weapon() {{
                reload = 60*10;
                x = y = shootX = shootY = 0;
                shootStatus = StatusEffects.unmoving;
                shootStatusDuration = shoot.firstShotDelay = Fx.heal.lifetime-1;
                bullet = new SpawnHelperBulletType(){{
                    hasParent = true;
                    shootEffect = Fx.shootBig;
                    spawnUnit = AxthrixDrones.basicFlame;
                }};
            }});
        }};
    }
}        