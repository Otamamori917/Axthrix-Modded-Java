package axthrix.world.util;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.Element;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import axthrix.world.types.block.defense.MultiTurretType;
import axthrix.world.types.weapontypes.BlockWeapon;
import mindustry.Vars;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.maps.Map;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import axthrix.world.types.recipes.*;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.*;
import axthrix.world.types.block.*;
import static arc.Core.*;


import static mindustry.Vars.*;
import static mindustry.world.meta.StatValues.displayItem;
import static mindustry.world.meta.StatValues.displayLiquid;

public class AxStatValues {
    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map){
        return ammo(map, 0, false);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean showUnit){
        return ammo(map, 0, showUnit);
    }

    public static StatValue blocks(Seq<Block> attr, boolean floating) {
        return blocks(attr, floating, true);
    }

    public static StatValue kisten() {
        return (table) -> table.table((c) -> {
            c.row();
            c.image(atlas.find("aj-meepyboi")).padTop(8f).scaling(Scaling.fit);
        });
    }

    public static StatValue image(String spritename,String name) {
        return (table) -> table.table((c) -> {
            c.image(atlas.find(spritename));
            c.add(name).marginRight(4);
        });
    }

    public static StatValue blocks(Seq<Block> attr, boolean floating, boolean checkFloors) {
        return (table) -> table.table((c) -> {
            Runnable[] rebuild = new Runnable[]{null};
            Map[] lastMap = new Map[]{null};
            rebuild[0] = () -> {
                c.clearChildren();
                c.left();
                if (state.isGame()) {
                    Seq<Block> blocks = content.blocks().select((blockx) -> {
                        boolean var10000;
                        label36: {
                            if ((!checkFloors || blockx instanceof Floor) && indexer.isBlockPresent(blockx)) {
                                if (!(blockx instanceof Floor f)) {
                                    break label36;
                                }

                                if (!f.isDeep() || floating) {
                                    break label36;
                                }
                            }

                            var10000 = false;
                            return var10000;
                        }

                        var10000 = true;
                        return var10000;
                    });
                    if (blocks.any()) {
                        int i = 0;

                        for(Block block : blocks) {
                            attr.each(fl -> {
                                if (fl == block) content(block,12).display(c);
                            });
                            ++i;
                            if (i % 5 == 0) {
                                c.row();
                            }
                        }
                    } else {
                        c.add("@none.inmap");
                    }
                } else {
                    c.add("@stat.showinmap");
                }

            };
            rebuild[0].run();
            c.update(() -> {
                Map current = state.isGame() ? state.map : null;
                if (current != lastMap[0]) {
                    rebuild[0].run();
                    lastMap[0] = current;
                }

            });
        });
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent, boolean showUnit){
        return table -> {
            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for(T t: orderedKeys){
                boolean compact = t instanceof UnitType && !showUnit || indent > 0;
                boolean payload = t instanceof Block || (t instanceof UnitType && !showUnit);

                if(payload && t instanceof PayloadAmmoBlock m && !m.displayCampaign && state.isCampaign()) continue;

                BulletType type = map.get(t);

                if(type.spawnUnit != null && type.spawnUnit.weapons.size > 0){
                    ammo(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), indent, false).display(table);
                    continue;
                }

                table.table(compact ? null : Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();

                    //no point in displaying unit icon twice
                    if(!compact && !(t instanceof PowerTurret)){
                        bt.table(title -> {
                            if(payload){
                                if(t.unlockedNow()){
                                    title.image(icon(t)).size(96f).padRight(4).right().top();
                                    title.table(n -> {
                                        n.add(t.localizedName);
                                        n.row();
                                        infoButton(n, t, 4f * 8f).padTop(4f);
                                    }).padRight(10).left().top();
                                }else{
                                    title.image(Icon.lock).color(Pal.darkerGray).size(40).padRight(4).right().top();
                                    title.add("@missing-research").left().colspan(2);
                                }
                            }else{
                                title.image(icon(t)).size(3 * 8).padRight(4).right().scaling(Scaling.fit).top();
                                title.add(t.localizedName).padRight(10).left().top();
                            }
                        });
                        if(payload && !t.unlockedNow()) return;
                        bt.row();
                    }
                }).padLeft(indent * 5).padTop(5).padBottom(compact ? 0 : 5).growX().margin(compact ? 0 : 10);

                table.row();
            }
        };
    }

    public static StatValue payloadProducts(Seq<PayloadRecipe> products){
        return table -> {
            table.row();

            for(PayloadRecipe payloadRecipe : products){
                table.table(Styles.grayPanel, t -> {
                    Block out = payloadRecipe.outputBlock;

                    if(state.rules.bannedBlocks.contains(out)){
                        t.image(Icon.cancel).color(Pal.remove).size(40);
                        return;
                    }

                    if(payloadRecipe.unlocked()){
                        if(payloadRecipe.hasInputBlock()){
                            t.table(i -> {
                                i.left();

                                i.image(payloadRecipe.inputBlock.fullIcon).size(40).left().scaling(Scaling.fit);
                                i.add(payloadRecipe.inputBlock.localizedName).padLeft(8f).left();
                                infoButton(i, payloadRecipe.inputBlock, 32).padLeft(8f).left();

                                i.image(Icon.right).color(Pal.darkishGray).size(40).pad(8f).center();

                                i.image(out.fullIcon).size(40).right().scaling(Scaling.fit);
                                i.add(out.localizedName).padLeft(8f).right();
                                infoButton(i, out, 32).padLeft(8f).right();
                            }).left().padTop(5).padBottom(5);
                            t.row();
                            t.add(Strings.autoFixed(payloadRecipe.craftTime / 60f, 1) + " " + StatUnit.seconds.localized()).color(Color.lightGray).padLeft(10f).left();
                            if(payloadRecipe.powerUse > 0){
                                t.row();
                                t.add(Strings.autoFixed(payloadRecipe.powerUse * 60f, 1) + " " + StatUnit.powerSecond.localized()).color(Color.lightGray).padLeft(10f).left();
                            }
                            t.row();
                        }else{
                            t.image(out.uiIcon).size(40).pad(10f).left().top();
                            t.table(info -> {
                                info.top().defaults().left();

                                info.add(out.localizedName);
                                infoButton(info, out, 32).padLeft(8f).expandX();

                                info.row();
                                info.add(Strings.autoFixed(payloadRecipe.craftTime / 60f, 1) + " " + StatUnit.seconds.localized()).color(Color.lightGray).colspan(2);
                                if(payloadRecipe.powerUse > 0){
                                    info.row();
                                    info.add(Strings.autoFixed(payloadRecipe.powerUse * 60f, 1) + " " + StatUnit.powerSecond.localized()).color(Color.lightGray).colspan(2);
                                }
                            }).top();
                        }

                        if(payloadRecipe.showReqList()){
                            t.table(req -> {
                                if(payloadRecipe.hasInputBlock()){
                                    req.left().defaults().left();
                                }else{
                                    req.right().defaults().right();
                                }

                                int i = 0;
                                int col = payloadRecipe.hasInputBlock() ? 12 : payloadRecipe.powerUse > 0 ? 4 : 6;
                                if(payloadRecipe.itemRequirements.length > 0){
                                    while(i < payloadRecipe.itemRequirements.length){
                                        if(i % col == 0) req.row();

                                        ItemStack stack = payloadRecipe.itemRequirements[i];
                                        req.add(displayItem(stack.item, stack.amount, false)).pad(5);

                                        i++;
                                    }
                                }
                                if(payloadRecipe.liquidRequirements != null){
                                    if(i % col == 0) req.row();
                                    req.add(new NamelessLiquidDis(payloadRecipe.liquidRequirements.liquid, payloadRecipe.liquidRequirements.amount, false)).pad(5);
                                }
                            }).right().top().grow().pad(10f);
                        }
                    }else{
                        t.image(Icon.lock).color(Pal.darkerGray).size(40);
                        t.add("@missing-research");
                    }
                }).growX().pad(5);
                table.row();
            }
        };
    }
    public static StatValue blockWeapons(MultiTurretType type, Seq<BlockWeapon> weapons){
        return table -> {
            table.row();
            for(int i = 0; i < weapons.size; i++){
                BlockWeapon weapon = weapons.get(i);

                if(weapon.flipSprite || !weapon.hasStats(type)){
                    //flipped weapons are not given stats
                    continue;
                }

                TextureRegion region = !weapon.name.isEmpty() ? Core.atlas.find(weapon.name + "-preview", weapon.region) : null;

                table.table(Styles.grayPanel, w -> {
                    w.left().top().defaults().padRight(3).left();
                    if(region != null && region.found()) w.image(region).size(60).scaling(Scaling.bounded).left().top();
                    w.row();

                    weapon.addStats(type);
                }).growX().pad(5).margin(10);
                table.row();
            }
        };
    }

    public static StatValue content(UnlockableContent content,float left){
        return table -> {
            table.row();
            table.table(t -> {
                t.image(icon(content)).size(3 * 8);
                t.add("[lightgray]" + content.localizedName).padLeft(left);
                infoButton(t, content, 4 * 8).padLeft(left);
            });
        };
    }

    public static Cell<TextButton> infoButton(Table table, UnlockableContent content, float size){
        return table.button("?", Styles.flatBordert, () -> ui.content.show(content)).size(size).left().name("contentinfo");
    }

    private static TextureRegion icon(UnlockableContent t){
        return t.fullIcon;
    }


}
