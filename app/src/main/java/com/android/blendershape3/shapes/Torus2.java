package com.android.blendershape3.shapes;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.android.blendershape3.shaders.TorusShaderProgram;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glUseProgram;

public class Torus2 {
    Context context;

    private List<String> verticesList;
    private List<String> facesList;

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;

    private TorusShaderProgram torusShaderProgram;
    private int program;
    private int aPositionLocation;
    private int uMatrixLocation;

    private float[] modelMatrix = new float[16];
    private float[] mvMatrix = new float[16];
    private float[] mvpMatrix = new float[16];
    private float[] temporaryMatrix=new float[16];

    public Torus2(Context context) {
        this.context = context;

        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(context.getAssets().open("Kostka.obj"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("v ")) {
                verticesList.add(line);
            } else if (line.startsWith("f ")) {
                facesList.add(line);
            }
        }
        scanner.close();

        ByteBuffer b1 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
        b1.order(ByteOrder.nativeOrder());
        verticesBuffer = b1.asFloatBuffer();

        ByteBuffer b2 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2);
        b2.order(ByteOrder.nativeOrder());
        facesBuffer = b2.asShortBuffer();

        for (String vertex : verticesList) {
            String coords[] = vertex.split(" ");
            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
          /*  Log.d(LOG_TAG,"vertex x= "+x);
            Log.d(LOG_TAG,"vertex y= "+y);
            Log.d(LOG_TAG,"vertex z= "+z);*/
            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);
        }
        verticesBuffer.position(0);

        for (String face : facesList) {
            String vertexIndices[] = face.split(" ");
            short v1 = Short.parseShort(vertexIndices[1]);
            short v2 = Short.parseShort(vertexIndices[2]);
            short v3 = Short.parseShort(vertexIndices[3]);

            facesBuffer.put((short) (v1 - 1));
            facesBuffer.put((short) (v2 - 1));
            facesBuffer.put((short) (v3 - 1));
        }
        facesBuffer.position(0);

        torusShaderProgram = new TorusShaderProgram(context);
        program = torusShaderProgram.getProgram();


    }

    public TorusShaderProgram getTorusShaderProgram() {
        return torusShaderProgram;
    }


    public void draw(float[] viewMatrix, float[] projectionMatrix, float[] rotation) {

        glUseProgram(program);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, -2.5f);
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

        torusShaderProgram.setUniforms(mvpMatrix);

        aPositionLocation = torusShaderProgram.getaPositionLocation();

        GLES20.glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, verticesBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);

        GLES20.glDisableVertexAttribArray(aPositionLocation);

    }


}
