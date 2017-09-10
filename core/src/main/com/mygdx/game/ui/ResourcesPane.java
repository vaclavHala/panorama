package com.mygdx.game.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Chunks;
import com.mygdx.game.Chunks.Chunk;
import com.mygdx.game.Conductor;
import com.mygdx.game.service.DebugFeedService;
import static java.lang.String.format;
import java.util.List;
import java.util.*;

public class ResourcesPane extends ScreenAdapter {

    private final Conductor conductor;
    private final Stage uiStage;
    private final Chunks chunks;
    //    private final Panorama p;
    private final Skin skin;
    private final ScrollPane scroll;
    private final Actor parent;
    private final Table container;
    private final List<Chunk> currentChunks;
    //    private final PopMenu popRight;
    private final Timer resourceCheckTimer;

    private Timer.Task updateTask;

    private InputProcessor debugInput = new InputAdapter() {
        //
        //        @Override
        //        public boolean keyDown(int keycode) {
        //
        //            if (keycode == Input.Keys.E) {
        //                popRight.enable();
        //            } else if (keycode == Input.Keys.D) {
        //                popRight.disable();
        //            }
        //
        //            if (keycode == Input.Keys.K) {
        //                chunks.fetchAvailableChunks();
        //            } else if (keycode == Input.Keys.L) {
        //                chunks.downloadChunk(48, 14);
        //            }
        //
        //            return false;
        //        }
        //
    };

    public ResourcesPane(Conductor conductor, Skin skin, Stage uiStage, Chunks chunks) {
        this.conductor = conductor;
        this.chunks = chunks;
        this.skin = skin;
        this.uiStage = uiStage;
        this.resourceCheckTimer = Timer.instance();

        this.currentChunks = new ArrayList<Chunk>();
        this.container = new Table();
        this.scroll = new ScrollPane(container, skin);

        //        List<Map.Entry<String, Runnable>> buttons = new ArrayList<Map.Entry<String, Runnable>>();
        //        buttons.add(new AbstractMap.SimpleEntry<String, Runnable>("red", new Runnable() {
        //
        //            @Override
        //            public void run() {
        //                log("Going to NEW_PANORAMA");
        //                ResourcesPane.this.conductor.screen(NewPanoramaPane.class);
        //            }
        //        }));
        //        buttons.add(new AbstractMap.SimpleEntry<String, Runnable>("blue", new Runnable() {
        //
        //            @Override
        //            public void run() {
        //                // this is resources button, we are already there
        //            }
        //        }));
        //        popRight = new PopMenu(skin, PopMenu.MenuSide.RIGHT,
        //                               uiStage.getWidth(), uiStage.getHeight(),
        //                               buttons);

        Table root = new Table(skin);
        root.setFillParent(true);

        root.add(new Table().add(new TextButton("A", skin)).expand().fill().getTable()
                            .row().getTable()
                            .add(new TextButton("B", skin)).expand().fill().getTable()
                            .row().getTable()
                            .add(new TextButton("C", skin)).expand().fill().getTable()
                            .row().getTable()
            ).width(80).expandY().fill();

        root.add(scroll).expand().fill();

        parent = root;

    }

    @Override
    public void show() {
        //        uiStage.addActor(scroll);
        //        uiStage.addActor(popRight.actor());
        uiStage.addActor(parent);

        updateTask = resourceCheckTimer.scheduleTask(new Task() {

            @Override
            public void run() {
                ResourcesPane.this.update();
            }
        }, 0, 5);

        Gdx.input.setInputProcessor(new InputMultiplexer(debugInput, uiStage));
    }

    private void update() {
        List<Chunk> newChunks = chunks.chunkList();
        if (!shouldUpdate(newChunks)) {
            return;
        }
        log("Rebuilding chunk table");
        this.currentChunks.clear();
        container.clear();
        this.currentChunks.addAll(newChunks);

        for (final Chunk chunk : this.currentChunks) {
            ProgressBar bar = null;
            ImageButton button = null;
            String description = null;
            if (chunk instanceof Chunks.OwnedChunk) {
                bar = new ProgressBar(0, 100, 1, false, skin);
                bar.setValue(100);
                button = new ImageButton(skin);
                description = format("Latitude: %s°..%s°, Longitude: %s°..%s° (Downloaded)",
                                     chunk.lat, chunk.lat + 1,
                                     chunk.lon, chunk.lon + 1);
            } else if (chunk instanceof Chunks.AvailableChunk) {
                bar = new ProgressBar(0, 100, 1, false, skin);
                bar.setValue(0);
                button = new ImageButton(skin);
                description = format("Latitude: %s°..%s°, Longitude: %s°..%s° (Available)",
                                     chunk.lat, chunk.lat + 1,
                                     chunk.lon, chunk.lon + 1);
            } else if (chunk instanceof Chunks.ObtainingChunk) {
                int downloadedPercent = ((Chunks.ObtainingChunk) chunk).progressPercent;
                bar = new ProgressBar(0, 100, 1, false, skin);
                bar.setValue(downloadedPercent);
                button = new ImageButton(skin);
                description = format("Latitude: %s°..%s°, Longitude: %s°..%s° (Downloading: %s %%)",
                                     chunk.lat, chunk.lat + 1,
                                     chunk.lon, chunk.lon + 1,
                                     downloadedPercent);
            } else {
                throw new IllegalArgumentException("Unexpected chunk: " + chunk);
            }
            button.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    chunks.downloadChunk(chunk.lon, chunk.lat);
                    ResourcesPane.this.update();
                }
            });
            Label label = new Label(description, skin);
            bar.setHeight(100);
            c = container.stack(bar).expandX().fill();
            container.add(button).size(50, 50);
            container.row();
        }

        if (chunks.availableChunksFetchInProgress()) {
            container.add(new Label("Loading list of available chunks", skin)).height(50);
            container.row();
        }

    }

    Cell c;

    private boolean shouldUpdate(List<Chunk> newChunks) {
        if (this.container.getRows() != newChunks.size()) {
            return true;
        }
        Iterator<Chunk> i = this.currentChunks.iterator();
        for (Chunk newChunk : newChunks) {
            Chunk oldChunk = i.next();
            if (!oldChunk.equals(newChunk)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        uiStage.act(delta);
        Button b;
        uiStage.draw();
        System.out.println(c.getPrefHeight() + " " + c.getActor().getHeight());
    }

    @Override
    public void hide() {
        uiStage.getActors().clear();
        updateTask.cancel();
    }

    private static void log(String message, Object... args) {
        Gdx.app.log("ResourcesPane", String.format(message, args));
    }
}
