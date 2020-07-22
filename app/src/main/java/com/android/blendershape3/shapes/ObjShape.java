package com.android.blendershape3.shapes;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;

import androidx.core.content.res.ResourcesCompat;

import com.android.blendershape3.R;
import com.android.blendershape3.shaders.ShapeShaderProgram;
import com.android.blendershape3.util.Light;
import com.android.blendershape3.util.Material;
import com.android.blendershape3.util.TextureHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import static android.opengl.GLES20.*;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;
import static com.android.blendershape3.util.Constants.BYTES_PER_INT;

public class ObjShape {

    private final int vertexVBOIndex;
    private final int normalsVBOIndex;
    private final int texCoordsVBOIndex;
    private final int facesVBOIndex;

    private final int numberOfFaces;
    private final int numberOfVertices;

    private final int textureDataHandle;

    private final ShapeShaderProgram shapeShaderProgram;
    private final Material material;


    private InputStream objInputStream;
    private Context context;
    private Obj obj;

    private float[] shapeColor;

    private int aPositionLocation;
    private int aNormalLocation;
    private int aTexCoordLocation;

    public ObjShape(Context context, String filename, int textureID) {
        this.context = context;

        try {
            objInputStream = context.getAssets().open(filename);
            obj = ObjReader.read(objInputStream);
            objInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get desired color
        int intColor = ResourcesCompat.getColor(context.getResources(), R.color.colorShape, null);
        shapeColor = new float[]{Color.red(intColor) / (float) 255,
                Color.green(intColor) / (float) 255,
                Color.blue(intColor) / (float) 255,
                Color.alpha(intColor) / (float) 255};

        obj = ObjUtils.convertToRenderable(obj);

        IntBuffer indices = ObjData.getFaceVertexIndices(obj, 3);


        FloatBuffer vertices = ObjData.getVertices(obj);
        FloatBuffer texCoords = ObjData.getTexCoords(obj, 2,true);
        FloatBuffer normals = ObjData.getNormals(obj);



        int numberOfBuffers = 4;

        int[] bufferPtrs = new int[numberOfBuffers];
        glGenBuffers(numberOfBuffers, bufferPtrs, 0);

        glBindBuffer(GL_ARRAY_BUFFER, bufferPtrs[0]);
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * BYTES_PER_FLOAT, vertices, GL_STATIC_DRAW);
        vertexVBOIndex = bufferPtrs[0];
        numberOfVertices = vertices.capacity();

        glBindBuffer(GL_ARRAY_BUFFER, bufferPtrs[1]);
        glBufferData(GL_ARRAY_BUFFER, normals.capacity() * BYTES_PER_FLOAT, normals, GL_STATIC_DRAW);
        normalsVBOIndex = bufferPtrs[1];


        glBindBuffer(GL_ARRAY_BUFFER, bufferPtrs[2]);
        glBufferData(GL_ARRAY_BUFFER, texCoords.capacity() * BYTES_PER_FLOAT, texCoords, GL_STATIC_DRAW);
        texCoordsVBOIndex = bufferPtrs[2];

        glBindBuffer(GL_ARRAY_BUFFER, 0);


        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferPtrs[3]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * BYTES_PER_INT, indices, GL_STATIC_DRAW);
        facesVBOIndex = bufferPtrs[3];

        numberOfFaces = indices.capacity();

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        textureDataHandle = TextureHelper.loadTexture(context, textureID);

        glBindTexture(GL_TEXTURE_2D, textureDataHandle);
        glGenerateMipmap(GL_TEXTURE_2D);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);

        glBindTexture(GL_TEXTURE_2D,0);


        //removing buffers from native memory
        vertices.limit(0);
        vertices = null;
        normals.limit(0);
        normals = null;
        indices.limit(0);
        indices = null;

        //create shader program
        shapeShaderProgram = new ShapeShaderProgram(context);

        //create Material
        float[] ambient=new float[]{1.0f,0.5f,0.31f};
        float[] diffuse=new float[]{1.0f,0.5f,0.31f};
        float[] specular=new float[]{0.5f,0.5f,0.5f};
        float shininess=32.0f;
        material=new Material(ambient,diffuse,specular,shininess);

    }

    public void draw(float[] viewMatrix, float[] projectionMatrix, float[] lightPositionInEyeSpace, Light light) {


       shapeShaderProgram.useProgram();


        shapeShaderProgram.setUniforms(viewMatrix, projectionMatrix, light,0,material);

        aPositionLocation = shapeShaderProgram.getaPositionLocation();
        aNormalLocation = shapeShaderProgram.getaNormalLocation();
        aTexCoordLocation = shapeShaderProgram.getaTextureCoordLocation();

        //bind texture buffer
        // Pass in the texture information
        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandle);

//        vertexBuffer
        glBindBuffer(GL_ARRAY_BUFFER, vertexVBOIndex);
        glEnableVertexAttribArray(aPositionLocation);
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, 0);

//        normalsBuffer
        glBindBuffer(GL_ARRAY_BUFFER, normalsVBOIndex);
        glEnableVertexAttribArray(aNormalLocation);
        glVertexAttribPointer(aNormalLocation, 3, GL_FLOAT, false, 0, 0);

//      texture coords buffer
        glBindBuffer(GL_ARRAY_BUFFER, texCoordsVBOIndex);
        glEnableVertexAttribArray(aTexCoordLocation);
        glVertexAttribPointer(aTexCoordLocation, 2, GL_FLOAT, false, 0, 0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

//      faces buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, facesVBOIndex);

//        draw
        glDrawElements(GL_TRIANGLES, numberOfFaces, GL_UNSIGNED_INT, 0);
//        unbind
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//      disable Arrays
        glDisableVertexAttribArray(aPositionLocation);
        glDisableVertexAttribArray(aNormalLocation);
        glDisableVertexAttribArray(aTexCoordLocation);


    }
}
