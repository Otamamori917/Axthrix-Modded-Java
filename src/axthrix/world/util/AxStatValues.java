package axthrix.world.util;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import axthrix.AxthrixLoader;
import axthrix.world.types.block.defense.MultiTurretType;
import axthrix.world.types.block.defense.PayloadTurretType;
import axthrix.world.types.bulletypes.*;
import axthrix.world.types.weapontypes.BlockWeapon;
import mindustry.Vars;
import mindustry.content.StatusEffects;
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
        return ammoPayload(map, indent, showUnit, -1f, -1f);
    }

    public static <T extends UnlockableContent> StatValue ammoheat(ObjectMap<T, BulletType> map, int indent, boolean showUnit, float heat, float reload){
        return ammoPayload(map, indent, showUnit, heat, reload);
    }

    /**
     * Shared implementation behind the payload-style {@link #ammo(ObjectMap, int, boolean)} and
     * {@link #ammoheat(ObjectMap, int, boolean, float, float)}. Always shows the big payload icon
     * (or lock/research-gate) layout, always shows the full vanilla bullet stat body plus the
     * Axthrix custom per-bullet-type panel underneath — even for nested/compact entries — and
     * optionally shows a heat-per-second line when heat >= 0 and reload > 0.
     */
    private static <T extends UnlockableContent> StatValue ammoPayload(ObjectMap<T, BulletType> map, int indent, boolean showUnit, float heat, float reload){
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
                    ammoPayload(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), indent, false, heat, reload).display(table);
                    continue;
                }

                boolean locked = payload && !t.unlockedNow();

                table.table(compact ? null : Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();


                    if(!compact && !(t instanceof PowerTurret)){
                        bt.table(title -> {
                            if(payload){
                                if(!locked){
                                    title.image(icon(t)).size(96f).padRight(4).right().top();
                                    title.table(n -> {
                                        n.add(t.localizedName);
                                        n.row();
                                        infoButton(n, t, 4f * 8f).padTop(4f);
                                        if(heat >= 0f && reload > 0f && t instanceof Block b){
                                            float heatPerSec = (heat * b.size * 60f) / (reload * type.reloadMultiplier);

                                            String prefix = (heatPerSec > 0) ? "[red]+" : "[cyan]";
                                            String value = TempUnit.formatDelta(heatPerSec);

                                            n.add(prefix + value + StatUnit.perSecond.localized());
                                        }
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
                        bt.row();
                    }

                    if(!locked){

                        Table extra = new Table();
                        extra.left().defaults().padRight(3).left();
                        appendCustomStats(extra, type, t);
                        bt.table(Styles.grayPanel, et -> et.add(extra).left().grow()).padLeft(5).padTop(0).padBottom(5).growX().margin(10);
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
                    continue;
                }

                TextureRegion region = !weapon.name.isEmpty() ? Core.atlas.find(weapon.name + "-preview", weapon.region) : null;

                table.table(Styles.grayPanel, w -> {
                    w.left().top().defaults().padRight(3).left();
                    if(region != null && region.found()) w.image(region).size(60).scaling(Scaling.bounded).left().top();
                    w.row();

                    weapon.displayStats(w);
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




    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean nested, boolean showUnit, @Nullable String blockName){


        return table -> {
            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for(T t : orderedKeys){
                BulletType type = map.get(t);


                if(type.spawnUnit != null && type.spawnUnit.weapons.size > 0){
                    type = type.spawnUnit.weapons.first().bullet;
                }

                BulletType finalType = type;
                table.table(Styles.grayPanel, bt -> {
                    bt.left().top().defaults().padRight(3).left();

                    //icon + name so it's clear which ammo (item/liquid) this stat block belongs to
                    if(!nested){
                        bt.table(title -> {
                            title.image(icon(t)).size(3 * 8).padRight(4).right().scaling(Scaling.fit).top();
                            title.add(t.localizedName).padRight(10).left().top();
                        });
                        bt.row();
                    }

                    Table extra = new Table();
                    extra.left().defaults().padRight(3).left();
                    appendCustomStats(extra, finalType, t);
                    bt.table(Styles.grayPanel, et -> et.add(extra).left().grow()).padLeft(5).padTop(0).padBottom(5).growX().margin(10);
                }).padLeft(5).padTop(5).padBottom(5).growX().margin(10);

                table.row();
            }
        };
    }

    /**
     * Appends the Axthrix custom per-bullet-type stat panel (Tornado, Rend, Temperature, etc)
     * onto the given table. Returns true if anything was appended, false if this BulletType has
     * no custom stats to show. Public so other stat-building code (e.g. BlockWeapon) can reuse it
     * without duplicating the instanceof chain.
     */
    public static boolean appendCustomStats(Table t, BulletType type, UnlockableContent tb){
        boolean any = false;

        if(type instanceof TornadoBulletType b){
            line(t, "stat.aj-tornado-damage-radius", tiles(b.damageRadius)).left();
            line(t, "stat.aj-tornado-suction-radius", tiles(b.suctionRadius)).left();
            line(t, "stat.aj-tornado-force", fx(b.force) + " ~ " + fx(b.scaledForce)).left();
            line(t, "stat.aj-tornado-grow-time", secs(b.growTime)).left();
            line(t, "stat.aj-tornado-shrink-time", secs(b.shrinkTime)).left();
            any = true;
        }

        if(type instanceof blackhole.entities.bullet.BlackHoleBulletType b){
            line(t, "stat.aj-blackhole-damage-radius", tiles(b.damageRadius)).left();
            line(t, "stat.aj-blackhole-suction-radius", tiles(b.suctionRadius)).left();
            line(t, "stat.aj-blackhole-force", fx(b.force) + " ~ " + fx(b.scaledForce)).left();
            line(t, "stat.aj-blackhole-grow-time", secs(b.growTime)).left();
            line(t, "stat.aj-blackhole-shrink-time", secs(b.shrinkTime)).left();
            any = true;
        }

        if(type instanceof AnimationBulletType){
            header(t, "stat.aj-animation-header");
            any = true;
        }

        if(type instanceof InfFragBulletType inf){
            header(t, "stat.aj-inffrag-header");
            t.row();
            t.add("[lightgray]" + Core.bundle.get("stat.aj-inffrag-fragbullets") + " ").left();
            t.add(new RainbowWiggleLabel(Core.bundle.get("stat.aj-inffrag-everything"))).left();
            appendInfFragOptions(t, inf);
            any = true;
        }

        if(type instanceof TemperatureBulletType b){
            String prefix = (b.temperaturePerHit > 0) ? "[red]+" : "[cyan]";
            String key = (b.temperaturePerHit > 0) ? "stat.aj-heat-per-hit" : "stat.aj-cold-per-hit";
            line(t, key, prefix + TempUnit.formatDelta(b.temperaturePerHit)).left();
            any = true;
        }

        if(type instanceof RangBulletType b){
            float returnTimeSeconds = (b.lifetime * b.returnTrigger) / 60f;
            String prefix = (b.returnHeatAmount > 0) ? "[red]+" : "[cyan]";
            line(t, "stat.aj-rang-return", prefix + TempUnit.formatDelta(b.returnHeatAmount)).left();
            line(t, "stat.aj-rang-return-time", Strings.autoFixed(returnTimeSeconds, 2) + " " + mindustry.world.meta.StatUnit.seconds.localized()).left();
            any = true;
        }

        if(type instanceof VariantBulletType b){
            header(t, "stat.aj-variant-header");
            float totalWeight = 0f;
            if(b.useWeights && !b.variantWeights.isEmpty()){
                int limit = Math.min(b.variants.size, b.variantWeights.size);
                for(int i = 0; i < limit; i++) totalWeight += b.variantWeights.get(i);
            }
            final float finalTotalWeight = totalWeight;

            for(int i = 0; i < b.variants.size; i++){
                BulletType variant = b.variants.get(i);
                String chanceStr;
                if(b.useWeights && finalTotalWeight > 0f && i < b.variantWeights.size){
                    chanceStr = Strings.autoFixed((b.variantWeights.get(i) / finalTotalWeight) * 100f, 1) + "%";
                }else{
                    chanceStr = Strings.autoFixed((1f / b.variants.size) * 100f, 1) + "%";
                }

                t.row();
                t.add("[lightgray]" + Core.bundle.get("stat.aj-variant-bullet") + " #" + (i + 1) + " (Chance: [white]" + chanceStr + "[lightgray])[]").left();
                t.table(vStats -> {
                    baseBullet(variant, vStats, true, tb);
                    appendCustomStats(vStats, variant, tb);
                }).padLeft(12f).left();
            }
            any = true;
        }

        if(type instanceof WarningBulletType b){
            line(t, "stat.aj-warning-radius", tiles(b.spriteRadius)).left();
            line(t, "stat.aj-warning-slow-duration", secs(b.warningDuration)).left();
            any = true;
        }
        if(type instanceof AfterShockBulletType b){
            boolean puddle = b instanceof FirePuddleBulletType;
            header(t, puddle ? "stat.aj-firepuddle-header" : "stat.aj-aftershock-header");
            line(t, "stat.aj-aftershock-pulses", String.valueOf(b.splashAmount)).left();
            line(t, "stat.aj-aftershock-pulse-delay", secs(b.splashDelay)).left();
            if(!puddle){
                line(t, "stat.aj-aftershock-pulse-damage", fx(b.splashDamage)).left();
                line(t, "stat.aj-aftershock-pulse-radius", tiles(b.splashDamageRadius)).left();
            }
            any = true;
        }

        if(type instanceof AttachmentGrenadeBulletType b){
            line(t, "stat.aj-grenade-attach-duration", secs(b.attachDuration)).left();
            line(t, "stat.aj-grenade-detonation-damage", fx(b.detonationDamage)).left();
            line(t, "stat.aj-grenade-detonation-radius", tiles(b.detonationRadius)).left();
            any = true;
        }

        if(type instanceof BoltBulletType b){
            line(t, "stat.aj-bolt-length", b.boltLength + (b.boltLengthRand > 0 ? " ~ +" + b.boltLengthRand : "") + " " + unit("stat.aj-bolt-links")).left();
            any = true;
        }

        if(type instanceof ConeBulletType b){
            line(t, "stat.aj-cone-angle", Strings.autoFixed(b.cone * 2f, 0) + "\u00b0").left();
            line(t, "stat.aj-cone-length", tiles(b.length)).left();
            if(b.healPercent > 0) line(t, "stat.aj-cone-heal", Strings.autoFixed(b.healPercent, 1) + "%").left();
            any = true;
        }

        if(type instanceof DetonatorBulletType b){
            String suffix = b.damageOnHit ? "" : " " + unit("stat.aj-detonator-grenade-only");
            line(t, "stat.aj-detonator-hit-damage", fx(b.baseHitDamage) + suffix).left();
            any = true;
        }

        if(type instanceof GluonBulletType b){
            line(t, "stat.aj-gluon-snap-damage", fx(b.snapDamage)).left();
            line(t, "stat.aj-gluon-tether-duration", secs(b.tetherDuration)).left();
            line(t, "stat.aj-gluon-unit-tether", tiles(b.maxUnitTetherDistance)).left();
            line(t, "stat.aj-gluon-building-tether", tiles(b.maxBuildingTetherDistance) + " (" + b.maxBuildingsPerHit + " " + unit("stat.aj-gluon-building-tether-max") + ")").left();
            any = true;
        }

        if(type instanceof PlasmaCapacitorBulletType b){
            line(t, "stat.aj-capacitor-charge-time", secs(b.chargeDuration)).left();
            line(t, "stat.aj-capacitor-siphon-rate", "[red]-" + TempUnit.formatDelta(b.heatSiphonPerFrame*60) + StatUnit.perSecond.localized()).left();
            line(t, "stat.aj-capacitor-scale-factor", b.damagePerHeatStored +" [lightgray]"+ Stat.damage.localized()).left();
            any = true;
        }


        if(type instanceof ContinuousFractalLightBulletType b){

            if(b.heatApplyPerTick != 0f){
                String prefix = (b.heatApplyPerTick > 0) ? "[red]+" : "[cyan]";
                String key = (b.heatApplyPerTick > 0) ? "stat.aj-heat-per-hit" : "stat.aj-cold-per-hit";
                line(t, key, prefix + TempUnit.formatDelta(b.heatApplyPerTick*60) +" "+StatUnit.perSecond.localized()).left();
            }

            if(b.laserFractals > 0){
                t.row();
                Table fc = new Table();
                fc.left().defaults().left();

                float combinedSpread = b.fractalSpread * b.laserFractals;
                float actualFragDamage = b.damage * b.fractalDamageMult;

                fc.table(l -> line(l, "stat.aj-fractal-spread-combined", Strings.autoFixed(combinedSpread, 1) + "° "+StatUnit.degrees.localized())).right().row();
                fc.table(l -> line(l, "stat.aj-fractal-dmg-per", "  "+fx(actualFragDamage) +"[lightgray] "+Stat.damage.localized()+StatUnit.perSecond.localized())).right().row();

                Collapser coll = new Collapser(fc, true);
                coll.setDuration(0.1f);

                t.table(ft -> {
                    ft.left().defaults().left();
                    ft.add("[lightgray] " + Core.bundle.format("bullet.aj-fractals-count", b.laserFractals));
                    ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false))
                            .update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen))
                            .size(8).padLeft(16f).expandX();
                }).left();
                t.row();
                t.add(coll).left();
            }
            any = true;
        }

        if(type instanceof GrabBulletType b){
            line(t, "stat.aj-grab-duration", secs(b.grabDuration)).left();
            line(t, "stat.aj-grab-hold-damage", fx(b.holdDamage) + " / " + secs(b.holdDamageInterval)).left();
            line(t, "stat.aj-grab-size-ratio", Strings.autoFixed(b.maxSizeRatio, 2) + "x").left();
            any = true;
        }

        if(type instanceof GravityWellBulletType b){
            line(t, "stat.aj-gravitywell-radius", tiles(b.gravityRadius)).left();
            line(t, "stat.aj-gravitywell-duration", secs(b.gravityDuration)).left();
            line(t, "stat.aj-gravitywell-damage", fx(b.continuousDamage) + " (x" + Strings.autoFixed(b.flyingDamageMultiplier, 1) + " " + unit("stat.aj-gravitywell-flying-mult") + ")").left();
            any = true;
        }

        if(type instanceof HarpoonBulletType b){
            line(t, "stat.aj-harpoon-duration", secs(b.grabDuration)).left();
            line(t, "stat.aj-harpoon-hold-damage", fx(b.holdDamage) + " / " + secs(b.holdDamageInterval)).left();
            line(t, "stat.aj-harpoon-reel-speed", Strings.autoFixed(b.reelSpeed, 1)).left();
            any = true;
        }

        if(type instanceof MineBulletType b){
            header(t, "stat.aj-mine-header");
            line(t, "stat.aj-mine-places", b.mine != null ? b.mine.localizedName : "?").left();
            if(b.createChance) line(t, "stat.aj-mine-chance", Strings.autoFixed(b.createChancePercent * 100f, 1) + "%").left();
            any = true;
        }

        if(type instanceof NanobotBulletType b){
            if(b.activeWhileFlying){
                line(t, "stat.aj-nanobot-flying", fx(b.flyingNanoDamage) + " dmg / " + fx(b.flyingNanoHealAmount) + " heal, " + tiles(b.flyingRange)).left();
            }
            if(b.lingersOnHit){
                line(t, "stat.aj-nanobot-linger", fx(b.lingerNanoDamage) + " dmg / " + fx(b.lingerNanoHealAmount) + " heal, " + secs(b.lingerDuration) + ", " + tiles(b.lingerRange)).left();
            }
            any = true;
        }

        if(type instanceof RendBulletType b){
            line(t, "stat.aj-rend-cone", Strings.autoFixed(b.cone * 2f, 0) + "\u00b0 / " + tiles(b.length)).left();
            line(t, "stat.aj-rend-swipes", b.totalSwipes + " x " + fx(b.baseDamage) + " (" + unit("stat.aj-rend-falloff") + " " + Strings.autoFixed(b.damageFalloff * 100f, 0) + "%)").left();
            line(t, "stat.aj-rend-dot", fx(b.rendDoTDamage) + "/tick x6, " + secs(b.dotDuration)).left();
            any = true;
        }

        if(type instanceof SheildArcBullet b){
            line(t, "stat.aj-shieldarc-max-hp", fx(b.max)).left();
            line(t, "stat.aj-shieldarc-radius", tiles(b.radius)).left();
            any = true;
        }

        if(type instanceof StaticFieldBulletType b){
            line(t, "stat.aj-staticfield-radius", tiles(b.fieldRadius)).left();
            line(t, "stat.aj-staticfield-duration", secs(b.fieldDuration)).left();
            line(t, "stat.aj-staticfield-pulse", fx(b.fieldDamagePerPulse) + " / " + secs(b.fieldPulseInterval)).left();
            line(t, "stat.aj-staticfield-min-health", Strings.autoFixed(b.minHealthFraction * 100f, 0) + "%").left();
            any = true;
        }
        t.row();
        baseBullet(type, t, true, tb);

        return any;
    }
    public static boolean appendCustomStats(Table t, BulletType type){
        boolean any = false;

        if(type instanceof TornadoBulletType b){
            line(t, "stat.aj-tornado-damage-radius", tiles(b.damageRadius)).left();
            line(t, "stat.aj-tornado-suction-radius", tiles(b.suctionRadius)).left();
            line(t, "stat.aj-tornado-force", fx(b.force) + " ~ " + fx(b.scaledForce)).left();
            line(t, "stat.aj-tornado-grow-time", secs(b.growTime)).left();
            line(t, "stat.aj-tornado-shrink-time", secs(b.shrinkTime)).left();
            any = true;
        }

        if(type instanceof blackhole.entities.bullet.BlackHoleBulletType b){
            line(t, "stat.aj-blackhole-damage-radius", tiles(b.damageRadius)).left();
            line(t, "stat.aj-blackhole-suction-radius", tiles(b.suctionRadius)).left();
            line(t, "stat.aj-blackhole-force", fx(b.force) + " ~ " + fx(b.scaledForce)).left();
            line(t, "stat.aj-blackhole-grow-time", secs(b.growTime)).left();
            line(t, "stat.aj-blackhole-shrink-time", secs(b.shrinkTime)).left();
            any = true;
        }

        if(type instanceof AnimationBulletType){
            header(t, "stat.aj-animation-header");
            any = true;
        }

        if(type instanceof InfFragBulletType inf){
            header(t, "stat.aj-inffrag-header");
            t.row();
            t.add("[lightgray]" + Core.bundle.get("stat.aj-inffrag-fragbullets") + " ").left();
            t.add(new RainbowWiggleLabel(Core.bundle.get("stat.aj-inffrag-everything"))).left();
            any = true;
        }

        if(type instanceof TemperatureBulletType b){
            String prefix = (b.temperaturePerHit > 0) ? "[red]+" : "[cyan]";
            String key = (b.temperaturePerHit > 0) ? "stat.aj-heat-per-hit" : "stat.aj-cold-per-hit";
            line(t, key, prefix + TempUnit.formatDelta(b.temperaturePerHit)).left();
            any = true;
        }

        if(type instanceof RangBulletType b){
            float returnTimeSeconds = (b.lifetime * b.returnTrigger) / 60f;
            String prefix = (b.returnHeatAmount > 0) ? "[red]+" : "[cyan]";
            line(t, "stat.aj-rang-return", prefix + TempUnit.formatDelta(b.returnHeatAmount)).left();
            line(t, "stat.aj-rang-return-time", Strings.autoFixed(returnTimeSeconds, 2) + " " + mindustry.world.meta.StatUnit.seconds.localized()).left();
            any = true;
        }

        if(type instanceof VariantBulletType b){
            header(t, "stat.aj-variant-header");
            float totalWeight = 0f;
            if(b.useWeights && !b.variantWeights.isEmpty()){
                int limit = Math.min(b.variants.size, b.variantWeights.size);
                for(int i = 0; i < limit; i++) totalWeight += b.variantWeights.get(i);
            }
            final float finalTotalWeight = totalWeight;

            for(int i = 0; i < b.variants.size; i++){
                BulletType variant = b.variants.get(i);
                String chanceStr;
                if(b.useWeights && finalTotalWeight > 0f && i < b.variantWeights.size){
                    chanceStr = Strings.autoFixed((b.variantWeights.get(i) / finalTotalWeight) * 100f, 1) + "%";
                }else{
                    chanceStr = Strings.autoFixed((1f / b.variants.size) * 100f, 1) + "%";
                }

                t.row();
                t.add("[lightgray]" + Core.bundle.get("stat.aj-variant-bullet") + " #" + (i + 1) + " (Chance: [white]" + chanceStr + "[lightgray])[]").left();
                t.table(vStats -> {
                    appendCustomStats(vStats, variant);
                }).padLeft(12f).left();
            }
            any = true;
        }

        if(type instanceof WarningBulletType b){
            line(t, "stat.aj-warning-radius", tiles(b.spriteRadius)).left();
            line(t, "stat.aj-warning-slow-duration", secs(b.warningDuration)).left();
            any = true;
        }
        if(type instanceof AfterShockBulletType b){
            boolean puddle = b instanceof FirePuddleBulletType;
            header(t, puddle ? "stat.aj-firepuddle-header" : "stat.aj-aftershock-header");
            line(t, "stat.aj-aftershock-pulses", String.valueOf(b.splashAmount)).left();
            line(t, "stat.aj-aftershock-pulse-delay", secs(b.splashDelay)).left();
            if(!puddle){
                line(t, "stat.aj-aftershock-pulse-damage", fx(b.splashDamage)).left();
                line(t, "stat.aj-aftershock-pulse-radius", tiles(b.splashDamageRadius)).left();
            }
            any = true;
        }

        if(type instanceof AttachmentGrenadeBulletType b){
            line(t, "stat.aj-grenade-attach-duration", secs(b.attachDuration)).left();
            line(t, "stat.aj-grenade-detonation-damage", fx(b.detonationDamage)).left();
            line(t, "stat.aj-grenade-detonation-radius", tiles(b.detonationRadius)).left();
            any = true;
        }

        if(type instanceof BoltBulletType b){
            line(t, "stat.aj-bolt-length", b.boltLength + (b.boltLengthRand > 0 ? " ~ +" + b.boltLengthRand : "") + " " + unit("stat.aj-bolt-links")).left();
            any = true;
        }

        if(type instanceof ConeBulletType b){
            line(t, "stat.aj-cone-angle", Strings.autoFixed(b.cone * 2f, 0) + "\u00b0").left();
            line(t, "stat.aj-cone-length", tiles(b.length)).left();
            if(b.healPercent > 0) line(t, "stat.aj-cone-heal", Strings.autoFixed(b.healPercent, 1) + "%").left();
            any = true;
        }

        if(type instanceof DetonatorBulletType b){
            String suffix = b.damageOnHit ? "" : " " + unit("stat.aj-detonator-grenade-only");
            line(t, "stat.aj-detonator-hit-damage", fx(b.baseHitDamage) + suffix).left();
            any = true;
        }

        if(type instanceof GluonBulletType b){
            line(t, "stat.aj-gluon-snap-damage", fx(b.snapDamage)).left();
            line(t, "stat.aj-gluon-tether-duration", secs(b.tetherDuration)).left();
            line(t, "stat.aj-gluon-unit-tether", tiles(b.maxUnitTetherDistance)).left();
            line(t, "stat.aj-gluon-building-tether", tiles(b.maxBuildingTetherDistance) + " (" + b.maxBuildingsPerHit + " " + unit("stat.aj-gluon-building-tether-max") + ")").left();
            any = true;
        }

        if(type instanceof PlasmaCapacitorBulletType b){
            line(t, "stat.aj-capacitor-charge-time", secs(b.chargeDuration)).left();
            line(t, "stat.aj-capacitor-siphon-rate", "[red]-" + TempUnit.formatDelta(b.heatSiphonPerFrame*60) + StatUnit.perSecond.localized()).left();
            line(t, "stat.aj-capacitor-scale-factor", b.damagePerHeatStored +" [lightgray]"+ Stat.damage.localized()).left();
            any = true;
        }

        if(type instanceof ContinuousFractalLightBulletType b){

            if(b.heatApplyPerTick != 0f){
                String prefix = (b.heatApplyPerTick > 0) ? "[red]+" : "[cyan]";
                String key = (b.heatApplyPerTick > 0) ? "stat.aj-heat-per-hit" : "stat.aj-cold-per-hit";
                line(t, key, prefix + TempUnit.formatDelta(b.heatApplyPerTick*60) +" "+StatUnit.perSecond.localized()).left();
            }

            if(b.laserFractals > 0){
                t.row();
                Table fc = new Table();
                fc.left().defaults().left();

                float combinedSpread = b.fractalSpread * b.laserFractals;
                float actualFragDamage = b.damage * b.fractalDamageMult;

                fc.table(l -> line(l, "stat.aj-fractal-spread-combined", Strings.autoFixed(combinedSpread, 1) + "° "+StatUnit.degrees.localized())).right().row();
                fc.table(l -> line(l, "stat.aj-fractal-dmg-per", "  "+fx(actualFragDamage) +"[lightgray] "+Stat.damage.localized()+StatUnit.perSecond.localized())).right().row();

                Collapser coll = new Collapser(fc, true);
                coll.setDuration(0.1f);

                t.table(ft -> {
                    ft.left().defaults().left();
                    ft.add("[lightgray] " + Core.bundle.format("bullet.aj-fractals-count", b.laserFractals));
                    ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false))
                            .update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen))
                            .size(8).padLeft(16f).expandX();
                }).left();
                t.row();
                t.add(coll).left();
            }
            any = true;
        }


        if(type instanceof GrabBulletType b){
            line(t, "stat.aj-grab-duration", secs(b.grabDuration)).left();
            line(t, "stat.aj-grab-hold-damage", fx(b.holdDamage) + " / " + secs(b.holdDamageInterval)).left();
            line(t, "stat.aj-grab-size-ratio", Strings.autoFixed(b.maxSizeRatio, 2) + "x").left();
            any = true;
        }

        if(type instanceof GravityWellBulletType b){
            line(t, "stat.aj-gravitywell-radius", tiles(b.gravityRadius)).left();
            line(t, "stat.aj-gravitywell-duration", secs(b.gravityDuration)).left();
            line(t, "stat.aj-gravitywell-damage", fx(b.continuousDamage) + " (x" + Strings.autoFixed(b.flyingDamageMultiplier, 1) + " " + unit("stat.aj-gravitywell-flying-mult") + ")").left();
            any = true;
        }

        if(type instanceof HarpoonBulletType b){
            line(t, "stat.aj-harpoon-duration", secs(b.grabDuration)).left();
            line(t, "stat.aj-harpoon-hold-damage", fx(b.holdDamage) + " / " + secs(b.holdDamageInterval)).left();
            line(t, "stat.aj-harpoon-reel-speed", Strings.autoFixed(b.reelSpeed, 1)).left();
            any = true;
        }

        if(type instanceof MineBulletType b){
            header(t, "stat.aj-mine-header");
            line(t, "stat.aj-mine-places", b.mine != null ? b.mine.localizedName : "?").left();
            if(b.createChance) line(t, "stat.aj-mine-chance", Strings.autoFixed(b.createChancePercent * 100f, 1) + "%").left();
            any = true;
        }

        if(type instanceof NanobotBulletType b){
            if(b.activeWhileFlying){
                line(t, "stat.aj-nanobot-flying", fx(b.flyingNanoDamage) + " dmg / " + fx(b.flyingNanoHealAmount) + " heal, " + tiles(b.flyingRange)).left();
            }
            if(b.lingersOnHit){
                line(t, "stat.aj-nanobot-linger", fx(b.lingerNanoDamage) + " dmg / " + fx(b.lingerNanoHealAmount) + " heal, " + secs(b.lingerDuration) + ", " + tiles(b.lingerRange)).left();
            }
            any = true;
        }

        if(type instanceof RendBulletType b){
            line(t, "stat.aj-rend-cone", Strings.autoFixed(b.cone * 2f, 0) + "\u00b0 / " + tiles(b.length)).left();
            line(t, "stat.aj-rend-swipes", b.totalSwipes + " x " + fx(b.baseDamage) + " (" + unit("stat.aj-rend-falloff") + " " + Strings.autoFixed(b.damageFalloff * 100f, 0) + "%)").left();
            line(t, "stat.aj-rend-dot", fx(b.rendDoTDamage) + "/tick x6, " + secs(b.dotDuration)).left();
            any = true;
        }

        if(type instanceof SheildArcBullet b){
            line(t, "stat.aj-shieldarc-max-hp", fx(b.max)).left();
            line(t, "stat.aj-shieldarc-radius", tiles(b.radius)).left();
            any = true;
        }

        if(type instanceof StaticFieldBulletType b){
            line(t, "stat.aj-staticfield-radius", tiles(b.fieldRadius)).left();
            line(t, "stat.aj-staticfield-duration", secs(b.fieldDuration)).left();
            line(t, "stat.aj-staticfield-pulse", fx(b.fieldDamagePerPulse) + " / " + secs(b.fieldPulseInterval)).left();
            line(t, "stat.aj-staticfield-min-health", Strings.autoFixed(b.minHealthFraction * 100f, 0) + "%").left();
            any = true;
        }

        return any;
    }





        /**
         * Builds a collapsible dropdown listing every bullet InfFragBulletType could possibly frag
         * into (drawn from AxthrixLoader.allBullets, respecting allowKillShooter the same way
         * getBullet() does). Since the pool can legitimately contain this exact InfFragBulletType
         * itself (it can frag into itself), any candidate that is itself an InfFragBulletType is
         * shown as a plain label instead of recursing into another full stat panel + another
         * possible-frags dropdown, which would otherwise nest infinitely.
             */
    private static void appendInfFragOptions(Table t, InfFragBulletType inf){
        Seq<AxthrixLoader.BulletData> candidates = AxthrixLoader.allBullets.select(d -> inf.allowKillShooter || !d.bulletType.killShooter);
        if(candidates.isEmpty()) return;

        Table list = new Table();
        list.left().defaults().padRight(3).left();

        for(AxthrixLoader.BulletData data : candidates){
            BulletType candidate = data.bulletType;

            list.row();
            if(candidate instanceof InfFragBulletType){
                list.add("[lightgray]" + Core.bundle.get("stat.aj-inffrag-self"));
            }else{
                Table candidatePanel = new Table();
                candidatePanel.left().defaults().padRight(3).left();
                candidatePanel.add("[accent]" + candidate.getClass().getName()).padTop(2);
                appendCustomStats(candidatePanel, candidate, mindustry.content.Items.copper);

                list.table(Styles.grayPanel, ct -> ct.add(candidatePanel).left().grow())
                        .padLeft(5).padTop(2).padBottom(2).growX().margin(8);
            }
        }

        Collapser coll = new Collapser(list, true);
        coll.setDuration(0.1f);

        t.row();
        t.table(ft -> {
            ft.left().defaults().left();
            ft.add(Core.bundle.format("stat.aj-inffrag-possible", inf.fragBullets, candidates.size));
            ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false))
                    .update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen))
                    .size(8).padLeft(16f).expandX();
        }).padTop(6f);
        t.row();
        t.add(coll);
    }

    private static void header(Table t, String key){
        t.row();
        t.add("[accent]" + Core.bundle.get(key)).padTop(2);
    }

    /** label from bundle key + already-formatted value */
    private static Cell<?> lineInv(Table t, String labelKey, String value){
        t.row();
        return t.add("[lightgray]" + Core.bundle.get(labelKey) + " [white]" + value + "[]");
    }
    private static Cell<?> line(Table t, String labelKey, String value){
        t.row();
        return t.add( " [stat]" + value + " [lightgray]" + Core.bundle.get(labelKey));
    }


    private static String unit(String key){
        return Core.bundle.get(key);
    }

    private static String tiles(float worldUnits){
        return Strings.autoFixed(worldUnits / tilesize, 1) + " " + Core.bundle.get("stat.aj-perk-tiles");
    }

    private static String secs(float ticks){
        return Strings.autoFixed(ticks / 60f, 1) + " " + Core.bundle.get("stat.aj-perk-seconds");
    }

    private static String fx(float value){
        return Strings.autoFixed(value, 1);
    }

    /**
     * Draws text where each letter cycles smoothly through the rainbow and bobs up/down,
     * with a slight phase delay per letter so the color and motion both ripple across the
     * word like a wiggling worm instead of moving in lockstep.
     */
    public static class RainbowWiggleLabel extends Element {
        private final String text;
        private final float amplitude;
        private final float waveSpeed;
        private final float letterDelay;
        private final float hueSpeed;
        private final float hueSpread;

        private final GlyphLayout layout = new GlyphLayout();

        public RainbowWiggleLabel(String text){
            this(text, 3.5f, 6f, 0.45f, 200f, 30f);
        }

        /**
         * @param amplitude   how far each letter bobs up/down, in pixels
         * @param waveSpeed   how fast the bob cycles
         * @param letterDelay phase offset (radians) applied per letter index, controls the "wiggle" lag
         * @param hueSpeed    degrees/second the hue cycles through the rainbow
         * @param hueSpread   degrees of hue offset per letter index, so adjacent letters show different colors
         */
        public RainbowWiggleLabel(String text, float amplitude, float waveSpeed, float letterDelay, float hueSpeed, float hueSpread){
            this.text = text;
            this.amplitude = amplitude;
            this.waveSpeed = waveSpeed;
            this.letterDelay = letterDelay;
            this.hueSpeed = hueSpeed;
            this.hueSpread = hueSpread;

            layout.setText(Fonts.def, text);
            setSize(layout.width, layout.height + amplitude * 2f);
        }

        @Override
        public void draw(){
            Font font = Fonts.def;
            float time = Time.time;
            float px = x;
            float py = y + amplitude;

            for(int i = 0; i < text.length(); i++){
                String s = String.valueOf(text.charAt(i));
                layout.setText(font, s);

                float yOff = Mathf.sin(time / 60f * waveSpeed - i * letterDelay) * amplitude;
                float hue = (time * hueSpeed / 60f + i * hueSpread) % 360f;

                font.setColor(Tmp.c1.fromHsv(hue, 0.85f, 1f));
                font.draw(s, px, py + yOff);

                px += layout.width;
            }

            font.setColor(Color.white);
        }
    }

    private static String ammoStat(float val){
        return (val > 0 ? "[stat]+" : "[negstat]") + Strings.autoFixed(val, 1);
    }

    private static String multStat(float val){
        return (val >= 1 ? "[stat]" : "[negstat]") + Strings.autoFixed(val, 2);
    }


    private static Cell<?> sep(Table table, String text){
        table.row();
        return table.add(text);
    }

    private static Cell<?> note(Table table, String text){
        table.row();
        return table.table(t -> {
            t.image(Icon.arrowNoteSmall.getRegion()).size(15).color(Pal.stat).scaling(Scaling.fit).padRight(6).padLeft(12);
            t.add(text);
        });
    }


    public static void baseBullet(BulletType type, Table bt, boolean compact, UnlockableContent t){
        if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
            bt.add(Core.bundle.format("bullet.damage", type.damage) + (type.continuousDamage() > 0 ?
                    "[lightgray] ~ [stat]" + Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized() : ""));
        }

        if(type.buildingDamageMultiplier != 1){
            sep(bt, Core.bundle.format("bullet.buildingdamage", ammoStat((int)(type.buildingDamageMultiplier * 100 - 100))));
        }

        if(type.rangeChange != 0 && !compact){
            sep(bt, Core.bundle.format("bullet.range", ammoStat(type.rangeChange / tilesize)));
        }

        if(type.shieldDamageMultiplier != 1){
            sep(bt, Core.bundle.format("bullet.shielddamage", ammoStat((int)(type.shieldDamageMultiplier * 100 - 100))));
        }

        if(type.splashDamage > 0){
            sep(bt, Core.bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
        }

        if(type.statLiquidConsumed <= 0f && !compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier && (!(t instanceof Turret turret) || turret.displayAmmoMultiplier)){
            sep(bt, Core.bundle.format("bullet.multiplier", (int)type.ammoMultiplier));
        }

        if(!compact && !Mathf.equal(type.reloadMultiplier, 1f)){
            int val = (int)(type.reloadMultiplier * 100 - 100);
            sep(bt, Core.bundle.format("bullet.reload", ammoStat(val)));
        }

        if(type.knockback > 0){
            sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
        }

        if(type.healPercent > 0f){
            sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
        }

        if(type.healAmount > 0f){
            sep(bt, Core.bundle.format("bullet.healamount", Strings.autoFixed(type.healAmount, 2)));
        }

        if(type.pierce || type.pierceCap != -1){
            sep(bt, type.pierceCap == -1 ? "@bullet.infinitepierce" : Core.bundle.format("bullet.pierce", type.pierceCap));
        }

        if(type.incendAmount > 0){
            sep(bt, "@bullet.incendiary");
        }

        if(type.homingPower > 0.01f){
            sep(bt, "@bullet.homing");
        }

        if(type.lightning > 0){
            sep(bt, Core.bundle.format("bullet.lightning", type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage));
        }

        if(type instanceof LaserBulletType b && b.lightningSpacing > 0){
            int count = (int)(b.length / b.lightningSpacing) * 2 + 2;
            float damage = b.lightningDamage < 0 ? b.damage : b.lightningDamage;
            sep(bt, Core.bundle.format("bullet.lightning", count, damage));
            note(bt, Core.bundle.format("bullet.lightninginterval", Strings.autoFixed(b.lightningSpacing / tilesize, 2), Strings.autoFixed(b.lightningLength, 2))).left();
        }

        if(type instanceof EmpBulletType b && b.radius > 0f){
            sep(bt, Core.bundle.format("bullet.empradius", Strings.fixed(b.radius / tilesize, 1)));
            if(b.timeDuration > 0f && b.timeIncrease > 1f){
                sep(bt, Core.bundle.format("bullet.empboost", Strings.autoFixed(b.timeIncrease * 100f, 2),
                        Strings.autoFixed(b.timeDuration / 60f, 1)) + " " + StatUnit.seconds.localized());
            }
            if(b.timeDuration > 0f && b.powerSclDecrease < 1f){
                sep(bt, Core.bundle.format("bullet.empslowdown",
                        (b.powerSclDecrease < 1f ? "[negstat]" : "") + Strings.autoFixed((b.powerSclDecrease - 1f) * 100f, 2),
                        Strings.autoFixed(b.timeDuration / 60f, 1)) + " " + StatUnit.seconds.localized());
            }
            if(!Mathf.equal(b.powerDamageScl, 1f)){
                sep(bt, Core.bundle.format("bullet.empdamage", Strings.autoFixed(b.powerDamageScl * 100f, 2)));
            }
            if(b.hitUnits){
                sep(bt, Core.bundle.format("bullet.empunitdamage",
                        (b.unitDamageScl < 1f ? "[negstat]" : "") + Strings.autoFixed(b.unitDamageScl * 100f, 2)));
            }
        }

        if(type.pierceArmor){
            sep(bt, "@bullet.armorpierce");
        }

        if(!type.pierceArmor){
            if(type.armorMultiplier != 1f){
                if(type.armorMultiplier > 1f){
                    sep(bt, Core.bundle.format("bullet.armorweakness", (type.armorMultiplier)));
                }else if(Mathf.sign(type.armorMultiplier) == 1){
                    sep(bt, Core.bundle.format("bullet.partialarmorpierce", (int)((1 - type.armorMultiplier) * 100)));
                }else{
                    sep(bt, Core.bundle.format("bullet.antiarmor", (-type.armorMultiplier)));
                }
            }
        }

        if(type.maxDamageFraction > 0){
            sep(bt, Core.bundle.format("bullet.maxdamagefraction", (int)(type.maxDamageFraction * 100)));
        }

        if(type.suppressionRange > 0){
            sep(bt, Core.bundle.format("bullet.suppression", Strings.autoFixed(type.suppressionDuration / 60f, 2), Strings.fixed(type.suppressionRange / tilesize, 1)));
        }

        if(type.status != StatusEffects.none){
            sep(bt, (type.status.hasEmoji() ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive ? "" : "[lightgray] ~ [stat]" +
                    Strings.autoFixed(type.statusDuration / 60f, 1) + "[lightgray] " + Core.bundle.get("unit.seconds"))).with(c -> StatValues.withTooltip(c, type.status));
        }

        if(!type.targetMissiles){
            sep(bt, "@bullet.notargetsmissiles");
        }

        if(!type.targetBlocks){
            sep(bt, "@bullet.notargetsbuildings");
        }

        if(type.intervalBullet != null){
            bt.row();

            Table ic = new Table();
            ammo(ObjectMap.of(t, type.intervalBullet), 1, false).display(ic);
            Collapser coll = new Collapser(ic, true);
            coll.setDuration(0.1f);

            bt.table(it -> {
                it.left().defaults().left();

                it.add(Core.bundle.format("bullet.interval", Strings.autoFixed(type.intervalBullets / type.bulletInterval * 60, 2)));
                it.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
            });
            bt.row();
            bt.add(coll);
        }

        if(type.fragBullet != null && !(type instanceof InfFragBulletType)){
            bt.row();

            Table fc = new Table();
            ammo(ObjectMap.of(t, type.fragBullet), 1, false).display(fc);
            Collapser coll = new Collapser(fc, true);
            coll.setDuration(0.1f);

            bt.table(ft -> {
                ft.left().defaults().left();

                ft.add(Core.bundle.format("bullet.frags", type.fragBullets));
                ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
            });
            bt.row();
            bt.add(coll);
        }

        if(type.spawnBullets != null && type.spawnBullets.size > 0){
            bt.row();

            Table sc = new Table();
            for(BulletType spawn : type.spawnBullets){
                if(spawn.showStats) ammo(ObjectMap.of(t, spawn), 1, false).display(sc);
            }
            Collapser coll = new Collapser(sc, true);
            coll.setDuration(0.1f);

            bt.table(st -> {
                st.left().defaults().left();

                st.add(Core.bundle.format("bullet.spawnBullets", type.spawnBullets.size));
                if(sc.getChildren().size > 0) st.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
            });
            bt.row();
            bt.add(coll);
        }
    }
}