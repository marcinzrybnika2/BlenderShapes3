precision mediump float;
uniform vec4 u_Color;
uniform vec3 u_LightPosition;

varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.

void main() {


    float distance = length(u_LightPosition - v_Position);

    vec3 lightVector = normalize(u_LightPosition - v_Position);

    float diffuse = max(dot(v_Normal, lightVector), 0.0);

    diffuse = diffuse * (1.0 / distance);

    //ambient
    diffuse = diffuse + 0.2;

    gl_FragColor=(diffuse * u_Color);
//    gl_FragColor = vec4(1, 0.5, 0, 1.0);
}
