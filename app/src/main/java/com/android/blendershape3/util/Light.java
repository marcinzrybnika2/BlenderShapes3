package com.android.blendershape3.util;

public class Light {

    public float[] positionWorld;
    public float[] positionEye;

    public float[] ambient;
    public float[] diffuse;
    public float[] specular;

    public Light(float[] positionWorld,float[] positionEye, float[] ambient, float[] diffuse, float[] specular){
        this.positionWorld = positionWorld;
        this.positionEye = positionEye;
        this.ambient=ambient;
        this.diffuse=diffuse;
        this.specular=specular;
    }


}
