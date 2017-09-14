package com.mygdx.game.ui;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.I18NBundle;
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

    private static final Comparator<Actor> COMPARATOR = new RowsComparator();

    private final Conductor conductor;
    private final Terraformer terraformer;
    private final Stage uiStage;
    private final ChunksService chunks;
    private final Skin skin;
    private final I18NBundle i18n;
    private final Actor parent;
    private final VerticalGroup chunkContainer;
    private final Map<Actor, Container<?>> rowToContainer;

    public ResourcesPane(
            Conductor conductor, Terraformer terraformer,
            Skin skin, I18NBundle i18n, Stage uiStage,
            ChunksService chunks) {
        this.conductor = conductor;
        this.terraformer = terraformer;
        this.chunks = chunks;
        this.skin = skin;
        this.i18n = i18n;
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
        for (Chunk chunk : this.chunks.ownedChunks()) {
            this.addRow(new RowOwned(chunk));
        }
        for (Chunk chunk : this.chunks.availableChunks()) {
            this.addRow(new RowAvailable(chunk));
        }
        for (Entry<Chunk, Integer> chunkWithProgress : this.chunks.downloadingChunks()) {
            this.addRow(new RowDownloading(chunkWithProgress.getKey(), chunkWithProgress.getValue()));
        }
        if (this.chunks.availableChunksFetchInProgress()) {
            this.addRow(new RowFetchInProgress(skin));
        }

        this.sortChildren();
        uiStage.addActor(parent);
        chunks.addListener(this);

        Gdx.input.setInputProcessor(uiStage);
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
                    this.addRow(new RowOwned(chunk));
                } else if (state.equals(ChunkState.DOWNLOADING)) {
                    this.addRow(new RowDownloading(chunk, 0));
                } else if (state.equals(ChunkState.AVAILABLE)) {
                    this.addRow(new RowAvailable(chunk));
                } else {
                    throw new AssertionError(state);
                }
            } else if (e instanceof ChunkEventDownloadProgress) {
                ChunkEventDownloadProgress eDown = (ChunkEventDownloadProgress) e;
                RowDownloading row = (RowDownloading) this.rowFor(eDown.chunk);
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

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        uiStage.act(delta);
        uiStage.draw();
    }

    private static void log(String message, Object... args) {
        Gdx.app.log("pano.pane.res", String.format(message, args));
    }

    private static class RowForChunk extends Table {

        protected final Chunk chunk;

        public RowForChunk(Chunk chunk, Skin skin) {
            super(skin);
            this.chunk = chunk;
        }
    }

    private class RowOwned extends RowForChunk {

        public RowOwned(Chunk chunk) {
            super(chunk, skin);
            Label lbl = new Label(i18n.format("pane.res.row.owned", chunk.name), skin);
            TextButton btnDelete = new TextButton(i18n.format("pane.res.row.btn.delete"), skin);
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

        public RowDownloading(Chunk chunk, int percentDone) {
            super(chunk, skin);
            this.progress = new ProgressBar(0, 100, 1, false, skin);
            this.progress.setValue(percentDone);
            Label lbl = new Label(i18n.format("pane.res.row.downloading", chunk.name), skin);
            TextButton btnCancel = new TextButton(i18n.format("pane.res.row.btn.cancel"), skin);
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

        public RowAvailable(Chunk chunk) {
            super(chunk, skin);
            Label lbl = new Label(i18n.format("pane.res.row.available", chunk.name), skin);
            TextButton btnDownload = new TextButton(i18n.format("pane.res.row.btn.download"), skin);
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

    private class RowFetchInProgress extends Table {

        public RowFetchInProgress(Skin skin) {
            Label lbl = new Label(i18n.format("pane.res.row.meta"), skin);

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
