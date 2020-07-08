attribute vec4 a_Position;
attribute vec3 a_Normal;

uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;


void main() {

    gl_Position = u_MVPMatrix * a_Position;


}
