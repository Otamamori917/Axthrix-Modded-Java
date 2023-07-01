package axthrix.content;

import arc.graphics.*;
import arc.math.*;
import mindustry.entities.abilities.*;
import axthrix.types.abilities.*;
import axthrix.types.bulletypes.*;
import mindustry.entities.bullet.*;
import mindustry.entities.part.*;
import mindustry.world.*;
import mindustry.world.draw.*;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.content.*;

public class AxthrixUnits {
    public static UnitType
     
    
    //barrier tree
    barrier, blockade, palisade, parapet, impediment,
    //bolt tree
    bolt,
    //yin and yang tree
    escalate, carillon;
    
    public static void load(){
        barrier = new UnitType("barrier"){{
           outlineColor = Pal.darkOutline;           
           speed = 0.55f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           canBoost = true;
           boostMultiplier = 2.5f;
           constructor = MechUnit::create;
           weapons.add(new Weapon("puw"){{
                shootSound = Sounds.swish;
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

            abilities.add(new ForceFieldAbility(20f, 0.8f, 400f, 20f * 6));
        }};

        blockade = new UnitType("blockade"){{
           outlineColor = Pal.darkOutline;
           armor = 5f;
           speed = 0.7f;
           hitSize = 11f;
           health = 650;
           buildSpeed = 2f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

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

        palisade = new UnitType("palisade"){{
           outlineColor = Pal.darkOutline;
           armor = 12f;
           speed = 0.8f;
           hitSize = 13;
           health = 4050;
           buildSpeed = 3f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

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

        parapet = new UnitType("parapet"){{
           outlineColor = Pal.darkOutline;
           armor = 17f;           
           speed = 0.70f;
           hitSize = 24f;
           health = 8600;
           buildSpeed = 4f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

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
        impediment = new UnitType("impediment"){{
           outlineColor = Pal.darkOutline;
           armor = 25f;           
           speed = 0.60f;
           hitSize = 24f;
           health = 14460;
           buildSpeed = 4f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-impediment-shield";
                radius = 30f;
                angle = 100f;
                y = -22f;
                regen = 0.6f;
                cooldown = 200f;
                max = 1000f;
                width = 10f; 
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
                x = 12;
                y = 0;
                mirror = true;
                reload = 80;
                inaccuracy = 10;
                shoot.shots = 4;
                shoot.shotDelay = 5;
                immunities.add(AxthrixStatus.vibration);
                parts.add(
                new RegionPart("-arm"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    mirror = true;
                    under = false;
                    moveX = -1.5f;
                    moveY = -2f;
                    moveRot = -15f;
                    moves.add(new PartMove(PartProgress.recoil, 0f,  1f, -5f));
                    children.add(new RegionPart("-plate"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.recoil;
                        heatColor = Pal.heal;
                        mirror = false;
                        under = true;
                        moveY = 0f;
                        moveX = 0f;
                        layerOffset = -1f;
                        moves.add(new PartMove(PartProgress.recoil, 0f, -3.5f, 0f));
                        children.add(new RegionPart("-wing"){{
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
                }});
                bullet = new SonicBulletType(){{
                    damage = 200;
                    width = 18f;
                    height = 9f;
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
                y = -2;
                mirror = false;
                reload = 1020;
                inaccuracy = 60;
                shoot.shots = 60;
                shoot.shotDelay = 2;
                heatColor = Pal.heal;
                layerOffset = -2;
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
        //legends
        //yin and yang tree
        escalate = new UnitType("escalate"){{
           outlineColor = Pal.darkOutline;           
           speed = 2f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           canBoost = true;
           boostMultiplier = 2.5f;
           constructor = EntityMapping.map("alpha");
           weapons.add(new Weapon("puw"){{
                shootSound = Sounds.swish;
                shootY = 2f;
                x = 1f;
                y = 0f;
                mirror = true;
                top = false;
                reload = 40;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }});  

            abilities.add(new SStatusFieldAbility(AxthrixStatus.precludedA, 180f, 200f, 100f));
        }};
    }
}    