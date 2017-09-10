package com.mygdx.game;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import static android.os.Looper.myLooper;
import android.util.Log;
import android.view.SurfaceView;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.camera.AndroidDeviceCameraController;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import com.mygdx.game.orientation.AndroidAccelerometerOrientation;
import com.mygdx.game.service.DeviceCameraControl;
import com.mygdx.game.orientation.AndroidOrientationService;
import com.mygdx.game.service.DebugFeedService;
import static java.lang.String.format;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidLauncher extends AndroidApplication {

    //	private android.hardware.Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("panorama", "Starting the show");

        for (Sensor s : ((SensorManager) getApplicationContext().getSystemService(Activity.SENSOR_SERVICE)).getSensorList(Sensor.TYPE_ALL)) {
            Log.i("shit", format("Available sensor: %s, %s, %s, %s", s.getName(), s.getVendor(), s.getVersion(), s.getType()));
        }

        //		camera = Camera.open();
        //		Camera.Parameters params = camera.getParameters();
        //		params.setpre
        //		camera.getParameters().setPreviewFormat(ImageFormat.NV21);

        //		camera.setParameters(params);

        //		Camera.Size previewSize = params.getPreviewSize();
        //		Runnable startCamera = new Runnable() {
        //			@Override
        //			public void run() {
        //				camera.startPreview();
        //			}
        //		};
        //        Log.i("LAUNCH", "Starting camera service");
        //		AndroidCameraService camService = new AndroidCameraService(previewSize.width, previewSize.height, startCamera);

        //        Log.i("LAUNCH", "Starting orientation service");
        //		new AndroidOrientationService(getApplicationContext());
        new AndroidAccelerometerOrientation(getApplicationContext());
        //		camera.setPreviewCallback(camService);

        //		LocationManager gps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.r = 8;
        config.g = 8;
        config.b = 8;
        config.a = 8;

        final Looper[] backgroundLooperHolder = new Looper[1];
        final CountDownLatch looperLatch = new CountDownLatch(1);
        Thread backgroundLooperThread = new Thread(new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                backgroundLooperHolder[0] = myLooper();
                Log.d("pano.back", "Starting background looper");
                looperLatch.countDown();
                Looper.loop();
            }
        });
        backgroundLooperThread.setDaemon(true);
        backgroundLooperThread.start();
        try {
            looperLatch.await();
        } catch (InterruptedException e) {
            Log.e("pano.back", "Error waiting for background looper to start: " + formatException(e));
            throw new RuntimeException(e);
        }
        Looper backgroundLooper = backgroundLooperHolder[0];
        Log.d("pano.back", "Background looper running: " + backgroundLooper);

        DeviceCameraControl cameraControl = new AndroidDeviceCameraController(this);

        //        LogcatDebugFeedService debug = new LogcatDebugFeedService(this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        initialize(new Panorama(new AndroidLocationServicePush(locationManager, backgroundLooper, this), null), config);

        //        debug.start();

        if (graphics.getView() instanceof SurfaceView) {
            SurfaceView glView = (SurfaceView) graphics.getView();
            // force alpha channel - I'm not sure we need this as the GL surface is already using alpha channel
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        //        // we don't want the screen to turn off during the long image saving process
        graphics.getView().setKeepScreenOn(true);

    }
    //
    //	@Override
    //	protected void onResume() {
    //		super.onResume();
    //	}
    //
    //	@Override
    //	protected void onPause() {
    //		super.onPause();
    ////		camera.release();
    //	}

}
