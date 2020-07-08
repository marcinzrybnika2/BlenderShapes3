package com.android.blendershape3;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.android.blendershape3.shapes.AirHockeyTable;
import com.android.blendershape3.shapes.Torus2;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.frustumM;

public class BSRenderer implements GLSurfaceView.Renderer {

    private static final String TAG="BSRenderer";

    AirHockeyTable table;
    Torus2 torus2;

    /** References to other main objects. */
    private final MainActivity mainActivity;
    private final ErrorHandler errorHandler;

    /** Retain the most recent delta for touch events. */
    // These still work without volatile, but refreshes are not guaranteed to
    // happen.
    public volatile float deltaX;
    public volatile float deltaY;


    /**
     * Store the model matrix. This matrix is used to move models from object
     * space (where each model can be thought of being located at the center of
     * the universe) to world space.
     */
    private final float[] modelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix
     * transforms world space to eye space; it positions things relative to our
     * eye.
     */
    private final float[] viewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D
     * viewport.
     */
    private final float[] projectionMatrix = new float[16];

    /**
     * Allocate storage for the model/view combined matrix. This will be passed into
     * the shader program.
     */
    private final float[] mvMatrix = new float[16];
    /**
     * Allocate storage for the final combined matrix. This will be passed into
     * the shader program.
     */
    private final float[] mvpMatrix = new float[16];

    /**
     * Additional matrix
     */
    private final float[] temporaryMatrix = new float[16];

    public BSRenderer(MainActivity activity, ErrorHandler errorHandler) {
//        this.context = context;
            this.mainActivity=activity;
            this.errorHandler=errorHandler;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -1.0f;

        // Set our up vector. This is where our head would be pointing were we
        // holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera
        // position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination
        // of a model and view matrix. In OpenGL 2, we can keep track of these
        // matrices separately if we choose.
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);


//        table = new AirHockeyTable(mainActivity);
        torus2=new Torus2(mainActivity);

    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        glViewport(0, 0, width, height);



        // Create a new perspective projection matrix. The height will stay the
        // same while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);

//        Matrix.perspectiveM(projectionMatrix,0, 45, (float) width / (float) height, 1f, 10f);
    }


    @Override
    public void onDrawFrame(GL10 gl) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


//        table.draw();
        torus2.draw(viewMatrix,projectionMatrix);

    }
}
