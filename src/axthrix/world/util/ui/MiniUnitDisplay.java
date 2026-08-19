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
import axthrix.world.util.draw.FlatIconShader;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;

public class MiniUnitDisplay extends Element {
    public final Unit unit;

    //main values
    public static float COMPACT_SCALE_MODIFIER = 2.5f;
    public static float PLATE_SILHOUETTE_SCALE_RATIO = 1.15f;
    public static float DRONE_SILHOUETTE_SCALE_RATIO = 0.7f;
    public static float PLATE_SCALE_RATIO = 1.15f;
    public static float DRONE_SCALE_RATIO = 1.1f;
    public static float TARGET_HIT = 40f;
    public static float MIN_SIZE_FACTOR = 0.9f;
    public static float MAX_SIZE_FACTOR = 1.15f;
    public static float TARGET_BOX = 46f;
    public static float BASE_SCALE = 0.7f;
    public static float SIZE_LERP = 1f;


    //tooltip hitboxes
    public static final float HIT_RADIUS = 12;
    public static float HIT_RADIUS_PLATE = 12f;
    public static float HIT_RADIUS_DRONE = 22f;


    public String hoverText = "";
    public Label tipLabel;
    public ShieldPlateAbility hoveredPlate;
    public DroneSpawnAbility hoveredDrone;
    public boolean pointerOver;

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

    public void setHover(float x, float y) {
        hoverText = resolveHover(x, y);
        if (hoverText.isEmpty()) hoverText = unitSummary();
        if (tipLabel != null) {
            tipLabel.setText(hoverText);
            tipLabel.pack();
            if (tipLabel.parent != null) tipLabel.parent.pack();
        }
    }

    public boolean hasContent() {
        if (unit == null || unit.abilities == null) return false;
        for (var ab : unit.abilities) {
            if (ab instanceof ShieldPlateAbility || ab instanceof DroneSpawnAbility) return true;
        }
        return false;
    }

    public Color getHealthColor(float fraction) {
        fraction = Mathf.clamp(fraction);
        Color low = Color.valueOf("FF6666");
        Color mid = Color.valueOf("FFEA7C");
        Color high = Color.valueOf("7CFF7C");
        if (fraction < 0.5f) {
            return Tmp.c1.set(low).lerp(mid, fraction / 0.5f);
        }
        return Tmp.c1.set(mid).lerp(high, (fraction - 0.5f) / 0.5f);
    }

    public Color getBuildColor(float fraction) {
        fraction = Mathf.clamp(fraction);
        return Tmp.c1.set(Color.valueOf("FFAA5E")).lerp(Color.valueOf("FFEA7C"), fraction);
    }

    public float computeUniformBaseline() {
        float iconW = unit.type != null && unit.type.fullIcon != null ? unit.type.fullIcon.width : 40f;
        float iconH = unit.type != null && unit.type.fullIcon != null ? unit.type.fullIcon.height : 40f;
        float maxIconSize = Math.max(iconW, iconH);

        float baseScale = BASE_SCALE;
        if (maxIconSize * baseScale > TARGET_BOX) {
            baseScale = TARGET_BOX / maxIconSize;
        }

        float uniformBaseline = baseScale * COMPACT_SCALE_MODIFIER;

        if (unit.type != null) {
            float hit = Math.max(unit.type.hitSize, 1f);
            float raw = TARGET_HIT / hit;
            float sizeFactor = Mathf.lerp(1f, raw, SIZE_LERP);
            sizeFactor = Mathf.clamp(sizeFactor, MIN_SIZE_FACTOR, MAX_SIZE_FACTOR);
            uniformBaseline *= sizeFactor;
        }

        return uniformBaseline;
    }

