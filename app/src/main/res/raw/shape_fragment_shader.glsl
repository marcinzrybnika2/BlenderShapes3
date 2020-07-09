precision mediump float;
varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.

void main() {

    vec4 color=vec4(1, 0.5, 0, 1.0);
    vec3 u_LightPos=vec3(1.0,1.0,1.0);

    float distance = length(u_LightPos - v_Position);

    vec3 lightVector = normalize(u_LightPos - v_Position);

    float diffuse = max(dot(v_Normal, lightVector), 0.0);

    diffuse = diffuse * (1.0 / distance);
    diffuse = diffuse + 0.2;

    gl_FragColor=(diffuse * color);
//    gl_FragColor = vec4(1, 0.5, 0, 1.0);
}
