package axthrix.content.units;

import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.content.AxFactions;
import axthrix.content.AxLiquids;
import axthrix.content.AxthrixSounds;
import axthrix.content.AxthrixStatus;
import axthrix.content.FX.AxthrixFfx;
import axthrix.content.FX.AxthrixFx;
import axthrix.world.types.bulletypes.bulletpatterntypes.SpiralPattern;
import axthrix.world.types.entities.CptrUnitEntity;
import axthrix.world.types.parts.LightningPart;
import axthrix.world.types.parts.Propeller;
import axthrix.world.types.unittypes.*;
import axthrix.world.types.weapontypes.AcceleratedWeapon;
import axthrix.world.types.weapontypes.RevolverWeapon;
import axthrix.world.types.weapontypes.WeaponHelix;
import axthrix.world.util.AxUtil;
import blackhole.entities.part.BlackHolePart;

import static arc.math.Interp.pow10Out;
import static blackhole.utils.BlackHoleUtils.immuneUnits;

import mindustry.entities.abilities.*;
import axthrix.world.types.abilities.*;
import axthrix.world.types.bulletypes.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.effect.ParticleEffect;
import mindustry.entities.part.*;

import mindustry.entities.pattern.ShootAlternate;
import axthrix.world.types.ai.*;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootSpread;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.PowerAmmoType;
import mindustry.type.unit.*;
import mindustry.content.*;
import arc.func.Prov;
import arc.graphics.Color;
import arc.struct.*;
import arc.struct.ObjectMap.Entry;
import mindustry.graphics.Pal;
import mindustry.type.ammo.ItemAmmoType;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.*;
import static mindustry.content.StatusEffects.*;

public class AxthrixUnits {
    public static UnitType
    //Axthrix  |6 trees|
        //Ground
            //Assault Hovers |SubAtomic|
                quark, electron, baryon, hadron, photon,

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
                amos,arvad,amasya,rahela,chavash,
                //TX
                atlas,
            //Elemental Shuttles | leader |
                aza,enzo,ashur,shiva,vishnu,
                //TX
                murugan,
            //Specialist Spiders | Revolver |
                sig,colt,caiber,magnum,siegfried;
    //Core Units |4 units|

    // Steal from UAW which stole from Progressed Material which stole from Endless Rusting which stole from Progressed Materials in the past which stole from BetaMindy
    private static final Entry<Class<? extends Entityc>, Prov<? extends Entityc>>[] types = new Entry[]{
            prov(CptrUnitEntity.class, CptrUnitEntity::new),
    };

    private static final ObjectIntMap<Class<? extends Entityc>> idMap = new ObjectIntMap<>();

    /**
     * Internal function to flatmap {@code Class -> Prov} into an {@link Entry}.
     * @author GlennFolker
     */
    private static <T extends Entityc> Entry<Class<T>, Prov<T>> prov(Class<T> type, Prov<T> prov) {
        Entry<Class<T>, Prov<T>> entry = new Entry<>();
        entry.key = type;
        entry.value = prov;
        return entry;
    }

    /**
     * Setups all entity IDs and maps them into {@link EntityMapping}.
     * <p>
     * Put this inside load()
     * </p>
     * @author GlennFolker
     */
    private static void setupID() {
        for (
                int i = 0,
                j = 0,
                len = EntityMapping.idMap.length;
                i < len;
                i++
        ) {
            if (EntityMapping.idMap[i] == null) {
                idMap.put(types[j].key, i);
                EntityMapping.idMap[i] = types[j].value;
                if (++j >= types.length) break;
            }
        }
    }

    /**
     * Retrieves the class ID for a certain entity type.
     * @author GlennFolker
     */
    public static <T extends Entityc> int classID(Class<T> type) {
        return idMap.get(type, -1);
    }

    public static void load(){
        setupID();
        quark = new AxUnitType("quark") {{
            localizedName = "[orange]Quark";
            factions.add(AxFactions.axthrix);
            description = """
                          [orange]A little nant, the Quark is an agile hover.
                          Quark Fires A Atomic Tri-helix.
                          """;
            constructor = ElevationMoveUnit::create;
            ammoType = new PowerAmmoType(10);
            flying = false;
            speed = 8.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 275;
            armor = 3;
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

            weapons.add(new WeaponHelix(){{
                mirror = false;
                minWarmup = 0.8f;
                x = 0;
                y = 0;
                reload = 60f/0.8f;
                shootY = 2f;
                inaccuracy = 0;
                shoot = new SpiralPattern(2f, 1){{
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
                    shootSound = Sounds.blaster;
                    soundPitchMax = soundPitchMin = 6;

                }};
            }});
        }};
        electron = new AxUnitType("electron") {{
            localizedName = "[orange]Electron";
            factions.add(AxFactions.axthrix);
            description = """
                          [orange]A Fast Attacker, Electron always has the first strike.
                          Electron Fires a burst of super fast charged Particles.
                          """;
            constructor = ElevationMoveUnit::create;
            ammoType = new PowerAmmoType(15);
            flying = false;
            speed = 7.9f/7.5f;
            drag = 0.13f;
            hitSize = 18f;
            health = 890;
            armor = 5;
            accel = 0.8f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            parts.add(
                    new RegionPart("-arm"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2;
                        moveX = -2;
                        moveRot = 5;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 22f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 40;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1.2f;
                        radius = 20f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new RegionPart("-piston") {{
                        progress = p -> Mathf.cos(Time.time / 12) / 2 + 0.2f;
                        mirror = true;
                        x = -0.03f;
                        y = -0.03f;
                        moveY = 1f;
                        moveX = 1f;
                        moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                    }}

            );

            weapons.add(new Weapon(){{
                mirror = false;
                minWarmup = 0.8f;
                reload = 60f/0.4f;
                x = 0;
                y = 0;
                shootY = 2f;
                inaccuracy = 20;
                shoot.shots = 5;
                shoot.shotDelay = 2;
                immunities.add(shocked);
                bullet = new BasicBulletType(24f, 80){{
                    width = 4;
                    height = 4;
                    lifetime = 14;
                    status = shocked;
                    statusDuration = 180;
                    keepVelocity = true;
                    homingRange = 80;
                    homingPower = 50;
                    homingDelay = 1;
                    weaveRandom = true;
                    weaveMag = 5;
                    weaveScale = 5;
                    trailColor = backColor = lightColor = Pal.techBlue;
                    frontColor = Pal.techBlue;
                    trailLength = 12;
                    trailChance = 0f;
                    trailWidth = 0.7f;
                    despawnEffect = hitEffect = Fx.none;
                    shootSound = Sounds.cannon;
                    soundPitchMax = 14;
                    soundPitchMin = 8;
                    intervalBullet = new LightningBulletType(){{
                        damage = 12;
                        collidesAir = true;
                        ammoMultiplier = 1f;
                        lightningColor = Pal.techBlue;
                        lightningLength = 2;
                        lightningLengthRand = 8;

                        lightningType = new BulletType(0.0001f, 12f) {{
                            lifetime = Fx.lightning.lifetime;
                            hitEffect = Fx.hitLancer;
                            despawnEffect = Fx.none;
                            status = StatusEffects.shocked;
                            statusDuration = 10f;
                            hittable = false;
                            lightColor = Color.white;
                        }};
                    }};

                    bulletInterval = 1f;
                    parts.add(
                            new ShapePart(){{
                                color = Pal.techBlue;
                                sides = 10;
                                hollow = true;
                                stroke = 0.8f;
                                radius = 2f;
                                layer = Layer.effect;
                                y = 0;
                                x = 0;
                            }}
                    );
                }};
            }});
        }};
        baryon = new AxUnitType("baryon") {{
            localizedName = "[orange]Baryon";
            factions.add(AxFactions.axthrix);
            description = """
                          [orange]A Large Brawler,Baryon Has 2 pistons on its back that help pump energy and expel heat.
                          Baryon uses this to expel large amounts of heat damaging any foe that comes too close.
                          Baryon Fires A Large Atomic Tri-helix, That Explodes Violently On Contact.
                          """;
            constructor = ElevationMoveUnit::create;
            ammoType = new PowerAmmoType(30);
            flying = false;
            speed = 6.3f/7.5f;
            drag = 0.13f;
            hitSize = 24f;
            health = 3476;
            armor = 10;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            abilities.add(new HeatWaveAbility(600,80,600,Color.valueOf("de9458")));
            //hover/mechanical parts
            parts.add(
                    new RegionPart("-pin"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -1;
                        moveX = 1;
                    }},
                    new RegionPart("-plate"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2.2f;
                        moveX = -2;
                        moveRot = 10;
                    }},
                    new RegionPart("-piston") {{
                        progress = p -> Mathf.cos(Time.time / 24) / 2 + 0.2f;
                        mirror = true;
                        x = 0.5f;
                        y = 0.5f;
                        moveY = 1f;
                        moveX = 1f;
                        moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 28f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }}
            );
            // halo/atomic presence
            parts.add(
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 8;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1.2f;
                        radius = 25f;
                        layer = Layer.effect;
                        rotateSpeed = 2;
                        y = 0;
                        x = 0;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 40;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1.2f;
                        radius = 32f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 6;
                        hollow = true;
                        shapes = 8;
                        stroke = 0.2f;
                        strokeTo = 0.8f;
                        radius = 4f;
                        haloRadius = 28f;
                        haloRotateSpeed = 2f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }}
            );

