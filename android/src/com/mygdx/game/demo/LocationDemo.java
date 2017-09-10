package com.mygdx.game.demo;

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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.service.LocationServicePull;
import static java.lang.String.format;

public class LocationDemo implements ApplicationListener {

    BitmapFont font;
    SpriteBatch batch;

    private final LocationServicePull gps;

    public LocationDemo(LocationServicePull gps) {
        this.gps = gps;
    }

    @Override
    public void create() {

        TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"), Gdx.files.internal(""));
        Skin skin = new Skin();
        skin.addRegions(uiAtlas);
        skin.add("font", new BitmapFont(Gdx.files.internal("default.fnt"), Gdx.files.internal("default.png"), false));

        this.font = skin.getFont("font");
        this.batch = new SpriteBatch();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin();
        String msg = null;
        try {
            msg = "Location: " + gps.getLocation(new Vector2()).toString();
        } catch (Exception e) {
            msg = e.getMessage();
        }
        this.font.draw(batch,
                       msg,
                       100, 200);
        batch.end();

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

}
