package com.android.blendershape3.shapes;

import android.content.Context;

import com.android.blendershape3.shaders.TableShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.android.blendershape3.util.Constants.BYTES_PER_FLOAT;

public class AirHockeyTable {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private Context context;
    private FloatBuffer vertexData;
    private int aColorLocation;
    private int aPositionLocation;
    private TableShaderProgram tableShaderProgram;
    private int program;

    public AirHockeyTable(Context context){
        this.context=context;


        float[] tableVerticesWithTriangles = {

                //TriangleFan
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.5f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
                //Line 1
                -0.5f, 0f, 1f, 0f, 0f,
                0.5f, 0f, 1f, 0f, 0f,
                //Malets
                0f, -0.25f, 0f, 0f, 1f,
                0f, 0.25f, 1f, 0f, 0f
        };


        vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexData.put(tableVerticesWithTriangles);




        tableShaderProgram=new TableShaderProgram(context);
        program=tableShaderProgram.getProgram();


    }

    public TableShaderProgram getTableShaderProgram(){
        return tableShaderProgram;
    }


    public void draw(){


        glUseProgram(program);

        aColorLocation=tableShaderProgram.get_aColorLocation();
        aPositionLocation=tableShaderProgram.get_aPositionLocation();


        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        vertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        glEnableVertexAttribArray(aColorLocation);


        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);


        glDrawArrays(GL_LINES, 6, 2);


        glDrawArrays(GL_POINTS, 8, 1);


        glDrawArrays(GL_POINTS, 9, 1);
    }

}
