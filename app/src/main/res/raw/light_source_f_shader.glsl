precision mediump float;

uniform vec3 u_LightColor;


void main() {

    gl_FragColor = vec4(u_LightColor,0.0);
}
