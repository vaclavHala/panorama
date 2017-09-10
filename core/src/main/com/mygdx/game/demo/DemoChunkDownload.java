package com.mygdx.game.demo;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.Chunks;
import com.mygdx.game.PanoramaSkin;
import com.mygdx.game.ui.NewPanoramaPane;
import com.mygdx.game.ui.PopMenu;
import com.mygdx.game.ui.ResourcesPane;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DemoChunkDownload extends ApplicationAdapter {

    Chunks chunks;

    @Override
    public void create() {
        System.out.println("CREATE");

        Executor netPool = Executors.newFixedThreadPool(4);
        chunks = new Chunks(netPool);

        Gdx.input.setInputProcessor(debugInput);
    }

    private InputProcessor debugInput = new InputAdapter() {

        @Override
        public boolean keyDown(int keycode) {

            if (keycode == Input.Keys.E) {
                chunks.fetchAvailableChunks();
            } else if (keycode == Input.Keys.D) {
                chunks.downloadChunk(14, 48);
            }

            return false;
        }

    };

}
