package axthrix.world.util;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Nullable;
import arc.util.serialization.JsonValue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.content.Liquids;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.Effect;
import mindustry.entities.effect.MultiEffect;
import mindustry.gen.Icon;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.type.UnitType;
import mindustry.world.Block;
import multicraft.IOEntry;
import multicraft.Recipe;
import multicraft.RecipeAnalyzerException;

public class AxMulticrafterAnalyzer {
    private static final String[] inputAlias = new String[]{"input", "in", "i"};
    private static final String[] outputAlias = new String[]{"output", "out", "o"};
    private static String curBlock = "";
    private static int index = 0;
    private static final Prov<TextureRegion> NotFound = () -> Icon.cancel.getRegion();
    private static final Effect[] EffectType = new Effect[0];

    public AxMulticrafterAnalyzer() {
    }

    public static Object preProcessArc(Object seq) {
        try {
            return processFunc(seq);
        } catch (Exception e) {
            error("Can't convert Seq in preprocess " + seq, e);
            return Collections.emptyList();
        }
    }

    public static Object processFunc(Object o) {
        if (o instanceof Seq) {
            Seq seq = (Seq)o;
            ArrayList list = new ArrayList(seq.size);

            for(Object e : new Seq.SeqIterable(seq)) {
                list.add(processFunc(e));
            }

            return list;
        } else if (!(o instanceof ObjectMap)) {
            return o instanceof JsonValue ? convert((JsonValue)o) : o;
        } else {
            ObjectMap objMap = (ObjectMap)o;
            HashMap map = new HashMap();
            ObjectMap.Entries var3 = (new ObjectMap.Entries(objMap)).iterator();

            while(var3.hasNext()) {
                ObjectMap.Entry<Object, Object> entry = (ObjectMap.Entry)var3.next();
                map.put(entry.key, processFunc(entry.value));
            }

            return map;
        }
    }

    public static Seq<AxRecipe> analyze(Block meta, Object o) {
        curBlock = genName(meta);
        o = preProcessArc(o);
        Seq<AxRecipe> recipes = new Seq(Recipe.class);
        index = 0;
        if (o instanceof List) {
            for(Object recipeMapObj : (List)o) {
                Map recipeMap = (Map)recipeMapObj;
                analyzeRecipe(recipeMap, recipes);
                ++index;
            }
        } else {
            if (!(o instanceof Map)) {
                throw new RecipeAnalyzerException("Unsupported recipe list from <" + o + ">");
            }

            Map recipeMap = (Map)o;
            analyzeRecipe(recipeMap, recipes);
        }

        return recipes;
    }

    public static void analyzeRecipe(Map recipeMap, Seq<AxRecipe> to) {
        try {
            AxRecipe recipe = new AxRecipe();
            Object inputsRaw = findValueByAlias(recipeMap, inputAlias);
            if (inputsRaw == null) {
                Log.warn("Recipe doesn't have any input, so skip it", new Object[0]);
                return;
            }

            Object outputsRaw = findValueByAlias(recipeMap, outputAlias);
            if (outputsRaw == null) {
                Log.warn("Recipe doesn't have any output, so skip it", new Object[0]);
                return;
            }

            recipe.input = analyzeIOEntry("input", inputsRaw);
            recipe.output = analyzeIOEntry("output", outputsRaw);
            Object craftTimeObj = recipeMap.get("craftTime");
            recipe.craftTime = analyzeFloat(craftTimeObj);
            Object iconObj = recipeMap.get("icon");
            if (iconObj instanceof String) {
                recipe.icon = findIcon((String)iconObj);
            }

            Object iconColorObj = recipeMap.get("iconColor");
            if (iconColorObj instanceof String) {
                recipe.iconColor = Color.valueOf((String)iconColorObj);
            }

            Object fxObj = recipeMap.get("craftEffect");
            Effect fx = analyzeFx(fxObj);
            if (fx != null) {
                recipe.craftEffect = fx;
            }

            if (!recipe.isAnyEmpty()) {
                to.add(recipe);
            } else {
                Log.warn("Recipe is empty, so skip it", new Object[]{recipe});
            }
        } catch (Exception e) {
            error("Can't load a recipe", e);
        }

    }

    @Nullable
    public static Object findValueByAlias(Map map, String... aliases) {
        for(String alias : aliases) {
            Object tried = map.get(alias);
            if (tried != null) {
                return tried;
            }
        }

        return null;
    }

