package com.mygdx.game.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.*;
import static com.mygdx.game.Asserts.onUI;
import com.mygdx.game.ChunksService.ChunkEvent;
import com.mygdx.game.ChunksService.ChunkEventDownloadProgress;
import com.mygdx.game.ChunksService.ChunkEventMetadataFetch;
import com.mygdx.game.ChunksService.ChunkEventStateUpdate;
import com.mygdx.game.ChunksService.ChunkState;
import com.mygdx.game.ChunksService.ChunksServiceListener;
import com.mygdx.game.model.Chunk;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;

/*
 * Row types:
 *  Owned - chunk is stored on local device, DELETE button
 *  Available - chunk can be downloaded, DOWNLOAD button
 *  Downloading - cuhnk is being slurped, CANCEL button, progress bar updated by listener registered on download start
 *  Available chunks sync in progress - no button, appears when chunk lookup starts, disappears when ends
 */
public class ResourcesPane extends ScreenAdapter implements ChunksServiceListener {

    private static final float MILLIS_PER_UNIT = 5;
    private static final Comparator<Actor> COMPARATOR = new RowsComparator();

    private final Conductor conductor;
    private final Terraformer terraformer;
    private final Stage uiStage;
    private final ChunksService chunks;
    private final Skin skin;
    private final Actor parent;
    private final VerticalGroup chunkContainer;
    private final Map<Actor, Container<?>> rowToContainer;

    int x = 1;

    private InputProcessor debugInput = new InputAdapter() {

        @Override
        public boolean keyDown(int keycode) {

            if (keycode == Input.Keys.E) {
                addRow(new RowOwned(skin, new Chunk(1, x++)));
            } else if (keycode == Input.Keys.W) {
                addRow(new RowAvailable(skin, new Chunk(1, x++)));
            } else if (keycode == Input.Keys.R) {
                addRow(new RowDownloading(skin, new Chunk(1, x++), 0));
            } else if (keycode == Input.Keys.D) {
                Actor row = rowFor(new Chunk(1, --x));
                removeRow(row);
            }

            sortChildren();

            return false;
        }
        //
    };

