package axthrix.content.units;

import arc.graphics.*;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.struct.Seq;
import arc.util.Time;
import axthrix.content.AxFactions;
import axthrix.content.AxthrixStatus;
import axthrix.content.FX.AxthrixFfx;
import axthrix.world.types.bulletypes.bulletpatterntypes.SpiralPattern;
import axthrix.world.types.entities.CptrUnitEntity;
import axthrix.world.types.parts.Propeller;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.unittypes.CopterUnitType;
import axthrix.world.types.unittypes.MountUnitType;
import axthrix.world.types.weapontypes.WeaponHelix;
import blackhole.entities.part.BlackHolePart;
import static blackhole.utils.BlackHoleUtils.immuneUnits;
import mindustry.entities.abilities.*;
import axthrix.world.types.abilities.*;
import axthrix.world.types.bulletypes.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.ExplosionEffect;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.*;

import mindustry.entities.pattern.ShootAlternate;
import axthrix.world.types.ai.*;
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
import mindustry.entities.abilities.MoveEffectAbility;
import mindustry.graphics.Pal;
import mindustry.type.ammo.ItemAmmoType;
import mindustry.world.meta.BlockFlag;

import static mindustry.Vars.tilesize;
import static mindustry. Vars.tilePayload;
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
                amos,aymoss,amalik,anuvaha,ambuvahini,
                //TX
                arcalishion,
            //Elemental Shuttles | leader |
                aza,enzo,ashur,alaric,aldrich,
                //TX
                enrique;
    //Ikatusa |undetermined|

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
                        damage = 50;
                        collidesAir = true;
                        ammoMultiplier = 1f;
                        lightningColor = Pal.techBlue;
                        lightningLength = 2;
                        lightningLengthRand = 8;

                        lightningType = new BulletType(0.0001f, 50f) {{
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
                bullet = new ArtilleryBulletType(6f, 0){{
                    width = 8;
                    height = 8;
                    lifetime = 189;
                    scaleLife = true;
                    keepVelocity = false;
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
                    shootEffect = Fx.instShoot;
                    hitEffect = Fx.instHit;
                    pierceEffect = Fx.railHit;
                    smokeEffect = Fx.smokeCloud;
                    pointEffect = Fx.instTrail;
                    despawnEffect = Fx.instBomb;
                    pointEffectSpace = 20f;
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
                mirror = false;

                minWarmup = 0.8f;
                x = 0;
                y = 0;
                reload = 1f;
                shootY = 10f;
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
                    homingDelay = 6f;
                    homingRange = 100;
                    collidesGround = true;
                    collidesAir = true;

                    shootSound = Sounds.plasmadrop;
                    soundPitchMax = 8;
                    soundPitchMin = 6;

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
                    shootEffect = Fx.instShoot;
                    hitEffect = Fx.instHit;
                    pierceEffect = Fx.railHit;
                    smokeEffect = Fx.smokeCloud;
                    pointEffect = Fx.instTrail;
                    despawnEffect = Fx.instBomb;
                    pointEffectSpace = 20f;
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
                    shootEffect = Fx.instShoot;
                    hitEffect = Fx.instHit;
                    pierceEffect = Fx.railHit;
                    smokeEffect = Fx.smokeCloud;
                    pointEffect = Fx.instTrail;
                    despawnEffect = Fx.instBomb;
                    pointEffectSpace = 20f;
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
                          [green]Protects Allies with small but durable force field.
                          Barrier Fires A single Hostile Nanobot that deals DOT.
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

            abilities.add(new ForceFieldAbility(20f, 0.1f, 200f, 40f * 6));
        }};

        blockade = new AxUnitType("blockade"){{
            localizedName = "[green]Blockade";
            description = """
                          [green]Heals Allies and Deals Great Damage at Medium range.
                          Blockade Fires Missiles containing Nanobots in quick succession.
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
                    ammoMultiplier = 4;
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("nano-missile"){{
                        targetAir = false;
                        speed = 2.3f;
                        maxRange = 6f;
                        lifetime = 60f * 1.4f;
                        outlineColor = Color.valueOf("#181a1b");
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

                bullet = new BasicBulletType(2f, 20){{
                    homingRange = 40f;
                    homingPower = 4f;
                    homingDelay = 5f;
                    width = 0.5f;
                    height = 0.5f;
                    lifetime = 15;
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
                    ammoMultiplier = 6;
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("burst-missile"){{
                        targetAir = false;
                        speed = 3f;
                        maxRange = 6f;
                        lifetime = 60f * 1.6f;
                        outlineColor = Color.valueOf("#181a1b");
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
            ammoType = new ItemAmmoType(Items.silicon);
            ammoCapacity = 1000;
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
            new RegionPart("-mount-l"){{
                progress = PartProgress.warmup;
                heatProgress = PartProgress.warmup.delay(0.6f);
                heatColor = Pal.heal;
                mirror = false;
                under = true;
                children.add(new RegionPart("-arm-l"){{
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
                    children.add(new RegionPart("-plate-l"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.warmup.delay(0.6f);
                        heatColor = Pal.heal;
                        mirror = false;
                        under = true;
                        moveY = 0f;
                        moveX = -6.8f;
                        children.add(new RegionPart("-shell-l"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.warmup.delay(0.6f);
                            heatColor = Pal.heal;
                            mirror = false;
                            under = true;
                            moveY = -1f;
                            moveX = 0f;
                            moveRot = -10f;
                            moves.add(new PartMove(PartProgress.recoil, 0f, 0f, 0f));
                        }});
                    }});
                }});
            }},
            new RegionPart("-mount-r"){{
                progress = PartProgress.warmup;
                heatProgress = PartProgress.warmup.delay(0.6f);
                heatColor = Pal.heal;
                mirror = false;
                under = true;
                children.add(new RegionPart("-arm-r"){{
                    progress = PartProgress.warmup;
                    heatProgress = PartProgress.warmup.delay(0.6f);
                    heatColor = Pal.heal;
                    mirror = false;
                    under = false;
                    x = 0f;
                    moveX = 0;
                    moveY = 1f;
                    moveRot = -5f;
                    moves.add(new PartMove(PartProgress.recoil, 0f,  0, 0f));
                    children.add(new RegionPart("-plate-r"){{
                        progress = PartProgress.warmup;
                        heatProgress = PartProgress.warmup.delay(0.6f);
                        heatColor = Pal.heal;
                        mirror = false;
                        under = true;
                        moveY = 0f;
                        moveX = 6.8f;
                        children.add(new RegionPart("-shell-r"){{
                            progress = PartProgress.warmup;
                            heatProgress = PartProgress.warmup.delay(0.6f);
                            heatColor = Pal.heal;
                            mirror = false;
                            under = true;
                            moveY = -1f;
                            moveX = 0f;
                            moveRot = 10f;
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
                reload = 400;
                recoil = 0;
                inaccuracy = 0;
                parts.add(
                    new RegionPart("-missile") {{
                        progress = PartProgress.reload.curve(Interp.pow2In);

                        colorTo = new Color(1f, 1f, 1f, 0f);
                        color = Color.white;
                        mixColorTo = Pal.accent;
                        mixColor = new Color(1f, 1f, 1f, 0f);
                        outline = false;
                        under = true;
                        y = 2.5f;
                        x = 2;
                        layerOffset = -0.1f;
                        moves.add(
                                new PartMove(PartProgress.warmup, 8f, -2.5f, -90f),
                                new PartMove(PartProgress.warmup.delay(0.4f), 2f, 2f, 24f),
                                new PartMove(PartProgress.warmup.delay(0.8f), 2f, 1f, 24f)
                        );
                    }}
                );
                bullet = new BasicBulletType(){{
                    ammoMultiplier = 10;
                    displayAmmoMultiplier = false;
                    speed = 0f;
                    keepVelocity = false;
                    collidesAir = false;
                    spawnUnit = new MissileUnitType("bruh-missile"){{
                        speed = 4.6f;
                        maxRange = 6f;
                        lifetime = 60f * 5.5f;
                        outlineColor = Color.valueOf("#181a1b");
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
                            float rad = 100f;
                            shootCone = 360f;
                            mirror = false;
                            reload = 1f;
                            deathExplosionEffect = Fx.massiveExplosion;
                            shootOnDeath = true;
                            shake = 10f;
                            bullet = new ExplosionBulletType(600f, rad){{
                                hitColor = lightColor = Pal.heal;
                                backColor = Pal.heal;
                                frontColor = Color.white;
                                mixColorTo = Color.white;

                                despawnEffect = Fx.greenBomb;
                                hitEffect = Fx.massiveExplosion;


                                collidesAir = true;

                                ammoMultiplier = 1f;
                                fragLifeMin = 0.1f;
                                fragBullets = 8;
                                fragBullet = new EmpBulletType(){{
                                    lightOpacity = 0.7f;
                                    unitDamageScl = 0.8f;
                                    healPercent = 1f;
                                    timeIncrease = 3f;
                                    timeDuration = 60f * 20f;
                                    powerDamageScl = 2f;
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
                                    trailRotation = false;
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

        //special black hole tanks
        anagh = new AxUnitType("anagh") {{
            localizedName = "[purple]Anagh";
            description = """
                          [purple]Lead Engineers at [#de9458]Axthrix[purple] decided not to make anagh a weapon, but make anagh itself the weapon!
                          Anagh contains an Miniature Blackhole on its back which pulls in any unit or bullet close to it. 
                          get too close and it will rip nearby bullets, units, and buildings apart.
                          has a special field around Anagh preventing allies from being effected by its Blackhole.
                          
                          [#800000]if 2 units on different teams with such ability meet they cancel each other's attraction but will still rip each other apart.
                          """;
            treadPullOffset = 0;
            itemCapacity = 0;
            treadRects = new Rect[]{new Rect(12 - 32f, 8 - 32f, 11, 50)};
            factions.add(AxFactions.axthrix);
            constructor = TankUnit::create;
            outlines = false;
            flying = false;
            speed = 5.3f/7.5f;
            drag = 0.13f;
            hitSize = 10f;
            health = 400;
            armor = 8;
            accel = 0.6f;
            rotateSpeed = 3.3f;
            faceTarget = false;
            range = 120;
            abilities.add(new AttractionFieldAbility(){{
                damageRadius = 30;
                suctionRadius = 120;
                damage = 8f;
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
                        edge = 3f;
                        edgeTo = 6f;
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

            weapons.add(new Weapon(name + "-weapon"){{
                shootY = 6f;
                x = 0f;
                y = 0f;
                mirror = false;
                reload = 6;
                top = true;
                heatColor =  Color.orange;
                shoot.shotDelay = 30;
                shoot.shots = 2;
                bullet = new LightningBulletType(){{
                    damage = 15;
                    lightningLength = 15;
                    lightningColor = Color.orange;
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
            outlines = false;
            health = 300;
            hitSize = 18;

            speed = 2f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;
            buildSpeed = 2f;

            ammoType = new ItemAmmoType(Items.silicon, 10);

            circleTarget = true;
            lowAltitude = true;
            faceTarget = false;
            flying = true;
            range = unitRange;
            engineColor = Color.valueOf("4ea572");
            engineLayer = 0.05f;
            engineSize = 0;
            engines = Seq.with(new UnitEngine(0,-6.5f,2.5f,-90));

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;

            constructor = CptrUnitEntity::new;
            aiController = UnitHealerAi::new;

            abilities.add(new StatusFieldAbility(AxthrixStatus.repair,60,80,16*8){{
                applyEffect = AxthrixFfx.circleOut(60,4,1,Color.green);
                activeEffect = AxthrixFfx.circleOut(40,16*8, 4,Color.green);
            }});

            weapons.add(new Weapon() {{
                rotate = false;
                reload = 120*6;
                x = y = shootX = 0;
                shootY = 10;
                baseRotation = 180f;
                shootCone = 360;

                bullet = new BulletType(){{
                    lifetime = 0;
                    speed = 0;
                    shootSound = Sounds.release;
                    soundPitchMax = soundPitchMin = 1.5f;
                    shootEffect = Fx.none;
                    smokeEffect = Fx.none;
                    spawnUnit = AxthrixDrones.ivy;
                }};
            }});
            float layerOffset = -0.00009f;

            float rotX = 19f * 0.25f;
            float rotY = -10f * 0.25f;
            float rotorScaling = 0.2f;
            //small rotors
            float rotXs = 10f * 0.25f;
            float rotYs = 7f * 0.25f;
            float rotorScalings = 0.18f;
            propeller.add(
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = -rotX;
                        y = rotY;
                        rotorSpeed = 32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 8;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScaling;
                    }},
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = rotX;
                        y = rotY;
                        rotorSpeed = -32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 8;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScaling;
                    }},
                    //small rotors
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = -rotXs;
                        y = rotYs;
                        rotorSpeed = 32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 6;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScalings;
                    }},
                    new Propeller("aj-short-turbine-repair") {{
                        topBladeName = "short-blade";
                        x = rotXs;
                        y = rotYs;
                        rotorSpeed = -32;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 6;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScalings;
                    }}
            );
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
            rotateSpeed = 18 / 7.5f;
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
                    new UnitEngine(0,-14,4,-90),
                    new UnitEngine(-10,-10,4,180+45),
                    new UnitEngine(10,-10,4,270+45)
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
            rotateSpeed = 16 / 7.5f;
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
            rotateSpeed = 14 / 7.5f;
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
            rotateSpeed = 12 / 7.5f;
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
            rotateSpeed = 10 / 7.5f;
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
                          [orange]|Teir X Unit|
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
            rotateSpeed = 6 / 7.5f;
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
            omniMovement = false;
            range = 12*8;
            engineSize = 0;
            engines = Seq.with(
                    new UnitEngine(0,-14,6,-90),
                    new UnitEngine(-10,-10,6,180+45),
                    new UnitEngine(10,-10,6,270+45)
            );
            weapons.add(new Weapon() {{
                reload = 120*10;
                x = y = shootX = shootY = 0;
                shootCone = 360;
                bullet = new SpawnHelperBulletType(){{
                    hasParent = true;
                    shootEffect = Fx.shootBig;
                    spawnUnit = AxthrixDrones.wattFlame;
                }};
            }});
            weapons.add(new Weapon() {{
                reload = 120*10;
                x = y = shootX = shootY = 0;
                baseRotation = -90f;
                shootCone = 360;
                bullet = new SpawnHelperBulletType(){{
                    hasParent = true;
                    shootEffect = Fx.shootBig;
                    spawnUnit = AxthrixDrones.wattIce;
                }};
            }});
            weapons.add(new Weapon() {{
                reload = 120*10;
                x = y = shootX = shootY = 0;
                baseRotation = 90f;
                shootCone = 360;
                bullet = new SpawnHelperBulletType(){{
                    hasParent = true;
                    shootEffect = Fx.shootBig;
                    spawnUnit = AxthrixDrones.wattGround;
                }};
            }});
            weapons.add(new Weapon() {{
                reload = 120*10;
                x = y = shootX = shootY = 0;
                baseRotation = 180f;
                shootCone = 360;
                bullet = new SpawnHelperBulletType(){{
                    hasParent = true;
                    shootEffect = Fx.shootBig;
                    spawnUnit = AxthrixDrones.wattAir;
                }};
            }});
        }};

        immuneUnits.add(
            anagh
        );

    }
}