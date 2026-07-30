package axthrix.world.util.ui;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.Rect;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.pooling.Pools;
import axthrix.world.types.abilities.ShieldPlateAbility;
import axthrix.world.util.draw.FlatIconShader;
import mindustry.gen.Tex;
import mindustry.gen.Unit;
import mindustry.ui.Bar;
import mindustry.ui.Fonts;

public class ShieldPlatesBar extends Bar {
    private final Unit unit;
    private final Rect scissor = new Rect();

    public ShieldPlatesBar(Unit unit) {
        super(() -> "", () -> Color.white, () -> 0f);
        this.unit = unit;
    }

    @Override
    public void draw() {
        if (unit == null || unit.abilities == null) return;

        Seq<ShieldPlateAbility> plates = new Seq<>();
        for (var ab : unit.abilities) {
            if (ab instanceof ShieldPlateAbility sp) plates.add(sp);
        }

        int totalPlates = plates.size;
        if (totalPlates == 0) return;

        Draw.colorl(0.1f);
        Draw.alpha(parentAlpha);
        Tex.bar.draw(x, y, width, height);

        float segmentWidth = width / totalPlates;

        for (int index = 0; index < totalPlates; index++) {
            ShieldPlateAbility plate = plates.get(index);
            var state = plate.stateFor(unit);

            float durFraction = plate.maxDurability > 0f ? Mathf.clamp(state.durability / plate.maxDurability) : 0f;
            float segmentX = x + (index * segmentWidth);

            Color barColor;
            String textStatus;
            if (state.underConstruction) {
                float progress = plate.buildTime > 0f ? Mathf.clamp(1f - (state.buildRemaining / plate.buildTime)) : 1f;
                barColor = Color.valueOf("FFAA5E").cpy().lerp(Color.valueOf("FFEA7C"), progress);
                textStatus = "[accent]...[]";
            } else if (state.durability <= 0f && !plate.canRebuild) {
                barColor = Color.valueOf("8B4444");
                textStatus = "[scarlet]0%[]";
            } else {
                int percentage = (int)(durFraction * 100);
                if (durFraction > 0.65f) {
                    barColor = Color.valueOf("7CFF7C");
                    textStatus = "[#7CFF7C]" + percentage + "%[]";
                } else if (durFraction > 0.35f) {
                    barColor = Color.valueOf("FFEA7C");
                    textStatus = "[#FFEA7C]" + percentage + "%[]";
                } else {
                    barColor = Color.valueOf("FF6666");
                    textStatus = "[#FF6666]" + percentage + "%[]";
                }
            }

            boolean isLeftBorder = (index == 0);
            boolean isRightBorder = (index == totalPlates - 1);
            drawSegmentFill(segmentX, y, segmentWidth * durFraction, segmentWidth, barColor, isLeftBorder, isRightBorder);

            float iconCenterX = segmentX + (segmentWidth / 2f);
            float iconCenterY = y + (height / 2f);

            TextureRegion icon = (plate.part.regions != null && plate.part.regions.length > 0) ? plate.part.regions[0] : null;
            if (icon != null && icon.found()) {
                float sizeScale = Math.min((segmentWidth * 0.85f) / icon.width, (height * 0.95f) / icon.height);
                float iconW = icon.width * sizeScale;
                float iconH = icon.height * sizeScale;

                Draw.shader(FlatIconShader.instance);
                Draw.color(Color.white);
                Draw.alpha(parentAlpha * 0.45f);
                Draw.rect(icon, iconCenterX, iconCenterY, iconW, iconH);
                Draw.shader();
            }

            drawSegmentText(textStatus, iconCenterX, iconCenterY);

            if (!isRightBorder) {
                Draw.color(Color.darkGray);
                Draw.alpha(parentAlpha * 0.5f);
                Fill.crect(segmentX + segmentWidth - 1f, y, 2f, height);
            }
        }

        Draw.reset();
    }

    private void drawSegmentFill(float dx, float dy, float currentFillW, float totalSegmentW, Color col, boolean left, boolean right) {
        if (currentFillW <= 0) return;
        Draw.color(col);
        Draw.alpha(parentAlpha);

        if (left && right) {
            if (ScissorStack.push(scissor.set(dx, dy, currentFillW, height))) {
                Tex.barTop.draw(dx, dy, width, height);
                ScissorStack.pop();
            }
        } else if (left) {
            if (ScissorStack.push(scissor.set(dx, dy, currentFillW, height))) {
                Tex.barTop.draw(dx, dy, totalSegmentW * 1.5f, height);
                ScissorStack.pop();
            }
        } else if (right) {
            if (ScissorStack.push(scissor.set(dx, dy, currentFillW, height))) {
                Tex.barTop.draw(dx - (totalSegmentW * 0.5f), dy, totalSegmentW * 1.5f, height);
                ScissorStack.pop();
            }
        } else {
            Fill.crect(dx, dy, currentFillW, height);
        }
    }

    private void drawSegmentText(String text, float centerX, float centerY) {
        Font font = Fonts.outline;
        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;

        font.getData().setScale(0.55f / Scl.scl(1f));
        font.setColor(Color.white);

        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, text);

        font.getCache().clear();
        font.getCache().addText(text, centerX - lay.width / 2f, centerY + lay.height / 2f);
        font.getCache().draw(parentAlpha);

        Pools.free(lay);
        font.getData().setScale(oldScaleX, oldScaleY);
    }
}