    public static IOEntry analyzeIOEntry(String meta, Object ioEntry) {
        IOEntry res = new IOEntry();
        if (ioEntry instanceof Map) {
            Map ioRawMap = (Map)ioEntry;
            Object items = ioRawMap.get("items");
            if (items != null) {
                if (items instanceof List) {
                    analyzeItems((List)items, res.items);
                } else if (items instanceof String) {
                    analyzeItemPair((String)items, res.items);
                } else {
                    if (!(items instanceof Map)) {
                        throw new RecipeAnalyzerException("Unsupported type of items at " + meta + " from <" + items + ">");
                    }

                    analyzeItemMap((Map)items, res.items);
                }
            }

            Object fluids = ioRawMap.get("fluids");
            if (fluids != null) {
                if (fluids instanceof List) {
                    analyzeFluids((List)fluids, res.fluids);
                } else if (fluids instanceof String) {
                    analyzeFluidPair((String)fluids, res.fluids);
                } else {
                    if (!(fluids instanceof Map)) {
                        throw new RecipeAnalyzerException("Unsupported type of fluids at " + meta + " from <" + fluids + ">");
                    }

                    analyzeFluidMap((Map)fluids, res.fluids);
                }
            }

            Object powerObj = ioRawMap.get("power");
            res.power = analyzeFloat(powerObj);
            Object heatObj = ioRawMap.get("heat");
            res.heat = analyzeFloat(heatObj);
            Object iconObj = ioRawMap.get("icon");
            if (iconObj instanceof String) {
                res.icon = findIcon((String)iconObj);
            }

            Object iconColorObj = ioRawMap.get("iconColor");
            if (iconColorObj instanceof String) {
                res.iconColor = Color.valueOf((String)iconColorObj);
            }
        } else if (ioEntry instanceof List) {
            for(Object content : (List)ioEntry) {
                if (content instanceof String) {
                    analyzeAnyPair((String)content, res.items, res.fluids);
                } else {
                    if (!(content instanceof Map)) {
                        throw new RecipeAnalyzerException("Unsupported type of content at " + meta + " from <" + content + ">");
                    }

                    analyzeAnyMap((Map)content, res.items, res.fluids);
                }
            }
        } else {
            if (!(ioEntry instanceof String)) {
                throw new RecipeAnalyzerException("Unsupported type of " + meta + " <" + ioEntry + ">");
            }

            analyzeAnyPair((String)ioEntry, res.items, res.fluids);
        }

        return res;
    }

    public static void analyzeItems(List items, Seq<ItemStack> to) {
        for(Object entryRaw : items) {
            if (entryRaw instanceof String) {
                analyzeItemPair((String)entryRaw, to);
            } else if (entryRaw instanceof Map) {
                analyzeItemMap((Map)entryRaw, to);
            } else {
                error("Unsupported type of items <" + entryRaw + ">, so skip them");
            }
        }

    }

    public static void analyzeItemPair(String pair, Seq<ItemStack> to) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String itemID = id2Amount[0];
            Item item = findItem(itemID);
            if (item == null) {
                error("<" + itemID + "> doesn't exist in all items, so skip this");
                return;
            }

