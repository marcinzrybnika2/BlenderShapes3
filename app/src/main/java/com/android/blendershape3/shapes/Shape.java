package com.android.blendershape3.shapes;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.android.blendershape3.R;
import com.android.blendershape3.shaders.ShapeShaderProgram;
import com.android.blendershape3.util.BlenderShapeFileReader;
import com.android.blendershape3.util.BufferObject;
import com.android.blendershape3.util.Light;
import com.android.blendershape3.util.Material;
import com.android.blendershape3.util.MaterialTex;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Scanner;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glUseProgram;
import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;
import static com.android.blendershape3.util.Constants.BYTES_PER_SHORT;

public class Shape {
    private static final String TAG = "Shape";

    public static final int VERTEX_NORMALS = 1;
    public static final int FACES_NORMALS = 2;
    private final int numberOfVertices;
//    private final Material material;
    private final MaterialTex materialTex;

    private int numberOfFaces=0;
    private final int numberOfBuffers;

    private float[] modelMatrix = new float[16];
    private float[] mvMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] temporaryMatrix = new float[16];

    private float[] shapeColor;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalsBuffer;
    private ShortBuffer facesBuffer;

    private ShapeShaderProgram shapeShaderProgram;


    private Context context;

    private int aPositionLocation;
    private int aNormalLocation;

    private int vertexVBOIndex;
    private int normalsVBOIndex;
    private int facesVBOIndex;

    private int mode;

    /**
     * @param context
     * @param fileName original Blender *.obj file or recalculated *VN.obj file
     */
    public Shape(Context context, String fileName) {
        this.context = context;

        //check file type
        mode = getMode(fileName);
        if (mode == 0) {
            Log.e(TAG, "File: " + fileName + " is not valid Shape file");
            try {
                throw new Exception("File: " + fileName + " is not valid Shape file");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //get desired color
        int intColor = ResourcesCompat.getColor(context.getResources(), R.color.colorShape, null);
        shapeColor = new float[]{Color.red(intColor) / (float) 255,
                Color.green(intColor) / (float) 255,
                Color.blue(intColor) / (float) 255,
                Color.alpha(intColor) / (float) 255};

        BlenderShapeFileReader shapeReader = new BlenderShapeFileReader(context);
//        shapeReader.writeShapeVNToFile("WolfNoCircle.obj");

        BufferObject bufferObject = shapeReader.getBuffers(fileName, mode);

        vertexBuffer = bufferObject.vertexBuffer;
        normalsBuffer = bufferObject.normalBuffer;
        facesBuffer = bufferObject.facesBuffer; //in FACES_NORMALS mode this is null


        //create and bind VBOs
        if (mode == VERTEX_NORMALS) {
            numberOfBuffers = 3;
        } else {
            numberOfBuffers = 2;
        }
        int[] bufferIndxs = new int[numberOfBuffers];
        glGenBuffers(numberOfBuffers, bufferIndxs, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndxs[0]);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GL_STATIC_DRAW);
        vertexVBOIndex = bufferIndxs[0];
        numberOfVertices=vertexBuffer.capacity();

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndxs[1]);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);
        normalsVBOIndex = bufferIndxs[1];

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        if (mode == VERTEX_NORMALS) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferIndxs[2]);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, facesBuffer.capacity() * BYTES_PER_SHORT, facesBuffer, GL_STATIC_DRAW);
            facesVBOIndex = bufferIndxs[2];

            numberOfFaces = facesBuffer.capacity();

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        //removing buffers from native memory
        vertexBuffer.limit(0);
        vertexBuffer = null;
        normalsBuffer.limit(0);
        normalsBuffer = null;
        if(mode==VERTEX_NORMALS) {
            facesBuffer.limit(0);
            facesBuffer = null;
        }
        //create shader program
        shapeShaderProgram = new ShapeShaderProgram(context);

/*
        //create Material
        float[] ambient=new float[]{1.0f,0.5f,0.31f};
        float[] diffuse=new float[]{1.0f,0.5f,0.31f};
        float[] specular=new float[]{0.5f,0.5f,0.5f};
        float shininess=32.0f;
        material=new Material(ambient,diffuse,specular,shininess);
*/

        float[] specular=new float[]{0.5f,0.5f,0.5f};
        float shininess=32.0f;
        materialTex=new MaterialTex(0,specular,shininess);

    }

    /**
     * Checks this file type.
     *
     * @param fileName
     * @return FACES_NORMALS if this is *.obj file with faces normals.
     * VERTEX_NORMALS if this is *VN.obj file with vertex normals.
     */
    private int getMode(String fileName) {
        int result = 0;
        try {
            Scanner scanner = new Scanner(context.getAssets().open(fileName));
            scanner.nextLine();
            String line2 = scanner.nextLine();
            if (line2.contains("# VN")) {
                result = VERTEX_NORMALS;
            } else if (line2.contains("# www.blender.org")) {
                result = FACES_NORMALS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * @param viewMatrix
     * @param projectionMatrix
     * @param lightPositionInEyeSpace
     */
    public void draw(float[] viewMatrix, float[] projectionMatrix, float[] lightPositionInEyeSpace, Light light) {
       shapeShaderProgram.useProgram();


        shapeShaderProgram.setUniforms(viewMatrix, projectionMatrix, light,0,materialTex);

        aPositionLocation = shapeShaderProgram.getaPositionLocation();
        aNormalLocation = shapeShaderProgram.getaNormalLocation();

//        vertexBuffer.position(0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBOIndex);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, 0);



//        normalsBuffer.position(0);
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBOIndex);
        glEnableVertexAttribArray(aNormalLocation);
        GLES20.glVertexAttribPointer(aNormalLocation, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        if(mode==VERTEX_NORMALS) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facesVBOIndex);

            glDrawElements(GL_TRIANGLES, numberOfFaces, GL_UNSIGNED_SHORT, 0);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }else{ //FACES_NORMAL mode
            glDrawArrays(GL_TRIANGLES,0,numberOfVertices);
        }
        glDisableVertexAttribArray(aPositionLocation);
        glDisableVertexAttribArray(aNormalLocation);

    }

}