    public String resolveHover(float localX, float localY) {
        if (unit == null || unit.abilities == null) return "";

        float cx = width / 2f;
        float cy = height / 2f;
        float uniformBaseline = computeUniformBaseline();
        float platesScale = uniformBaseline * PLATE_SCALE_RATIO;
        float dronesScale = uniformBaseline * DRONE_SCALE_RATIO;

        String best = "";
        float bestDst = HIT_RADIUS;

        hoveredPlate = null;
        hoveredDrone = null;

        for (var ab : unit.abilities) {
            if (!(ab instanceof ShieldPlateAbility plate)) continue;

            Tmp.v2.set(plate.displayX * platesScale * 2.4f, plate.displayY * platesScale * 2.4f);
            Tmp.v2.rotate(0f);

            float dst = Mathf.dst(localX, localY, cx + Tmp.v2.x, cy + Tmp.v2.y);
            if (dst < HIT_RADIUS_PLATE && dst < bestDst) {
                bestDst = dst;
                hoveredDrone = null;
                hoveredPlate = plate;
                best = plateTooltip(plate);
            }
        }

        for (var ab : unit.abilities) {
            if (!(ab instanceof DroneSpawnAbility droneAb)) continue;
            if (droneAb.spawnOnShoot && !unit.isShooting()) continue;

            float warmup = droneAb.warmup.getOrDefault(unit, 0f);
            float offsetX = droneAb.getPoscMath(warmup, droneAb.dY, droneAb.moveY);
            float offsetY = droneAb.getPoscMath(warmup, droneAb.dX, droneAb.moveX);

            Tmp.v2.set(offsetX, offsetY);
            Tmp.v1.rotate(90);
            Tmp.v2.scl(dronesScale * 2.5f);

            droneOffset(droneAb, dronesScale, Tmp.v2);
            float dst = Mathf.dst(localX, localY, cx + Tmp.v2.x, cy + Tmp.v2.y);
            if (dst < HIT_RADIUS_DRONE && dst < bestDst) {
                bestDst = dst;
                best = droneTooltip(droneAb);
                hoveredDrone = droneAb;
                hoveredPlate = null;
            }
        }

        return best;
    }

