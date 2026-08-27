package axthrix.world.util.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.Tooltip;
import arc.util.Align;
import arc.util.Strings;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import axthrix.AxthrixLoader;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.abilities.ShieldPlateAbility;
import axthrix.world.types.unittypes.MountUnitType;
import axthrix.world.util.draw.FlatIconShader;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public class MiniUnitDisplay extends Element {
    private final Unit unit;

    public static float COMPACT_SCALE_MODIFIER = 1.3f;
    public static float PLATE_SILHOUETTE_SCALE_RATIO = 1.5f;
    public static float DRONE_SILHOUETTE_SCALE_RATIO = 0.9f;
    public static float PLATE_SCALE_RATIO = 1.5f;
    public static float DRONE_SCALE_RATIO = 1.1f;
    public static float TARGET_HIT = 18f;
    public static float MIN_SIZE_FACTOR = 0.9f;
    public static float MAX_SIZE_FACTOR = 1.15f;
    public static float TARGET_BOX = 58f;
    public static float BASE_SCALE = 2f;
    public static float SIZE_LERP = 0.4f;
    public static float HIT_RADIUS_PLATE = 12f;
    public static float HIT_RADIUS_DRONE = 22f;
    public static float HIT_RADIUS_MOUNT = 18f;
    public static float MOUNT_OFFSET_SCALE = 1f;

    private String hoverText = "";
    private Label tipLabel;
    private ShieldPlateAbility hoveredPlate;
    private DroneSpawnAbility hoveredDrone;
    private int hoveredMountSlot = -1;
    private boolean pointerOver;

    public MiniUnitDisplay(Unit unit) {
        this.unit = unit;
        touchable = Touchable.enabled;

        addListener(new Tooltip(t -> {
            t.background(Tex.button);
            t.margin(8f);
            t.defaults().left();
            tipLabel = t.label(() -> hoverText.isEmpty() ? " " : hoverText)
                    .style(Styles.outlineLabel)
                    .wrap()
                    .width(Math.min(220f, Core.graphics.getWidth() * 0.35f))
                    .get();
        }));

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Element fromActor) {
                pointerOver = true;
                setHover(x, y);
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                setHover(x, y);
                return true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Element toActor) {
                pointerOver = false;
                hoveredPlate = null;
                hoveredDrone = null;
                hoveredMountSlot = -1;
                hoverText = "";
                if (tipLabel != null) {
                    tipLabel.setText(" ");
                    tipLabel.pack();
                    if (tipLabel.parent != null) tipLabel.parent.pack();
                }
            }
        });
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!pointerOver) return;
        Vec2 mouse = Tmp.v3.set(Core.input.mouse());
        stageToLocalCoordinates(mouse);
        setHover(mouse.x, mouse.y);
    }

    private void setHover(float x, float y) {
        hoverText = resolveHover(x, y);
        if (hoverText.isEmpty()) hoverText = unitSummary();
        if (tipLabel != null) {
            tipLabel.setText(hoverText);
            tipLabel.pack();
            if (tipLabel.parent != null) tipLabel.parent.pack();
        }
    }

    public boolean hasContent() {
        if (unit == null) return false;
        if (unit.type instanceof MountUnitType mt && mt.slots.size > 0) return true;
        if (unit.abilities == null) return false;
        for (var ab : unit.abilities) {
            if (ab instanceof ShieldPlateAbility || ab instanceof DroneSpawnAbility) return true;
        }
        return false;
    }

    private Color getHealthColor(float fraction) {
        fraction = Mathf.clamp(fraction);
        Color low = Color.valueOf("FF6666");
        Color mid = Color.valueOf("FFEA7C");
        Color high = Color.valueOf("7CFF7C");
        if (fraction < 0.5f) return Tmp.c1.set(low).lerp(mid, fraction / 0.5f);
        return Tmp.c1.set(mid).lerp(high, (fraction - 0.5f) / 0.5f);
    }

    private Color getBuildColor(float fraction) {
        return Tmp.c1.set(Color.valueOf("FFAA5E")).lerp(Color.valueOf("FFEA7C"), Mathf.clamp(fraction));
    }

    private float computeUniformBaseline() {
        float iconW = unit.type != null && unit.type.fullIcon != null ? unit.type.fullIcon.width : 40f;
        float iconH = unit.type != null && unit.type.fullIcon != null ? unit.type.fullIcon.height : 40f;
        float maxIconSize = Math.max(iconW, iconH);

        float b = BASE_SCALE;
        if (maxIconSize * b > TARGET_BOX) b = TARGET_BOX / maxIconSize;

        float uniformBaseline = b * COMPACT_SCALE_MODIFIER;
        if (unit.type != null) {
            float hit = Math.max(unit.type.hitSize, 1f);
            float raw = TARGET_HIT / hit;
            float sizeFactor = Mathf.lerp(1f, raw, SIZE_LERP);
            float min = Math.min(MIN_SIZE_FACTOR, MAX_SIZE_FACTOR);
            float max = Math.max(MIN_SIZE_FACTOR, MAX_SIZE_FACTOR);
            uniformBaseline *= Mathf.clamp(sizeFactor, min, max);
        }
        return uniformBaseline;
    }

    private String resolveHover(float localX, float localY) {
        if (unit == null) return "";

        float cx = width / 2f;
        float cy = height / 2f;
        float uniformBaseline = computeUniformBaseline();
        float platesScale = uniformBaseline * PLATE_SCALE_RATIO;
        float dronesScale = uniformBaseline * DRONE_SCALE_RATIO;

        String best = "";
        float bestDst = Math.max(HIT_RADIUS_PLATE, Math.max(HIT_RADIUS_DRONE, HIT_RADIUS_MOUNT));

        hoveredPlate = null;
        hoveredDrone = null;
        hoveredMountSlot = -1;

        if (unit.type instanceof MountUnitType mt) {
            MountUnitType.MountData d = MountUnitType.data(unit);
            for (int i = 0; i < mt.slots.size; i++) {
                MountUnitType.MountSlot slot = mt.slots.get(i);
                Tmp.v2.set(slot.x, slot.y).scl(MOUNT_OFFSET_SCALE * platesScale);
                float dst = Mathf.dst(localX, localY, cx + Tmp.v2.x, cy + Tmp.v2.y);
                if (dst < HIT_RADIUS_MOUNT && dst < bestDst) {
                    bestDst = dst;
                    hoveredMountSlot = i;
                    best = mountSlotTooltip(mt, d, i);
                }
            }
        }

        if (unit.abilities != null) {
            for (var ab : unit.abilities) {
                if (!(ab instanceof ShieldPlateAbility plate)) continue;
                Tmp.v2.set(plate.displayX * platesScale * 2.4f, plate.displayY * platesScale * 2.4f);
                float dst = Mathf.dst(localX, localY, cx + Tmp.v2.x, cy + Tmp.v2.y);
                if (dst < HIT_RADIUS_PLATE && dst < bestDst) {
                    bestDst = dst;
                    hoveredMountSlot = -1;
                    hoveredDrone = null;
                    hoveredPlate = plate;
                    best = plateTooltip(plate);
                }
            }

            for (var ab : unit.abilities) {
                if (!(ab instanceof DroneSpawnAbility droneAb)) continue;
                if (droneAb.spawnOnShoot && !unit.isShooting()) continue;
                droneOffset(droneAb, dronesScale, Tmp.v2);
                float dst = Mathf.dst(localX, localY, cx + Tmp.v2.x, cy + Tmp.v2.y);
                if (dst < HIT_RADIUS_DRONE && dst < bestDst) {
                    bestDst = dst;
                    hoveredMountSlot = -1;
                    hoveredPlate = null;
                    hoveredDrone = droneAb;
                    best = droneTooltip(droneAb);
                }
            }
        }

        return best;
    }

    private String mountSlotTooltip(MountUnitType mt, MountUnitType.MountData d, int index) {
        MountUnitType.ModuleState m = d.modules.get(index);
        MountUnitType.MountSlot slot = mt.slots.get(index);
        if (m.block == null) {
            return Core.bundle.format("ui.ax-info.mount-slot-empty", index + 1, slot.maxSize);
        }
        float hp = m.maxHealth > 0 ? m.health / m.maxHealth : 0f;
        return m.block.localizedName + "\n"
                + Core.bundle.format("ui.ax-info.mount-slot-hp",
                Strings.autoFixed(m.health, 0),
                Strings.autoFixed(m.maxHealth, 0),
                Strings.autoFixed(hp * 100f, 0));
    }

    private String unitSummary() {
        if (unit == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(unit.type != null ? unit.type.localizedName : "Unit").append('\n');
        sb.append("[#").append(getHealthColor(unit.healthf())).append("]")
                .append(Core.bundle.format("ui.ax-info.unit-hp",
                        Strings.autoFixed(unit.health, 0),
                        Strings.autoFixed(unit.maxHealth, 0),
                        Strings.autoFixed(unit.healthf() * 100f, 0)))
                .append("[]");
        return sb.toString();
    }

    private String plateTooltip(ShieldPlateAbility plate) {
        var state = plate.stateFor(unit);
        float durFrac = plate.maxDurability > 0 ? state.durability / plate.maxDurability : 0f;
        String title = plate.displayLabelKey != null && !plate.displayLabelKey.isEmpty()
                ? Core.bundle.get(plate.displayLabelKey)
                : Core.bundle.get("ui.ax-info.plate");

        if (state.building) {
            return title + "\n" + Core.bundle.format("ui.ax-info.plate-building", Strings.autoFixed(durFrac * 100f, 0));
        }
        if (state.durability <= 0f || state.underConstruction) {
            return title + "\n" + Core.bundle.get("ui.ax-info.plate-broken");
        }
        return title + "\n[#" + getHealthColor(durFrac) + "]"
                + Core.bundle.format("ui.ax-info.plate-hp",
                Strings.autoFixed(state.durability, 0),
                Strings.autoFixed(plate.maxDurability, 0),
                Strings.autoFixed(durFrac * 100f, 0));
    }

    private String droneTooltip(DroneSpawnAbility ab) {
        Unit drone = ab.aliveUnit.get(unit);
        String name = ab.drone != null ? ab.drone.localizedName : "Drone";
        if (drone != null && drone.isValid()) {
            return name + "\n[#" + getHealthColor(drone.healthf()) + "]"
                    + Core.bundle.format("ui.ax-info.drone-hp",
                    Strings.autoFixed(drone.health, 0),
                    Strings.autoFixed(drone.maxHealth, 0),
                    Strings.autoFixed(drone.healthf() * 100f, 0));
        }
        float progress = ab.spawnTime > 0 ? ab.timer / ab.spawnTime : 0f;
        return name + "\n" + Core.bundle.format("ui.ax-info.drone-building",
                Strings.autoFixed(Math.min(progress, 1f) * 100f, 0));
    }

    private void droneOffset(DroneSpawnAbility ab, float dronesScale, Vec2 out) {
        float warmup = ab.warmup.getOrDefault(unit, 0f);
        float offsetX = ab.getPoscMath(warmup, ab.dY, ab.moveY);
        float offsetY = ab.getPoscMath(warmup, ab.dX, ab.moveX);
        out.set(offsetX, offsetY);
        out.rotate(90f);
        out.scl(dronesScale * 2.5f);
    }

    @Override
    public void draw() {
        if (unit == null || !hasContent()) return;

        float cx = x + width / 2f;
        float cy = y + height / 2f;
        float uniformBaseline = computeUniformBaseline();
        float silhouetteScale = uniformBaseline;
        float platesScale = uniformBaseline * PLATE_SCALE_RATIO;
        float dronesScale = uniformBaseline * DRONE_SCALE_RATIO;

        boolean hasPlate = false, hasDrone = false;
        if (unit.abilities != null) {
            for (var ab : unit.abilities) {
                if (ab instanceof ShieldPlateAbility) hasPlate = true;
                if (ab instanceof DroneSpawnAbility) hasDrone = true;
            }
        }
        if (hasPlate) silhouetteScale *= PLATE_SILHOUETTE_SCALE_RATIO;
        if (hasDrone) silhouetteScale *= DRONE_SILHOUETTE_SCALE_RATIO;

        if (unit.type != null && unit.type.fullIcon != null) {
            Draw.shader(FlatIconShader.instance);
            Draw.color(Color.valueOf("383838"));
            Draw.rect(unit.type.fullIcon, cx, cy,
                    unit.type.fullIcon.width * silhouetteScale,
                    unit.type.fullIcon.height * silhouetteScale, 0f);
            Draw.shader();
            Draw.reset();
        }

        if (unit.type instanceof MountUnitType mt) {
            drawMountSlots(mt, cx, cy, platesScale);
        }

        if (unit.abilities != null) {
            for (var ab : unit.abilities) {
                if (ab instanceof ShieldPlateAbility plate) drawPlates(plate, cx, cy, platesScale);
            }
            for (var ab : unit.abilities) {
                if (ab instanceof DroneSpawnAbility droneAb) drawDrones(droneAb, cx, cy, dronesScale);
            }
        }

        drawHintMarks(cx, cy, platesScale, dronesScale);
    }

    private void drawMountSlots(MountUnitType mt, float cx, float cy, float scale) {
        MountUnitType.MountData d = MountUnitType.data(unit);
        for (int i = 0; i < mt.slots.size; i++) {
            MountUnitType.MountSlot slot = mt.slots.get(i);
            MountUnitType.ModuleState m = d.modules.get(i);
            float k = MOUNT_OFFSET_SCALE * BASE_SCALE * Math.max(1f, unit.type.hitSize / 12f);
            Tmp.v2.set(slot.x, slot.y).scl(k);
            float px = cx + Tmp.v1.x;
            float py = cy + Tmp.v1.y;
            boolean hot = hoveredMountSlot == i || d.selectedSlot == i;

            if (m.block == null) {
                Draw.color(hot ? Pal.accent : Pal.gray, hot ? 0.85f : 0.4f);
                Fill.square(px, py, 4f + slot.maxSize, 0f);
                Draw.color();
            } else {
                float hp = m.maxHealth > 0 ? m.health / m.maxHealth : 1f;
                Color col = getHealthColor(hp);
                if (hot) col = col.cpy().lerp(Color.white, 0.45f);
                Draw.color(col);
                float sz = (6f + m.block.size * 3f) * (hot ? 1.1f : 1f);
                Draw.rect(m.block.fullIcon, px, py, sz, sz, 0f);
                Draw.color();
            }
        }
    }

    private void drawPlates(ShieldPlateAbility plate, float cx, float cy, float scale) {
        var state = plate.stateFor(unit);
        TextureRegion icon = plate.part.regions.length > 0 ? plate.part.regions[0] : null;
        if (icon == null) return;

        float durFrac = plate.maxDurability > 0 ? state.durability / plate.maxDurability : 0f;
        Color statusCol = (state.durability <= 0f && !state.underConstruction && !plate.canRebuild)
                ? Color.valueOf("8B4444") : getHealthColor(durFrac);
        if (plate == hoveredPlate) statusCol = statusCol.cpy().lerp(Color.white, 0.55f);

        Vec2 offsetVec = Pools.obtain(Vec2.class, Vec2::new);
        offsetVec.set(plate.part.x * scale * 2.4f, plate.part.y * scale * 2.4f);
        float px = cx + offsetVec.x;
        float py = cy + offsetVec.y;
        Pools.free(offsetVec);

        float drawScale = plate == hoveredPlate ? scale * 1.12f : scale;
        Draw.shader(FlatIconShader.instance);
        Draw.color(statusCol);
        Draw.rect(icon, px, py, icon.width * drawScale, icon.height * drawScale, plate.part.rotation);
        Draw.shader();
        Draw.reset();
    }

    private void drawDrones(DroneSpawnAbility ab, float cx, float cy, float scale) {
        if (ab.spawnOnShoot && !unit.isShooting()) return;
        Unit drone = ab.aliveUnit.get(unit);
        float progress = ab.spawnTime > 0 ? ab.timer / ab.spawnTime : 0f;
        TextureRegion icon = ab.drone.fullIcon;

        Color col = (drone != null && drone.isValid())
                ? getHealthColor(drone.health / drone.maxHealth)
                : getBuildColor(progress);
        if (ab == hoveredDrone) col = col.cpy().lerp(Color.white, 0.55f);

        droneOffset(ab, scale, Tmp.v1);
        float finalX = cx + Tmp.v1.x;
        float finalY = cy + Tmp.v1.y;

        float warmup = ab.warmup.getOrDefault(unit, 0f);
        float droneRot = 90f + ab.getRotShooter(unit, warmup, ab.dRot, ab.moveRot, ab.drone.isShield);
        float drawScale = ab == hoveredDrone ? scale : scale * 0.7f;
        float droneW = icon.width * drawScale;
        float droneH = icon.height * drawScale;

        Draw.color(col);
        Draw.rect(icon, finalX, finalY, droneW, droneH, droneRot - 90f);
        Draw.reset();
    }

    private void drawHintMarks(float cx, float cy, float platesScale, float dronesScale) {
        if (unit.type instanceof MountUnitType mt) {
            for (int i = 0; i < mt.slots.size; i++) {
                MountUnitType.MountSlot slot = mt.slots.get(i);
                Tmp.v2.set(slot.x, slot.y).scl(MOUNT_OFFSET_SCALE * platesScale);
                drawQuestion(cx + Tmp.v2.x, cy + Tmp.v2.y, hoveredMountSlot == i);
            }
        }
        if (unit.abilities == null) return;

        for (var ab : unit.abilities) {
            if (!(ab instanceof ShieldPlateAbility plate)) continue;
            Tmp.v2.set(plate.displayX * platesScale * 2.4f, plate.displayY * platesScale * 2.4f);
            drawQuestion(cx + Tmp.v2.x, cy + Tmp.v2.y, plate == hoveredPlate);
        }
        for (var ab : unit.abilities) {
            if (!(ab instanceof DroneSpawnAbility droneAb)) continue;
            if (droneAb.spawnOnShoot && !unit.isShooting()) continue;
            droneOffset(droneAb, dronesScale, Tmp.v2);
            drawQuestion(cx + Tmp.v2.x, cy + Tmp.v2.y, droneAb == hoveredDrone);
        }
    }

    private void drawQuestion(float qx, float qy, boolean hot) {
        float a = hot ? 1f : 0.75f;
        float s = AxthrixLoader.hudButtonScale * 1.2f;
        Draw.color(Color.white, a);
        Fill.circle(qx, qy, 5f * s);
        Draw.color(Color.valueOf("2a2a2a"), a);
        Fill.circle(qx, qy, 4f * s);
        Draw.color(hot ? Pal.accent : Color.white, a);
        Fonts.outline.draw("?", qx, qy + 1f, hot ? Pal.accent : Color.white, 0.3f * s, false, Align.center);
        Draw.reset();
    }
}