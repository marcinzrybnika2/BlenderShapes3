package com.android.blendershape3;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.android.blendershape3.shapes.LightSource;
import com.android.blendershape3.shapes.ObjShape;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class BSRenderer implements GLSurfaceView.Renderer {

    private static final String TAG="BSRenderer";

 /*   AirHockeyTable table;
    Torus2 torus2;
    Shape shape;
*/
    /** References to other main objects. */
    private final MainActivity context;
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

    /** Store the accumulated rotation. */
    private final float[] accumulatedRotation = new float[16];

    /** Store the current rotation. */
    private final float[] currentRotation = new float[16];

    /**
     * Additional matrix
     */
    private final float[] temporaryMatrix = new float[16];

    float[] lightPosInEyeSpace=new float[4];
    private float[] lightPositionInProjectionSpace=new float[4];

    private float[] lightPosition;
    private ObjShape objShape;
    private long savedTime;

    private LightSource lightSource;



    public BSRenderer(MainActivity activity, ErrorHandler errorHandler) {
//        this.context = context;
            this.context =activity;
            this.errorHandler=errorHandler;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.5f;
        final float eyeZ = 3.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

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

        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(accumulatedRotation, 0);

//        table = new AirHockeyTable(mainActivity);
//        torus2=new Torus2(mainActivity);
        lightPosition=new float[]{
            2.0f, 2.0f, 2.0f, 1.0f
        };
        //light source position passed to shape(draw)
        Matrix.multiplyMV(lightPosInEyeSpace,0,viewMatrix,0,lightPosition,0);


        lightSource=new LightSource(context,"Ball.obj",R.color.colorLightSource,lightPosition,lightPosInEyeSpace);

        objShape=new ObjShape(context,"TexCube.obj",R.drawable.wood_texture);
//        shape=new Shape(context,"TorusVN.obj");
//        savedTime=System.currentTimeMillis();
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
        final float far = 1000.0f;

        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);

//        Matrix.perspectiveM(projectionMatrix,0, 45, (float) width / (float) height, 1f, 10f);
    }


    @Override
    public void onDrawFrame(GL10 gl) {

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //light source
        float[] lp=lightSource.getPositionWorld();
        /*
        savedTime=(System.nanoTime()%360)/60;
        float dx=(float)Math.sin(savedTime);
        float dz=(float)Math.cos(savedTime);
        lp[0]=dx;
        lp[1]=dz;
        lightSource.setPositionWorld(lp);
        float[] temp=new float[4];
        Matrix.multiplyMV(temp,0,viewMatrix,0,lp,0);
        lightSource.setPositionEye(temp);

*/
        Matrix.setIdentityM(modelMatrix,0);
        Matrix.translateM(modelMatrix,0,lp[0],lp[1],lp[2]);
        Matrix.scaleM(modelMatrix,0,0.1f,0.1f,0.1f);

        Matrix.multiplyMM(mvMatrix,0,viewMatrix,0,modelMatrix,0);
        Matrix.multiplyMM(mvpMatrix,0,projectionMatrix,0,mvMatrix,0);
//        draw light source
        lightSource.draw(mvpMatrix);

        //light source position passed to shape(draw)
//        Matrix.multiplyMV(lightPosInEyeSpace,0,viewMatrix,0,lp,0);


        // Set a matrix that contains the current rotation.
        Matrix.setIdentityM(currentRotation, 0);
        Matrix.rotateM(currentRotation, 0, deltaX, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(currentRotation, 0, deltaY, 1.0f, 0.0f, 0.0f);
        deltaX = 0.0f;
        deltaY = 0.0f;

        // Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(temporaryMatrix, 0, currentRotation, 0, accumulatedRotation, 0);
        System.arraycopy(temporaryMatrix, 0, accumulatedRotation, 0, 16);


        //model Matrix for shape
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0.0f, -0.5f, 0.0f);
        Matrix.rotateM(modelMatrix, 0, 30, 1.0f, 0.0f, 0.0f);


        // Rotate the shape taking the overall rotation into account.
        Matrix.multiplyMM(temporaryMatrix, 0, modelMatrix, 0, accumulatedRotation, 0);
        System.arraycopy(temporaryMatrix, 0, modelMatrix, 0, 16);


        // This multiplies the view matrix by the model matrix, and stores
        // the result in the MV matrix
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix,
        // and stores the result in the MVP matrix
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);



        objShape.draw(mvMatrix,mvpMatrix, lightSource.getLight());

    }
}
