package com.mygdx.game.demo;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.camera.AndroidDeviceCameraController;
import com.mygdx.game.service.DeviceCameraControl;

public class CameraDemoLauncher extends AndroidApplication {

    private int origWidth;
    private int origHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        // we need to change the default pixel format - since it does not include an alpha channel
        // we need the alpha channel so the camera preview will be seen behind the GL scene
        cfg.r = 8;
        cfg.g = 8;
        cfg.b = 8;
        cfg.a = 8;


        DeviceCameraControl cameraControl = new AndroidDeviceCameraController(this);
//        DeviceCameraControl cameraControl = null;
        initialize(new CameraDemo(cameraControl), cfg);

        log("%ss: Demo main", Thread.currentThread());

        if (graphics.getView() instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) graphics.getView();
            // force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
//        // we don't want the screen to turn off during the long image saving process
        graphics.getView().setKeepScreenOn(true);
    }



    private static final String TAG = CameraDemoLauncher.class.getSimpleName();

    private static  void log(String format, Object... args){
        Gdx.app.log(TAG, String.format(format, args));
    }

}