    public String unitSummary() {
        if (unit == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(unit.type != null ? unit.type.localizedName : "Unit");
        sb.append('\n');
        sb.append("[#").append(getHealthColor(unit.healthf()).toString()).append("]").append(Core.bundle.format("ui.ax-info.unit-hp",
                Strings.autoFixed(unit.health, 0),
                Strings.autoFixed(unit.maxHealth, 0),
                Strings.autoFixed(unit.healthf() * 100f, 0))).append("[]");

        boolean anyPlate = false;
        boolean canRebuild = false;
        boolean onlyOutOfCombat = false;
        boolean inCombat = false;

        if (unit.abilities != null) {
            for (var ab : unit.abilities) {
                if (!(ab instanceof ShieldPlateAbility plate)) continue;
                anyPlate = true;
                if (plate.canRebuild) canRebuild = true;
                if (plate.requireOutOfCombat) onlyOutOfCombat = true;
                inCombat = plate.enemyNearby(unit);
            }
        }

        if (anyPlate) {
            sb.append('\n');
            if (!canRebuild) {
                sb.append(Core.bundle.get("ui.ax-info.plates-no-rebuild"));
            } else if (onlyOutOfCombat) {
                sb.append(Core.bundle.get(inCombat
                        ? "ui.ax-info.plates-in-combat"
                        : "ui.ax-info.plates-out-combat"));
            } else {
                sb.append(Core.bundle.get("ui.ax-info.plates-rebuild-anytime"));
            }
        }

        return sb.toString();
    }

    public String plateTooltip(ShieldPlateAbility plate) {
        var state = plate.stateFor(unit);
        float durFrac = plate.maxDurability > 0 ? state.durability / plate.maxDurability : 0f;
        String title = plate.displayLabelKey != null && !plate.displayLabelKey.isEmpty()
                ? Core.bundle.get(plate.displayLabelKey)
                : Core.bundle.get("ui.ax-info.plate");

        if (state.building) {
            return title + "\n" + Core.bundle.format("ui.ax-info.plate-building",
                    Strings.autoFixed(durFrac * 100f, 0));
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

    public String droneTooltip(DroneSpawnAbility ab) {
        Unit drone = ab.aliveUnit.get(unit);
        String name = ab.drone != null ? ab.drone.localizedName : "Drone";

        if (drone != null && drone.isValid()) {
            return name + "\n[#"+getHealthColor(drone.healthf()).toString()+"]"+  Core.bundle.format("ui.ax-info.drone-hp",
                    Strings.autoFixed(drone.health, 0),
                    Strings.autoFixed(drone.maxHealth, 0),
                    Strings.autoFixed(drone.healthf() * 100f, 0));
        }

        float progress = ab.spawnTime > 0 ? ab.timer / ab.spawnTime : 0f;
        return name + "\n" + Core.bundle.format("ui.ax-info.drone-building",
                Strings.autoFixed(Math.min(progress, 1f) * 100f, 0));
    }



    @Override
    public void draw() {
        if (unit == null || !hasContent()) return;

        float cx = x + width / 2f;
        float cy = y + height / 2f;

        float uniformBaseline = computeUniformBaseline();
        float platesScale = uniformBaseline * PLATE_SCALE_RATIO;
        float dronesScale = uniformBaseline * DRONE_SCALE_RATIO;

        float silhouetteScale = uniformBaseline;
        boolean hasPlate = false, hasDrone = false;
        for (var ab : unit.abilities) {
            if (ab instanceof ShieldPlateAbility) hasPlate = true;
            if (ab instanceof DroneSpawnAbility) hasDrone = true;
        }
        if (hasPlate) silhouetteScale *= PLATE_SILHOUETTE_SCALE_RATIO;
        if (hasDrone) silhouetteScale *= DRONE_SILHOUETTE_SCALE_RATIO;

        if (unit.type != null && unit.type.fullIcon != null) {
            Draw.shader(FlatIconShader.instance);
            Draw.color(Color.valueOf("383838"));
            Draw.rect(unit.type.fullIcon, cx, cy,
                    unit.type.fullIcon.width * silhouetteScale,
                    unit.type.fullIcon.height * silhouetteScale,
                    0f);
            Draw.shader();
            Draw.reset();
        }

        for (var ab : unit.abilities) {
            if (ab instanceof ShieldPlateAbility plate) {
                drawPlates(plate, cx, cy, platesScale);
            }
        }

        for (var ab : unit.abilities) {
            if (ab instanceof DroneSpawnAbility droneAb) {
                drawDrones(droneAb, cx, cy, dronesScale);
            }
        }

        drawHintMarks(cx, cy, platesScale, dronesScale);
    }

    public void drawPlates(ShieldPlateAbility plate, float cx, float cy, float scale) {
        var state = plate.stateFor(unit);
        TextureRegion icon = plate.part.regions.length > 0 ? plate.part.regions[0] : null;
        if (icon == null) return;

        float durFrac = plate.maxDurability > 0 ? state.durability / plate.maxDurability : 0f;
        Color statusCol = (state.durability <= 0f && !state.underConstruction && !plate.canRebuild)
                ? Color.valueOf("8B4444")
                : getHealthColor(durFrac);

        Vec2 offsetVec = Pools.obtain(Vec2.class, Vec2::new);
        offsetVec.set(plate.part.x * scale * 2.4f, plate.part.y * scale * 2.4f);
        offsetVec.rotate(0f);

        float px = cx + offsetVec.x;
        float py = cy + offsetVec.y;
        Pools.free(offsetVec);

        float rot = 90f + plate.part.rotation - 90f;

        if (plate == hoveredPlate) {
            statusCol = statusCol.cpy().lerp(Color.white, 0.55f);
        }

        float drawScale = (plate == hoveredPlate) ? scale * 1.12f : scale;

        Draw.shader(FlatIconShader.instance);
        Draw.color(statusCol);
        Draw.rect(icon, px, py, icon.width * drawScale, icon.height * drawScale, rot);
        Draw.shader();
        Draw.reset();
    }

    public void drawDrones(DroneSpawnAbility ab, float cx, float cy, float scale) {
        if (ab.spawnOnShoot && !unit.isShooting()) return;

        Unit drone = ab.aliveUnit.get(unit);
        float progress = ab.timer / ab.spawnTime;
        TextureRegion icon = ab.drone.fullIcon;

        Color col = (drone != null && drone.isValid())
                ? getHealthColor(drone.health / drone.maxHealth)
                : getBuildColor(progress);

        float warmup = ab.warmup.getOrDefault(unit, 0f);
        float offsetX = ab.getPoscMath(warmup, ab.dY, ab.moveY);
        float offsetY = ab.getPoscMath(warmup, ab.dX, ab.moveX);

        Tmp.v1.set(offsetX, offsetY);
        Tmp.v1.rotate(90);
        Tmp.v1.scl(scale * 2.5f);

        droneOffset(ab, scale, Tmp.v1);
        float finalX = cx + Tmp.v1.x;
        float finalY = cy + Tmp.v1.y;

        float droneRot;
        if (ab.drone.isShield) {
            droneRot = 90f + ab.getRotShooter(unit, warmup, ab.dRot, ab.moveRot, true);
        } else {
            droneRot = 90f + ab.getRotShooter(unit, warmup, ab.dRot, ab.moveRot, false);
        }

        if (ab == hoveredDrone) {
            col = col.cpy().lerp(Color.white, 0.55f);
        }

        float drawScale = (ab == hoveredDrone) ? scale : scale * 0.7f;

        float droneW = icon.width * drawScale;
        float droneH = icon.height  * drawScale;
        float targetRot = droneRot - 90f;


//        Draw.shader(FlatIconShader.instance);
//        Draw.color(Color.valueOf("383838"));
//        Draw.rect(icon, finalX, finalY, droneW + 4, droneH + 4, targetRot);
//        Draw.shader();
//        Draw.reset();

        Draw.color(col);
        Draw.rect(icon, finalX, finalY, droneW, droneH, targetRot);
        Draw.reset();
    }

    public void droneOffset(DroneSpawnAbility ab, float dronesScale, Vec2 out) {
        float warmup = ab.warmup.getOrDefault(unit, 0f);
        float offsetX = ab.getPoscMath(warmup, ab.dY, ab.moveY);
        float offsetY = ab.getPoscMath(warmup, ab.dX, ab.moveX);
        out.set(offsetX, offsetY);
        out.rotate(90f);
        out.scl(dronesScale * 2.5f);
    }

    public void drawHintMarks(float cx, float cy, float platesScale, float dronesScale) {
        if (unit.abilities == null) return;

        for (var ab : unit.abilities) {
            if (!(ab instanceof ShieldPlateAbility plate)) continue;
            Tmp.v2.set(plate.displayX * platesScale * 2.4f, plate.displayY * platesScale * 2.4f);
            drawQuestion(cx + Tmp.v2.x, cy + Tmp.v2.y, plate == hoveredPlate);
        }

        for (var ab : unit.abilities) {
            if (!(ab instanceof DroneSpawnAbility droneAb)) continue;
            if (droneAb.spawnOnShoot && !unit.isShooting()) continue;

            float warmup = droneAb.warmup.getOrDefault(unit, 0f);
            float offsetX = droneAb.getPoscMath(warmup, droneAb.dY, droneAb.moveY);
            float offsetY = droneAb.getPoscMath(warmup, droneAb.dX, droneAb.moveX);
            Tmp.v2.set(offsetX, offsetY).scl(dronesScale * 2.5f);
            Tmp.v1.rotate(90f);
            droneOffset(droneAb, dronesScale, Tmp.v2);
            drawQuestion(cx + Tmp.v2.x, cy + Tmp.v2.y, droneAb == hoveredDrone);
        }
    }

    public void drawQuestion(float qx, float qy, boolean hot) {
        float a = hot ? 1f : 0.75f;
        Draw.color(Color.white, a);
        Fill.circle(qx, qy, 5f * (AxthrixLoader.hudButtonScale*1.2f));
        Draw.color(Color.valueOf("2a2a2a"), a);
        Fill.circle(qx, qy, 4f * (AxthrixLoader.hudButtonScale*1.2f));
        Draw.color(hot ? Pal.accent : Color.white, a);
        Fonts.outline.draw("?", qx, qy + 1f, hot ? Pal.accent : Color.white, 0.3f * (AxthrixLoader.hudButtonScale*1.2f), false, Align.center);
        Draw.reset();
    }
}