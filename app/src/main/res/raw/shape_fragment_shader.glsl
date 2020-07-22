precision mediump float;

struct Material {
    sampler2D diffuse;
    vec3 specular;
    float shininess;
};
uniform Material material;

struct Light {
    vec3 positionEye;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};
uniform Light light;


varying vec3 v_Position;// Interpolated position for this fragment.
varying vec3 v_Normal;// Interpolated normal for this fragment.
varying vec2 v_TexCoord;// Interpolated texture coordinate per fragment.


void main() {


//    float ambientStrength = 0.1;
    //ambient
    vec3 ambient = light.ambient * texture2D(material.diffuse, v_TexCoord).rgb;

    //diffuse
    vec3 normalizedNormal = normalize(v_Normal);
    vec3 lightVector = normalize(light.positionEye - v_Position);
    float diff = max(dot(normalizedNormal, lightVector), 0.0);
    vec3 diffuse=light.diffuse * diff * texture2D(material.diffuse, v_TexCoord).rgb;

    //specular
    vec3 viewPosition=vec3(0.0, 0.0, 0.0);
    vec3 viewDir = normalize(viewPosition - v_Position);
    vec3 reflectDir = reflect(-lightVector, normalizedNormal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = light.specular * (spec * material.specular);

    vec3 result= ambient+diffuse+specular;

    // Multiply the color by the diffuse illumination level and texture value to get final output color.
    gl_FragColor = vec4(result, 1.0);

}
