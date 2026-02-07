package axthrix.world.util;

import arc.graphics.g2d.TextureRegion;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Scaling;
import java.util.HashMap;

import axthrix.world.types.block.production.AxMulticrafter;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import multicraft.IOEntry;

public abstract class AxRecipeSelector {
    public static HashMap<String, AxRecipeSelector> all = new HashMap();
    public static AxRecipeSelector Simple = new AxRecipeSelector("simple") {
        public void build(AxMulticrafter b, AxMulticrafter.MultiCrafterBuild c, Table table) {
            Table t = new Table();
            t.background(Tex.whiteui);
            t.setColor(Pal.darkerGray);

            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                AxRecipe recipe = (AxRecipe)b.resolvedRecipes.get(i);
                ImageButton button = new ImageButton(Styles.clearTogglei);
                Image img;
                if (recipe.icon != null) {
                    img = new Image((TextureRegion)recipe.icon.get());
                    if (recipe.iconColor != null) {
                        img.setColor(recipe.iconColor);
                    }
                } else {
                    img = getDefaultIcon(b, c, recipe.output);
                }

                button.replaceImage(img);
                button.getImageCell().scaling(Scaling.fit).size(40.0F);
                int finalI = i;
                button.changed(() -> c.configure(finalI));
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                t.add(button).grow().margin(10.0F);
                if (i != 0 && i % 3 == 0) {
                    t.row();
                }
            }

            table.add(t).grow();
        }
    };
    public static AxRecipeSelector Number = new AxRecipeSelector("number") {
        public void build(AxMulticrafter b, AxMulticrafter.MultiCrafterBuild c, Table table) {
            Table t = new Table();

            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                AxRecipe recipe = (AxRecipe)b.resolvedRecipes.get(i);
                int finalI = i;
                TextButton button = Elem.newButton("" + i, Styles.togglet, () -> c.configure(finalI));
                if (recipe.iconColor != null) {
                    button.setColor(recipe.iconColor);
                }

                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                t.add(button).size(50.0F);
                if (i != 0 && i % 3 == 0) {
                    t.row();
                }
            }

            table.add(t).grow();
        }
    };
    public static AxRecipeSelector Transform = new AxRecipeSelector("transform") {
        public void build(AxMulticrafter b, AxMulticrafter.MultiCrafterBuild c, Table table) {
            Table t = new Table();

            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                if (i != 0 && i % 2 == 0) {
                    t.row();
                }

                AxRecipe recipe = (AxRecipe)b.resolvedRecipes.get(i);
                ImageButton button = new ImageButton(Styles.clearTogglei);
                Table bt = new Table();
                Image in = getDefaultIcon(b, c, recipe.input);
                bt.add(in).pad(6.0F);
                bt.image(Icon.right).pad(6.0F);
                Image out = getDefaultIcon(b, c, recipe.output);
                bt.add(out).pad(6.0F);
                button.replaceImage(bt);
                int finalI = i;
                button.changed(() -> c.configure(finalI));
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                t.add(button).grow().pad(8.0F).margin(10.0F);
            }

            table.add(t).grow();
        }
    };
    public static AxRecipeSelector Detailed = new AxRecipeSelector("detailed") {
        public void build(AxMulticrafter b, AxMulticrafter.MultiCrafterBuild c, Table table) {
            for(int i = 0; i < b.resolvedRecipes.size; ++i) {
                AxRecipe recipe = (AxRecipe)b.resolvedRecipes.get(i);
                Table t = new Table();
                t.background(Tex.whiteui);
                t.setColor(Pal.darkestGray);
                b.buildIOEntry(t, recipe, true);
                t.image(Icon.right);
                b.buildIOEntry(t, recipe, false);
                ImageButton button = new ImageButton(Styles.clearTogglei);
                int finalI = i;
                button.changed(() -> c.configure(finalI));
                button.update(() -> button.setChecked(c.curRecipeIndex == finalI));
                button.replaceImage(t);
                table.add(button).pad(5.0F).margin(10.0F).grow();
                table.row();
            }

        }
    };

    public static AxRecipeSelector get(@Nullable String name) {
        if (name == null) {
            return Transform;
        } else {
            AxRecipeSelector inMap = (AxRecipeSelector)all.get(name.toLowerCase());
            return inMap == null ? Transform : inMap;
        }
    }

    public AxRecipeSelector(String name) {
        all.put(name.toLowerCase(), this);
    }

    public abstract void build(AxMulticrafter var1, AxMulticrafter.MultiCrafterBuild var2, Table var3);

    public static Image getDefaultIcon(AxMulticrafter b, AxMulticrafter.MultiCrafterBuild c, IOEntry entry) {
        if (entry.icon != null) {
            Image img = new Image((TextureRegion)entry.icon.get());
            if (entry.iconColor != null) {
                img.setColor(entry.iconColor);
            }

            return img;
        } else {
            Seq<ItemStack> items = entry.items;
            Seq<LiquidStack> fluids = entry.fluids;
            boolean outputPower = entry.power > 0.0F;
            boolean outputHeat = entry.heat > 0.0F;
            if (items.size > 0) {
                return new Image(((ItemStack)items.get(0)).item.uiIcon);
            } else if (fluids.size > 0) {
                return new Image(((LiquidStack)fluids.get(0)).liquid.uiIcon);
            } else if (outputPower) {
                Image img = new Image(Icon.power.getRegion());
                img.setColor(Pal.power);
                return img;
            } else if (outputHeat) {
                Image img = new Image(Icon.terrain.getRegion());
                img.setColor(b.heatColor);
                return img;
            } else {
                return new Image(Icon.cancel.getRegion());
            }
        }
    }
}
