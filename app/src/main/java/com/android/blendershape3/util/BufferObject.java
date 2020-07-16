package com.android.blendershape3.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BufferObject {

    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;
    public ShortBuffer facesBuffer;

    public BufferObject(FloatBuffer vB, FloatBuffer nB, ShortBuffer fB){
        this.vertexBuffer =vB;
        this.normalBuffer =nB;
        this.facesBuffer =fB;
    }


}
