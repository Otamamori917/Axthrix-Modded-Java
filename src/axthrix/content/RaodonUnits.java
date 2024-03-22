package axthrix.content;

import arc.func.Prov;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap.Entry;
import axthrix.world.types.abilities.*;
import axthrix.world.types.ai.DynFlyingAI;
import axthrix.world.types.entities.CptrUnitEntity;
import axthrix.world.types.parts.Propeller;
import axthrix.world.types.unittypes.CopterUnitType;
import mindustry.content.Items;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.ammo.ItemAmmoType;

import static mindustry.Vars.tilesize;

public class RaodonUnits {
    public static UnitType
    //Raodon |3 trees|
        //Assault Walker |Power|
            asta,adira,allura,andrea,athena,
        //Support Tank  |Wealth|
            danu,dorit,duarte,dhanya,dhanashri,
        //Specialist aircraft |Fame|
            efim,estes,elmena,evdoxia,estanislao
    //Ikatusa |undetermined|

    //Core Units |3 units|
            ;
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

        efim = new CopterUnitType("efim") {{
            float unitRange = 28 * tilesize;
            health = 450;
            hitSize = 18;

            speed = 2.5f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;

            ammoType = new ItemAmmoType(Items.graphite);

            circleTarget = true;
            lowAltitude = true;
            faceTarget = flying = true;
            range = unitRange;

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;

            engineSize = 0;
            constructor = CptrUnitEntity::new;
            aiController = DynFlyingAI::new;

            abilities.add(new StealthFieldAbility());
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

            float rotX = 41 * 0.25f;
            float rotY = -4 * 0.25f;
            float rotSpeed = 32f;
            float layerOffset = -0.00009f;
            float rotorScaling = 0.6f;
            propeller.add(
                    new Propeller("aj-short-blade-purp-dark") {{
                        topBladeName = "short-blade-dark";
                        x = -rotX;
                        y = rotY;
                        rotorSpeed = rotSpeed;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 4;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScaling;
                    }},
                    new Propeller("aj-short-blade-purp-dark") {{
                        topBladeName = "short-blade-dark";
                        x = rotX;
                        y = rotY;
                        rotorSpeed = rotSpeed;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 4;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScaling;
                    }}
            );
        }};
        estes = new CopterUnitType("estes") {{
            float unitRange = 28 * tilesize;
            health = 450;
            hitSize = 18;

            speed = 2.5f;
            accel = 0.04f;
            drag = 0.016f;
            rotateSpeed = 5.5f;

            ammoType = new ItemAmmoType(Items.graphite);

            circleTarget = true;
            lowAltitude = true;
            faceTarget = flying = true;
            range = unitRange;

            fallSpeed = 0.0015f;
            spinningFallSpeed = 4;
            fallSmokeY = -10f;

            engineSize = 0;
            constructor = CptrUnitEntity::new;
            aiController = DynFlyingAI::new;

            abilities.add(new StealthFieldAbility());
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

            float rotX = 41 * 0.25f;
            float rotY = -4 * 0.25f;
            float rotSpeed = 32f;
            float layerOffset = -0.00009f;
            float rotorScaling = 0.8f;
            propeller.add(
                    new Propeller("aj-short-blade-purp-dark") {{
                        topBladeName = "short-blade-dark";
                        x = -rotX;
                        y = rotY;
                        rotorSpeed = rotSpeed;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 4;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScaling;
                    }},
                    new Propeller("aj-short-blade-purp-dark") {{
                        topBladeName = "short-blade-dark";
                        x = rotX;
                        y = rotY;
                        rotorSpeed = rotSpeed;
                        rotorBlurSpeedMultiplier = 0.08f;
                        bladeCount = 4;
                        rotorLayer = layerOffset;
                        rotorSizeScl = rotorTopSizeScl = rotorScaling;
                    }}
            );
        }};
    }
}