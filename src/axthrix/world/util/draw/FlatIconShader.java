package axthrix.world.util.draw;

import arc.graphics.gl.Shader;

/**
 * Recolors a sprite to a flat, solid color.
 *
 * Usage:
 *   Draw.shader(FlatIconShader.instance);
 *   Draw.color(statusColor);
 *   Draw.rect(icon, x, y, w, h, rotation);
 *   Draw.shader(); // reset back to default
 */
public class FlatIconShader extends Shader {

    public static final FlatIconShader instance = new FlatIconShader();

    private static final String VERT = """
        attribute vec4 a_position;
        attribute vec4 a_color;
        attribute vec2 a_texCoord0;
        uniform mat4 u_projTrans;
        varying vec4 v_color;
        varying vec2 v_texCoords;
        void main(){
            v_color = a_color;
            v_texCoords = a_texCoord0;
            gl_Position = u_projTrans * a_position;
        }
        """;

    private static final String FRAG = """
        varying vec4 v_color;
        varying vec2 v_texCoords;
        uniform sampler2D u_texture;
        void main(){
            float srcAlpha = texture2D(u_texture, v_texCoords).a;
            gl_FragColor = vec4(v_color.rgb, srcAlpha * v_color.a);
        }
        """;

    public FlatIconShader() {
        super(VERT, FRAG);
    }
}