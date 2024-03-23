package axthrix.world.types.entities;

import arc.func.Prov;
import arc.struct.ObjectIntMap;
import arc.struct.ObjectMap.Entry;
import mindustry.gen.EntityMapping;
import mindustry.gen.Entityc;
import axthrix.world.types.entities.comp.ProductionUnit;
import axthrix.world.types.entities.comp.StealthUnit;


public class AXEntityMapping {
  public static int customUnits;
  public static ObjectIntMap<Class<? extends Entityc>> idMap = new ObjectIntMap<>();
  public static Entry<Class<? extends Entityc>, Prov<? extends Entityc>>[] entities = new Entry[]{
    entry(StealthUnit.class, StealthUnit::new),
    entry(ProductionUnit.class, ProductionUnit::new)
  };

  private static <T extends Entityc> Entry<Class<T>, Prov<T>> entry(Class<T> name, Prov<T> prov) {
    Entry<Class<T>, Prov<T>> out = new Entry<>();
    out.key = name;
    out.value = prov;
    return out;
  }

  public static void load() {
    for (Entry<Class<? extends Entityc>, Prov<? extends Entityc>> entry : entities) {
      customUnits++;
      idMap.put(entry.key, EntityMapping.register("CustomUnit:" + customUnits, entry.value));
    }
  }
}
