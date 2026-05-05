package axthrix.world.types.sea.block;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import axthrix.world.types.block.effect.AxCore;
import axthrix.world.types.sea.managers.LayerManager;
import mindustry.gen.Unit;
import mindustry.graphics.*;

public class SeaCore extends AxCore {

    public SeaCore(String name) {
        super(name);
        solid = false;
        update = true;
    }

    public class SeaCoreBuild extends CoreBuild {

        @Override
        public void draw() {
            float intensity = LayerManager.shaderIntensity;
            boolean playerSub = LayerManager.isPlayerSubmerged();

            // 1. SHADOW PASS (Surface looking down)
            // As a waterBlock, it is a shadow when NOT submerged (intensity < 1)
            if (intensity < 1f) {
                float shadowWeight = 1f - intensity;
                float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
                float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;

                Draw.z(Layer.block - 1f);
                // Shadow fades out as player dives (intensity goes to 1)
                Draw.color(Color.black);
                Draw.alpha(0.4f * shadowWeight);

                // Draw only the base region for the shadow (no team regions)
                Draw.rect(region, x + ox, y + oy);
                Draw.reset();
            }

            // 2. PHYSICAL CORE (Deep View)
            // Draw the real block tinted by the floor color as player dives
            if (intensity > 0.01f) {
                Draw.z(Layer.block);
                Color floorCol = tile.floor().mapColor;

                // Match the Drill hue-shift logic
                Tmp.c1.set(Color.white).lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
                Draw.color(Tmp.c1);
                Draw.alpha(intensity);

                // Draw the full block (Base + Team regions)
                // We don't call super.draw() directly if we want to ensure custom tinting
                // applies to team regions too, but super.draw() is standard.
                super.draw();
                Draw.reset();
            }

            // 3. HOLOGRAM PASS (The "Ghost" overlay)
            // It should be 100% visible on surface, and fade as you submerge
            float holoAlpha = 1f - intensity;
            if (holoAlpha > 0.01f) {
                drawHologram(holoAlpha);
            }
        }

        private void drawHologram(float alpha) {
            Draw.z(Layer.flyingUnitLow);
            // Orange-tinted pulse logic
            float pulse = 0.3f + Mathf.absin(Time.time, 10f, 0.2f);

            // Draw the "Ghost" version using team color
            Draw.color(team.color);
            Draw.alpha(alpha * pulse);
            Draw.rect(region, x, y);

            // Add an orange additive glow
            Draw.blend(Blending.additive);
            Draw.color(Color.orange);
            Draw.alpha(alpha * pulse * 0.5f);
            Draw.rect(region, x, y);

            Draw.blend();
            Draw.reset();
        }



        @Override
        public void updateTile() {
            super.updateTile();


            float range = (block.size * 4f) + 8f;
            mindustry.gen.Groups.unit.intersect(x - range, y - range, range * 2, range * 2, unit -> {
                if (LayerManager.submergedUnits.contains(unit) && !unit.isFlying()) {
                    applyBlockBounce(unit);
                }
            });
        }

        private void applyBlockBounce(Unit unit) {
            float dx = unit.x - x;
            float dy = unit.y - y;
            float len = Mathf.len(dx, dy);
            float minLen = (block.size * 4f) + (unit.hitSize / 2f);

            if (len < minLen) {
                if (len < 0.1f) { dx = 0.1f; len = 0.1f; }
                dx /= len; dy /= len;

                float overlap = minLen - len;
                unit.x += dx * overlap;
                unit.y += dy * overlap;

                float dot = unit.vel.x * dx + unit.vel.y * dy;
                if (dot < 0) unit.vel.sub(dx * dot, dy * dot);
            }
        }
    }
}
