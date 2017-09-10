package com.mygdx.game.camera;

import android.graphics.ImageFormat;
import com.badlogic.gdx.Gdx;
import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera;

    public CameraSurface( Context context ) {
        super( context );
        // We're implementing the Callback interface and want to get notified
        // about certain surface events.
        getHolder().addCallback( this );
        // We're changing the surface to a PUSH surface, meaning we're receiving
        // all buffer data from another component - the camera, in this case.
        getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
    }

    public void surfaceCreated( SurfaceHolder holder ) {
        // Once the surface is created, simply open a handle to the camera hardware.
        camera = Camera.open();
    }

    public void surfaceChanged( SurfaceHolder holder, int format, int width, int height ) {
        // This method is called when the surface changes, e.g. when it's size is set.
        // We use the opportunity to initialize the camera preview display dimensions.
        Camera.Parameters p = camera.getParameters();
        for(Camera.Size s: p.getSupportedPictureSizes()) {
            Gdx.app.log("CAMERA", "Supported size: " +s.width+"x"+s.height);
        }
        for(Camera.Size s: p.getSupportedPreviewSizes()) {
            Gdx.app.log("CAMERA", "Supported preview: " +s.width+"x"+s.height);
        }
//        Gdx.app.log("CAMERA", "Supported formats: "+p.getSupportedPictureFormats());
//        Gdx.app.log("CAMERA", "Supported previewSizes: "+p.getSupportedPreviewSizes());
        p.setPreviewSize( 1280, 720);
        p.setPictureSize( 640, 480);
//        p.setPictureFormat(ImageFormat.NV21);
        camera.setParameters( p );

        // We also assign the preview display to this surface...
        try {
            camera.setPreviewDisplay( holder );
        } catch( IOException e ) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed( SurfaceHolder holder ) {
        // Once the surface gets destroyed, we stop the preview mode and release
        // the whole camera since we no longer need it.
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public Camera getCamera() {
        return camera;
    }

}