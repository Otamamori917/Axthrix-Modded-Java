package axthrix.content.blocks;

import arc.graphics.Color;
import arc.struct.Seq;
import arc.util.*;
import axthrix.AxthrixLoader;
import axthrix.content.AxthrixStatus;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.types.block.PayloadAmmoBlock;
import axthrix.world.types.bulletypes.AfterShockBulletType;
import axthrix.world.types.bulletypes.InfFragBulletType;
import axthrix.world.util.AxUtil;
import blackhole.entities.bullet.BlackHoleBulletType;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;

public class PayloadAmmoBlocks {
    public static PayloadAmmoBlock
    empty1mCaliber,
    //basics
    basic1mCaliber, // starting solid round
    arcane1mCaliber,// Releases a surge of arcane energy upon impact. starting energy round
    //solids
    frostbite1mCaliber,// Freezes targets upon impact.
    incendiary1mCaliber,// Ignites everything in the vicinity upon detonation.
    quicksilver1mCaliber,// Penetrates through armor with ease, causing debilitating effects.
    //energy
    void1mCaliber,// Dark energy implosion effect upon impact.
    sonicwave1mCaliber,// Emits a powerful sonic wave upon impact, disorienting and deafening enemies.
    tempest1mCaliber,// Creates a burst of lightning

    funniBullet; //ohno


