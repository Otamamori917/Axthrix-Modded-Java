package axthrix.world.util.ui;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Strings;
import axthrix.AxthrixLoader;
import axthrix.world.types.abilities.AfterBurnAbility;
import axthrix.world.types.abilities.PayloadCrafterAbility;
import axthrix.world.types.abilities.StaticEMPability;
import axthrix.world.types.abilities.heatbased.ThermalDashAbility;
import axthrix.world.types.perks.BulletPerk;
import axthrix.world.types.perks.DurationPerk;
import axthrix.world.types.perks.Perk;
import axthrix.world.types.perks.PerkStateData;
import axthrix.world.types.perks.RangePerk;
import axthrix.world.types.perks.ShotBuffPerk;
import axthrix.world.types.perks.SpeedPerk;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.types.sea.unit.SubmarineUnitType;
import axthrix.world.types.statuseffects.AccumStackStatusEffect;
import axthrix.world.types.statuseffects.AccumStackStatusEffect.TriggerLock;
import axthrix.world.types.unittypes.AxUnitType;
import axthrix.world.types.weapontypes.PerkWeapon;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.content.Fx;
import mindustry.ctype.UnlockableContent;
import mindustry.entities.abilities.Ability;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.*;
import mindustry.graphics.Pal;
import mindustry.input.Binding;
import mindustry.type.StatusEffect;
import mindustry.ui.Styles;
import mindustry.world.Tile;
import mindustry.world.blocks.defense.turrets.Turret;
import mindustry.world.blocks.payloads.BuildPayload;
import mindustry.world.blocks.payloads.Payload;
import mindustry.world.blocks.payloads.UnitPayload;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.*;

public class CustomUnitInfoBar {
    public Table container;

    private int lastUnitId = -1;
    private String lastHudKey = "";
    private UnlockableContent lastSelectedCraft = null;
    private Table buttonRowCache = null;

    public void build() {
        if (container != null) return;

        container = new Table(Tex.buttonEdge3);
        container.top().left();
        container.touchable(() -> Touchable.enabled);
        container.visible(() -> player.unit() != null && ui.hudfrag.shown);

        ui.hudGroup.addChild(container);

        container.update(() -> {
            Unit unit = player.unit();
            if (unit == null || !ui.hudfrag.shown) {
                container.clearChildren();
                container.visible = false;
                lastUnitId = -1;
                lastHudKey = "";
                lastSelectedCraft = null;
                buttonRowCache = null;
                return;
            }

            rebuildInfo(unit);

            container.setPosition(
                    AxthrixLoader.customHudOffsetX,
                    Core.graphics.getHeight() - container.getHeight() + AxthrixLoader.customHudOffsetY
            );
            container.pack();
        });
    }

    private boolean showMobileControls() {
        return mobile || AxthrixLoader.showMobileButtons;
    }

    private float bs(float base) {
        return base * AxthrixLoader.hudButtonScale;
    }

    private void tip(Element e, Prov<String> text) {
        e.addListener(new Tooltip(t -> {
            t.background(Tex.button);
            t.defaults().left();
            t.label(text::get);
        }));
    }

    private void tip(Element e, String text) {
        tip(e, () -> text);
    }

    private float hudScale() {
        return Math.max(1f, AxthrixLoader.hudButtonScale);
    }

    private void addSolidDivider(Table c) {
        c.image(Tex.whiteui)
                .color(Color.valueOf("404042"))
                .growX()
                .height(4f)
                .padTop(5f)
                .padBottom(5f);
        c.row();
    }

