package com.mygdx.game.demo;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.Arrays;
import static java.lang.String.format;

public class SensorDemo implements ApplicationListener, SensorEventListener {

    BitmapFont font;
    SpriteBatch batch ;

    final SensorManager sensors;

    public SensorDemo(SensorManager sensors) {
        this.sensors = sensors;
    }

    @Override
    public void create() {

        TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"), Gdx.files.internal(""));
        Skin skin = new Skin();
        skin.addRegions(uiAtlas);
        skin.add("font", new BitmapFont(Gdx.files.internal("default.fnt"), Gdx.files.internal("default.png"), false));


        this.font = skin.getFont("font");
this.batch = new SpriteBatch();




        Sensor accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometer == null) {
            throw new IllegalStateException("Expected accelerometer to be available");
        }

        Log.i("ORIENTATION", "Registering orientation sensor listener");
        sensors.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        this.font.draw(batch,
                format("yaw:   %s\npitch: %s\nroll:  %s", scrVec[0], scrVec[1], scrVec[2]),
                100, 200);
        batch.end();

    }

    float[] scrVec = new float[3];

    static void canonicalOrientationToScreenOrientation(
            int displayRotation, float[] canVec, float[] screenVec)
    {
        final int axisSwap[][] = {
                {  1,  -1,  0,  1  },     // ROTATION_0
                {-1,  -1,  1,  0  },     // ROTATION_90
                {-1,    1,  0,  1  },     // ROTATION_180
                {  1,    1,  1,  0  }  }; // ROTATION_270

        final int[] as = axisSwap[displayRotation];
        screenVec[0]  =  (float)as[0] * canVec[ as[2] ];
        screenVec[1]  =  (float)as[1] * canVec[ as[3] ];
        screenVec[2]  =  canVec[2];
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        canonicalOrientationToScreenOrientation(1, event.values, scrVec);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
