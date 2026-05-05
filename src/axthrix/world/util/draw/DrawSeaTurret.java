package axthrix.world.util.draw;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.FrameBuffer;
import arc.math.*;
import arc.util.*;
import axthrix.AxthrixLoader;
import axthrix.world.types.sea.managers.LayerManager;
import axthrix.world.types.sea.block.SeaTurret;
import mindustry.entities.part.*;
import mindustry.game.Gamemode;
import mindustry.gen.Building;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.defense.turrets.Turret.*;
import mindustry.world.draw.*;
import mindustry.Vars;

import static mindustry.Vars.tilesize;

public class DrawSeaTurret extends DrawTurret {
    public static FrameBuffer buffer;

    /// Renderer Resolution
    private static final int bufferSize = 4096;

    public DrawSeaTurret(String basePrefix){
        super(basePrefix);
    }

    @Override
    public void draw(Building build) {
        if(!AxthrixLoader.drawEnchancedShadows){
            super.draw(build);
        }
//        Turret turret = (Turret)build.block;
//        TurretBuild tb = (TurretBuild)build;
//
//        float intensity = LayerManager.shaderIntensity;
//        boolean playerSub = LayerManager.isPlayerSubmerged();
//        boolean onLiquid = build.tile.floor().isLiquid;
//        boolean isWaterBlock = build.block instanceof SeaTurret st && st.waterBlock;
//        boolean isMini = AxthrixLoader.MINI && Vars.state.rules.mode() == Gamemode.sandbox;
//
//        float shadowWeight = isWaterBlock ? (1f - intensity) : (onLiquid ? intensity : 0f);
//        boolean isShadowing = shadowWeight > 0.1f;
//
//        float ox = Mathf.sin(Time.time, 15f, 1.2f) * shadowWeight;
//        float oy = Mathf.cos(Time.time, 18f, 0.8f) * shadowWeight;
//
//        Color stateColor = Tmp.c3.set(Color.white).lerp(Color.black, shadowWeight);
//        float stateAlpha = Mathf.lerp(1f, 0.25f, shadowWeight);
//
//        if (isWaterBlock && playerSub) {
//            Color floorCol = build.tile.floor().mapColor;
//            stateColor.lerp(Tmp.c2.set(floorCol).add(0.2f, 0.2f, 0.2f, 0f), intensity);
//        }
//
//        int baseSize = isMini ? 8 : turret.size*tilesize;
//        int scaleSize = isMini ? 16 : 4;
//
//        Draw.color(stateColor);
//        Draw.alpha(stateAlpha);
//        Draw.rect(base, build.x + ox, build.y + oy,baseSize,baseSize);
//        /// buffer starts
//        if(buffer == null || buffer.getWidth() != bufferSize){
//            if(buffer != null) buffer.dispose();
//            buffer = new FrameBuffer(bufferSize, bufferSize);
//        }
//
//
//        Mat oldProj = new Mat(Draw.proj());
//        Draw.flush();
//        buffer.begin();
//        Core.graphics.clear(Color.clear);
//
//        /// scaling
//        Draw.proj().setOrtho(0, 0, bufferSize/4, bufferSize/4);
//        float bx = bufferSize / 8f;
//        float by = bufferSize / 8f;
//
//
//        Draw.color(Color.white);
//        Draw.alpha(1f);
//        /// i draw the parts
//        if(parts.size > 0){
//            var params = DrawPart.params.set(build.warmup(), 1f - tb.progress(), 1f - tb.progress(), tb.heat, tb.curRecoil, tb.charge, bx, by, tb.rotation);
//            for(var part : parts){
//                params.setRecoil(part.recoilIndex >= 0 && tb.curRecoils != null ? tb.curRecoils[part.recoilIndex] : tb.curRecoil);
//                part.draw(params);
//            }
//        }
//        /// buffer ends
//        Draw.flush();
//        buffer.end();
//        Draw.proj(oldProj);
//
//
//        Draw.z(turretLayer);
//        Draw.color(stateColor);
//        Draw.alpha(stateAlpha);
//        /// i get the texture
//        TextureRegion region = new TextureRegion(buffer.getTexture());
//        region.flip(false, true);
//
//
//
//        /// draw the combined texture use same scaling from ortho
//        Draw.rect(region, build.x + tb.recoilOffset.x + ox, build.y + tb.recoilOffset.y + oy, bufferSize/scaleSize, bufferSize/scaleSize);
//
//        /// ingore heat :p
//        if(!isShadowing && tb.heat > 0.0001f && heat.found()){
//            Drawf.additive(heat, turret.heatColor.write(Tmp.c1).a(tb.heat), build.x + tb.recoilOffset.x + ox, build.y + tb.recoilOffset.y + oy, tb.drawrot(), heatLayer);
//        }
//
//        Draw.reset();
    }
}
