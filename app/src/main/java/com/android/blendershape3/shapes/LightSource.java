package com.android.blendershape3.shapes;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.res.ResourcesCompat;

import com.android.blendershape3.shaders.LightShaderProgram;
import com.android.blendershape3.util.Light;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;
import static com.android.blendershape3.util.Constants.BYTES_PER_INT;

public class LightSource {
    private final int vertexVBOIndex;
    private final int facesVBOIndex;

    private final int numberOfVertices;
    private final int numberOfFaces;

    private final LightShaderProgram lightShaderProgram;
    private final Light light;

    //light parameters
    private final float ambientPar;
    private final float diffusePar;
    private final float specularPar;


    private InputStream objInputStream;
    private Context context;
    private String shape;
    private int colorId;

    private float[] lightColor;
    private float[] position;

    private Obj obj;


    public LightSource(Context context, String shape, int colorId, float[] position){
        this.context=context;
        this.shape=shape;
        this.colorId=colorId;
        this.position=position;


        //get desired color
        int intColor = ResourcesCompat.getColor(context.getResources(), this.colorId, null);
        lightColor = new float[]{
                Color.red(intColor) / (float) 255,
                Color.green(intColor) / (float) 255,
                Color.blue(intColor) / (float) 255
        };

        //parse Mesh obj
        try {
            objInputStream = context.getAssets().open(this.shape);
            obj = ObjReader.read(objInputStream);
            objInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        obj = ObjUtils.convertToRenderable(obj);

        IntBuffer indices = ObjData.getFaceVertexIndices(obj, 3);
        FloatBuffer vertices = ObjData.getVertices(obj);

        int numberOfBuffers = 2;

        int[] bufferPtrs = new int[numberOfBuffers];
        glGenBuffers(numberOfBuffers, bufferPtrs, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferPtrs[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * BYTES_PER_FLOAT, vertices, GL_STATIC_DRAW);
        vertexVBOIndex = bufferPtrs[0];
        numberOfVertices = vertices.capacity();
        glBindBuffer(GL_ARRAY_BUFFER, 0);


        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferPtrs[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * BYTES_PER_INT, indices, GL_STATIC_DRAW);
        facesVBOIndex = bufferPtrs[1];
        numberOfFaces = indices.capacity();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        //removing buffers from native memory
        vertices.limit(0);
        vertices = null;
        indices.limit(0);
        indices = null;


        //light parameters
        ambientPar=0.2f;
        diffusePar=0.5f;
        specularPar=1.0f;

        float[] ambient=new float[]{lightColor[0]*ambientPar,lightColor[1]*ambientPar,lightColor[2]*ambientPar};
        float[] diffuse=new float[]{lightColor[0]*diffusePar,lightColor[1]*diffusePar,lightColor[2]*diffusePar};
        float[] specular=new float[]{lightColor[0]*specularPar,lightColor[1]*specularPar,lightColor[2]*specularPar};

        light=new Light(position,ambient,diffuse,specular);


        //create shader program
        lightShaderProgram = new LightShaderProgram(context);


    }

    public Light getLight(){
        return this.light;
    }

//    public float[] getColor(){
//        return this.lightColor;
//    }


    public void draw(float[] projectionMatrix){
        lightShaderProgram.useProgram();

        lightShaderProgram.setUniforms(projectionMatrix, this.lightColor);

        glBindBuffer(GL_ARRAY_BUFFER, vertexVBOIndex);
        glEnableVertexAttribArray(lightShaderProgram.getaPositionLocation());
        glVertexAttribPointer(lightShaderProgram.getaPositionLocation(), 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

//      faces buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facesVBOIndex);
        glDrawElements(GL_TRIANGLES, numberOfFaces, GL_UNSIGNED_INT, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glDisableVertexAttribArray(lightShaderProgram.getaPositionLocation());

    }



}
