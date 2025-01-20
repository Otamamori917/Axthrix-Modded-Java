package axthrix.content.units;

import arc.func.Prov;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap.Entry;
import axthrix.world.types.abilities.SStatusFieldAbility;
import axthrix.world.types.entities.CptrUnitEntity;
import axthrix.content.AxthrixStatus;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.type.Weapon;

public class LegendUnits {
    public static UnitType
    //Core Units |1 units|

    //Legends |undetermined|
            athena,
        //yin and yang tree
            spate, influx
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
        //legends
        //yin and yang tree
        spate = new UnitType("spate"){{//Todo Missile weapon
           flying = true;
           speed = 2f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           constructor = UnitEntity::create;
           weapons.add(new Weapon("puw"){{
                shootY = 2f;
                x = 1f;
                y = 0f;
                mirror = true;
                reload = 10;
                top = false;
                heatColor = Pal.heal;
                bullet = new BasicBulletType(){{
                    damage = 40;
                    lifetime = 60;
                    speed = 5;
                }};
            }});

            abilities.add(new SStatusFieldAbility(AxthrixStatus.precludedA, 160f, 140f, 100f){{
                atNotShoot = true;
            }});
        }};
        influx = new UnitType("influx"){{//Todo Cannon weapon
           speed = 2f;
           hitSize = 6f;
           health = 340;
           armor = 2f;
           faceTarget = false;
           crushDamage = 500f;
           constructor = TankUnit::create;
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

            abilities.add(new SStatusFieldAbility(AxthrixStatus.precludedX, 160f, 140f, 100){{
                onShoot = true;
            }});
        }};
    }
}