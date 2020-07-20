package com.android.blendershape3.shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.android.blendershape3.R;
import com.android.blendershape3.util.Light;
import com.android.blendershape3.util.Material;

import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class ShapeShaderProgram extends ShaderProgram {

    private final int aPositionLocation;
    private final int aNormalLocation;
    private final int aTextureCoordLocation;

    private final int uMVMatrixLoc;
    private final int uMVPMatrixLoc;
    private final int uTextureSamplerLoc;

    private final int uMaterialAmbientLoc;
    private final int uMaterialDiffuseLoc;
    private final int uMaterialSpecularLoc;
    private final int uMaterialShininessLoc;

    private final int uLightPositionLoc;
    private final int uLightAmbientLoc;
    private final int uLightDiffuseLoc;
    private final int uLightSpecularLoc;

    /**
     * Superclass for all shader programs
     *
     * @param context
     */
    public ShapeShaderProgram(Context context) {
        super(context, R.raw.shape_vertex_shader, R.raw.shape_fragment_shader);


        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aNormalLocation = GLES20.glGetAttribLocation(program, A_NORMAL);
        aTextureCoordLocation = GLES20.glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        uMVMatrixLoc = glGetUniformLocation(program, U_MVMATRIX);
        uMVPMatrixLoc = glGetUniformLocation(program, U_MVPMATRIX);
        uTextureSamplerLoc =glGetUniformLocation(program,U_TEXTURE_UNIT);

        uMaterialAmbientLoc=glGetUniformLocation(program,MATERIAL_AMBIENT);
        uMaterialDiffuseLoc=glGetUniformLocation(program,MATERIAL_DIFFUSE);
        uMaterialSpecularLoc=glGetUniformLocation(program,MATERIAL_SPECULAR);
        uMaterialShininessLoc =glGetUniformLocation(program,MATERIAL_SHININESS);

        uLightPositionLoc=glGetUniformLocation(program,LIGHT_POSITION);
        uLightAmbientLoc=glGetUniformLocation(program,LIGHT_AMBIENT);
        uLightDiffuseLoc=glGetUniformLocation(program,LIGHT_DIFFUSE);
        uLightSpecularLoc=glGetUniformLocation(program,LIGHT_SPECULAR);

    }

    public void setUniforms(float[] MVMatrix, float[] MVPMatrix, Light light, int textureUnit, Material material) {

        glUniformMatrix4fv(uMVMatrixLoc, 1, false, MVMatrix, 0);
        glUniformMatrix4fv(uMVPMatrixLoc, 1, false, MVPMatrix, 0);
        // Tell the texture uniform sampler to use this texture in the
        // shader by binding to texture unit 0.
        glUniform1i(uTextureSamplerLoc, textureUnit);

        glUniform3fv(uMaterialAmbientLoc,1,material.ambient,0);
        glUniform3fv(uMaterialDiffuseLoc,1,material.diffuse,0);
        glUniform3fv(uMaterialSpecularLoc,1,material.specular,0);
        glUniform1f(uMaterialShininessLoc,material.shininess);

        glUniform3fv(uLightPositionLoc,1,light.position,0);
        glUniform3fv(uLightAmbientLoc,1,light.ambient,0);
        glUniform3fv(uLightDiffuseLoc,1,light.diffuse,0);
        glUniform3fv(uLightSpecularLoc,1,light.specular,0);


    }

//    public int getProgram() {
//        return program;
//    }

    public int getaPositionLocation() {

        return aPositionLocation;
    }

    public int getaNormalLocation() {
        return aNormalLocation;
    }


    public int getaTextureCoordLocation() {
        return aTextureCoordLocation;
    }
}