            weapons.add(new WeaponHelix(){{
                mirror = false;
                minWarmup = 0.8f;
                x = 0;
                y = 0;
                reload = 180f;
                shootY = 2f;
                inaccuracy = 0;
                shoot = new SpiralPattern(4f, 2){{
                    shots = 3;
                }};

                immunities.add(StatusEffects.blasted);
                bullet = new BasicBulletType(3f, 20){{
                    width = 4;
                    height = 4;
                    lifetime = 160;
                    keepVelocity = false;
                    trailColor = backColor = lightColor = Color.valueOf("683b3d");
                    frontColor = Color.valueOf("de9458");
                    despawnEffect = hitEffect = Fx.none;
                    hitEffect = new MultiEffect(Fx.titanExplosion, Fx.titanSmoke);
                    despawnEffect = Fx.none;
                    knockback = 2f;
                    splashDamageRadius = 65f;
                    splashDamage = 120f;
                    scaledSplashDamage = true;
                    backColor = hitColor = trailColor = Color.valueOf("ea8878").lerp(Pal.redLight, 0.5f);
                    frontColor = Color.white;
                    hitSound = Sounds.titanExplosion;

                    status = StatusEffects.blasted;

                    trailLength = 16;
                    trailWidth = 3.35f;
                    trailSinScl = 2.5f;
                    trailSinMag = 0.5f;
                    trailEffect = Fx.none;
                    despawnShake = 7f;

                    shootEffect = Fx.shootTitan;
                    smokeEffect = Fx.shootSmokeTitan;
                    shootSound = Sounds.corexplode;
                    soundPitchMax = soundPitchMin = 3;


                    trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
                    parts.add(
                            new ShapePart(){{
                                progress = PartProgress.warmup.delay(0.6f);
                                weaponIndex = 0;
                                color = Color.valueOf("de9458");
                                sides = 8;
                                hollow = true;
                                stroke = 1.2f;
                                radius = 4f;
                                layer = Layer.effect;
                                rotateSpeed = 2;
                                y = 0;
                                x = 0;
                            }},
                            new ShapePart(){{
                                progress = PartProgress.warmup.delay(0.6f);
                                weaponIndex = 0;
                                color = Color.valueOf("de9458");
                                sides = 40;
                                hollow = true;
                                stroke = 1.2f;
                                radius = 6f;
                                layer = Layer.effect;
                                y = 0;
                                x = 0;
                            }},
                            new HaloPart(){{
                                progress = PartProgress.warmup.delay(0.6f);
                                weaponIndex = 0;
                                color = Color.valueOf("de9458");
                                sides = 6;
                                hollow = true;
                                shapes = 4;
                                stroke = 0.8f;
                                radius = 1f;
                                haloRadius = 5f;
                                haloRotateSpeed = 2.1f;
                                layer = Layer.effect;
                                y = 0;
                                x = 0;
                            }}
                    );
                }};
            }});
        }};
        hadron = new AxUnitType("hadron") {{
            localizedName = "[orange]Hadron";
            factions.add(AxFactions.axthrix);
            description = """
                          [orange]An Area defender, Hadron can lay mines behind enemy defences.
                          Hadron stores up heat and releases it at enemy units.
                          Hadron Fires A Large Artillery HeatMine, The mines expel heat at enemy units then decays into 4 homing particles.
                          has 2 automatic assault railguns that slow targets and deals more damage to stuctures.
                          """;
            constructor = ElevationMoveUnit::create;
            ammoType = new ItemAmmoType(Items.silicon);
            flying = false;
            speed = 5.6f/7.5f;
            drag = 0.13f;
            hitSize = 34f;
            health = 13200;
            armor = 24;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            abilities.add(new HeatWaveAbility(600,160,800,Color.valueOf("de9458")));
            //hover/mechanical parts
            parts.addAll(
                    new RegionPart("-pin"){{
                        progress = PartProgress.warmup;
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -1;
                        moveX = -1;
                    }},
                    new RegionPart("-plate"){{
                        progress = PartProgress.warmup.delay(0.3f);
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2.2f;
                        moveX = -2;
                        moveRot = 10;
                    }},
                    new RegionPart("-arm"){{
                        progress = PartProgress.warmup.delay(0.6f);
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = 2;
                        moveX = -2;
                        moveRot = 5;
                    }},
                    new RegionPart("-side-piston") {{
                        progress = p -> Mathf.cos(Time.time / 20) / 2 + 0.2f;
                        mirror = true;
                        x = 0.5f;
                        y = 0.5f;
                        moveY = 1f;
                        moveX = 1f;
                        moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                    }},
                    new RegionPart("-rear-piston") {{
                        progress = p -> Mathf.cos(Time.time / 24) / 2 + 0.2f;
                        mirror = true;
                        x = 0.5f;
                        y = 0.5f;
                        moveY = 1f;
                        moveX = 1f;
                        moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                    }},
                    new RegionPart("-glow"){{
                        color = Color.valueOf("de9458");
                        blending = Blending.additive;
                        outline = mirror = false;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 38f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }}
            );
            // halo/atomic presence
            parts.add(

                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 40;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1.2f;
                        radius = 30f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 40;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1.2f;
                        radius = 35f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 40;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1.2f;
                        radius = 40f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }}
            );

            weapons.add(new Weapon(){{
                mirror = false;
                minWarmup = 0.8f;
                x = 0;
                y = 0;
                reload = 100f;
                shootY = 2f;
                inaccuracy = 0;
                bullet = new ArtilleryBulletType(5f, 0){{
                    width = 8;
                    height = 8;
                    lifetime = 109;
                    scaleLife = true;
                    keepVelocity = false;
                    collidesAir = true;
                    trailColor = backColor = lightColor = Color.valueOf("683b3d");
                    frontColor = Color.valueOf("de9458");
                    despawnEffect = hitEffect = Fx.none;
                    weaveMag = 2;
                    weaveScale = 4;
                    knockback = 4f;
                    backColor = hitColor = trailColor = Color.valueOf("ea8878").lerp(Pal.redLight, 0.5f);
                    frontColor = Color.white;
                    hitSound = Sounds.none;

                    trailLength = 16;
                    trailWidth = 5.35f;
                    trailSinScl = 2.5f;
                    trailSinMag = 0.5f;
                    trailEffect = Fx.none;
                    despawnShake = 7f;

                    shootEffect = Fx.shootTitan;
                    smokeEffect = Fx.shootSmokeTitan;
                    shootSound = Sounds.largeExplosion;
                    soundPitchMax = soundPitchMin = 3;

                    trailInterp = v -> Math.max(Mathf.slope(v), 0.8f);
                    parts.add(
                            new ShapePart(){{
                                progress = PartProgress.warmup.delay(0.6f);
                                weaponIndex = 0;
                                color = Color.valueOf("de9458");
                                sides = 40;
                                hollow = true;
                                stroke = 1.2f;
                                radius = 8f;
                                layer = Layer.effect;
                                y = 0;
                                x = 0;
                            }},
                            new ShapePart(){{
                                progress = PartProgress.warmup.delay(0.6f);
                                weaponIndex = 0;
                                color = Color.valueOf("de9458");
                                sides = 40;
                                hollow = true;
                                stroke = 1.2f;
                                radius = 10f;
                                layer = Layer.effect;
                                y = 0;
                                x = 0;
                            }},
                            new ShapePart(){{
                                progress = PartProgress.warmup.delay(0.6f);
                                weaponIndex = 0;
                                color = Color.valueOf("de9458");
                                sides = 40;
                                hollow = true;
                                stroke = 1.2f;
                                radius = 12f;
                                layer = Layer.effect;
                                y = 0;
                                x = 0;
                            }}

                    );
                    fragBullets = 1;
                    fragAngle = 0;
                    fragSpread = 0;
                    fragRandomSpread = 0;
                    fragBullet = new BasicBulletType(){{
                        ammoMultiplier = 5;
                        speed = 0f;
                        keepVelocity = false;
                        collidesAir = false;
                        spawnUnit = new MissileUnitType("sub-bullet"){{
                            abilities.add(new HeatWaveAbility(12,80,80,Color.valueOf("de9458")));
                            rotateSpeed = 0;
                            speed = 0.01f;
                            lifetime = 80;
                            engineSize = 0;
                            health = 1;
                            loopSoundVolume = 0.1f;
                            deathExplosionEffect = despawnEffect = Fx.none;
                            deathSound = despawnSound = Sounds.none;
                            hittable = false;
                            targetable = false;
                            parts.add(

                                    new ShapePart(){{
                                        progress = PartProgress.warmup.delay(0.6f);
                                        weaponIndex = 0;
                                        color = Color.valueOf("de9458");
                                        sides = 40;
                                        hollow = false;
                                        stroke = 1.2f;
                                        radius = 2f;
                                        layer = Layer.effect;
                                        y = 0;
                                        x = 0;
                                    }},
                                    new ShapePart(){{
                                        progress = PartProgress.warmup.delay(0.6f);
                                        weaponIndex = 0;
                                        color = Color.valueOf("de9458");
                                        sides = 40;
                                        hollow = true;
                                        stroke = 1.2f;
                                        radius = 4f;
                                        layer = Layer.effect;
                                        y = 0;
                                        x = 0;
                                    }},
                                    new ShapePart(){{
                                        progress = PartProgress.warmup.delay(0.6f);
                                        weaponIndex = 0;
                                        color = Color.valueOf("de9458");
                                        sides = 40;
                                        hollow = true;
                                        stroke = 1.2f;
                                        radius = 6;
                                        layer = Layer.effect;
                                        y = 0;
                                        x = 0;
                                    }}

                            );

                            weapons.add(new Weapon(){{
                                shootCone = 360;
                                mirror = false;
                                reload = 1f;
                                shootOnDeath = true;
                                noAttack = true;
                                bullet = new BulletType(0f, 0){{
                                    killShooter = true;
                                    absorbable = false;
                                    laserAbsorb = false;
                                    hittable = false;
                                    reflectable = false;
                                    collides = false;
                                    instantDisappear = true;
                                    hitEffect = despawnEffect = shootEffect = Fx.none;
                                    lifetime = 1;
                                    fragBullets = 4;
                                    fragAngle = 0;
                                    fragSpread = 45;
                                    fragRandomSpread = 0;
                                    fragBullet = new BasicBulletType(6f, 200){{
                                        buildingDamageMultiplier = 3;
                                        width = 8;
                                        height = 8;
                                        lifetime = 60;
                                        keepVelocity = true;
                                        homingRange = 100;
                                        homingPower = 10;
                                        homingDelay = 1;
                                        weaveMag = 2;
                                        weaveScale = 4;
                                        trailColor = backColor = lightColor = Color.valueOf("de9458");
                                        frontColor = Color.valueOf("de9458");
                                        trailLength = 12;
                                        trailChance = 0f;
                                        trailWidth = 4f;
                                        despawnEffect = hitEffect = Fx.none;
                                        parts.add(
                                            new ShapePart(){{
                                                color = Color.valueOf("de9458");
                                                sides = 10;
                                                hollow = true;
                                                stroke = 0.8f;
                                                radius = 4f;
                                                layer = Layer.effect;
                                                y = 0;
                                                x = 0;
                                            }}
                                        );
                                    }};
                                }};
                            }});
                        }};
                    }};
                }};
            }});
            weapons.add(new Weapon("aj-assault-railgun"){{
                float brange = range = 400f;
                controllable = false;
                autoTarget = true;
                ignoreRotation = false;
                mirror = true;
                x = 12;
                y = 2;
                layerOffset = 1;
                shootY = 2f;
                inaccuracy = 0;
                rotate = true;
                rotateSpeed = 2f;
                reload = 80f;
                ejectEffect = Fx.casing3Double;
                recoil = 5f;
                cooldownTime = reload;
                shake = 2;
                shootCone = 2f;
                shootSound = Sounds.railgun;
                parts.add(
                    new RegionPart("-front"){{
                        progress = PartProgress.recoil;
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                        mirror = true;
                        under = false;
                        moveY = -2f;
                    }}
                );
                bullet = new RailBulletType(){{
                    buildingDamageMultiplier = 2;
                    shootEffect = AxthrixFx.instSmallShoot;
                    hitEffect = AxthrixFx.instSmallHit;
                    pierceEffect = AxthrixFx.railSmallHit;
                    smokeEffect = Fx.smokeCloud;
                    pointEffect = AxthrixFx.instSmallTrail;
                    despawnEffect = AxthrixFx.instSmallBomb;
                    pointEffectSpace = 10f;
                    damage = 200;
                    pierceDamageFactor = 1f;
                    length = brange;
                    hitShake = 1;
                    status = StatusEffects.slow;
                    statusDuration = 200;
                }};
            }});
        }};
        photon = new AxUnitType("photon") {{
            localizedName = "[orange]Photon";
            description = """
                          [white]The Light of the Gods,[orange] Photon has a Experimental light Cannon,  dubed "[white]Baldur[orange]".
                          Photon stores up heat and releases it at enemy units.
                          Photon hits its enemies with the light of the gods themselves, Leaving nothing left of the target.
                          Has 2 Large automatic assault railguns that slow targets and deals more damage to stuctures.
                          """;
            constructor = ElevationMoveUnit::create;
            factions.add(AxFactions.axthrix);
            ammoType = new PowerAmmoType(50);
            flying = false;
            speed = 4f/7.5f;
            drag = 0.13f;
            hitSize = 44f;
            health = 21300;
            armor = 14;
            range = 8 * 26;
            accel = 1f;
            rotateSpeed = 3.3f;
            faceTarget = true;
            hovering = true;
            abilities.add(new HeatWaveAbility(600,240,1000,Color.valueOf("de9458")));
            //hover/mechanical parts
            parts.addAll(
                    new RegionPart("-bar"){{
                        progress = PartProgress.warmup;
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = 1;
                        moveX = -0.5f;
                        moveRot = 6;
                        children.add(new RegionPart("-plate"){{
                            progress = PartProgress.warmup.delay(0.4f);
                            mirror = under = true;
                            weaponIndex = 0;
                            moveY = 1;
                            moveX = 1;
                            moveRot = 0;
                            children.add(new RegionPart("-shield"){{
                                progress = PartProgress.warmup.delay(0.8f);
                                mirror = under = true;
                                weaponIndex = 0;
                                moveY = 2;
                                moveX = -2f;
                            }});
                        }});
                    }},


                    new RegionPart("-front-piston") {{
                        progress = p -> Mathf.cos(Time.time / 20) / 2 + 0.2f;
                        mirror = true;
                        x = 0;
                        y = 0;
                        moveY = 1f;
                        moveX = 1f;
                        moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                    }},
                    new RegionPart("-rear-piston") {{
                        progress = p -> Mathf.cos(Time.time / 24) / 2 + 0.2f;
                        mirror = true;
                        x = 0;
                        y = 0;
                        moveY = 1f;
                        moveX = -1f;
                        moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, 0.5f, 0f));
                        heatProgress = PartProgress.recoil;
                        heatColor = Color.valueOf("de9458");
                    }},
                    new RegionPart("-glow"){{
                        color = Color.valueOf("de94580");
                        blending = Blending.additive;
                        outline = mirror = false;
                    }},
                    new HoverPart(){{
                        x = 0f;
                        y = 0f;
                        mirror = false;
                        radius = 48f;
                        phase = 60f;
                        stroke = 5f;
                        layerOffset = -0.05f;
                        color = Color.valueOf("de9458");
                    }}
            );
            // halo/atomic presence
            Color haloColor = Color.white;
            float haloRotSpeed = 1.5f;

            var circleProgress = DrawPart.PartProgress.warmup.delay(0.9f);
            float circleY = 10f, circleRad = 11f, circleRotSpeed = 3.5f, circleStroke = 0.8f;
            parts.addAll(
                    new ShapePart(){{
                        progress = circleProgress;
                        color = haloColor;
                        circle = true;
                        hollow = true;
                        stroke = 0.8f;
                        strokeTo = circleStroke;
                        radius = circleRad;
                        layer = Layer.effect;
                        y = circleY;
                    }},

                    new ShapePart(){{
                        progress = circleProgress;
                        rotateSpeed = -circleRotSpeed;
                        color = haloColor;
                        sides = 4;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = circleStroke;
                        radius = circleRad - 1f;
                        layer = Layer.effect;
                        y = circleY;
                    }},

                    //outer squares

                    new ShapePart(){{
                        progress = circleProgress;
                        rotateSpeed = -circleRotSpeed;
                        color = haloColor;
                        sides = 4;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = circleStroke;
                        radius = circleRad - 1f;
                        layer = Layer.effect;
                        y = circleY;
                    }},

                    //inner square
                    new ShapePart(){{
                        progress = circleProgress;
                        rotateSpeed = -circleRotSpeed/2f;
                        color = haloColor;
                        sides = 4;
                        hollow = true;
                        stroke = 0.4f;
                        strokeTo = 1f;
                        radius = 3f;
                        layer = Layer.effect;
                        y = circleY;
                    }},

                    //spikes on circle
                    new HaloPart(){{
                        progress = circleProgress;
                        color = haloColor;
                        tri = true;
                        shapes = 3;
                        triLength = 2f;
                        triLengthTo = 5f;
                        radius = 6.2f;
                        haloRadius = circleRad;
                        haloRotateSpeed = haloRotSpeed / 2f;
                        shapeRotation = 180f;
                        haloRotation = 180f;
                        layer = Layer.effect;
                        y = circleY;
                    }}
            );
            parts.addAll(
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 40;
                        hollow = true;
                        stroke = 0.8f;                    //Inner line
                        strokeTo = 1.2f;
                        radius = 45f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        color = Color.valueOf("de9458");
                        tri = true;
                        shapes = 6;
                        triLength = 2f;
                        triLengthTo = 5f;
                        radius = 8f;                     //inner line spikes
                        haloRadius = 45;
                        haloRotateSpeed = 2;
                        shapeRotation = 0f;
                        haloRotation = 0f;
                        layer = Layer.effect;
                        y = 0;
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 8;
                        hollow = true;
                        shapes = 12;
                        stroke = 0.4f;
                        strokeTo = 0.8f;           //middle shape
                        radius = 5.8f;
                        haloRadius = 49.8f;
                        haloRotateSpeed = 2f;
                        shapeRotation = 20f;
                        haloRotation = 45f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        color = Color.valueOf("de9458");
                        tri = true;
                        shapes = 6;
                        triLength = 2;
                        triLengthTo = 5f;
                        radius = 8f;                //outside inner facing spikes
                        haloRadius = 54.5f;
                        haloRotateSpeed = 2;
                        shapeRotation = 180f;
                        haloRotation = 90f;
                        layer = Layer.effect;
                        y = 0;
                    }},
                    new ShapePart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        weaponIndex = 0;
                        color = Color.valueOf("de9458");
                        sides = 55;
                        hollow = true;
                        stroke = 0.8f;           //outside line
                        strokeTo = 1.2f;
                        radius = 55f;
                        layer = Layer.effect;
                        y = 0;
                        x = 0;
                    }},
                    new HaloPart(){{
                        progress = PartProgress.warmup.delay(0.6f);
                        color = Color.valueOf("de9458");
                        tri = true;
                        shapes = 6;
                        triLength = 2f;
                        triLengthTo = 5f;
                        radius = 8f;                  //outside outward facing spikes
                        haloRadius = 55;
                        haloRotateSpeed = 2;
                        shapeRotation = 0f;
                        haloRotation = 90f;
                        layer = Layer.effect;
                        y = 0;
                    }}
            );

            weapons.add(new Weapon(){{
                x = 0;
                y = 0;
                //old photon weapon looked sick af but was too laggy remains here for now
                /*
                reload = 1f;
                inaccuracy = 20;
                bullet = new BasicBulletType(30, 15f){{
                    sprite = "missile-large";
                    lightColor = Color.white;
                    lightOpacity = 0.6f;
                    lightRadius = 15;

                    lifetime = 16f;
                    width = 4f;
                    height = 14f;
                    pierce = true;
                    pierceArmor = true;
                    pierceBuilding = true;
                    pierceCap = 20;
                    pierceDamageFactor = 0.8f;
                    laserAbsorb = true;

                    hitEffect = despawnEffect = AxthrixFfx.circleOut(4,30,10,Color.white);
                    knockback = 2f;
                    splashDamageRadius = 60f;
                    splashDamage = 50f;
                    scaledSplashDamage = true;

                    hittable = true;
                    keepVelocity = false;
                    reflectable = false;
                    shootEffect = Fx.shootSmokeSquareBig;
                    smokeEffect = Fx.shootSmokeDisperse;
                    hitColor = backColor = trailColor = Color.white;
                    frontColor = Color.white;
                    trailWidth = 8f;
                    trailLength = 40;
                    trailInterval = 0.01f;

                    trailEffect = AxthrixFfx.circleOut(6,10,5,Color.white);

                    homingPower = 1f;
                    homingDelay = 4f;
                    homingRange = 100;
                    collidesGround = true;
                    collidesAir = true;

                    shootSound = Sounds.plasmadrop;
                    soundPitchMax = 8;
                    soundPitchMin = 6;

                }};*/
                rotate = false;
                minWarmup = 0.8f;

                mirror = false;
                top = false;
                shake = 4.0F;
                shootY = -2;
                x = y = 0.0F;
                shoot.firstShotDelay = AxthrixFfx.LightPulse.lifetime - 1.0F;
                parentizeEffects = true;
                recoil = 0.0F;
                chargeSound = AxthrixSounds.SolarEmbrace;
                shootSound = AxthrixSounds.laserememit;
                continuous = true;
                cooldownTime = 200.0F;
                reload = 1000;


                bullet = new ContinuousFlameBulletType() {{
                    damage = 150.0F;
                    reflectable = false;
                    //lengthInterp = Interp.reverse;
                    lengthInterp = v -> (float) Math.sqrt((double)(1.0F - v * v * v));
                    lifetime = 900;
                    hitEffect = Fx.none;
                    drawFlare = false;
                    length = 250;
                    width = 15;
                    knockback = 1.0F;
                    pierceCap = 10;
                    layer = Layer.light;
                    colors = new Color[]{new Color(255F, 255F, 255F, 0.45F), new Color(255F, 255F, 255F, 0.1F), new Color(255F, 255F, 255F, 0.1F), new Color(255F, 255F, 255F, 0.05F), new Color(255F, 255F, 255F, 0.01F)};
                }};
            }},
            new Weapon(){{
                rotate = false;
                minWarmup = 0.8f;
                display = false;
                mirror = false;
                top = false;
                shake = 4.0F;
                shootY = 10.0F;
                x = y = 0.0F;
                shoot.firstShotDelay = AxthrixFfx.LightPulse.lifetime - 1.0F;
                parentizeEffects = true;
                recoil = 0.0F;
                continuous = true;
                cooldownTime = 200.0F;
                reload = 1000;
                shootSound = Sounds.none;

                bullet = new ContinuousFlameBulletType() {{
                    damage = 50F;
                    chargeEffect = AxthrixFfx.LightPulse;
                    lengthInterp = Interp.reverse;
                    lifetime = 900;
                    hitEffect = Fx.none;
                    drawFlare = false;
                    length = 250;
                    width = 0;
                    knockback = 0F;
                    pierceCap = 0;
                    layer = Layer.light;
                }};
            }});
            weapons.add(new Weapon("aj-large-assault-railgun"){{
                float brange = range = 500f;
                controllable = false;
                autoTarget = true;
                mirror = false;
                x = 12;
                y = -10;
                layerOffset = 1;
                shootY = 2f;
                inaccuracy = 0;
                rotate = true;
                rotateSpeed = 2f;
                reload = 40f;
                ejectEffect = Fx.casing3Double;
                recoil = 5f;
                cooldownTime = reload;
                shake = 2;
                shootCone = 2f;
                shootSound = Sounds.railgun;
                shoot = new ShootAlternate(7f);
                recoils =2;
                parts.add(
                        new RegionPart("-piston") {{
                            progress = p -> Mathf.cos(Time.time / 20) / 2 + 0.2f;
                            mirror = true;
                            x = 0.5f;
                            y = 0.5f;
                            moveY = 1f;
                            moveX = 1f;
                            moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                            heatProgress = PartProgress.recoil;
                            heatColor = Color.valueOf("de9458");
                        }}


                );
                for(int i = 0; i < 2; i ++){
                    int f = i;
                    parts.add(new RegionPart("-barrel-" + (i == 0 ? "l" : "r")){{
                        progress = PartProgress.recoil;
                        recoilIndex = f;
                        under = true;
                        moveY = -1.5f;
                    }});
                }

                bullet = new RailBulletType(){{
                    buildingDamageMultiplier = 2;
                    shootEffect = AxthrixFx.instSmallShoot;
                    hitEffect = AxthrixFx.instSmallHit;
                    pierceEffect = AxthrixFx.railSmallHit;
                    smokeEffect = Fx.smokeCloud;
                    pointEffect = AxthrixFx.instSmallTrail;
                    despawnEffect = AxthrixFx.instSmallBomb;
                    pointEffectSpace = 10f;
                    damage = 250;
                    pierceDamageFactor = 1f;
                    length = brange;
                    hitShake = 1;
                    status = StatusEffects.slow;
                    statusDuration = 300;
                }};
            }});
            weapons.add(new Weapon("aj-large-assault-railgun"){{
                float brange = range = 500f;
                controllable = false;
                autoTarget = true;
                mirror = false;
                x = -12;
                y = -10;
                layerOffset = 1;
                shootY = 2f;
                inaccuracy = 0;
                rotate = true;
                rotateSpeed = 2f;
                reload = 40f;
                ejectEffect = Fx.casing3Double;
                recoil = 5f;
                cooldownTime = reload;
                shake = 2;
                shootCone = 2f;
                shootSound = Sounds.railgun;
                shoot = new ShootAlternate(7f);
                recoils =2;
                parts.add(
                        new RegionPart("-piston") {{
                            progress = p -> Mathf.cos(Time.time / 20) / 2 + 0.2f;
                            mirror = true;
                            x = 0.5f;
                            y = 0.5f;
                            moveY = 1f;
                            moveX = 1f;
                            moves.add(new PartMove(PartProgress.recoil.inv(), -0.5f, -0.5f, 0f));
                            heatProgress = PartProgress.recoil;
                            heatColor = Color.valueOf("de9458");
                        }}


                );
                for(int i = 0; i < 2; i ++){
                    int f = i;
                    parts.add(new RegionPart("-barrel-" + (i == 0 ? "l" : "r")){{
                        progress = PartProgress.recoil;
                        recoilIndex = f;
                        under = true;
                        moveY = -1.5f;
                    }});
                }

                bullet = new RailBulletType(){{
                    buildingDamageMultiplier = 2;
                    shootEffect = AxthrixFx.instSmallShoot;
                    hitEffect = AxthrixFx.instSmallHit;
                    pierceEffect = AxthrixFx.railSmallHit;
                    smokeEffect = Fx.smokeCloud;
                    pointEffect = AxthrixFx.instSmallTrail;
                    despawnEffect = AxthrixFx.instSmallBomb;
                    pointEffectSpace = 10f;
                    damage = 250;
                    pierceDamageFactor = 1f;
                    length = brange;
                    hitShake = 1;
                    status = StatusEffects.slow;
                    statusDuration = 300;
                }};
            }});
        }};
        //support walkers
        barrier = new AxUnitType("barrier"){{
            localizedName = "[green]Barrier";
            description = """
                          [green].
                          Barrier Fires A single Nanobot that deals DOT.
                          """;
            ammoType = new ItemAmmoType(Items.silicon);
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
        }};

        blockade = new AxUnitType("blockade"){{
            localizedName = "[green]Blockade";
            description = """
                          [green]Blocks Damage and Deals Great Damage at Medium range.
                          Blockade Fires ForceFields in quick succession.
                          """;
            ammoType = new ItemAmmoType(Items.silicon);
            armor = 5f;
            speed = 0.7f;
            hitSize = 11f;
            health = 650;
            buildSpeed = 2f;
            canBoost = true;
            boostMultiplier = 1.5f;
            constructor = MechUnit::create;
            factions.add(AxFactions.axthrix);

            parts.add(
                    new LightningPart(){{
                        mirror = true;
                        x = 2;
                        x2 = 6;
                        y = -5;
                        y2 = 0;
                        spawnRate = 15;
                        layer = Layer.blockOver;
                        color = Color.white;
                        color2 = Pal.heal;
                    }}
            );

            weapons.add(new Weapon("aj-force-launcher"){{
                shootSound = Sounds.blaster;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                x = 6;
                y = 0;
                shootX = 2f;
                shootY = -1f;
                mirror = true;
                top = false;
                reload = 20;
                inaccuracy = 0;
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
                    moves.add(new PartMove(PartProgress.recoil, 0, 0f, -25f));
                }},
                new RegionPart("-bar"){{
                    progress = PartProgress.warmup;
                    heatColor = Pal.heal;
                    layerOffset = -0.5f;
                    mirror = false;
                    under = true;
                    moveX = 2f;
                }});


                bullet = new SheildArcBullet(60,"aj-blockade-shield"){{
                    lifetime = 140;
                    damage = 25;
                }};
            }});
        }};

        palisade = new AxUnitType("palisade"){{
            localizedName = "[green]Palisade";
            description = """
                          [green]A Nimble Walker Deals Heavy Damage at close quarters.
                          Protects Allies with 3 small but durable shield drones
                          Palisade Fires A Wave of Nanobots.[]
                          [#800000]Slows Down And Gives Great resistance To itself and allies when attacking.
                          """;
            ammoType = new ItemAmmoType(Items.silicon);
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

            abilities.add(
                    new DroneSpawnAbility(){{
                        spawnTime = 300;
                        dRot = 90;
                        dY = 20;
                        moveY = 10;
                        drone = AxthrixDrones.paliShield;
                    }},new DroneSpawnAbility(){{
                        droneSlot = 1;
                        spawnTime = 300;
                        dRot = 330;
                        dY = -10;
                        moveY = -5f;
                        dX = 17.32051f;
                        moveX = 8.66025f;
                        drone = AxthrixDrones.paliShield;
                    }},
                    new DroneSpawnAbility(){{
                        droneSlot = 2;
                        spawnTime = 300;
                        dRot = -150;
                        dY = -10;
                        moveY = -5f;
                        dX = -17.32051f;
                        moveX = -8.66025f;
                        drone = AxthrixDrones.paliShield;
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

                bullet = new BasicBulletType(4f, 40){{
                    homingRange = 40f;
                    homingPower = 4f;
                    homingDelay = 2f;
                    width = 0.5f;
                    height = 0.5f;
                    lifetime = 20;
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
            ammoType = new ItemAmmoType(Items.silicon);
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

            abilities.add(
                    new DroneSpawnAbility(){{
                        spawnTime = 350;
                        dRot = 45;
                        dY = dX = 17.5f;
                        moveY = moveX = 17.5f;
                        drone = AxthrixDrones.paliShield;
                    }},new DroneSpawnAbility(){{
                        droneSlot = 1;
                        spawnTime = 350;
                        dRot = 315;
                        dY = -17.5f;
                        dX = 17.5f;
                        moveY = -17.5f;
                        moveX = 17.5f;
                        drone = AxthrixDrones.paliShield;
                    }},
                    new DroneSpawnAbility(){{
                        droneSlot = 2;
                        spawnTime = 350;
                        dRot = 225;
                        dY = dX = -17.5f;
                        moveY = moveX = -17.5f;
                        drone = AxthrixDrones.paliShield;
                    }},
                    new DroneSpawnAbility(){{
                        droneSlot = 3;
                        spawnTime = 350;
                        dRot = 135;
                        dY = 17.5f;
                        dX = -17.5f;
                        moveY = 17.5f;
                        moveX = -17.5f;
                        drone = AxthrixDrones.paliShield;
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
                mirror = false;
                reload = 80;
                inaccuracy = 0;
                shoot.shots = 2;
                shoot.shotDelay = 10;
                parts.add(
                new LightningPart(){{
                    mirror = true;
                    x = 0;
                    x2 = 4;
                    y = -4.5f;
                    y2 = 2;
                    spawnRate = 20;
                    layerOffset = .3f;
                    color = Color.white;
                    color2 = Pal.heal;
                }},
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
                    }},
                    new RegionPart("-piston"){{
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
                bullet = new SheildArcBullet(120,"aj-blockade-shield"){{
                    lifetime = 140;
                    damage = 125;
                }};
            }},
            new Weapon("aj-burst"){{
                shootSound = Sounds.plasmaboom;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                top = true;
                layerOffset = 0.001f;
                x = -8.2f;
                y = -3f;
                rotate = true;
                mirror = false;
                reload = 80;
                inaccuracy = 0;
                shoot.shots = 2;
                shoot.shotDelay = 10;
                parts.add(
                new LightningPart(){{
                    mirror = true;
                    x = 0;
                    x2 = 4;
                    y = -4.5f;
                    y2 = 2;
                    spawnRate = 20;
                    layerOffset = .3f;
                    color = Color.white;
                    color2 = Pal.heal;
                }},
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
                    }},
                    new RegionPart("-piston"){{
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
                bullet = new SheildArcBullet(120,"aj-blockade-shield"){{
                    lifetime = 140;
                    damage = 125;
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
            ammoType = new ItemAmmoType(Items.silicon);
            ammoCapacity = 1000;
            armor = 25f;
            speed = 0.60f;
            health = 14460;
            hitSize = 28;
            buildSpeed = 4f;
            constructor = MechUnit::create;
            mechStepParticles = true;
            stepShake = 0.75f;
            drownTimeMultiplier = 6f;
            mechFrontSway = 1.9f;
            mechSideSway = 0.6f;
            factions.add(AxFactions.axthrix);
            immunities.add(AxthrixStatus.nanodiverge);
            immunities.add(AxthrixStatus.vindicationII);
            immunities.add(AxthrixStatus.vindicationI);
            abilities.add(new NanobotStormAbility());

            /*abilities.add(new ShieldArcAbility(){{
                region = "aj-impediment-shield";
                radius = 40f;
                angle = 100f;
                y = -22f;
                regen = 0.6f;
                cooldown = 200f;
                max = 1000f;
                width = 14f;
                whenShooting = false;
            }});*/
            abilities.add(new SStatusFieldAbility(AxthrixStatus.vindicationIII, 400f, 360f, 90){{
                onShoot = true;
            }});


            weapons.add(new Weapon("aj-force"){{
                shootSound = Sounds.shockBlast;
                shootWarmupSpeed = 0.06f;
                minWarmup = 0.9f;
                maxRange = AxUtil.GetRange(4,60);
                range = AxUtil.GetRange(4,20);
                top = false;
                x = 0;
                y = 0f;
                shootX = 16f;
                shootY = 6;
                mirror = true;
                alternate = true;
                reload = 35;
                recoil = 2;
                inaccuracy = 10;
                shoot = new ShootSpread(18, 2);
                parts.add(
                        new RegionPart("-mount"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.warmup.delay(0.6f);
                            heatColor = Pal.heal;
                            mirror = false;
                            under = true;
                            children.add(new RegionPart("-arm"){{
                                progress = PartProgress.warmup;
                                heatProgress = PartProgress.warmup.delay(0.6f);
                                heatColor = Pal.heal;
                                mirror = false;
                                under = false;
                                x = 0f;
                                moveX = 0;
                                moveY = 1f;
                                moveRot = 5f;
                                moves.add(new PartMove(PartProgress.recoil, 0f,  0, 0f));
                                children.add(new RegionPart("-plate"){{
                                    progress = PartProgress.warmup;
                                    heatProgress = PartProgress.warmup.delay(0.6f);
                                    heatColor = Pal.heal;
                                    mirror = false;
                                    under = true;
                                    moveY = 0f;
                                    moveX = -6.8f;
                                    moves.add(new PartMove(PartProgress.recoil, 4f, 1f, 4f));
                                    children.add(new RegionPart("-shell"){{
                                        progress = PartProgress.warmup;
                                        heatProgress = PartProgress.warmup.delay(0.6f);
                                        heatColor = Pal.heal;
                                        mirror = false;
                                        under = true;
                                        moveY = -1f;
                                        moveX = 0f;
                                        moveRot = -10f;
                                        moves.add(new PartMove(PartProgress.recoil, 0f, 0f, 10f));
                                    }},
                                    new RegionPart("-gen"){{
                                        progress = PartProgress.warmup;
                                        heatProgress = PartProgress.warmup.delay(0.6f);
                                        heatColor = Pal.heal;
                                        mirror = false;
                                        under = true;
                                        layerOffset = -0.5f;
                                        y = 0;
                                        x = -23;
                                        moveY = -4f;
                                        moveX = 4f;
                                        moveRot = -100f;
                                        moves.add(new PartMove(PartProgress.recoil, 0f, 0f, 100f));
                                    }},
                                    new RegionPart("-gen-attachment"){{
                                        progress = PartProgress.warmup;
                                        mirror = false;
                                        under = true;
                                        layerOffset = -0.6f;
                                        y = 0;
                                        x = -20.5f;
                                        moveY = -4f;
                                        moveX = 2f;
                                        moves.add(new PartMove(PartProgress.recoil, -4f, -1f, 0));
                                    }});
                                }});
                            }});
                        }});
                bullet = new SheildArcBullet(10,"aj-impediment-shield"){{
                        speed = 4;
                        lifetime = 50;
                        damage = 60;
                }};
            }});
        }};

        //special black hole tanks
        anagh = new AxUnitType("anagh") {{
            localizedName = "[purple]Anagh";
            description = """
                          Lead Engineers at [#de9458]Axthrix[] decided not to make anagh a weapon, but make anagh itself the weapon!
                          Anagh contains an Miniature Blackhole on its back which pulls in any unit or bullet close to it. 
                          get too close and it will rip nearby bullets, units, and buildings apart.
                          has a special field around Anagh preventing allies from being effected by its Blackhole.
                          """;
            treadPullOffset = 0;
            itemCapacity = 0;
            treadRects = new Rect[]{new Rect(12 - 32f, 8 - 32f, 11, 50)};
            factions.add(AxFactions.axthrix);
            immunities.add(AxthrixStatus.gravicalSlow);
            constructor = TankUnit::create;
            outlines = false;
            flying = false;
            speed = 5.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 1000;
            armor = 8;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = false;
            range = 120;
            abilities.add(new AttractionFieldAbility(){{
                damageRadius = 30;
                suctionRadius = 120;
                damage = 6f;
                bulletDamage = 4f;

                scaledBulletForce = 1;
                bulletForce = 0.5f;

                scaledForce = 300;
                force = 20;

            }});
            parts.add(

            new BlackHolePart(){{
                        growProgress = p -> Mathf.cos(Time.time / 16) / 2 + 0.2f;
                        x = 0;
                        y = -2;
                        size = 0.8f;
                        sizeTo = 1f;
                        edge = 5f;
                        edgeTo = 7f;
                        color = Color.purple;
                    }});

        }};
        akshaj = new AxUnitType("akshaj") {{
            localizedName = "[purple]Akshaj";
            description = """
                          
                          """;
            treadPullOffset = 5;
            itemCapacity = 0;
            treadRects = new Rect[]{new Rect(17 - 96f/2f, 10 - 96f/2f, 19, 76)};
            factions.add(AxFactions.axthrix);
            constructor = TankUnit::create;
            outlines = false;
            flying = false;
            speed = 5.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 2600;
            armor = 9;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = false;
            range = 160;
            weapons.add(new Weapon("gravic"){{
                shootSound = Sounds.none;
                shootCone = 360;
                ignoreRotation = true;
                chargeSound = Sounds.drillCharge;
                shootY = 0f;
                x = 0f;
                y = 0f;
                mirror = false;
                top = false;
                reload = 400;
                shoot.firstShotDelay = 180;
                bullet = new GravityWellBulletType(){{
                    scaleLife = false;
                    speed = 0;
                    lifetime = 1;
                    instantDisappear = true;
                    gravityRadius = 140;
                    gravityDuration = 300;
                    pullStrength = 0.05f;
                }};
            }});
            abilities.add(new AttractionFieldAbility(){{
                damageRadius = 60;
                suctionRadius = 160;
                damage = 8f;
                bulletDamage = 3f;
                scaledBulletForce = 1;
                bulletForce = 0.5f;
                scaledForce = 350;
                force = 25;

            }});
            parts.add(

                    new BlackHolePart(){{
                        growProgress = p -> Mathf.cos(Time.time / 16) / 2 + 0.2f;
                        x = 0;
                        y = -3;
                        size = 1f;
                        sizeTo = 1.8f;
                        edge = 6f;
                        edgeTo = 9.8f;
                        color = Color.purple;
                    }},
                     new BlackHolePart(){{
                        growProgress = PartProgress.charge;
                        x = 0;
                        y = -3;
                        size = edge = 0f;
                        sizeTo = 2.5f;
                        edgeTo = 28f;
                        color = Color.purple;
                    }});

        }};
        amitojas = new AxUnitType("amitojas") {{
            localizedName = "[purple]Amitojas";
            description = """
                          Lead Engineers at [#de9458]Axthrix[] decided not to make anagh a weapon, but make anagh itself the weapon!
                          Anagh contains an Miniature Blackhole on its back which pulls in any unit or bullet close to it. 
                          get too close and it will rip nearby bullets, units, and buildings apart.
                          has a special field around Anagh preventing allies from being effected by its Blackhole.
                          """;
            treadPullOffset = 0;
            itemCapacity = 0;
            treadRects = new Rect[]{new Rect(12 - 32f, 8 - 32f, 11, 50)};
            factions.add(AxFactions.axthrix);
            immunities.add(AxthrixStatus.gravicalSlow);
            constructor = TankUnit::create;
            outlines = false;
            flying = false;
            speed = 5.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 5000;
            armor = 10;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = false;
            range = 160;
            weapons.add(new Weapon("gravcal"){{
                shootSound = Sounds.none;
                shootY = 0f;
                x = 0f;
                y = 0f;
                mirror = false;
                rotate = true;
                top = false;
                reload = 150;
                bullet = new GravityWellBulletType(){{
                    scaleLife = true;
                    gravityRadius = 48;
                    pullStrength = 0.1f;
                }};
            }});
            abilities.add(
                    new AttractionFieldAbility(){{
                        whenShooting = true;
                        damageRadius = 60;
                        suctionRadius = 160;
                        damage = 4f;
                        bulletDamage = 3f;
                        scaledBulletForce = 1;
                        bulletForce = 0.5f;
                        scaledForce = 300;
                        force = 20;
                    }},
                    new AttractionFieldAbility(){{
                        whenNotShooting = true;
                        damageRadius = suctionRadius = 50;
                        repel = true;
                        damage = 2f;
                        bulletDamage = 6f;
                        scaledBulletForce = 5.5f;
                        bulletForce = 1.5f;
                        scaledForce = 400;
                        force = 30;
                    }});
            parts.add(

                    new BlackHolePart(){{
                        growProgress = PartProgress.recoil.add(p -> Mathf.cos(Time.time / 16) / 2 + 0.2f);
                        progress = PartProgress.warmup;
                        moveY = 6;
                        x = 0;
                        y = -3;
                        size = 1.4f;
                        sizeTo = 2.8f;
                        edge = 6.4f;
                        edgeTo = 12.8f;
                        color = Color.purple;
                    }});

        }};
        //assault helicopters
        rai = new CopterUnitType("rai") {{
            localizedName = "[orange]Rai";
            description = """
                          [orange]A Quick Helicopter, Rai is rather fast compared to other fliers but much less durable.
                          Rai Has A Coil Up Tesla Weapon Needs to Ramp up to top Firerate and Cooldown to stop firing.
                          """;
            itemCapacity = 0;
            factions.add(AxFactions.axthrix);
            health = 250;
            hitSize = 18;

            speed = 4.5f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;

            ammoType = new PowerAmmoType(10);

            circleTarget = true;
            lowAltitude = true;
            faceTarget = flying = true;

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;
            engineSize = 0;

            targetFlags = new BlockFlag[]{BlockFlag.extinguisher, BlockFlag.repair, null};

            constructor = CptrUnitEntity::new;
            aiController = DynFlyingAI::new;

            weapons.add(new AcceleratedWeapon(name + "-weapon"){{
                shootY = 6f;
                accelPerShot = 1.2f;
                accelCooldownWaitTime = 10;
                accelCooldownTime = 20;
                x = 0f;
                y = 0f;
                mirror = false;
                reload = 18;
                top = true;
                heatColor =  Color.orange;
                bullet = new LightningBulletType(){{
                    damage = 15;
                    lightningLength = 15;
                    lightningColor = Color.orange.cpy().add(Color.maroon);
                    collidesAir = true;
                    shootSound = Sounds.spark;
                    soundPitchMax = soundPitchMin = 1.2f;
                }};
            }});
            parts.add(new RegionPart("-blade"){{
                        mirror = under = true;
                        weaponIndex = 0;
                        moveY = -2.25f;
                        moveX = -2;
                    }});

            propeller.add(
                    new Propeller("aj-short-blade") {{
                        x = y = 0;
                        rotorSpeed = -30f;
                        bladeCount = 4;
                        rotorTopSizeScl = 0.8f;
                        rotorSizeScl = 1.4f;
                    }}
            );
        }};
        zyran = new CopterUnitType("zyran") {{
            localizedName = "[orange]Zyran";
            description = """
                          [orange]Fast As Thunder, Zyran Is a fast and agile Helicopter.
                          Releases Static Energy at enemy blocks with power, can recharge at ally Power Nodes, or over time.
                          """;
            itemCapacity = 0;
            factions.add(AxFactions.axthrix);
            health = 680;
            hitSize = 24;

            speed = 4.45f;
            accel = 0.07f;
            drag = 0.026f;
            rotateSpeed = 5.5f;
            abilities.add(
                    new StaticEMPability(){{
                        color = Color.orange.cpy().add(Color.maroon);
                        range = 120;
                        y = 0;
                        poweredCharge = 0.5f;
                        passiveCharge = 0.2f;
                    }},
                    new SStatusFieldAbility(AxthrixStatus.Lightning,380,120,120){{
                        activeEffect = AxthrixFfx.circleOut(30,120,3,Layer.blockOver,Color.orange.cpy().add(Color.maroon)).followParent(true);
                        effectY = 0;
                    }}
            );

            ammoType = new PowerAmmoType(10);

            circleTarget = true;
            lowAltitude = true;
            faceTarget = flying = true;

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;
            engineSize = 0;

            targetFlags = new BlockFlag[]{BlockFlag.battery, BlockFlag.generator, null};

            constructor = CptrUnitEntity::new;
            aiController = DynFlyingAI::new;

            weapons.add(new AcceleratedWeapon(name + "-weapon"){{
                shootY = 6f;
                accelPerShot = 1.5f;
                accelCooldownWaitTime = 130f;
                accelCooldownTime = 10;
                x = y = recoil = 0f;
                mirror = true;
                reload = 128;
                top = false;
                minWarmup = 0.58f;
                heatColor =  Color.orange;
                shootStatus = AxthrixStatus.Thundering;
                shootStatusDuration = 140;
                alternate = false;
                shoot = new ShootBarrel(){{
                    barrels = new float[]{
                            7f, 3f, -110,
                            8f, -3.5f, -100,
                    };
                    shotDelay = 65;
                    shots = 2;
                }};
                shootSound = Sounds.missileLaunch;
                bullet = new MissileBulletType(3.5f, 100, "circle-bullet"){{
                    shootEffect = Fx.sparkShoot;
                    smokeEffect = AxthrixFx.shootSmokeMiniTitan;
                    width = height = 6f;
                    trailWidth = 3f;
                    homingPower = 0.58f;
                    homingDelay = 10;
                    drag = -0.05f;
                    homingRange = AxUtil.GetRange(4.5f,40);
                    shrinkY = 0.6f;
                    shrinkX = 0.8f;
                    lifetime = 40;
                    velocityRnd = 0;
                    frontColor = hitColor = trailColor = backColor = Color.orange.cpy().add(Color.maroon);
                    keepVelocity = false;
                    hitEffect = Fx.hitLancer;
                    despawnEffect = Fx.none;
                    trailLength = 8;
                    trailEffect = new ParticleEffect(){{
                        lifetime = 45;
                        particles = 8;
                        length = 14;
                        baseLength = 2;
                        interp = pow10Out;
                        colorFrom = Color.valueOf("576A7399");
                        colorTo = Color.valueOf("576A734E");
                        sizeFrom = 0;
                        sizeTo = 3.5f;
                        layer = Layer.blockOver;
                    }};
                    hitSound = Sounds.none;

                    fragBullets =  1;
                    fragBullet = new LightningBulletType(){{
                        damage = 20;
                        homingPower = 0.1f;
                        lightningLength = 4;
                        lightningLengthRand = 0;
                        buildingDamageMultiplier = 1.1f;

                        status = StatusEffects.none;
                        hitEffect= shootEffect = Fx.hitLancer;
                        lightningColor = hitColor = Pal.surge;
                        lightningType = new BulletType(0.0001f, 10f){{
                            hittable = false;
                            hitEffect = Fx.hitLancer;
                            despawnEffect = Fx.none;
                            status = StatusEffects.none;
                            lifetime = Fx.lightning.lifetime;
                            lightningColor = hitColor =  Pal.surge;
                        }};
                    }};
                }};
                parts.add(
                new RegionPart("-crest"){{
                    mirror = false;
                    under = true;
                    progress = PartProgress.warmup;
                    moveRot = -10;
                    moveY = 0.5f;
                    moveX = 0.5f;
                }},
                new RegionPart("-plate"){{
                    mirror = false;
                    under = true;
                    progress = PartProgress.warmup;
                    moveRot = 10;
                    moveY = -0.5f;
                    moveX = 0.5f;
                }});
            }});

            propeller.add(
                    new Propeller("aj-short-blade") {{
                        x = 0;
                        y = 3f;
                        rotorSpeed = -45f;
                        bladeCount = 5;
                        rotorTopSizeScl = 1.2f;
                        rotorSizeScl = 1.8f;
                    }}
            );
        }};
        //support airships
        naji = new CopterUnitType("naji") {{
            localizedName = "[green]Naji";
            description = """
                          [green]A Slow Moving Airship, Naji Supports its allies With a healing burst.
                          Naji Deploys an Ivy Sentry that defends the area its in for a short while.
                          """;
            itemCapacity = 40;
            factions.add(AxFactions.axthrix);
            float unitRange = 28 * tilesize;
            health = 300;
            hitSize = 18;
            canHeal = true;

            speed = 2f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;
            buildSpeed = 2f;
            outlines = true;
            outlineRadius = 0;

            ammoType = new ItemAmmoType(Items.silicon, 10);

            circleTarget = false;
            lowAltitude = true;
            faceTarget = true;
            flying = true;
            range = unitRange;
            engineSize = 0;
            engines = Seq.with(new UnitEngine(0,-6.5f,4f,-90));

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;

            constructor = CptrUnitEntity::new;
            aiController = UnitHealerAi::new;


            abilities.add(new StatusFieldAbility(AxthrixStatus.repair,60,280,16*8){{
                activeEffect = AxthrixFfx.circleOut(40,16*8, 4,Layer.blockOver,Color.green);
            }});

            parts.add(new RegionPart("-lineout"){{
                mirror = false;
                under = true;
                outline = false;
                layerOffset = -0.1f;
            }});

            weapons.add(new Weapon(name+"-inferno"){{
                shootSound = Sounds.shootSnap;
                x = 8F;
                y = -0.5F;
                mirror = true;
                rotate = true;
                rotateSpeed = 0.4F;
                reload = 70.0F;
                layerOffset = -20.0F;
                recoil = 1.0F;
                rotationLimit = 22.0F;
                minWarmup = 0.95F;
                shootWarmupSpeed = 0.1F;
                shootY = 2.0F;
                shootCone =20.0F;
                inaccuracy = 14.0F;
                canHeal = true;
                parts.add(new RegionPart("-blade") {{
                    heatProgress = PartProgress.warmup;
                    progress = PartProgress.warmup.blend(PartProgress.reload, 0.15F);
                    heatColor = Color.valueOf("9c50ff");
                    x = 1.25F;
                    y = 0.0F;
                    moveRot = 10.0F;
                    moveY = -1.0F;
                    moveX = -1.0F;
                    under = true;
                    mirror = true;
                }});
                bullet = new LaserBoltBulletType(3f, 12.5f){{
                    shootSound = Sounds.pulseBlast;
                    soundPitchMax = soundPitchMin = 2;
                    trailLength = 4;
                    trailEffect = AxthrixFx.PlasmaFlame2;
                    trailInterval = 1;
                    scaleLife = true;
                    homingPower = 0.4f;
                    lifetime = 140f;
                    keepVelocity = false;
                    width = 1f;
                    height = 3.5f;
                    trailColor = backColor = Color.valueOf("4ea572");
                    frontColor = Color.white;
                    fragBullets = 1;
                    healAmount = 30;
                    healPercent = 1f;
                    fragBullet = new FirePuddleBulletType(15,20,true){{
                        specialEffect = AxthrixFx.PlasmaFlame1;
                        healAmount = 30;
                        healPercent = 1f;

                    }};

                }};

            }});
            float layerOffset = -0.01f;

            float rotX = 18f * 0.25f;
            float rotY = 0.25f;
            propeller.add(
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = -rotX;
                        y = rotY;
                        rotorSpeed = 32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 8;
                        rotorLayer = layerOffset;
                        rotorSizeScl = 0.6f;
                        rotorTopSizeScl = 0.9f;
                    }},
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = rotX;
                        y = rotY;
                        rotorSpeed = -32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 8;
                        rotorLayer = layerOffset;
                        rotorSizeScl = 0.6f;
                        rotorTopSizeScl = 0.9f;
                    }}
            );
        }};

        haven = new CopterUnitType("haven") {{
            localizedName = "[green]Haven";
            description = """
                          [green]A Slow Moving Airship, Naji Supports its allies With a healing burst.
                          Naji Deploys an Ivy Sentry that defends the area its in for a short while.
                          """;
            itemCapacity = 40;
            factions.add(AxFactions.axthrix);
            float unitRange = 28 * tilesize;
            health = 300;
            hitSize = 18;
            canHeal = true;

            speed = 2f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;
            buildSpeed = 2f;
            outlines = false;

            ammoType = new ItemAmmoType(Items.silicon, 10);

            circleTarget = false;
            lowAltitude = true;
            faceTarget = false;
            flying = true;
            range = unitRange;
            engineSize = 0;
            engines = Seq.with(new UnitEngine(0,-6.5f,2.5f,-90));

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;

            constructor = CptrUnitEntity::new;
            aiController = UnitHealerAi::new;

            abilities.add(new StatusFieldAbility(AxthrixStatus.repair,60,280,16*8){{
                activeEffect = AxthrixFfx.circleOut(40,16*8, 4,Layer.blockOver,Color.green);
            }});

            parts.add(new RegionPart("-lineout"){{
                mirror = false;
                under = true;
                outline = false;
                layerOffset = -0.1f;
            }});

            weapons.add(new Weapon(name+"-weapon") {{
                rotate = true;
                x = y = shootX = 0;
                shootY = 2;
                mirror = false;
                parentizeEffects = true;
                reload = 155.0F;
                recoil = 0.0F;
                shootSound = AxthrixSounds.laserwoosh;
                continuous = true;

                bullet = new ConeBulletType(60){{
                    length = 75;
                    lifetime = 140;
                    allyStatus = AxthrixStatus.repair;
                    allyStatusDuration = 60;
                    healAmount = 4;
                    healPercent = 4f;
                }};
            }});
            float layerOffset = -0.01f;

            float rotX = 17f * 0.25f;
            float rotY = 3f * 0.25f;
            propeller.add(
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = -rotX;
                        y = rotY;
                        rotorSpeed = 32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 8;
                        rotorLayer = layerOffset;
                        rotorSizeScl = 0.34f;
                        rotorTopSizeScl = 0.45f;
                    }},
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = rotX;
                        y = rotY;
                        rotorSpeed = -32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 8;
                        rotorLayer = layerOffset;
                        rotorSizeScl = 0.34f;
                        rotorTopSizeScl = 0.45f;
                    }}
            );
        }};
        //Special Flying Mount
        //amos,arvad,amasya,anuvaha,ambuvahini,
        amos = new MountUnitType("amos")
        {{
            localizedName = "Amos";
            description = """
                          Can pick up and use any 1x1 Turret.
                          Unit Item Storage will restock current attached turret.
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 600;
            armor = 2;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            speed = 2.5f;
            accel = 0.06f;
            drag = 0.017f;
            hitSize = 16.05f;
            itemCapacity = 300;
            itemOffsetY = 6;
            lowAltitude = true;
            pickupUnits = false;
            flying = true;
            payloadCapacity = tilePayload * ((2 * 2) - 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,4.5f,-90),
                    new UnitEngine(-10,-10,4.5f,180+45),
                    new UnitEngine(10,-10,4.5f,270+45)
            );
        }};
        arvad = new MountUnitType("arvad")
        {{
            localizedName = "arvad";
            description = """
                          Can pick up and use any 2x2 Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 1800;
            armor = 5;
            faceTarget = true;
            flying = true;
            factions.add(AxFactions.axthrix);
            speed = 2.5f;
            accel = 0.06f;
            drag = 0.017f;
            hitSize = 24.05f;
            itemCapacity = liquidCap = 600;
            itemOffsetY = 6;
            lowAltitude = true;
            pickupUnits = false;
            payloadCapacity = tilePayload * ((3 * 3) - 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,8,-90)//,
                    //new UnitEngine(-10,-12,10,180+45),
                    //new UnitEngine(10,-10,6,270+45)
            );
        }};
        amasya = new MountUnitType("amasya")
        {{
            localizedName = "amasya";
            description = """
                          Can pick up and use any 3x3 Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 5400;
            armor = 8;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            speed = 1.2f;
            rotateSpeed = 2f;
            accel = 0.05f;
            drag = 0.017f;
            faceTarget = true;
            hitSize = 36f;
            flying = true;
            itemCapacity = 1200;
            itemOffsetY = 6;
            lowAltitude = true;
            pickupUnits = false;
            payloadCapacity = tilePayload * ((4 * 4) - 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        rahela = new MountUnitType("rahela")
        {{
            localizedName = "Rahela";
            description = """
                          Can pick up and use any 4x4 Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 16200;
            armor = 11;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            speed = 1.2f;
            rotateSpeed = 2f;
            accel = 0.05f;
            drag = 0.017f;
            faceTarget = true;
            hitSize = 50f;
            flying = true;
            itemCapacity = 2400;
            itemOffsetY = 6;
            lowAltitude = true;
            pickupUnits = false;
            payloadCapacity = tilePayload * ((5 * 5) - 1);
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        chavash = new MountUnitType("chavash")
        {{
            localizedName = "chavash";
            description = """
                          Can pick up and use any 5x5 Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 32500;
            armor = 26;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            speed = 0.8f;
            rotateSpeed = 1f;
            accel = 0.04f;
            drag = 0.018f;
            flying = true;
            hitSize = 66f;
            payloadCapacity = tilePayload * ((6 * 6) - 1);
            range = 12*8;
            engineSize = 0;
            lowAltitude = true;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
        }};
        atlas = new MountUnitType("atlas")//Todo Might have weapons or abilities
        {{
            localizedName = "atlas";
            description = """
                          [orange]|Teir X Unit|
                          (This Means This Is A Boss)[]
                          --------------------------------------------------
                          Can pick up and use any 6x6 Turret or smaller.
                          Unit Item Storage will restock current attached turret.[]
                          [#800000]Only the first turret picked up will be operational.
                          """;

            constructor = PayloadUnit::create;
            health = 65000;
            armor = 39;
            faceTarget = true;
            factions.add(AxFactions.axthrix);
            speed = 0.8f;
            rotateSpeed = 1f;
            accel = 0.04f;
            drag = 0.018f;
            flying = true;
            hitSize = 80f;
            payloadCapacity = tilePayload * ((7 * 7) -1);
            range = 12*8;
            engineSize = 0;
            lowAltitude = true;
            engines = Seq.with(
                    new UnitEngine(-30,-70,14,-90),
                    new UnitEngine(30,-70,14,-90),

                    new UnitEngine(-70,-35,8,180+45),
                    new UnitEngine(70,-35,8,270+45),

                    new UnitEngine(-60,29,10,180+45),
                    new UnitEngine(60,29,10,270+45)
            );
        }};
        aza = new AxUnitType("aza")
        {{
            factions.add(AxFactions.axthrix);
            localizedName = "Aza";

            constructor = UnitEntity::create;
            health = 6000;
            armor = 2;
            faceTarget = true;
            flying = true;
            hitSize = 2*2;
            engineColor = Color.valueOf("95abd9");
            ammoType = new ItemAmmoType(Items.silicon, 20);
            itemCapacity = 300;
            itemOffsetY = 6;
            speed = 20f / 7.5f;
            rotateSpeed = 18 / 7.5f;
            strafePenalty = 1;
            drag = 0.8f;
            lowAltitude = true;
            omniMovement = true;
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
            abilities.add(
                    new DroneSpawnAbility(){{
                        spawnTime = 350;
                        dRot = 45;
                        dY = dX = 17.5f;
                        moveY = moveX = 17.5f;
                        drone = AxthrixDrones.wattGround;
                    }},new DroneSpawnAbility(){{
                        droneSlot = 1;
                        spawnTime = 350;
                        dRot = 315;
                        dY = -17.5f;
                        dX = 17.5f;
                        moveY = -17.5f;
                        moveX = 17.5f;
                        drone = AxthrixDrones.wattFlame;
                    }},
                    new DroneSpawnAbility(){{
                        droneSlot = 2;
                        spawnTime = 350;
                        dRot = 225;
                        dY = dX = -17.5f;
                        moveY = moveX = -17.5f;
                        drone = AxthrixDrones.wattAir;
                    }},
                    new DroneSpawnAbility(){{
                        droneSlot = 3;
                        spawnTime = 350;
                        dRot = 135;
                        dY = 17.5f;
                        dX = -17.5f;
                        moveY = 17.5f;
                        moveX = -17.5f;
                        drone = AxthrixDrones.wattIce;
                    }});

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
        }};

        //revolver bois
        sig = new AxUnitType("sig") {{
                constructor = LegsUnit::create;
                outlineRadius = 2;
                speed = 0.72F;
                drag = 0.11F;
                hitSize = 9.0F;
                rotateSpeed = 3.0F;
                health = 680.0F;
                armor = 4.0F;
                legStraightness = 0.3F;
                stepShake = 0.0F;
                legCount = 6;
                legLength = 10.0F;
                lockLegBase = true;
                legContinuousMove = true;
                legExtension = -2.5F;
                legBaseOffset = 3.0F;
                legMaxLength = 1.3F;
                legMinLength = 0.2F;
                legLengthScl = 0.96F;
                legForwardScl = 1.1F;
                legGroupSize = 3;
                rippleScale = 0.2F;
                legMoveSpace = 1.2F;
                allowLegStep = true;
                hovering = true;
                legPhysicsLayer = false;
                shadowElevation = 0.1F;
                targetAir = false;
                researchCostMultiplier = 0.0F;
                range = 8*8;
                maxRange = 14*8;

                weapons.add(new RevolverWeapon("sig-weapon") {
                    {
                        shootSound = Sounds.spray;
                        inaccuracy = 10;
                        shoot.shots = 6;
                        mirror = false;
                        showStatSprite = false;
                        x = 0.0F;
                        y = 1.0F;
                        shootY = 4.0F;
                        reload = 5.0F;
                        maxCartridges = 6;
                        numOfReloadCartridges = 2;
                        cartridgeReloadTime = 60;
                        cooldownTime = 42.0F;
                        heatColor = Pal.turretHeat;
                        bullet = new LiquidBulletType(){{
                            lifetime = 46;
                            speed = 22;
                            hitSize = 12;
                            damage = 15;
                            liquid = AxLiquids.xenon;
                            fragBullets = 1;
                            fragBullet = new LiquidBulletType(){{
                                lifetime = 1;
                                speed = 0;
                                hitSize = puddleSize = 12;
                                splashDamageRadius = 8*8;
                                splashDamage = 50;
                                liquid = AxLiquids.xenon;
                                status = AxthrixStatus.ReapAndSow;
                                statusDuration = 360;
                            }};
                        }};
                    }
                });
            parts.add(
                    new RegionPart("-chamber"){{
                        y = -0.5f;
                        layerOffset = -0.001f;
                        color = Color.valueOf("717380");
                        colorTo = Color.valueOf("505163");
                        growProgress = PartProgress.recoil;
                        progress = PartProgress.recoil;
                        growX = -0.5f;
                        moveX = -1.4f;
                    }},
                    new RegionPart("-chamber"){{
                        y = -0.5f;
                        x = 1.4f;
                        layerOffset = -0.001f;
                        color = Color.valueOf("505163");
                        colorTo = Color.valueOf("717380");
                        growProgress = PartProgress.recoil.inv();
                        progress = PartProgress.recoil;
                        growX = -0.5f;
                        moveX = -1.4f;
                    }},
                    new RegionPart("-chamber"){{
                        y = -0.5f;
                        x = -2.8f;
                        layerOffset = -0.001f;
                        color = Color.valueOf("505163");
                        growProgress = PartProgress.recoil.inv();
                        progress = PartProgress.recoil;
                        growX = -0.5f;
                        moveX = -1.4f;
                        moves.add(
                                new PartMove(PartProgress.constant(1), 0f, 0f, -0.5f, 0, 0f)
                        );
                    }},
                    new RegionPart("-chamber"){{
                        y = -0.5f;
                        x = -1.4f;
                        layerOffset = -0.001f;
                        color = Color.valueOf("505163");
                        growProgress = PartProgress.recoil;
                        progress = PartProgress.recoil;
                        growX = -0.5f;
                        moveX = -1.4f;
                        moves.add(
                                new PartMove(PartProgress.constant(1), 0f, 0f, -0.5f, 0, 0f)
                        );
                    }});
            }
        };

        immuneUnits.add(
                anagh
        );
        immuneUnits.add(
                akshaj
        );
        immuneUnits.add(
                amitojas
        );
        immuneUnits.add(
                agnitejas
        );
        immuneUnits.add(
                ayustejas
        );

    }
}