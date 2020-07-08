package com.android.blendershape3;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TempRenderer implements GLSurfaceView.Renderer {
  private Context context;
  private Torus torus;

  public TempRenderer(Context context){
      this.context=context;
  }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        torus = new Torus(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

      torus.draw();
    }
}
