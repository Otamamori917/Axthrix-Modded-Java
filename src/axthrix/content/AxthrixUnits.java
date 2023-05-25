package axthrix.content;

import arc.graphics.*;
import arc.math.*;
import mindustry.entities.abilities.*;
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
    barrier, blockade, palisade, parapet, impediment;
    
    public static void load(){
        barrier = new UnitType("barrier"){{
           outlineColor = Pal.darkOutline;           
           speed = 0.55f;
           hitSize = 6f;
           health = 140;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ForceFieldAbility(20f, 0.8f, 400f, 20f * 6));
        }};

        blockade = new UnitType("blockade"){{
           outlineColor = Pal.darkOutline;
           speed = 0.7f;
           hitSize = 11f;
           health = 450;
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
           hitSize = 13;
           health = 850;
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
                shootStatusDuration = 200f;
                shootSound = Sounds.shockBlast;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                x = 8f;
                y = 0.5f;
                shootX = 4f;
                shootY = -2f;
                mirror = true;
                recoil = 5f;
                alternate = false;
                top = false;
                reload = 120;
                inaccuracy = 50;
                shoot.shots = 60;
                shoot.shotDelay = 1;
                heatColor = Pal.heal;
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
           speed = 0.44f;
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
                shootStatusDuration = 320f;
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
        }}; 
    }
}    