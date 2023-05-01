package ajmain.units;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;

import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.type.unit.*;
import mindustry.type.weapons.*;
import mindustry.world.meta.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;
import static mindustry.Vars.*;
import mindustry.content.*;

public class UnitsAJava {
    public static UnitType barrier, blockade, palisade, parapet, impediment;
    
    public static void load(){


        UnitType barrier = new UnitType("barrier"){{
           speed = 0.55f;
           hitSize = 6f;
           health = 140;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ForceFieldAbility(20f, 0.2f, 400f, 20f * 6));

             weapons.add(new PointDefenseWeapon("aj-1-point-def"){{
                mirror = false;
                x = 0f;
                y = 0f;
                reload = 9f;
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




        UnitType blockade = new UnitType("blockade"){{
           speed = 0.55f;
           hitSize = 6f;
           health = 140;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-blockade-shield";
                radius = 50f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 8f;
                y = -20f;
                whenShooting = false;
                offsetRegion = true;
            }});

             weapons.add(new Weapon("aj-blockade-grs"){{
                shootSound = Sounds.missile;
                x = 6;
                y = 1;
                mirror = true;
                top = false;
                reload = 40;
                inaccuracy = 20;
                shoot.shots = 3;
                shoot.shotDelay = 5;  

                bullet = new MissileBulletType(2f, 9){{
                    damage = 8;
                    lifetime = 100;
                    speed = 3;
                    healPercent = 1;
                    collidesTeam = true;
                    backColor = Pal.heal;
                    frontColor = Color.white;
                }};
            }});

                weapons.add(new PointDefenseWeapon("aj-1-point-def"){{
                mirror = false;
                x = 0f;
                y = 0f;
                reload = 9f;
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




        UnitType palisade = new UnitType("palisade"){{
           speed = 0.55f;
           hitSize = 6f;
           health = 140;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-palisade-shield";
                radius = 50f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                whenShooting = false;
                offsetRegion = true;
                width = 8f;
                y = -20f;
            }});

             weapons.add(new Weapon("aj-repeater"){{
                shootSound = Sounds.blaster;
                x = 7;
                y = 1;
                mirror = true;
                alternate = false;
                top = false;
                reload = 20;
                inaccuracy = 1;
                shoot.shots = 4;
                shoot.shotDelay = Mathf.random(30,80);

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




        UnitType parapet = new UnitType("parapet"){{
           speed = 0.55f;
           hitSize = 6f;
           health = 140;
           canBoost = true;
           boostMultiplier = 1.5f;
           constructor = MechUnit::create;

            abilities.add(new ShieldArcAbility(){{
                region = "aj-parapet-shield";
                radius = 50f;
                angle = 50f;
                regen = 0.6f;
                cooldown = 200f;
                max = 600f;
                width = 8f;
                y = -20f;
                offsetRegion = true;
            }});

             weapons.add(new Weapon("aj-repeater"){{
                shootStatus:
                shootSound = Sounds.blaster;
                x = 7;
                y = 1;
                mirror = true;
                alternate = false;
                top = false;
                reload = 20;
                inaccuracy = 1;
                shoot.shots = 4;
                shoot.shotDelay = Mathf.random(30,80);

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
