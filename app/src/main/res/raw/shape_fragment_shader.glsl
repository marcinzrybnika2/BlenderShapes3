precision mediump float;

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};
uniform Material material;

struct Light {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform Light light;

uniform sampler2D u_TextureUnit;// The texture unit.

varying vec3 v_Position;// Interpolated position for this fragment.
varying vec3 v_Normal;// Interpolated normal for this fragment.
varying vec2 v_TexCoord;// Interpolated texture coordinate per fragment.

void main() {

//    float ambientStrength = 0.1;
    //ambient
    vec3 ambient = material.ambient * light.ambient;

    //diffuse
    vec3 normalizedNormal = normalize(v_Normal);
    vec3 lightVector = normalize(light.position - v_Position);
    float diff = max(dot(normalizedNormal, lightVector), 0.0);
    vec3 diffuse=light.diffuse * (diff * material.diffuse);

    //specular
//    float specularStrength = 0.5;
    vec3 viewPosition=vec3(0.0, 0.0, 0.0);
    vec3 viewDir = normalize(viewPosition - v_Position);
    vec3 reflectDir = reflect(-lightVector, normalizedNormal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = light.specular * (spec * material.specular);

    vec3 result=(ambient+diffuse+specular);

    // Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = (vec4(result, 0.0) * texture2D(u_TextureUnit, v_TexCoord));

}
