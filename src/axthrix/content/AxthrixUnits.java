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

            abilities.add(new ForceFieldAbility(20f, 0.2f, 600f, 20f * 6));
        }};

        blockade = new UnitType("blockade"){{
           outlineColor = Pal.darkOutline;
           speed = 0.7f;
           hitSize = 11f;
           health = 350;
           buildSpeed = 2f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-blockade-shield";
                radius = 36f;
                angle = 82f;
                y = -24f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 4f;
                whenShooting = false;
            }});

             weapons.add(new Weapon("aj-nano-shotgun"){{
                shootSound = Sounds.blaster;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                x = 6;
                y = 0;
                mirror = true;
                top = false;
                reload = 40;
                inaccuracy = 40;
                shoot.shots = Mathf.random(20,40);
                shoot.shotDelay = 0f;
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

        palisade = new UnitType("palisade"){{
           outlineColor = Pal.darkOutline;
           hitSize = 13;
           health = 750;
           buildSpeed = 3f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-palisade-shield";
                radius = 40f;
                y = -24f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 6f;
                whenShooting = false;
            }});

            weapons.add(new Weapon("aj-recursor"){{
                shootStatus = AxthrixStatus.vindicationI;
                shootStatusDuration = 420f;
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
                shoot.shots = Mathf.random(20,60);
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
                radius = 80f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 1f;            
            }});

             weapons.add(new Weapon("aj-obilvion"){{
                shootSound = Sounds.blaster;
                shootStatus = AxthrixStatus.vindicationII;
                shootStatusDuration = 120f;
                x = 7;
                y = 1;
                mirror = true;
                alternate = false;
                reload = 40;
                inaccuracy = 1;
                shoot.shots = 4;
                shoot.shotDelay = Mathf.random(50,80);

                bullet = new LaserBoltBulletType(2f, 9){{
                    damage = 40;
                    lifetime = 80;
                    speed = 3;
                    healPercent = 1;
                    collidesTeam = true;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});

                weapons.add(new PointDefenseWeapon("aj-1-point-def"){{
                mirror = true;
                alternate = false;
                x = 2f;
                y = -1f;
                reload = 8f;
                targetInterval = 10f;
                targetSwitchInterval = 15f;

                bullet = new BulletType(){{
                    shootEffect = Fx.sparkShoot;
                    hitEffect = Fx.pointHit;
                    maxRange = 100f;
                    damage = 17f;
                }};
            }});
        }}; 
    }
}    