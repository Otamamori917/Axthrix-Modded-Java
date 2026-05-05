package axthrix.world.util.ui;

import arc.Core;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.pooling.*;
import arc.func.Prov;
import axthrix.AxthrixLoader;
import axthrix.world.util.TempUnit;
import axthrix.world.util.logics.TemperatureLogic;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

public class TemperatureBar extends Bar {
    Building entity;
    Prov<String> labelProv;

    public TemperatureBar(String label, Building entity) {
        super(() -> label, () -> Color.white, () -> 0f);
        this.entity = entity;
        this.labelProv = () -> label;
    }

    @Override
    public void draw() {
        // 1. Data Setup
        float maxTemp = 100f;
        try {
            var maxTempField = entity.block.getClass().getField("maxTempStorage");
            maxTemp = Math.max(maxTempField.getFloat(entity.block), 1f);
        } catch(Exception e) { /* Default 100 */ }

        float hV = TemperatureLogic.getHeatBuilding(entity);
        float cV = TemperatureLogic.getColdBuilding(entity);

        // --- SHAPE & COLOR CONSTANTS ---
        float slant = height * 0.5f; // Matches official Power Bar lean
        float pad = 2f;
        float iW = width - slant - (pad * 2);
        float iH = height - (pad * 2);
        float cX = x + pad + (iW / 2f);

        // 2. The Glow Effect (Restored)
        // Calculated before the bar so it renders "behind"
        if (hV > 0 || cV > 0) {
            Color gCol = Color.cyan.cpy();
            // Void Purple logic
            if (!AxthrixLoader.followRealCaps && cV > 273.15f) {
                gCol.lerp(Color.valueOf("bf92f9"), Math.min((cV - 273.15f) / 100f, 1f));
            }
            if (hV > 0) gCol.lerp(Color.red, hV / (hV + cV + 0.01f));

            float intensity = Math.min((hV + cV) / maxTemp, 1f);
            float pulse = Mathf.absin(Time.time, 4f, 0.4f);

            Draw.color(gCol);
            Draw.alpha(0.3f * intensity * (0.6f + pulse) * parentAlpha);
            // Draw a slightly larger slanted quad for the glow "aura"
            drawSlantedRect(x - 3, x + width - slant + 3, y - 3, height + 6, slant);
        }
        Draw.color(Color.white);
        Draw.alpha(parentAlpha);
        Tex.bar.draw(x, y, width, height);

        // 4. Gradient Background
        float c1 = Color.cyan.toFloatBits();
        float c2 = Color.red.toFloatBits();
        Fill.quad(
                x + pad, y + pad, c1,
                x + pad + iW, y + pad, c2,
                x + pad + iW + slant, y + pad + iH, c2,
                x + pad + slant, y + pad + iH, c1
        );

        // 5. Indicators & Neutral Split
        float heat = Math.min(hV / maxTemp, 1f);
        float cold = Math.min(cV / maxTemp, 1f);
        float lw = 4f;

        if (heat > 0 && cold > 0) {
            float clX = cX - (cold * (iW / 2f));
            float hlX = cX + (heat * (iW / 2f));
            Draw.color(Color.darkGray);
            drawSlantedRect(clX, hlX, y + pad, iH, slant);
            Draw.color(Color.white);
            drawSlantedLine(clX, y + pad, lw, iH, slant);
            drawSlantedLine(hlX, y + pad, lw, iH, slant);
        } else {
            float off = (heat > 0) ? (heat * (iW / 2f)) : (cold > 0) ? (-cold * (iW / 2f)) : 0;
            Draw.color(Color.white);
            drawSlantedLine(cX + off, y + pad, lw, iH, slant);
        }

        // 6. Dynamic Text
        renderText(hV, cV, maxTemp);
        Draw.reset();
    }

    private void drawSlantedRect(float x1, float x2, float y, float h, float s) {
        Fill.quad(x1, y, x2, y, x2 + s, y + h, x1 + s, y + h);
    }

    private void drawSlantedLine(float bx, float y, float lw, float h, float s) {
        float hlw = lw / 2f;
        Fill.quad(bx - hlw, y, bx + hlw, y, bx + s + hlw, y + h, bx + s - hlw, y + h);
    }

    private void renderText(float hV, float cV, float max) {
        String hS = TempUnit.format(hV, max);
        String cS = TempUnit.format(-cV, max);
        String display = (hV > 0 && cV > 0) ? "[gray]Neutral [red]" + hS + " [cyan]" + cS :
                (hV > 0) ? "[red]Heat " + hS :
                        (cV > 0) ? "[cyan]Cold " + cS : labelProv.get();

        Font font = Fonts.outline;
        GlyphLayout lay = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        lay.setText(font, display);
        font.draw(display, x + width / 2f - lay.width / 2f, y + height / 2f + lay.height / 2f + 1f);
        Pools.free(lay);
    }
}
