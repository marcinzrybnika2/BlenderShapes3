package com.android.blendershape3.shaders;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.android.blendershape3.R;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class LightShaderProgram extends ShaderProgram {
    private static final String TAG = "LightShaderProgram";
    private final int uColorLocation;
    private final int aPositionLocation;
    private final int uMVPMatrixLocation;

    /**
     * Superclass for all shader programs
     *
     * @param context
        */
    public LightShaderProgram(Context context) {
        super(context, R.raw.light_source_v_shader, R.raw.light_source_f_shader);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);

        uMVPMatrixLocation = glGetUniformLocation(program, U_MVPMATRIX);
        uColorLocation = glGetUniformLocation(program, U_LIGHT_COLOR);


    }


    public void setUniforms(float[] uMVPMatrix, float[] uLightColor){

        glUniformMatrix4fv(uMVPMatrixLocation,1,false,uMVPMatrix,0);
        glUniform3fv(uColorLocation,1,uLightColor,0);

    }



    public int getaPositionLocation() {
        return aPositionLocation;
    }

}