    private String buildHudKey(Unit unit, UnlockableContent craft) {
        StringBuilder sb = new StringBuilder();
        sb.append(unit.id).append('|');
        if (craft != null) sb.append(craft.id);
        sb.append('|');

        float t = TemperatureLogic.getTemperatureUnit(unit);
        sb.append(t > 0.5f ? 1 : t < -0.5f ? -1 : 0).append('|');
        sb.append(showMobileControls() ? 1 : 0).append('|');
        sb.append(AxthrixLoader.hudButtonScale).append('|');
        sb.append(unit.type instanceof SubmarineUnitType ? 1 : 0).append('|');
        sb.append(LayerManager.isPlayerSubmerged() ? 1 : 0).append('|');

        for (StatusEffect se : content.statusEffects()) {
            if (!(se instanceof AccumStackStatusEffect acc)) continue;
            float cooldown = TriggerLock.remaining(unit, acc);
            boolean has = unit.hasEffect(acc);
            if (cooldown <= 0.05f && !has) continue;
            sb.append(acc.id).append(':').append((int) cooldown).append(',').append(has ? 1 : 0).append(';');
        }

        if (unit.abilities != null) {
            for (Ability ab : unit.abilities) {
                if (ab instanceof ThermalDashAbility) {
                    sb.append("td").append(ThermalDashAbility.isAuto(unit) ? 1 : 0);
                }
                if (ab instanceof AfterBurnAbility) sb.append("ab");
                if (ab instanceof PayloadCrafterAbility) {
                    UnlockableContent last = PayloadCrafterAbility.lastCraft.get(unit.id);
                    if (last != null) sb.append("lc").append(last.id);
                    sb.append("cr").append(craft != null ? 1 : 0);
                }
            }
        }

        if (unit.stack.amount > 0 && unit.stack.item != null) {
            sb.append(unit.stack.item.id);
        } else {
            sb.append("noitem");
        }
        sb.append('|');

        if (unit instanceof Payloadc pay) {
            sb.append(pay.payloads().size);
        } else {
            sb.append(0);
        }
        sb.append('|');

        return sb.toString();
    }

    private void rebuildInfo(Unit unit) {
        PayloadCrafterAbility crafter = null;
        if (unit.abilities != null) {
            for (Ability ab : unit.abilities) {
                if (ab instanceof PayloadCrafterAbility pca) {
                    crafter = pca;
                    break;
                }
            }
        }

        UnlockableContent currentCraft = crafter != null ? crafter.getSelectedCraft(unit) : null;
        boolean craftChanged = currentCraft != lastSelectedCraft;
        boolean unitChanged = unit.id != lastUnitId;

        String key = buildHudKey(unit, currentCraft);
        if (!unitChanged && !craftChanged && key.equals(lastHudKey) && container.hasChildren()) {
            return;
        }

        lastUnitId = unit.id;
        lastHudKey = key;
        lastSelectedCraft = currentCraft;

        container.clearChildren();
        container.background(Tex.buttonEdge3);
        container.margin(6f);

        boolean hasAnyInfo = false;

        Building selected = control.input.config.getSelected();

        if (unit.isPlayer() || selected instanceof Turret.TurretBuild) {
            float resistance = 1f;
            if (unit.type instanceof AxUnitType axType) {
                float t = TemperatureLogic.getTemperatureUnit(unit);
                resistance = t > 0 ? axType.effectResistanceHeat : axType.effectResistanceCold;
            }
            String typeLabelKey = unit.isPlayer() ? "ui.ax-info.unit" : "ui.ax-info.turret";
            if (liveTemp(unit, selected) != 0f) {
                addTemperatureInfo(container, unit, selected, typeLabelKey, resistance);
                hasAnyInfo = true;
                addSolidDivider(container);
            }
        }

        if (addResultHud(container, unit)) {
            hasAnyInfo = true;
            addSolidDivider(container);
        }

        if (unit.type instanceof SubmarineUnitType) {
            addSubmarineDive(container, unit);
            hasAnyInfo = true;
            addSolidDivider(container);
        }

        MiniUnitDisplay mini = new MiniUnitDisplay(unit);
        if (mini.hasContent()) {
            container.add(mini).size(210f).padBottom(8f).row();
            hasAnyInfo = true;
            addSolidDivider(container);
        }

        if (unit.abilities != null) {
            for (Ability ab : unit.abilities) {
                if (ab instanceof AfterBurnAbility afterburn) {
                    addAfterBurnInfo(container, unit, afterburn);
                    hasAnyInfo = true;
                    addSolidDivider(container);
                } else if (ab instanceof StaticEMPability emp) {
                    addEMPInfo(container, unit, emp);
                    hasAnyInfo = true;
                    addSolidDivider(container);
                } else if (ab instanceof ThermalDashAbility dash) {
                    addThermalDashInfo(container, unit, dash);
                    hasAnyInfo = true;
                    addSolidDivider(container);
                }
            }
        }

        if (unit.mounts != null) {
            for (WeaponMount mount : unit.mounts) {
                if (mount.weapon instanceof PerkWeapon pw && pw.perks != null) {
                    for (Perk perk : pw.perks) {
                        addPerkInfo(container, perk, pw.getState(unit, perk));
                        hasAnyInfo = true;
                        addSolidDivider(container);
                    }
                }
            }
        }

        if (crafter != null) {
            if (unitChanged || craftChanged || buttonRowCache == null) {
                Table newButtons = new Table(Tex.pane);
                crafter.buildEmbeddedSelectionTable(unit, newButtons);
                buttonRowCache = newButtons;
            }
            addPayloadCrafterInfo(container, unit, crafter);
            hasAnyInfo = true;
        }

        container.visible = hasAnyInfo;
    }

