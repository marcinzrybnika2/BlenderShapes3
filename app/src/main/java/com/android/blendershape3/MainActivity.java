package com.android.blendershape3;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private Torus torus;
    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        glSurfaceView =new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                torus=new Torus(getApplicationContext());

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

                GLES20.glViewport(0,0,width,height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                torus.draw();

            }
        });


        setContentView(glSurfaceView);

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(rendererSet){
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(rendererSet){
            glSurfaceView.onResume();
        }

    }
}