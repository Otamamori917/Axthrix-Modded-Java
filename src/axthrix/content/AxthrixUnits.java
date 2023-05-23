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
import mindustry.type.weapons.*;
import mindustry.content.*;

public class AxthrixUnits {
    public static UnitType 
    
    //barrier tree
    barrier, blockade, palisade, parapet, impediment,
    //barrier tree turrets
    repairturret, assaulturret;
    
    public static void load(){
        barrier = new UnitType("barrier"){{
           speed = 0.55f;
           hitSize = 6f;
           health = 140;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ForceFieldAbility(20f, 0.2f, 600f, 20f * 6));
        }};

        blockade = new UnitType("blockade"){{
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
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 4f;
                whenShooting = false;
            }});

             weapons.add(new Weapon("aj-nano-shotgun"){{
                shootSound = Sounds.shockBlast;
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
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    layerOffset = -0.3f;
                    turretHeatLayer = Layer.turret - 0.2f;
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
                    status = AxthrixStatus.nanodiverge;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});
        }}; 

        palisade = new UnitType("palisade"){{
           hitSize = 13;
           health = 750;
           buildSpeed = 3f;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-palisade-shield";
                radius = 60f;
                y = -24f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 4f;
            }});

             weapons.add(new Weapon("aj-recursor"){{
                shootStatus = AxthrixStatus.vindicationI;
                shootStatusDuration = 120f;
                shootSound = Sounds.blaster;
                x = 7;
                y = 1;
                mirror = true;
                alternate = false;
                top = false;
                reload = 20;
                inaccuracy = 1;
                shoot.shots = 4;
                shoot.shotDelay = 10;

                bullet = new LaserBoltBulletType(2f, 9){{
                    damage = 20;
                    lifetime = 60;
                    speed = 3;
                    healPercent = 1;
                    collidesTeam = true;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});

                weapons.add(new Weapon("aj-dispatch"){{
                x = 2f;
                y = -1f;
                reload = 8f;
                targetInterval = 10f;
                targetSwitchInterval = 15f;

                bullet = new BulletType(){{
                    damage = 17f;
                }};
            }});
        }}; 

        parapet = new UnitType("parapet"){{
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



        //turrets for barrier tree
        
        repairturret = new UnitType("repairturret"){{
            speed = 0f;
            hitSize = 6f;
            health = 400;
            constructor = MechUnit::create;

            abilities.add(new EnergyFieldAbility(40f, 65f, 180f){{
                statusDuration = 60f * 6f;
                maxTargets = 15;
            }});
        }}; 

        
        assaulturret = new UnitType("assaulturret"){{
            speed = 0f;
            hitSize = 4f;
            health = 650;
            faceTarget = false;
            constructor = MechUnit::create;

             weapons.add(new Weapon("aj-energy-cannon"){{
                shootSound = Sounds.shockBlast;
                x = 0;
                y = 0;
                mirror = false;
                top = false;
                reload = 40;
                rotate = true;
                rotateSpeed = 1.6f;
                parts.add(
                new RegionPart("-arm"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.recoil;
                    heatColor = Pal.heal;
                    mirror = true;
                    under = true;
                    moveX = 1f;
                    moves.add(new PartMove(PartProgress.recoil, -1f, 1f, -25f)); 
                }});
                    

                bullet = new BasicBulletType(2f, 9){{
                    shootEffect = Fx.none;
                    smokeEffect = Fx.shootBigSmoke2;
                    impact = true;
                    hittable = false;
                    reflectable = false;
                    absorbable = false;
                    homingRange = 80f;
                    homingPower = 4f;
                    homingDelay = 20f;
                    spin = 20f;
                    shrinkY = -0.6f;
                    shrinkX = -0.6f;
                    knockback = -2f;
                    width = 6f;
                    height = 4f;
                    damage = 40;
                    lifetime = 150f;
                    speed = 1.5f;
                    healPercent = 4f;
                    collidesTeam = true;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});
        }}; 
    }
}    