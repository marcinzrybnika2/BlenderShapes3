package com.android.blendershape3.util;

public class Material {
    public float[] ambient;
    public float[] diffuse;
    public float[] specular;
    public float shininess;

    public Material(float[] ambient, float[] diffuse, float[] specular, float shininess){
        this.ambient=ambient;
        this.diffuse=diffuse;
        this.specular=specular;
        this.shininess=shininess;
    }
}
