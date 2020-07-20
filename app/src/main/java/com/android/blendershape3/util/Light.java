package com.android.blendershape3.util;

public class Light {

    public float[] position;
    public float[] ambient;
    public float[] diffuse;
    public float[] specular;

    public Light(float[] position, float[] ambient, float[] diffuse, float[] specular){
        this.position=position;
        this.ambient=ambient;
        this.diffuse=diffuse;
        this.specular=specular;
    }


}
