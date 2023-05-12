package ajmain.content;

import ajmain.content.types.turretypes.*;
import mindustry.content.*;
import mindustry.gen.Sounds;
import ajmain.content.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.campaign.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.heat.*;
import mindustry.world.blocks.legacy.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.logic.*;
import mindustry.world.blocks.payloads.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.production.*;
import mindustry.world.blocks.sandbox.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.units.*;
import mindustry.world.consumers.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

import static mindustry.type.ItemStack.*;

public class BlocksA {
    public static Block

    //Bendy miniguns        Rocket Artilery
    kramola, razdor, smuta, kisten;

    public static void load(){
        kramola = new AcceleratedTurret("kramola"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 120f;
            acceleratedBonus = 1.5f;
            acceleratedSteps = 3;
            burnoutDelay = 600f;
            cooldownDelay = 300f;

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
            shoot = new SpiralPattern(){{
                mag = 1f;
                scl = 2f;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 15f;
                    homingPower = 4f;
                    homingRange = 50;
                    homingDelay = 20f;
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
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 120f;
            acceleratedBonus = 1.5f;
            acceleratedSteps = 3;
            burnoutDelay = 700f;
            cooldownDelay = 400f;

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
            shoot = new SpiralPattern(){{
                mag = 1.5f;
                scl = 2.5f;
                shots = 3;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 30f;
                    homingPower = 4f;
                    homingRange = 50;
                    homingDelay = 20f;
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
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));
            //custom varibles
            acceleratedDelay = 120f;
            acceleratedBonus = 1.5f;
            acceleratedSteps = 3;
            burnoutDelay = 800f;
            cooldownDelay = 500f;

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
            shoot = new SpiralPattern(){{
                mag = 1.75f;
                scl = 2.75f;
                shots = 4;
            }};
            ammo(
                Items.titanium, new BasicBulletType(4f, 100){{
                    damage = 60f;
                    homingPower = 4f;
                    homingRange = 50;
                    homingDelay = 20f;
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

        kisten = new ItemTurret("kisten"){{
            requirements(Category.turret, with(Items.titanium, 300, Items.thorium, 200, Items.plastanium, 125));

            buildCostMultiplier = 0.1f;
            size = 4;
            scaledHealth = 320f;
            reload = 300f;
            range = 560f;
            maxAmmo = 20;
            ammoPerShot = 10;
            recoil = 3f;
            rotateSpeed = 2f;
            targetAir = true;
            targetGround = true;
            shootY = -2f;
            shootSound = Sounds.mediumCannon;
            minWarmup = 0.94f;
            shootWarmupSpeed = 0.03f;
            shoot = new SpiralPattern(){{
                mag = 2f;
                scl = 4f;
                shots = 10;
            }};
            ammo(
                Items.pyratite, new MissileBulletType(4f, 100){{
                    damage = 300f;
                    makeFire = true;
                    homingPower = 3f;
                    homingRange = 80;
                    homingDelay = 60f;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    trailEffect = Fx.fire;
                    hitEffect = Fx.hitFlameSmall;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.darkFlame;
                    status = StatusEffects.burning;
                    trailLength = 16;
                    trailWidth = 2f;
                }},

                Items.titanium, new MissileBulletType(4f, 100){{
                    damage = 300f;
                    homingPower = 3f;
                    homingRange = 80;
                    homingDelay = 60f;
                    width = 6f;
                    height = 12f;
                    hitSize = 6f;
                    lifetime = 200f;
                    trailEffect = Fx.freezing;
                    hitEffect = Fx.instHit;
                    trailInterval = 3f;
                    trailParam = 4f;
                    trailColor = Pal.techBlue;
                    status = StatusEffects.freezing;
                    trailLength = 16;
                    trailWidth = 2f;
                }}
            );
            inaccuracy = 0f;

            drawer = new DrawTurret("reinforced-"){{
                parts.add(new RegionPart("-blade"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup;
                    heatColor = Pal.techBlue;
                    moveRot = -22f;
                    moveX = 0f;
                    moveY = -5f;
                    mirror = true;
                    children.add(new RegionPart("-wing"){{
                        progress = PartProgress.warmup.delay(0.6f);
                        heatProgress = PartProgress.recoil;
                        heatColor = Pal.techBlue;
                        mirror = true;
                        under = true;
                        moveY = 0.5f;
                        moveX = 1f;

                        moves.add(new PartMove(PartProgress.recoil, -1f, 6f, -40f));
                    }});
                }},
                new RegionPart("-mid"){{
                    progress = PartProgress.recoil;
                    heatProgress = PartProgress.warmup.add(-0.2f).add(p -> Mathf.sin(9f, 0.2f) * p.warmup);
                    heatColor = Pal.techBlue;
                    mirror = false;
                    under = true;
                    moveY = -5f;
                }}, new RegionPart("-missile"){{
                    progress = PartProgress.reload.curve(Interp.pow2In);

                    colorTo = new Color(1f, 1f, 1f, 0f);
                    color = Color.white;
                    mixColorTo = Pal.accent;
                    mixColor = new Color(1f, 1f, 1f, 0f);
                    outline = false;
                    under = true;

                    layerOffset = -0.01f;

                    moves.add(new PartMove(PartProgress.warmup.inv(), 0f, -4f, 0f));
                }});
            }};
        }};
    }
}