            ItemStack entry = new ItemStack();
            entry.item = item;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Integer.parseInt(amountStr);
            } else {
                entry.amount = 1;
            }

            to.add(entry);
        } catch (Exception e) {
            error("Can't analyze an item from <" + pair + ">, so skip it", e);
        }

    }

    public static void analyzeFluids(List fluids, Seq<LiquidStack> to) {
        for(Object entryRaw : fluids) {
            if (entryRaw instanceof String) {
                analyzeFluidPair((String)entryRaw, to);
            } else if (entryRaw instanceof Map) {
                analyzeFluidMap((Map)entryRaw, to);
            } else {
                error("Unsupported type of fluids <" + entryRaw + ">, so skip them");
            }
        }

    }

    public static void analyzeFluidPair(String pair, Seq<LiquidStack> to) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String fluidID = id2Amount[0];
            Liquid fluid = findFluid(fluidID);
            if (fluid == null) {
                error("<" + fluidID + "> doesn't exist in all fluids, so skip this");
                return;
            }

            LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
            entry.liquid = fluid;
            if (id2Amount.length == 2) {
                String amountStr = id2Amount[1];
                entry.amount = Float.parseFloat(amountStr);
            } else {
                entry.amount = 1.0F;
            }

            to.add(entry);
        } catch (Exception e) {
            error("Can't analyze a fluid from <" + pair + ">, so skip it", e);
        }

    }

    public static void analyzeAnyPair(String pair, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        try {
            String[] id2Amount = pair.split("/");
            if (id2Amount.length != 1 && id2Amount.length != 2) {
                error("<" + Arrays.toString(id2Amount) + "> doesn't contain 1 or 2 parts, so skip this");
                return;
            }

            String id = id2Amount[0];
            Item item = findItem(id);
            if (item != null) {
                ItemStack entry = new ItemStack();
                entry.item = item;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Integer.parseInt(amountStr);
                } else {
                    entry.amount = 1;
                }

                items.add(entry);
                return;
            }

            Liquid fluid = findFluid(id);
            if (fluid != null) {
                LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
                entry.liquid = fluid;
                if (id2Amount.length == 2) {
                    String amountStr = id2Amount[1];
                    entry.amount = Float.parseFloat(amountStr);
                } else {
                    entry.amount = 1.0F;
                }

                fluids.add(entry);
                return;
            }

            error("Can't find the corresponding item or fluid from this <" + pair + ">, so skip it");
        } catch (Exception e) {
            error("Can't analyze this uncertain <" + pair + ">, so skip it", e);
        }

    }

    public static void analyzeAnyMap(Map map, Seq<ItemStack> items, Seq<LiquidStack> fluids) {
        try {
            Object itemRaw = map.get("item");
            if (itemRaw instanceof String) {
                Item item = findItem((String)itemRaw);
                if (item != null) {
                    ItemStack entry = new ItemStack();
                    entry.item = item;
                    Object amountRaw = map.get("amount");
                    entry.amount = analyzeInt(amountRaw);
                    items.add(entry);
                    return;
                }
            }

            Object fluidRaw = map.get("fluid");
            if (fluidRaw instanceof String) {
                Liquid fluid = findFluid((String)fluidRaw);
                if (fluid != null) {
                    LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
                    entry.liquid = fluid;
                    Object amountRaw = map.get("amount");
                    entry.amount = analyzeFloat(amountRaw);
                    fluids.add(entry);
                    return;
                }
            }

            error("Can't find the corresponding item or fluid from <" + map + ">, so skip it");
        } catch (Exception e) {
            error("Can't analyze this uncertain <" + map + ">, so skip it", e);
        }

    }

    public static void analyzeItemMap(Map map, Seq<ItemStack> to) {
        try {
            ItemStack entry = new ItemStack();
            Object itemID = map.get("item");
            if (!(itemID instanceof String)) {
                error("Can't recognize a fluid from <" + map + ">");
                return;
            }

            Item item = findItem((String)itemID);
            if (item == null) {
                error("<" + itemID + "> doesn't exist in all items, so skip this");
                return;
            }

            entry.item = item;
            int amount = analyzeInt(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0) {
                error("Item amount is +" + amount + " <=0, so reset as 1");
                entry.amount = 1;
            }

            to.add(entry);
        } catch (Exception e) {
            error("Can't analyze an item <" + map + ">, so skip it", e);
        }

    }

    public static void analyzeFluidMap(Map map, Seq<LiquidStack> to) {
        try {
            LiquidStack entry = new LiquidStack(Liquids.water, 0.0F);
            Object itemID = map.get("fluid");
            if (!(itemID instanceof String)) {
                error("Can't recognize an item from <" + map + ">");
                return;
            }

            Liquid fluid = findFluid((String)itemID);
            if (fluid == null) {
                error(itemID + " doesn't exist in all fluids, so skip this");
                return;
            }

            entry.liquid = fluid;
            float amount = analyzeFloat(map.get("amount"));
            entry.amount = amount;
            if (amount <= 0.0F) {
                error("Fluids amount is +" + amount + " <=0, so reset as 1.0f");
                entry.amount = 1.0F;
            }

            to.add(entry);
        } catch (Exception e) {
            error("Can't analyze <" + map + ">, so skip it", e);
        }

    }

    public static float analyzeFloat(@Nullable Object floatObj) {
        if (floatObj == null) {
            return 0.0F;
        } else if (floatObj instanceof Number) {
            return ((Number)floatObj).floatValue();
        } else {
            try {
                return Float.parseFloat((String)floatObj);
            } catch (Exception var2) {
                return 0.0F;
            }
        }
    }

    public static int analyzeInt(@Nullable Object intObj) {
        if (intObj == null) {
            return 0;
        } else if (intObj instanceof Number) {
            return ((Number)intObj).intValue();
        } else {
            try {
                return Integer.parseInt((String)intObj);
            } catch (Exception var2) {
                return 0;
            }
        }
    }

    public static void error(String content) {
        Log.err("[" + curBlock + "](at recipe " + index + ")\n" + content, new Object[0]);
    }

    public static void error(String content, Throwable e) {
        Log.err("[" + curBlock + "](at recipe " + index + ")\n" + content, e);
    }

    public static String genName(Block meta) {
        return meta.localizedName.equals(meta.name) ? meta.name : meta.localizedName + "(" + meta.name + ")";
    }

    @Nullable
    public static Item findItem(String id) {
        for(Item item : Vars.content.items()) {
            if (id.equals(item.name)) {
                return item;
            }
        }

        return null;
    }

    @Nullable
    public static Liquid findFluid(String id) {
        for(Liquid fluid : Vars.content.liquids()) {
            if (id.equals(fluid.name)) {
                return fluid;
            }
        }

        return null;
    }

    @Nullable
    public static Block findBlock(String id) {
        for(Block block : Vars.content.blocks()) {
            if (id.equals(block.name)) {
                return block;
            }
        }

        return null;
    }

    @Nullable
    public static UnitType findUnit(String id) {
        for(UnitType unit : Vars.content.units()) {
            if (id.equals(unit.name)) {
                return unit;
            }
        }

        return null;
    }

    @Nullable
    public static UnlockableContent findPayload(String id) {
        UnitType unit = findUnit(id);
        return (UnlockableContent)(unit != null ? unit : findBlock(id));
    }

    @Nullable
    public static Prov<TextureRegion> findIcon(String name) {
        if (name.startsWith("Icon.") && name.length() > 5) {
            try {
                String fieldName = name.substring(5);
                Field field = Icon.class.getField(fieldName.contains("-") ? kebab2camel(fieldName) : fieldName);
                Object icon = field.get((Object)null);
                TextureRegion tr = ((TextureRegionDrawable)icon).getRegion();
                return () -> tr;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                error("Icon <" + name + "> not found, so use a cross instead.", e);
                return NotFound;
            }
        } else {
            Item item = findItem(name);
            if (item != null) {
                return () -> item.uiIcon;
            } else {
                Liquid fluid = findFluid(name);
                if (fluid != null) {
                    return () -> fluid.uiIcon;
                } else {
                    UnlockableContent payload = findPayload(name);
                    if (payload != null) {
                        return () -> payload.uiIcon;
                    } else {
                        TextureRegion tr = Core.atlas.find(name);
                        if (tr.found()) {
                            return () -> tr;
                        } else {
                            error("Texture <" + name + "> not found, so use a cross instead.");
                            return NotFound;
                        }
                    }
                }
            }
        }
    }

    public static String kebab2camel(String kebab) {
        StringBuilder sb = new StringBuilder();
        boolean hyphen = false;

        for(int i = 0; i < kebab.length(); ++i) {
            char c = kebab.charAt(i);
            if (c == '-') {
                hyphen = true;
            } else if (hyphen) {
                sb.append(Character.toUpperCase(c));
                hyphen = false;
            } else if (i == 0) {
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    @Nullable
    public static Effect analyzeFx(Object obj) {
        if (obj instanceof String) {
            return findFx((String)obj);
        } else {
            return obj instanceof List ? composeMultiFx((List)obj) : null;
        }
    }

    @Nullable
    public static Effect findFx(String name) {
        Object effect = field(Fx.class, name);
        return effect instanceof Effect ? (Effect)effect : null;
    }

    public static Effect composeMultiFx(List<String> names) {
        ArrayList<Effect> all = new ArrayList();

        for(String name : names) {
            Effect fx = findFx(name);
            if (fx != null) {
                all.add(fx);
            }
        }

        return new MultiEffect((Effect[])all.toArray(EffectType));
    }

    public static Object field(Class<?> type, JsonValue value) {
        return field(type, value.asString());
    }

    public static Object field(Class<?> type, String name) {
        try {
            Object b = type.getField(name).get((Object)null);
            if (b == null) {
                throw new IllegalArgumentException(type.getSimpleName() + ": not found: '" + name + "'");
            } else {
                return b;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    private static Object convert(JsonValue j) {
        JsonValue.ValueType type = j.type();
        switch (type) {
            case object:
                HashMap map = new HashMap();

                for(JsonValue cur = j.child; cur != null; cur = cur.next) {
                    map.put(cur.name, convert(cur));
                }

                return map;
            case array:
                ArrayList list = new ArrayList();

                for(JsonValue cur = j.child; cur != null; cur = cur.next) {
                    list.add(convert(cur));
                }

                return list;
            case stringValue:
                return j.asString();
            case doubleValue:
                return j.asDouble();
            case longValue:
                return j.asLong();
            case booleanValue:
                return j.asBoolean();
            case nullValue:
                return null;
            default:
                return Collections.emptyMap();
        }
    }
}