    public ResourcesPane(
            Conductor conductor,
            Terraformer terraformer,
            Skin skin, Stage uiStage,
            ChunksService chunks) {
        this.conductor = conductor;
        this.terraformer = terraformer;
        this.chunks = chunks;
        this.skin = skin;
        this.uiStage = uiStage;

        this.rowToContainer = new HashMap<Actor, Container<?>>();
        this.chunkContainer = new VerticalGroup();
        ScrollPane scroll = new ScrollPane(chunkContainer, skin);

        Table root = new Table(skin);
        root.setFillParent(true);

        TextButton btnPanorama = new TextButton("Pano", skin);
        btnPanorama.setDisabled(!this.terraformer.hasLandscape());
        btnPanorama.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ResourcesPane.this.conductor.screen(PanoramaPane.class);
            }
        });

        TextButton btnNewPanorama = new TextButton("New", skin);
        btnNewPanorama.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                ResourcesPane.this.conductor.screen(NewPanoramaPane.class);
            }
        });
        TextButton btnResources = new TextButton("Res", skin);
        // no listener, we are already on this pane

        btnPanorama.setDisabled(!terraformer.hasLandscape());

        root.add(new Table().add(btnPanorama).expand().fill().getTable()
                            .row().getTable()
                            .add(btnNewPanorama).expand().fill().getTable()
                            .row().getTable()
                            .add(btnResources).expand().fill().getTable()
                            .row().getTable()
            ).width(80).expandY().fill();

        root.add(scroll).expand().fill();

        parent = root;

    }

    @Override
    public void show() {
        assert onUI();

        for (Chunk chunk : this.chunks.ownedChunks()) {
            this.addRow(new RowOwned(skin, chunk));
        }
        for (Chunk chunk : this.chunks.availableChunks()) {
            this.addRow(new RowAvailable(skin, chunk));
        }
        for (Entry<Chunk, Integer> chunkWithProgress : this.chunks.downloadingChunks()) {
            this.addRow(new RowDownloading(skin, chunkWithProgress.getKey(), chunkWithProgress.getValue()));
        }
        if (this.chunks.availableChunksFetchInProgress()) {
            this.addRow(new RowFetchInProgress(skin));
        }

        this.sortChildren();
        uiStage.addActor(parent);
        chunks.addListener(this);

        Gdx.input.setInputProcessor(new InputMultiplexer(debugInput, uiStage));
    }

    @Override
    public void hide() {
        uiStage.getActors().clear();
        chunks.removeListener(this);
    }

    @Override
    public void receive(List<ChunkEvent> events) {
        assert onUI();

        boolean doSort = false;
        for (ChunkEvent e : events) {
            if (e instanceof ChunkEventStateUpdate) {
                doSort = true;
                ChunkEventStateUpdate eUpdate = (ChunkEventStateUpdate) e;
                Chunk chunk = eUpdate.chunk;
                ChunkState state = eUpdate.state;
                this.removeRow(rowFor(chunk));
                if (state.equals(ChunkState.OWNED)) {
                    this.addRow(new RowOwned(skin, chunk));
                } else if (state.equals(ChunkState.DOWNLOADING)) {
                    this.addRow(new RowDownloading(skin, chunk, 0));
                } else if (state.equals(ChunkState.AVAILABLE)) {
                    this.addRow(new RowAvailable(skin, chunk));
                } else {
                    throw new AssertionError(state);
                }
            } else if (e instanceof ChunkEventDownloadProgress) {
                ChunkEventDownloadProgress eDown = (ChunkEventDownloadProgress) e;
                RowDownloading row = (RowDownloading) this.rowFor(eDown.chunk);
                float progressDiff = eDown.percentDone - row.progress.getValue();
                float animTime = progressDiff * MILLIS_PER_UNIT;
                row.progress.setAnimateDuration(animTime);
                row.progress.setValue(eDown.percentDone);
            } else if (e instanceof ChunkEventMetadataFetch) {
                ChunkEventMetadataFetch eMeta = (ChunkEventMetadataFetch) e;
                if (eMeta.inProgress && this.rowDownloading() == null) {
                    this.addRow(new RowFetchInProgress(skin));
                } else {
                    this.removeRow(this.rowDownloading());
                }
            } else {
                log("Unexpected event: %s", e);
            }
        }

        if (doSort) {
            this.sortChildren();
        }
    }

    private RowForChunk rowFor(Chunk chunk) {
        Objects.requireNonNull(chunk, "chunk");
        Actor[] rows = this.chunkContainer.getChildren().begin();

        try {
            for (int i = 0, n = rows.length; i < n; i++) {
                if (rows[i] == null) {
                    // the backing array of snapshot arr may contain nulls
                    continue;
                }
                Actor row = ((Container) rows[i]).getActor();
                if ((row instanceof RowForChunk) && ((RowForChunk) row).chunk.equals(chunk)) {
                    return (RowForChunk) row;
                }
            }
            return null;
        } finally {
            this.chunkContainer.getChildren().end();
        }
    }

    private RowFetchInProgress rowDownloading() {
        Actor[] rows = this.chunkContainer.getChildren().begin();
        try {
            for (int i = 0, n = rows.length; i < n; i++) {
                Actor row = ((Container) rows[i]).getActor();
                if (row instanceof RowFetchInProgress) {
                    return (RowFetchInProgress) row;
                }
            }
            return null;
        } finally {
            this.chunkContainer.getChildren().end();
        }
    }

    private void addRow(Actor row) {
        Objects.requireNonNull(row, "row");
        Container c = new Container(row).prefWidth(Value.percentWidth(1, this.chunkContainer))
                                        .fill().center();
        this.rowToContainer.put(row, c);
        this.chunkContainer.addActor(c);
    }

    private void removeRow(Actor row) {
        Objects.requireNonNull(row, "row");
        Container c = this.rowToContainer.get(row);
        this.chunkContainer.removeActor(c);
    }

    private void sortChildren() {
        this.chunkContainer.getChildren().sort(COMPARATOR);
    }

    //    private void update() {
    //        List<Chunk> newChunks = chunks.chunkList();
    //        if (!shouldUpdate(newChunks)) {
    //            return;
    //        }
    //        log("Rebuilding chunk table");
    //        this.currentChunks.clear();
    //        container.clear();
    //        this.currentChunks.addAll(newChunks);
    //
    //        for (final Chunk chunk : this.currentChunks) {
    //            ProgressBar bar = null;
    //            ImageButton button = null;
    //            String description = null;
    //            if (chunk instanceof ChunksService.OwnedChunk) {
    //                bar = new ProgressBar(0, 100, 1, false, skin);
    //                bar.setValue(100);
    //                button = new ImageButton(skin);
    //                description = format("Latitude: %s°..%s°, Longitude: %s°..%s° (Downloaded)",
    //                                     chunk.lat, chunk.lat + 1,
    //                                     chunk.lon, chunk.lon + 1);
    //            } else if (chunk instanceof ChunksService.AvailableChunk) {
    //                bar = new ProgressBar(0, 100, 1, false, skin);
    //                bar.setValue(0);
    //                button = new ImageButton(skin);
    //                description = format("Latitude: %s°..%s°, Longitude: %s°..%s° (Available)",
    //                                     chunk.lat, chunk.lat + 1,
    //                                     chunk.lon, chunk.lon + 1);
    //            } else if (chunk instanceof ChunksService.ObtainingChunk) {
    //                int downloadedPercent = ((ChunksService.ObtainingChunk) chunk).progressPercent;
    //                bar = new ProgressBar(0, 100, 1, false, skin);
    //                bar.setValue(downloadedPercent);
    //                button = new ImageButton(skin);
    //                description = format("Latitude: %s°..%s°, Longitude: %s°..%s° (Downloading: %s %%)",
    //                                     chunk.lat, chunk.lat + 1,
    //                                     chunk.lon, chunk.lon + 1,
    //                                     downloadedPercent);
    //            } else {
    //                throw new IllegalArgumentException("Unexpected chunk: " + chunk);
    //            }
    //            button.addListener(new ChangeListener() {
    //
    //                @Override
    //                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
    //                    chunks.downloadChunk(chunk.lon, chunk.lat);
    //                    ResourcesPane.this.update();
    //                }
    //            });
    //            Label label = new Label(description, skin);
    //            bar.setHeight(100);
    //            container.add(button).size(50, 50);
    //            container.row();
    //        }
    //
    //        if (chunks.availableChunksFetchInProgress()) {
    //            container.add(new Label("Loading list of available chunks", skin)).height(50);
    //            container.row();
    //        }
    //
    //    }

    //    private boolean shouldUpdate(List<Chunk> newChunks) {
    //        if (this.container.getRows() != newChunks.size()) {
    //            return true;
    //        }
    //        Iterator<Chunk> i = this.currentChunks.iterator();
    //        for (Chunk newChunk : newChunks) {
    //            Chunk oldChunk = i.next();
    //            if (!oldChunk.equals(newChunk)) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        uiStage.act(delta);
        uiStage.draw();
    }

    private static void log(String message, Object... args) {
        Gdx.app.log("ResourcesPane", String.format(message, args));
    }

    private static class RowForChunk extends Table {

        protected final Chunk chunk;

        public RowForChunk(Chunk chunk, Skin skin) {
            super(skin);
            this.chunk = chunk;
        }
    }

    private class RowOwned extends RowForChunk {

        public RowOwned(Skin skin, Chunk chunk) {
            super(chunk, skin);
            Label lbl = new Label("Owned " + chunk.name, skin);
            TextButton btnDelete = new TextButton("Delete", skin);
            btnDelete.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    ResourcesPane.this.chunks.deleteChunk(RowOwned.super.chunk);
                }
            });

            this.add(lbl).pad(10).height(100).expandX().fill();
            this.add(btnDelete).width(200).fill();
        }
    }

    private class RowDownloading extends RowForChunk {

        private final ProgressBar progress;

        public RowDownloading(Skin skin, Chunk chunk, int percentDone) {
            super(chunk, skin);
            this.progress = new ProgressBar(0, 100, 1, false, skin);
            this.progress.setValue(percentDone);
            Label lbl = new Label("Downloading " + chunk.name, skin);
            TextButton btnCancel = new TextButton("Cancel", skin);
            btnCancel.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    ResourcesPane.this.chunks.cancelDonwloading(RowDownloading.super.chunk);
                }
            });

            this.stack(progress, lbl).pad(10).height(100).expandX().fill();
            this.add(btnCancel).width(200).fill();
        }
    }

    private class RowAvailable extends RowForChunk {

        public RowAvailable(Skin skin, Chunk chunk) {
            super(chunk, skin);
            Label lbl = new Label("Available " + chunk.name, skin);
            TextButton btnDownload = new TextButton("Download", skin);
            btnDownload.addListener(new ChangeListener() {

                @Override
                public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                    ResourcesPane.this.chunks.downloadChunk(RowAvailable.super.chunk);
                }
            });

            this.add(lbl).pad(10).height(100).expandX().fill();
            this.add(btnDownload).width(200).fill();
        }
    }

    private static class RowFetchInProgress extends Table {

        public RowFetchInProgress(Skin skin) {
            Label lbl = new Label("Downloading available chunks list...", skin);

            this.add(lbl).height(100).expandX().fill();
        }
    }

    private static final Map<Class<?>, Integer> TYPE_ORDER = new HashMap<Class<?>, Integer>();
    static {
        TYPE_ORDER.put(RowOwned.class, 0);
        TYPE_ORDER.put(RowDownloading.class, 1);
        TYPE_ORDER.put(RowAvailable.class, 2);
        TYPE_ORDER.put(RowFetchInProgress.class, 3);
    }

    private static class RowsComparator implements Comparator<Actor> {

        @Override
        public int compare(Actor cont1, Actor cont2) {
            Actor a1 = ((Container) cont1).getActor();
            Actor a2 = ((Container) cont2).getActor();
            int typeComparison = TYPE_ORDER.get(a1.getClass()) - TYPE_ORDER.get(a2.getClass());
            if (typeComparison != 0) {
                return typeComparison;
            }
            Chunk c1 = ((RowForChunk) a1).chunk;
            Chunk c2 = ((RowForChunk) a2).chunk;
            return c2.compareTo(c1); // ascending order
        }

    }
}
