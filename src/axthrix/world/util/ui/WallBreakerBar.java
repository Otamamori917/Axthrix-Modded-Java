package axthrix.world.util.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.geom.Rect;
import arc.util.pooling.Pools;
import axthrix.world.types.ai.WallBreakerAI;
import axthrix.world.types.block.drill.WallBreakerSpawner;
import axthrix.world.types.unittypes.WallBreakerUnitType;
import mindustry.gen.*;
import mindustry.ui.*;
import arc.scene.ui.layout.*;

public class WallBreakerBar extends Bar {
    public WallBreakerSpawner.WallBreakerBuilding build;
    private final Rect scissor = new Rect();

    public WallBreakerBar(WallBreakerSpawner.WallBreakerBuilding build) {
        super(() -> "", () -> Color.white, () -> 0f);
        this.build = build;
    }

    @Override
    public void draw() {
        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        Tex.bar.draw(x, y, width, height);

        boolean isBuilding = build.spawnedUnit == null || !build.spawnedUnit.isValid();

        if (isBuilding) {
            float prog = Math.min(build.progress / ((WallBreakerSpawner)build.block).buildTime, 1f);
            drawFill(x, y, width * prog, Color.sky, true, false);
            drawText("BUILDING: " + (int)(prog * 100) + "%");
        } else {
            WallBreakerAI ai = build.spawnedUnit.controller() instanceof WallBreakerAI a ? a : null;
            WallBreakerUnitType u = (WallBreakerUnitType)build.spawnedUnit.type;

            if (ai != null && ai.noWallsLeft) {
                drawFill(x, y, width, Color.scarlet, true, true);
                drawText("NO WALLS FOUND");
            } else {
                float sw = width / 3f;
                drawFill(x, y, sw * build.spawnedUnit.healthf(), Color.royal, true, false);

                float f = (float) build.spawnedUnit.stack.amount / u.itemCapacity;
                drawFill(x + sw, y, sw * f, Color.orange, false, false);

                boolean isReturning = false;
                if (ai != null) {
                    isReturning = ai.state == WallBreakerAI.State.returning;
                }
                float d = isReturning ? 1 : u.drillTime.get(build.spawnedUnit, 0f) / u.maxDrillTime;
                drawFill(x + sw * 2, y, sw * d, isReturning ? Color.tan : Color.green, false, true);

                drawText("HP [stat]" + (int)(build.spawnedUnit.healthf() * 100) + "%[]" +
                        "  [black]|[] " +
                        "Storage [stat]" + (int)(f * 100) + "%[]" +
                        " [black]|[]  " +
                        (isReturning ? "[]Returning: [stat]" + (int)build.spawnedUnit.dst(build) :
                                "[]Progress [stat]" + (int)(d * 100) + "%[]"));
            }
        }
        Draw.reset();
    }

    private void drawFill(float dx, float dy, float w, Color col, boolean left, boolean right) {
        if (w <= 0) return;
        Draw.color(col);
        Draw.alpha(parentAlpha);

        if (left && right) {
            if (ScissorStack.push(scissor.set(dx, dy, w, height))) {
                Tex.barTop.draw(dx, dy, width, height);
                ScissorStack.pop();
            }
        } else if (left) {
            if (ScissorStack.push(scissor.set(dx, dy, w, height))) {
                Tex.barTop.draw(dx, dy, width * 1.5f, height);
                ScissorStack.pop();
            }
        } else if (right) {
            if (ScissorStack.push(scissor.set(dx, dy, w, height))) {
                Tex.barTop.draw(dx - (width * 0.5f), dy, width * 1.5f, height);
                ScissorStack.pop();
            }
        } else {
            Fill.crect(dx, dy, w, height);
        }
    }

    private void drawText(String text) {
        Font font = Fonts.outline;
        float oldScale = font.getData().scaleX;

        font.getData().setScale(0.6f / Scl.scl(1f));
        font.setColor(Color.white);

        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, text);

        font.getCache().clear();
        font.getCache().addText(text, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f);
        font.getCache().draw(parentAlpha);

        Pools.free(lay);
        font.getData().setScale(oldScale);
    }
}