package com.android.blendershape3.util;

public class MaterialTex {


    public int diffuse;
    public float[] specular;
    public float shininess;

    public MaterialTex(int diffuse, float[] specular, float shininess){

        this.diffuse=diffuse;
        this.specular=specular;
        this.shininess=shininess;
    }


}
