package com.mygdx.game;

import android.hardware.Camera;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mygdx.game.camera.AndroidCameraService;

public class AndroidLauncher extends AndroidApplication {

	private android.hardware.Camera camera;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		camera = Camera.open();
		Camera.Parameters params = camera.getParameters();
//		params.setpre
//		camera.getParameters().setPreviewFormat(ImageFormat.NV21);

		camera.setParameters(params);

		Camera.Size previewSize = params.getPreviewSize();
		Runnable startCamera = new Runnable() {
			@Override
			public void run() {
				camera.startPreview();
			}
		};
		AndroidCameraService camService = new AndroidCameraService(previewSize.width, previewSize.height, startCamera);

		camera.setPreviewCallback(camService);


//		LocationManager gps = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.r = 8;
		config.g = 8;
		config.b = 8;
		config.a = 8;
		initialize(new MyGdxGame(camService), config);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		camera.release();
	}
}
