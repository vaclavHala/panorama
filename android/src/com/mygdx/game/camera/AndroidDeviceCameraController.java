package com.mygdx.game.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

import android.hardware.Camera;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewGroup.LayoutParams;
import com.mygdx.game.service.DeviceCameraControl;
import java.util.concurrent.CountDownLatch;

public class AndroidDeviceCameraController implements DeviceCameraControl, Camera.PictureCallback, Camera.AutoFocusCallback {

    private final AndroidApplication activity;
    private CameraSurface cameraSurface;

    public AndroidDeviceCameraController(AndroidApplication activity) {
        this.activity = activity;
        this.cameraSurface = new CameraSurface(activity);
    }

    @Override
    public void on() {
        log("Turning camera on");
        final CountDownLatch latch = new CountDownLatch(1);
        activity.handler.post(new Runnable() {
            @Override
            public void run() {

                activity.addContentView(cameraSurface, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log("Camera is running");
    }

    @Override
    public void off() {
        log("Turning camera off");
        final CountDownLatch latch = new CountDownLatch(1);
        activity.handler.post(new Runnable() {
            @Override
            public void run() {
                ViewParent parentView = cameraSurface.getParent();
                ViewGroup viewGroup = (ViewGroup) parentView;
                viewGroup.removeView(cameraSurface);

                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log("Camera is dead");
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera camera) {
    }

    @Override
    public synchronized void onPictureTaken(byte[] pictureData, Camera camera) {
    }

    private static final String TAG = AndroidDeviceCameraController.class.getSimpleName();

    private static void log(String format, Object... args) {
        Gdx.app.log(TAG, String.format(format, args));
    }
}