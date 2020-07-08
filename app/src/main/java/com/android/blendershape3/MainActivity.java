package com.android.blendershape3;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private BsGLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    //    private TempRenderer tempRenderer;
    private BSRenderer bsRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        glSurfaceView = (BsGLSurfaceView) findViewById(R.id.BsGLsurfaceView);

        final ActivityManager aM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo cI = aM.getDeviceConfigurationInfo();
        final boolean supportEs2 = cI.reqGlEsVersion >= 0x20000
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                && (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("AndroidSDKbuiltforx86")));

//                Log.d(TAG,"GLES version= "+cI.reqGlEsVersion+" 2 supported= "+supportEs2);
        if (supportEs2) {

            glSurfaceView.setEGLContextClientVersion(2);

            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);


//            tempRenderer=new TempRenderer(this);
            bsRenderer = new BSRenderer(this, glSurfaceView);

            glSurfaceView.setRenderer(bsRenderer, displayMetrics.density);
            rendererSet = true;

        } else {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0",
                    Toast.LENGTH_LONG).show();
            //or check for other GL versions
            return;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }

    }
}