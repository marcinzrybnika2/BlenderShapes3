package com.android.blendershape3.shapes;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import androidx.core.content.res.ResourcesCompat;

import com.android.blendershape3.R;
import com.android.blendershape3.shaders.ShapeShaderProgram;
import com.android.blendershape3.util.ShapeHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glUseProgram;
import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;

public class Shape {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMALS_COMPONENT_COUNT = 3;

    private float[] modelMatrix = new float[16];
    private float[] mvMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] temporaryMatrix = new float[16];

    private float[] shapeColor;

    private FloatBuffer vertexBuffer;
    private FloatBuffer normalsBuffer;

    private List<String> sourceVertexList;
    private List<String> sourceNormalList;
    private List<String> sourceFacesAndNormalsList;

    private List<String> outputVertexList;
    private List<String> outputNormalList;
    private List<String> outputFacesList;

    private ShapeShaderProgram shapeShaderProgram;
    private int program;

    private Context context;
    private int aPositionLocation;
    private int aNormalLocation;

    private int vertexVBOIndex;
    private int normalsVBOIndex;

    /**
     * @param context
     */
    public Shape(Context context) {
        this.context = context;

        //get desired color
        int intColor = ResourcesCompat.getColor(context.getResources(), R.color.colorShape, null);
        shapeColor = new float[]{Color.red(intColor) / (float) 255,
                Color.green(intColor) / (float) 255,
                Color.blue(intColor) / (float) 255,
                Color.alpha(intColor) / (float) 255};

        //build Data buffers
        sourceVertexList = new ArrayList<>();
        sourceNormalList = new ArrayList<>();
        sourceFacesAndNormalsList = new ArrayList<>();

        outputVertexList = new ArrayList<>();
        outputNormalList = new ArrayList<>();
//        outputFacesList = new ArrayList<>();

        //Scan the .obj file
        try {
            Scanner scanner = new Scanner(context.getAssets().open("Ball.obj"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("v ")) {
                    sourceVertexList.add(line);
                } else if (line.startsWith("vn")) {
                    sourceNormalList.add(line);
                } else if (line.startsWith("f ")) {
                    sourceFacesAndNormalsList.add(line);
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ShapeHelper sh = new ShapeHelper();
        FloatBuffer[] buffers = sh.getFacesNormalsBuffers(sourceVertexList, sourceNormalList, sourceFacesAndNormalsList, POSITION_COMPONENT_COUNT, NORMALS_COMPONENT_COUNT);
        vertexBuffer = buffers[0];
        normalsBuffer = buffers[1];
        System.gc();
/*
        //układanie buforów

        for (String faceAndNormal : sourceFacesAndNormalsList) {
            String combinedVI[] = faceAndNormal.split(" "); //combinedVI[0]="f"

            //są 4 elementy: f 5//1 3//1 1//1
            String complet1[] = combinedVI[1].split("//"); //returns 2 elements: complet1[0] is face vertex index
            String complet2[] = combinedVI[2].split("//");    //and complet1[1] is normal index
            String complet3[] = combinedVI[3].split("//");

            int vertex1 = Integer.parseInt(complet1[0]);
            int normal1 = Integer.parseInt(complet1[1]);
            int vertex2 = Integer.parseInt(complet2[0]);
            int normal2 = Integer.parseInt(complet2[1]);
            int vertex3 = Integer.parseInt(complet3[0]);
            int normal3 = Integer.parseInt(complet3[1]);

            // each index is from 1, so decrease them by 1
            outputVertexList.add(sourceVertexList.get(vertex1 - 1));
            outputVertexList.add(sourceVertexList.get(vertex2 - 1));
            outputVertexList.add(sourceVertexList.get(vertex3 - 1));

            outputNormalList.add(sourceNormalList.get(normal1 - 1));
            outputNormalList.add(sourceNormalList.get(normal2 - 1));
            outputNormalList.add(sourceNormalList.get(normal3 - 1));
        }

        //parsing vertices lines into floats
        //Buffer containing all vertices in float coordinates
        vertexBuffer = ByteBuffer.allocateDirect(outputVertexList.size() *
                POSITION_COMPONENT_COUNT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.position(0);

        for (String line : outputVertexList) {
            String coords[] = line.split(" "); // Split by space. coords[0]="v"
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);

            vertexBuffer.put(x);
            vertexBuffer.put(y);
            vertexBuffer.put(z);
        }
        vertexBuffer.position(0);

        //parsing normals into floats
        //This buffer will contain all normal vectors in the same order as vertices (above)
        normalsBuffer = ByteBuffer.allocateDirect(outputNormalList.size() *
                NORMALS_COMPONENT_COUNT * BYTES_PER_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normalsBuffer.position(0);

        for (String line : outputNormalList) {
            String normals[] = line.split(" ");  //normals[0]="vn"
            float x = Float.parseFloat(normals[1]);
            float y = Float.parseFloat(normals[2]);
            float z = Float.parseFloat(normals[3]);

            normalsBuffer.put(x);
            normalsBuffer.put(y);
            normalsBuffer.put(z);
        }
        normalsBuffer.position(0);
*/

        //create and bind VBOs
        int[] bufferIndxs = new int[2];
        glGenBuffers(2, bufferIndxs, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndxs[0]);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * BYTES_PER_FLOAT, vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, bufferIndxs[1]);
        glBufferData(GL_ARRAY_BUFFER, normalsBuffer.capacity() * BYTES_PER_FLOAT, normalsBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        vertexVBOIndex = bufferIndxs[0];
        normalsVBOIndex = bufferIndxs[1];

        //removing buffers from native memory
        vertexBuffer.limit(0);
        vertexBuffer = null;
        normalsBuffer.limit(0);
        normalsBuffer = null;

        shapeShaderProgram = new ShapeShaderProgram(context);
        program = shapeShaderProgram.getProgram();

    }

    public void draw(float[] viewMatrix, float[] projectionMatrix, float[] rotation, float[] lightPosition) {
        glUseProgram(program);


        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -1.25f);
        Matrix.rotateM(modelMatrix, 0, 30, 1.0f, 0.0f, 0.0f);


        // Rotate the cube taking the overall rotation into account.
        Matrix.multiplyMM(temporaryMatrix, 0, modelMatrix, 0, rotation, 0);
        System.arraycopy(temporaryMatrix, 0, modelMatrix, 0, 16);

        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MV matrix
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);

        shapeShaderProgram.setUniforms(mvMatrix, mvpMatrix, shapeColor, lightPosition);

        aPositionLocation = shapeShaderProgram.getaPositionLocation();
        aNormalLocation = shapeShaderProgram.getaNormalPosition();

//        vertexBuffer.position(0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBOIndex);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        GLES20.glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, 0);


//        normalsBuffer.position(0);
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBOIndex);
        glEnableVertexAttribArray(aNormalLocation);
        GLES20.glVertexAttribPointer(aNormalLocation, 3, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

//        GLES20.glDrawArrays(GL_POINTS,0,sourceFacesAndNormalsList.size()*3);
        GLES20.glDrawArrays(GL_TRIANGLES, 0, sourceFacesAndNormalsList.size() * 3);

        glDisableVertexAttribArray(aPositionLocation);
        glDisableVertexAttribArray(aNormalLocation);

    }

}
