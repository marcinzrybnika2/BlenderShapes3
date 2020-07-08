package com.android.blendershape3.shaders;

import android.content.Context;

import com.android.blendershape3.R;

import static android.opengl.GLES20.glGetAttribLocation;

public class TableShaderProgram extends ShaderProgram {
    private final int aColorLocation;
    private final int aPositionLocation;

    /**
     * Subclass of ShaderProgram
     *
     * @param context
     *
     */
    public TableShaderProgram(Context context) {
        super(context, R.raw.simple_vertex_shader, R.raw.simple_fragment_shader);


        aColorLocation = glGetAttribLocation(program, A_COLOR);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);


    }

    public int getProgram(){
        return program;
    }

    public int get_aColorLocation() {
        return aColorLocation;
    }

    public int get_aPositionLocation() {
        return aPositionLocation;
    }
}
