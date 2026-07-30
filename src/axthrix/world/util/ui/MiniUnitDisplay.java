package axthrix.world.util.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.pooling.Pools;
import arc.util.Tmp;
import axthrix.AxthrixLoader;
import axthrix.world.types.abilities.DroneSpawnAbility;
import axthrix.world.types.abilities.ShieldPlateAbility;
import axthrix.world.util.draw.FlatIconShader;
import mindustry.gen.Unit;

public class MiniUnitDisplay extends arc.scene.Element {
    private final Unit unit;

    private static final float COMPACT_SCALE_MODIFIER = 1.1f;
    private static final float SMALL_UNIT_SCALE_MULTIPLIER = 0.7f;
    private static final float SILHOUETTE_SCALE_RATIO = 1.6f;
    private static final float PLATE_SCALE_RATIO = 1.6f;
    private static final float DRONE_SCALE_RATIO = 2f;

    public MiniUnitDisplay(Unit unit) {
        this.unit = unit;
    }

    public boolean hasContent() {
        if (unit == null || unit.abilities == null) return false;
        for (var ab : unit.abilities) {
            if (ab instanceof ShieldPlateAbility || ab instanceof DroneSpawnAbility) return true;
        }
        return false;
    }

    private Color getHealthColor(float fraction) {
        if (fraction > 0.65f) return Color.valueOf("7CFF7C");
        if (fraction > 0.35f) return Color.valueOf("FFEA7C");
        return Color.valueOf("FF6666");
    }

    private Color getBuildColor(float fraction) {
        return Color.valueOf("FFAA5E").cpy().lerp(Color.valueOf("FFEA7C"), fraction);
    }

    @Override
    public void draw() {
        if (unit == null || !hasContent()) return;

        float cx = x + width / 2f;
        float cy = y + height / 2f;

        float iconW = unit.type != null && unit.type.fullIcon != null ? unit.type.fullIcon.width : 40f;
        float iconH = unit.type != null && unit.type.fullIcon != null ? unit.type.fullIcon.height : 40f;
        float maxIconSize = Math.max(iconW, iconH);

        float targetBoxEnvelope = 48f;
        float baseScale = 1.95f;

        if (maxIconSize * baseScale > targetBoxEnvelope) {
            baseScale = targetBoxEnvelope / maxIconSize;
        }

        float uniformBaseline = baseScale * COMPACT_SCALE_MODIFIER;

        if (unit.type != null) {
            int nt = (int) (unit.type.hitSize/5);
            if (nt <= 0){
                nt = 1;
            }
            float varible = SMALL_UNIT_SCALE_MULTIPLIER*nt;
            if(varible > 1){
                varible = 1;
            }
            uniformBaseline *= varible;
        }

        float silhouetteScale = uniformBaseline * SILHOUETTE_SCALE_RATIO;
        float platesScale = uniformBaseline * PLATE_SCALE_RATIO;
        float dronesScale = uniformBaseline * DRONE_SCALE_RATIO;

        float renderingRotation = AxthrixLoader.hudMiniFacesUp ? 90f : unit.rotation;

        if (unit.type != null && unit.type.fullIcon != null) {
            Draw.shader(FlatIconShader.instance);
            Draw.color(Color.valueOf("383838"));
            Draw.rect(unit.type.fullIcon, cx, cy,
                    unit.type.fullIcon.width * silhouetteScale,
                    unit.type.fullIcon.height * silhouetteScale,
                    renderingRotation - 90f);
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
    }


    private void drawPlates(ShieldPlateAbility plate, float cx, float cy, float scale) {
        var state = plate.stateFor(unit);
        TextureRegion icon = plate.part.regions.length > 0 ? plate.part.regions[0] : null;
        if (icon == null) return;

        float durFrac = plate.maxDurability > 0 ? state.durability / plate.maxDurability : 0f;
        Color statusCol = (state.durability <= 0f && !state.underConstruction && !plate.canRebuild)
                ? Color.valueOf("8B4444") : getHealthColor(durFrac);

        float renderingRotation = AxthrixLoader.hudMiniFacesUp ? 90f : unit.rotation;

        arc.math.geom.Vec2 offsetVec = Pools.obtain(arc.math.geom.Vec2.class, arc.math.geom.Vec2::new);
        offsetVec.set(plate.part.x * scale * 2.4f, plate.part.y * scale * 2.4f);
        if (AxthrixLoader.hudMiniFacesUp) {
            offsetVec.rotate(90f - unit.rotation);
        }

        float px = cx + offsetVec.x;
        float py = cy + offsetVec.y;
        Pools.free(offsetVec);

        float rot = renderingRotation + plate.part.rotation - 90f;

        Draw.shader(FlatIconShader.instance);
        Draw.color(statusCol);
        Draw.rect(icon, px, py, icon.width * scale, icon.height * scale, rot);
        Draw.shader();
        Draw.reset();
    }

    private void drawDrones(DroneSpawnAbility ab, float cx, float cy, float scale) {
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

        float activeRotation = AxthrixLoader.hudMiniFacesUp ? 90f : unit.rotation;

        Tmp.v1.set(offsetX, offsetY);
        if (AxthrixLoader.hudMiniFacesUp) {
            Tmp.v1.rotate(90f - unit.rotation);
        }
        Tmp.v1.rotate(unit.rotation);
        Tmp.v1.scl(scale * 2.5f);

        float finalX = cx + Tmp.v1.x;
        float finalY = cy + Tmp.v1.y;

        float droneRot;
        if (ab.drone.isShield) {
            droneRot = activeRotation + ab.getRotShooter(unit, warmup, ab.dRot, ab.moveRot, true);
        } else if (unit.isShooting() && !AxthrixLoader.hudMiniFacesUp) {
            droneRot = activeRotation;
        } else {
            droneRot = activeRotation + ab.getRotShooter(unit, warmup, ab.dRot, ab.moveRot, false);
        }

        float droneW = icon.width * scale * 1.05f;
        float droneH = icon.height * scale * 1.05f;
        float targetRot = droneRot - 90f;

        Draw.shader(FlatIconShader.instance);
        Draw.color(Color.valueOf("383838"));
        Draw.rect(icon, finalX, finalY, droneW + 1.5f, droneH + 1.5f, targetRot);
        Draw.shader();
        Draw.reset();

        Draw.color(col);
        Draw.rect(icon, finalX, finalY, droneW, droneH, targetRot);
        Draw.reset();
    }
}