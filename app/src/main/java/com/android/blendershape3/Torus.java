package com.android.blendershape3;



import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;


import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Torus {

    private static String TAG ="Torus: ";
    private Context context;

    private List<String> verticesList;
    private List<String> facesList;

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;

    private int program;

    /**
     * @param context
     */
    public Torus(Context context) {
        this.context=context;

        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(context.getAssets().open("TestTorus.obj"));
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
            /*Log.d(LOG_TAG,"face x= "+v1);
            Log.d(LOG_TAG,"face y= "+v2);
            Log.d(LOG_TAG,"face z= "+v3);*/
            facesBuffer.put((short) (v1 - 1));
            facesBuffer.put((short) (v2 - 1));
            facesBuffer.put((short) (v3 - 1));
        }
        facesBuffer.position(0);

        String vertexShaderCode = null;
        InputStream vertexShaderStream = context.getResources().openRawResource(R.raw.bs_vertex_shader);
        try {
            vertexShaderCode = IOUtils.toString(vertexShaderStream, Charset.defaultCharset());
            vertexShaderStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.d(LOG_TAG,"vertexShaderCode= "+vertexShaderCode);
        String fragmentShaderCode = null;
        InputStream fragmentShaderStream = context.getResources().openRawResource(R.raw.bs_fragment_shader);
        try {
            fragmentShaderCode = IOUtils.toString(fragmentShaderStream, Charset.defaultCharset());
            fragmentShaderStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        Log.d(LOG_TAG,"fragmentShaderCode= "+fragmentShaderCode);

        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        GLES20.glLinkProgram(program);
        GLES20.glUseProgram(program);


    }

    public void draw() {
        int position = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(position);

        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 0, verticesBuffer);

        float[] projectionMatrix = new float[16];
        float[] viewMatrix = new float[16];
        float[] modelMatrix=new float[16];

        Matrix.setIdentityM(modelMatrix,0);

        Matrix.frustumM(projectionMatrix, 0,
                -1, 1,
                -1, 1,
                1, 9);

        Matrix.setLookAtM(viewMatrix,0,
                0,0,-4,
                0,0,0,
                0,1,0);

        float[] temp=new float[16];
        Matrix.multiplyMM(temp,0,viewMatrix,0,modelMatrix,0);


        Matrix.multiplyMM(projectionMatrix,0,projectionMatrix,0,
                temp,0);

        int matrix=GLES20.glGetUniformLocation(program,"matrix");
        GLES20.glUniformMatrix4fv(matrix,1,false,projectionMatrix,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,facesList.size()*3,GLES20.GL_UNSIGNED_SHORT,facesBuffer);

        GLES20.glDisableVertexAttribArray(position);

    }
}
