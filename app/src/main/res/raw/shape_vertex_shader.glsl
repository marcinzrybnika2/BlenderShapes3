
uniform mat4 u_MVMatrix;
uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TexCoord;
//layout (location=0) in vec4 a_Position;
//layout (location=1) in vec3 a_Normal;



varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TexCoord;



void main() {


    v_Position = vec3(u_MVMatrix * a_Position);
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

    v_TexCoord = a_TexCoord;

    gl_Position = u_MVPMatrix * a_Position;

}
