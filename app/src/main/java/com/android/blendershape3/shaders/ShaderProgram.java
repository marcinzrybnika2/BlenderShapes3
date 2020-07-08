package com.android.blendershape3.shaders;



import android.content.Context;


import com.android.blendershape3.util.ShaderHelper;
import com.android.blendershape3.util.TextResourceReader;

import static android.opengl.GLES20.glUseProgram;

public class ShaderProgram {

    //Uniform constants
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_MVPMATRIX = "u_MVPMatrix";

    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_COLOR="u_Color";
    protected static final String U_VECTOR_TO_LIGHT="u_VectorToLight";
    //Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_NORMAL="a_Normal";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    //Shader program
    protected final int program;

    /**Superclass for all shader programs
     *
     * @param context
     * @param vertexShaderResourceId
     * @param fragmentShaderResourceId
     */
    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId) {
        //Compile the shaders and link the program.
        program = ShaderHelper.buildProgram(TextResourceReader.
                        readTextFileFromResource(context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(context,
                        fragmentShaderResourceId));
    }

    public void useProgram() {
        //Set the current OpenGL shader program to this program.
        glUseProgram(program);
    }


}
