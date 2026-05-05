#define HIGHP

in vec2 v_texCoords;
out vec4 out_color;

uniform vec2 u_resolution;
uniform float u_time;
uniform vec4 u_mouse;

void main(){
    // Use v_texCoords as your 'uv'
    vec2 st = (2. * v_texCoords - 1.) * vec2(u_resolution.x / u_resolution.y, 1.);

    // Your original logic
    out_color = vec4(
        0.5 * sin(u_time) + 0.5,
        abs(st),
        1.
    );
}

