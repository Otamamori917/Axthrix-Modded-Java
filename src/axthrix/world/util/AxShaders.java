package axthrix.world.util;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class AxShaders {
    public static MaterializeShader materialize;
    public static BlockBuildCenterShader blockBuildCenter;
    public static AlphaShader alphaShader;

    public static void init(){
        materialize = new MaterializeShader();
        blockBuildCenter = new BlockBuildCenterShader();
        alphaShader = new AlphaShader();
    }

    public static class MaterializeShader extends AxLoadShader{
        public float progress, offset, time;
        public int shadow;
        public Color color = new Color();
        public TextureRegion region;

        MaterializeShader(){
            super("materializei");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_offset", offset);
            setUniformf("u_time", time);
            setUniformf("u_width", region.width);
            setUniformf("u_shadow", shadow);
            setUniformf("u_color", color);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class BlockBuildCenterShader extends AxLoadShader{
        public float progress;
        public TextureRegion region;
        public float time;

        BlockBuildCenterShader(){
            super("blockbuildcenteri");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_time", time);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class AlphaShader extends AxLoadShader{
        public float alpha = 1f;

        AlphaShader(){
            super("screenspace", "postalpha");
        }

        @Override
        public void apply(){
            setUniformf("u_alpha", alpha);
        }
    }

    public static class AxLoadShader extends Shader{
        public AxLoadShader(String vert, String frag){
            super(
                files.internal("shaders/" + vert + ".vert"),
                tree.get("shaders/" + frag + ".frag")
            );
        }

        public AxLoadShader(String frag){
            this("default", frag);
        }
    }
}
