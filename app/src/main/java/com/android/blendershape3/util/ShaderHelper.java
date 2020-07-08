package com.android.blendershape3.util;



import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public class ShaderHelper {
    private static final String TAG="ShaderHelper";

    public static int compileVertexShader(String shaderCode){
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode){
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {

        final int shaderObjectId=glCreateShader(type);
        if(shaderObjectId==0){
            if(LogerConfig.ON){
                Log.w(TAG,"Could not create shader");
            }
            return 0;
        }

        glShaderSource(shaderObjectId,shaderCode);
        glCompileShader(shaderObjectId);

        final int[] compileStatus=new int[1];
        glGetShaderiv(shaderObjectId,GL_COMPILE_STATUS,compileStatus,0);
        if(LogerConfig.ON){
            Log.v(TAG,"Result of compiling source:"+"\n"+shaderCode+"\n"+
                    glGetShaderInfoLog(shaderObjectId));
        }
        if(compileStatus[0]==0){
            //compile failed
            glDeleteShader(shaderObjectId);
            if(LogerConfig.ON){
                Log.w(TAG,"Compilation of shader failed");
            }
            return 0;
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderID, int fragmentShaderID){
        final int programObjectID=glCreateProgram();

        if(programObjectID==0){
            if(LogerConfig.ON){
                Log.w(TAG,"Could not create new program");
            }
            return 0;
        }
        glAttachShader(programObjectID,vertexShaderID);
        glAttachShader(programObjectID,fragmentShaderID);

        glLinkProgram(programObjectID);
        final int[] linkStatus=new int[1];
        glGetProgramiv(programObjectID,GL_LINK_STATUS,linkStatus,0);

        if(LogerConfig.ON){
            Log.v(TAG,"Result of linking program:\n"+
                    glGetProgramInfoLog(programObjectID));
        }

        if(linkStatus[0]==0){
            //linking failed
            glDeleteProgram(programObjectID);
            if(LogerConfig.ON){
                Log.w(TAG,"Linking of program failed");
            }
            return 0;
        }

        return programObjectID;
    }

    public static boolean validateProgram(int programObjectID){
        glValidateProgram(programObjectID);

        final int[] validateStatus=new int[1];
        glGetProgramiv(programObjectID,GL_VALIDATE_STATUS,validateStatus,0);
        Log.v(TAG,"Result of validating program: "+validateStatus[0] +"\n"
                +"LOG:"+glGetProgramInfoLog(programObjectID));
        return validateStatus[0] !=0;
    }


    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {

        int program;
        //Compiletheshaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        //Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader);

        if (LogerConfig.ON) {
            validateProgram(program);
        }

        return program;
    }
}