    private boolean addResultHud(Table parent, Unit unit) {
        Table row = new Table();
        row.left();
        boolean any = false;

        for (StatusEffect se : content.statusEffects()) {
            if (!(se instanceof AccumStackStatusEffect acc)) continue;
            if (TriggerLock.remaining(unit, acc) <= 0.05f) continue;
            any = true;

            final AccumStackStatusEffect facc = acc;

            Button btn = new Button(Styles.clearNonei);
            btn.top();
            btn.stack(
                    new Image(facc.uiIcon),
                    new Table(badge -> {
                        badge.bottom().right();
                        badge.image(Icon.defense)
                                .size(2f * AxthrixLoader.hudButtonScale)
                                .color(Color.valueOf("4da6ff"))
                                .pad(1f);
                    })
            ).size(bs(48f * AxthrixLoader.hudButtonScale));
            btn.row();
            btn.label(() -> {
                float t = TriggerLock.remaining(unit, facc);
                return t > 0.05f ? Strings.autoFixed(t / 60f, 1) : "";
            }).style(Styles.outlineLabel).fontScale(0.7f);

            tip(btn, () -> {
                float cd = TriggerLock.remaining(unit, facc);
                return facc.localizedName + "\n"
                        + Core.bundle.format("ui.ax-info.result-cooldown",
                        Strings.autoFixed(cd / 60f, 1) + " " + StatUnit.seconds.localized()) + "\n"
                        + Core.bundle.get("ui.ax-info.result-protected");
            });

            btn.clicked(() -> {
                float cd = TriggerLock.remaining(unit, facc);
                ui.showInfoToast(
                        facc.localizedName + "\n"
                                + Core.bundle.format("ui.ax-info.result-cooldown",
                                Strings.autoFixed(cd / 60f, 1) + " " + StatUnit.seconds.localized()),
                        2f
                );
            });

            row.add(btn).size(bs(30f), bs(40f)).padRight(4f);
        }

        if (!any) return false;
        parent.add(row).left().padBottom(6f);
        parent.row();
        return true;
    }

    private void addSubmarineDive(Table c, Unit unit) {
        c.table(t -> {
            var state = t.label(() -> LayerManager.isPlayerSubmerged()
                            ? Core.bundle.get("ui.ax-info.surface-state")
                            : Core.bundle.get("ui.ax-info.dive-state"))
                    .style(Styles.outlineLabel)
                    .padRight(8f)
                    .get();

            tip(state, Core.bundle.format("ui.ax-info.dive-key", AxthrixLoader.bindActivate));

            if (showMobileControls()) {
                Button btn = t.button(b -> b.label(() -> LayerManager.isPlayerSubmerged()
                                ? Core.bundle.get("ui.ax-info.surface")
                                : Core.bundle.get("ui.ax-info.dive")).pad(6f),
                        Styles.flatBordert,
                        () -> {
                            Tile tile = unit.tileOn();
                            if (!LayerManager.isPlayerSubmerged()) {
                                if (tile == null || tile.floor().name.contains("shallow") || LayerManager.isShallow(tile)) {
                                    ui.showInfoToast(Core.bundle.get("ui.ax-info.dive-shallow"), 2f);
                                    return;
                                }
                            }
                            LayerManager.setSubmerged(unit, !LayerManager.submergedUnits.contains(unit));
                            Fx.bubble.at(unit.x, unit.y);
                        }
                ).size(bs(120f), bs(40f)).get();

                tip(btn, Core.bundle.get("ui.ax-info.dive-tooltip"));
            }
        }).left().padBottom(6f);
        c.row();
    }

