package com.android.blendershape3.shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.android.blendershape3.R;

public class ShapeShaderProgram extends ShaderProgram {

    private final int aPositionLocation;
    private final int aNormalPosition;
    private final int uMVMatrixLocation;
    private final int uMVPMatrixLocation;
    private final int uColorLocation;
    private final int uLightPosLocation;

    /**
     * Superclass for all shader programs
     *
     * @param context
     */
    public ShapeShaderProgram(Context context) {
        super(context, R.raw.shape_vertex_shader, R.raw.shape_fragment_shader);


        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aNormalPosition = GLES20.glGetAttribLocation(program, A_NORMAL);

        uMVMatrixLocation = GLES20.glGetUniformLocation(program, U_MVMATRIX);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, U_MVPMATRIX);
        uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR);
        uLightPosLocation=GLES20.glGetUniformLocation(program, U_LIGHT_POSITION);

    }

    public void setUniforms(float[] MVMatrix, float[] MVPMatrix, float[] color, float[] lightPosition) {
        GLES20.glUniformMatrix4fv(uMVMatrixLocation, 1, false, MVMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, MVPMatrix, 0);
        GLES20.glUniform4fv(uColorLocation, 1, color, 0);
        GLES20.glUniform3fv(uLightPosLocation,1, lightPosition,0);
    }

    public int getProgram() {
        return program;
    }

    public int getaPositionLocation() {

        return aPositionLocation;
    }

    public int getaNormalPosition() {
        return aNormalPosition;
    }


}