    public static void load(){
        empty1mCaliber = new PayloadAmmoBlock("empty-1m-caliber"){{
            localizedName = "Empty 1m round";
            description = """ 
                          An empty 1m round.
                          """;
            buildCost(
                Items.copper, 40,
                Items.lead, 20
            );

            size = 2;
            powerUse = 0.5f;
            constructTime = 60f * 2f;
            elevation = 2f / 3f;
            outlineIcon = true;
            outlinedIcon = 0;

            explosionArea = 0;
            explosions = 0;
            explosionBullet = new ExplosionBulletType(0, 0);

        }};
        //basic rounds
        basic1mCaliber = new PayloadAmmoBlock("basic-1m-caliber"){{
            localizedName = "1m Round";
            description = """
                          An 1m round.
                          load into payload turrets to fire.
                          """;
            buildCost(
                Items.copper, 10,
                Items.graphite, 10
            );

            prev = empty1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

            explosionArea = 40;
            explosionBullet = new BasicBulletType(){{
                var CoLor = Items.copper.color;
                lifetime = 0;
                speed = 0f;
                knockback = pierceCap = 4;
                damage = 200;
                splashDamageRadius = 40;
                splashDamage = 200;
                impact = pierce = pierceBuilding = true;
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
                    lifetime = AxUtil.GetRange(6,5);
                    speed = 6f;
                    knockback = pierceCap = 4;
                    splashDamageRadius = explodeRange = 40;
                    splashDamage = 100;
                    scaledSplashDamage = true;
                    impact = pierce = pierceBuilding = true;
                    collidesAir = true;
                    collidesGround = true;
                    trailLength = 40;
                    trailWidth = 2;
                    trailColor = lightColor = backColor = CoLor;
                    trailRotation = true;
                }};
            }};
        }};
        arcane1mCaliber = new PayloadAmmoBlock("arcane-1m-caliber"){{
            localizedName = "1m Arcane Round";
            description = """ 
                          An 1m Arcane round, creates a blast of energy on impact.
                          load into payload turrets to fire.
                          """;
            buildCost(
                    Items.copper, 10,
                    Items.phaseFabric, 10
            );

            prev = empty1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

            explosionArea = 0;
            explosionBullet = new BasicBulletType(){{
                var CoLor = Items.phaseFabric.color;
                lifetime = 0;
                speed = 0f;
                knockback = pierceCap = 4;
                damage = 600;
                impact = pierce = pierceBuilding = true;
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
            }};
        }};

        //solid rounds
        frostbite1mCaliber = new PayloadAmmoBlock("frost-1m-caliber"){{
            localizedName = "1m Frost Round";
            description = """ 
                          An 1m Frost round, creates an area of ice that slows targets.
                          load into payload turrets to fire.
                          """;
            buildCost(
                    Items.titanium, 20,
                    Items.graphite, 10
            );

            prev = basic1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

            explosionArea = 60;
            explosionBullet = new BasicBulletType(){{
                var CoLor = Items.titanium.color;
                speed = 0;
                lifetime = 0;
                knockback = pierceCap = 4;
                damage = 200;
                splashDamageRadius = 60;
                splashDamage = 200;
                impact = pierce = pierceBuilding = true;
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
                    speed = 0;
                    lifetime = 0;
                    splashDelay = 20;
                    splashAmount = 6;
                    status = StatusEffects.freezing;
                    statusDuration = 80;
                    frontColor = CoLor.cpy().a(0.4f);
                    particleColor = bottomColor = backColor = CoLor;
                }};
            }};
        }};
        incendiary1mCaliber = new PayloadAmmoBlock("incin-1m-caliber"){{
            localizedName = "1m Incendiary Round";
            description = """ 
                          An 1m Incendiary round, heat up targets and melts their armor.
                          load into payload turrets to fire.
                          """;
            details = """ 
                      Why is it [orange]Spicy[]
                      Enemy spotted in that direction
                      [orange]Spicy bullet[]: Got it burning that direction
                      """;
            buildCost(
                    Items.pyratite, 20,
                    Items.graphite, 10
            );

            prev = basic1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

            explosionArea = 0;
            explosionBullet = new BasicBulletType(){{
                var CoLor = Items.pyratite.color;
                lifetime = AxUtil.GetRange(20f,100);
                speed = 20f;
                knockback = pierceCap = 4;
                damage = 400;
                impact = pierce = pierceBuilding = true;
                collidesAir = true;
                collidesGround = true;
                trailInterval = 0;
                trailChance = Integer.MAX_VALUE;
                bulletInterval = 0.5f;
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
                        ammoMultiplier = 3f;
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
            }};
        }};
        quicksilver1mCaliber = new PayloadAmmoBlock("silver-1m-caliber"){{
            localizedName = "1m QuickSilver Round";
            description = """ 
                          An 1m QuickSilver round, a fast moving bullet with high penetration.
                          load into payload turrets to fire.
                          """;
            buildCost(
                    Items.surgeAlloy, 4,
                    Items.plastanium, 5
            );

            prev = basic1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

            explosionArea = 0;
            explosionBullet = new BasicBulletType(){{
                var CoLor = Color.valueOf("8b8696");
                lifetime = AxUtil.GetRange(40f,100);
                speed = 40f;
                knockback = 4;
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
            }};
        }};

        //energy rounds
        void1mCaliber = new PayloadAmmoBlock("void-1m-caliber"){{
            localizedName = "1m Void Round";
            description = """ 
                          An 1m Void round, Implodes and drags nearby targets into its point of detonation.
                          load into payload turrets to fire.
                          """;
            buildCost(
                    Items.thorium, 400,
                    Items.phaseFabric, 10
            );

            prev = arcane1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;
            explosionArea = 0;
            explosionBullet = new BasicBulletType(){{
                var CoLor = Color.valueOf("7b1c9b");
                lifetime = 0;
                speed = 0f;
                damage = 600;
                impact = true;
                collidesAir = true;
                collidesGround = true;
                trailInterval = 0;
                trailChance = Integer.MAX_VALUE;
                trailLength = 40;
                trailWidth = 2;
                trailColor = lightColor = backColor = CoLor;
                trailEffect = AxthrixFfx.energyRoundRadiate(CoLor);
                trailRotation = true;
                fragBullets = 1;
                fragVelocityMax = 0;
                fragBullet = new BlackHoleBulletType(){{
                    lifetime = 400;
                    speed = 0;
                    damage = 200;
                    horizonRadius = 10;
                    lensingRadius = 240;
                    damageRadius = 120;
                    suctionRadius = 480;
                    color = CoLor;
                    scaledForce = 4800;
                    force = 80;
                }};
            }};
        }};
        sonicwave1mCaliber = new PayloadAmmoBlock("sonic-1m-caliber"){{
            localizedName = "1m Sonic Round";
            description = """ 
                          An 1m Sonic round, Explodes and launches nearby targets away.
                          load into payload turrets to fire.
                          """;
            buildCost(
                    Items.metaglass, 100,
                    Items.phaseFabric, 10
            );

            prev = arcane1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;

        }};
        tempest1mCaliber = new PayloadAmmoBlock("tempest-1m-caliber"){{
            localizedName = "1m Tempest Round";
            description = """ 
                          An 1m Tempest round, creates a burst of lightning at point of detonation.
                          load into payload turrets to fire.
                          """;
            buildCost(
                    Items.surgeAlloy, 40,
                    Items.phaseFabric, 20
            );

            prev = arcane1mCaliber;
            size = 2;
            powerUse = 1f;
            constructTime = 60f * 4f;

            elevation = 2f / 3f;
            outlined = true;
        }};
        //ohno
        funniBullet = new PayloadAmmoBlock("funni"){{
            localizedName = "Funni Bullet";
            description = """ 
                          Needs to be enabled in properties.
                          """;
            buildCost(
                    Items.copper, 40,
                    Items.lead, 20
            );

            size = 2;
            powerUse = 0.5f;
            constructTime = 60f * 2f;
            elevation = 2f / 3f;
            outlineIcon = true;
            outlinedIcon = 0;

            if(AxthrixLoader.funibullet){
                explosionBullet = new InfFragBulletType(){{
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
                    allowKillShooter = true;
                }};
            }else {
                explosionArea = 100;
                explosions = 20;
                explosionBullet = new ExplosionBulletType(20000, 100);
            }
        }};
    }
}