    private void addAfterBurnInfo(Table c, Unit unit, AfterBurnAbility afterburn) {
        String keyTip = Core.bundle.format("ui.ax-info.afterburn-key", AxthrixLoader.bindActivate);

        c.table(t -> {
            t.image(Icon.defense).size(16f).color(afterburn.colors[0]).padRight(6f);

            var fuel = t.label(() -> {
                float fuelv = afterburn.fuel, max = afterburn.fuelCap;
                int i = Math.min(4, (int) (fuelv / (max / 5f)));
                Color color = afterburn.colors[i];
                return Core.bundle.format("ui.ax-info.fuel",
                        "[#" + color + "]" + Strings.fixed(fuelv, 0) + "[white]/[]" + Strings.fixed(max, 0));
            }).style(Styles.outlineLabel).minWidth(70f).padRight(8f).get();

            tip(fuel, keyTip + "\n" + Core.bundle.get("ui.ax-info.afterburn-hold"));

            if (showMobileControls()) {
                Button btn = t.button(Icon.up, Styles.clearNonei, () -> {}).size(bs(32)).get();
                tip(btn, keyTip + "\n" + Core.bundle.get("ui.ax-info.afterburn-hold"));
                btn.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent e, float x, float y, int p, KeyCode b) {
                        AfterBurnAbility.setBoostHeld(unit, true);
                        return true;
                    }
                    @Override
                    public void touchUp(InputEvent e, float x, float y, int p, KeyCode b) {
                        AfterBurnAbility.setBoostHeld(unit, false);
                    }
                });
            }
        }).left().padBottom(6f);
        c.row();
    }

    private void addThermalDashInfo(Table c, Unit unit, ThermalDashAbility dash) {
        boolean auto = ThermalDashAbility.isAuto(unit);
        String toggleTip = Core.bundle.format("ui.ax-info.dash-toggle-key", AxthrixLoader.bindToggle);
        String fireTip = Core.bundle.format("ui.ax-info.dash-fire-key", AxthrixLoader.bindActivate);
        String modeTip = auto ? toggleTip : toggleTip + "\n" + fireTip;

        c.table(t -> {
            t.image(Icon.move).size(16f).color(dash.dashColor).padRight(6f);

            var mode = t.label(() -> Core.bundle.get(ThermalDashAbility.isAuto(unit)
                            ? "ui.ax-info.dash-auto"
                            : "ui.ax-info.dash-manual"))
                    .style(Styles.outlineLabel)
                    .minWidth(90f)
                    .padRight(6f)
                    .get();
            mode.touchable = Touchable.enabled;
            tip(mode, modeTip);

            t.label(() -> {
                float cd = dash.cooldownLeft(unit);
                return cd > 0.05f ? "[accent]" + Strings.autoFixed(cd / 60f, 1) + "s[]" : "";
            }).style(Styles.outlineLabel).padRight(8f);

            if (showMobileControls()) {
                Button toggle = t.button(Icon.refresh, Styles.clearNonei, () -> {}).size(bs(32)).get();
                tip(toggle, toggleTip);
                toggle.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                        ThermalDashAbility.toggleAuto(unit);
                        return true;
                    }
                });

                if (!auto) {
                    Button fire = t.button(Icon.right, Styles.clearNonei, () -> {}).size(bs(32)).get();
                    tip(fire, fireTip);
                    fire.addListener(new InputListener() {
                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                            ThermalDashAbility.requestDash(unit);
                            return true;
                        }
                    });
                }
            }
        }).left().padBottom(6f);
        c.row();
    }

    private void addTemperatureInfo(Table c, Unit unit, Building selected, String typeLabelKey, float resistance) {
        c.table(t -> {
            t.image(Icon.waves).size(16f).padRight(6f)
                    .update(img -> {
                        float temp = liveTemp(unit, selected);
                        img.setColor(temp > 0 ? Color.red : Color.cyan);
                    });

            t.label(() -> {
                float temperature = liveTemp(unit, selected);
                String typeLabel = Core.bundle.get(typeLabelKey);
                String temperatureStr = TempUnit.format(temperature);
                String prefix = temperature > 0 ? "[red]+" : "[cyan]";
                String out = Core.bundle.format("ui.ax-info.temp-base", typeLabel, prefix + temperatureStr);
                if (resistance <= 0f) return out;
                if (temperature > 0) {
                    out += " " + Core.bundle.format("ui.ax-info.temp-heat-debuff",
                            Strings.fixed(temperature * resistance, 0));
                } else {
                    out += " " + Core.bundle.format("ui.ax-info.temp-cold-debuff",
                            Strings.fixed((-temperature) * resistance, 0));
                }
                return out;
            }).style(Styles.outlineLabel).minWidth(120f);
        }).left().padBottom(6f);
        c.row();
    }

    private float liveTemp(Unit unit, Building selected) {
        if (unit != null && unit.isPlayer()) {
            return TemperatureLogic.getTemperatureUnit(unit);
        }
        if (selected instanceof Turret.TurretBuild tb) {
            return TemperatureLogic.getTemperatureBuilding(tb);
        }
        return 0f;
    }

    private Color getPerkColor(Perk perk, PerkStateData s) {
        boolean active = s.smoothActivated > 0.4f
                || (perk instanceof DurationPerk dp && dp.getBuffScale(s) > 0f)
                || (perk instanceof BulletPerk && s.pendingShot);
        return active ? Pal.remove : (perk.color != null ? perk.color : Pal.accent);
    }

    private void addEMPInfo(Table c, Unit unit, StaticEMPability emp) {
        c.table(t -> {
            t.image(Icon.power).size(16f).color(emp.color).padRight(6f);
            t.label(() -> {
                Integer charge = emp.currentCharge.get(unit);
                if (charge == null) charge = 0;
                Color color = charge >= emp.minimumPowerToDischarge
                        ? emp.color.cpy().lerp(Color.white, 0.3f)
                        : emp.color.cpy().lerp(Color.gray, 0.5f);
                return Core.bundle.format("ui.ax-info.emp",
                        "[#" + color + "]" + charge + "[white]/[]" + emp.maxPower);
            }).style(Styles.outlineLabel).minWidth(70f);
        }).left().padBottom(6f);
        c.row();
    }

    private void addPerkInfo(Table c, Perk perk, PerkStateData s) {
        c.table(t -> {
            t.image(Icon.layers).size(16f).color(getPerkColor(perk, s)).padRight(6f);
            t.label(() -> {
                String title = perk.name;
                String hexColor = "[#" + getPerkColor(perk, s) + "]";
                if (perk instanceof DurationPerk dp) {
                    return hexColor + title + " " + Core.bundle.format("ui.ax-info.perk-duration",
                            (int) (dp.getBuffProgress(s) * 100));
                }
                if (perk instanceof BulletPerk) {
                    return hexColor + title + (s.pendingShot
                            ? Core.bundle.get("ui.ax-info.perk-ready")
                            : " " + s.currentStacks + "/" + perk.maxStacks);
                }
                if (perk instanceof RangePerk || perk instanceof SpeedPerk) {
                    return hexColor + title + " " + Core.bundle.format("ui.ax-info.perk-percent",
                            (int) (s.smoothProgress * 100));
                }
                if (perk instanceof ShotBuffPerk) {
                    return hexColor + title + " " + Core.bundle.format("ui.ax-info.perk-shotbuff",
                            s.currentStacks, s.hitProgress, perk.hitsPerStack);
                }
                return hexColor + title + " " + s.currentStacks + "/" + perk.maxStacks;
            }).style(Styles.outlineLabel).minWidth(100f);
        }).left().padBottom(6f);
        c.row();
    }

    private void addPayloadCrafterInfo(Table c, Unit unit, PayloadCrafterAbility crafter) {
        UnlockableContent selected = lastSelectedCraft;
        UnlockableContent preview = crafter.getPreviewCraft(unit);

        String keyTips = Core.bundle.format("ui.ax-info.craft-toggle-key", AxthrixLoader.bindToggle)
                + "\n" + Core.bundle.format("ui.ax-info.craft-cycle-key", AxthrixLoader.bindActivate);

        c.table(t -> {
            if (selected == null) {
                if (preview != null) {
                    t.image(preview.uiIcon).size(16f).padRight(6f);
                } else {
                    t.image(Icon.hammer).size(16f).color(Color.lightGray).padRight(6f);
                }

                var lab = t.label(() -> {
                    UnlockableContent p = crafter.getPreviewCraft(unit);
                    String name = p != null ? p.localizedName : Core.bundle.get("ui.ax-info.craft-idle");
                    return Core.bundle.get("ability.aj-payload-crafter") + ": [lightgray]" + name + "[]";
                }).style(Styles.outlineLabel).get();

                lab.touchable = Touchable.enabled;
                tip(lab, keyTips);
                return;
            }

            var option = crafter.craftOptions.get(selected);
            boolean showPower = option != null && (option.requirePower || option.poweredSpeedBonus != 1f);

            t.image(selected.uiIcon).size(16f).padRight(6f);

            t.table(barTable -> {
                var name = barTable.add(selected.localizedName).style(Styles.outlineLabel).left().get();
                name.touchable = Touchable.enabled;
                tip(name, keyTips);

                if (showPower) {
                    barTable.label(() -> {
                        boolean disconnected = crafter.isNotPowered(unit);
                        return " [#" + (disconnected ? "FF6666" : "FFEA7C") + "]⚡[]";
                    }).style(Styles.outlineLabel).left();
                }

                barTable.row();

                if (AxthrixLoader.showAdvanced) {
                    barTable.label(() -> {
                        float progress = 0f;
                        Float live = PayloadCrafterAbility.constructionProgress.get(unit.id);
                        if (live != null) progress = live;
                        float maxTime = option != null ? option.cost * crafter.ticksPerCost : 1f;
                        return Core.bundle.format("ui.ax-info.craft-progress",
                                (int) (Mathf.clamp(progress / maxTime) * 100));
                    }).style(Styles.outlineLabel).left();
                }
            }).left();
        }).left().padBottom(4f).row();

        c.table(row -> {
            row.left().top();

            if (buttonRowCache != null) {
                row.add(buttonRowCache).left().top().padRight(8f);
            }

            row.table(side -> {
                side.left().top();
                addUnitItemStack(side, unit);
                addUnitPayloads(side, unit);
            }).left().top();
        }).left().padBottom(8f).row();
    }

    private void addUnitItemStack(Table inv, Unit unit) {
        inv.table(line -> {
            line.left();
            float s = 24f * hudScale();

            Image icon = new Image();
            line.add(icon).size(s).padRight(4f);
            icon.touchable = Touchable.enabled;

            icon.update(() -> {
                if (unit.stack.amount > 0 && unit.stack.item != null) {
                    icon.setDrawable(unit.stack.item.uiIcon);
                    icon.visible = true;
                } else {
                    icon.visible = false;
                }
            });

            tip(icon, () -> unit.stack.amount > 0 && unit.stack.item != null
                    ? unit.stack.item.localizedName : "");

            line.label(() -> unit.stack.amount > 0 && unit.stack.item != null
                            ? String.valueOf(unit.stack.amount) : "")
                    .style(Styles.outlineLabel);
        }).left().padBottom(4f);
        inv.row();
    }

    private void addUnitPayloads(Table inv, Unit unit) {
        if (!(unit instanceof Payloadc pay) || pay.payloads().isEmpty()) return;

        ObjectMap<String, PayloadCount> counts = new ObjectMap<>();
        for (Payload p : pay.payloads()) {
            if (p == null) continue;
            String key = payloadKey(p);
            PayloadCount pc = counts.get(key);
            if (pc == null) {
                pc = new PayloadCount(p);
                counts.put(key, pc);
            }
            pc.amount++;
        }

        for (PayloadCount pc : counts.values()) {
            inv.table(line -> {
                line.left();
                float s = 24f * hudScale();

                Image icon = new Image(pc.icon);
                line.add(icon).size(s).padRight(4f);
                icon.touchable = Touchable.enabled;
                tip(icon, pc.name);

                line.label(() -> pc.amount + "x").style(Styles.outlineLabel);
            }).left().padBottom(2f);
            inv.row();
        }
    }

    private String payloadKey(Payload p) {
        if (p instanceof BuildPayload b && b.block() != null) return "b:" + b.block().id;
        if (p instanceof UnitPayload u && u.unit != null && u.unit.type != null) return "u:" + u.unit.type.id;
        return String.valueOf(p.hashCode());
    }

    private static class PayloadCount {
        final TextureRegion icon;
        final String name;
        int amount;

        PayloadCount(Payload p) {
            icon = p.icon();
            name = payloadName(p);
            amount = 0;
        }

        static String payloadName(Payload p) {
            if (p instanceof BuildPayload b && b.block() != null) return b.block().localizedName;
            if (p instanceof UnitPayload u && u.unit != null && u.unit.type != null) return u.unit.type.localizedName;
            return p.getClass().getSimpleName();
        }
    }
}