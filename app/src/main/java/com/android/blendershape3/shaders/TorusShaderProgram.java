package com.android.blendershape3.shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.android.blendershape3.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class TorusShaderProgram extends ShaderProgram {

    private final int aPositionLocation;
    private final int uMatrixLocation;

    /**
     * Subclass of ShaderProgram
     *
     * @param context
     *
     */
    public TorusShaderProgram(Context context) {
        super(context, R.raw.bs_vertex_shader, R.raw.bs_fragment_shader);

       aPositionLocation = glGetAttribLocation(program, A_POSITION);
       uMatrixLocation=glGetUniformLocation(program,U_MVPMATRIX);

    }

    public void setUniforms(float[] matrix){
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getProgram(){
        return program;
    }

    public int getaPositionLocation() {
        return aPositionLocation;
    }

    public int getuMatrixLocation() {
        return uMatrixLocation;
    }
}
