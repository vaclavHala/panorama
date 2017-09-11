package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import static com.mygdx.game.Asserts.onUI;
import com.mygdx.game.AvailableChunksHandler.AvailableChunksListener;
import com.mygdx.game.ChunkDataHandler.ChunkDataListener;
import static com.mygdx.game.common.Helpers.entry;
import com.mygdx.game.model.Chunk;
import java.util.*;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * Presents following abstracted view over chunks:
 * chunk is either known (stored locally, or information about its existence is obtained in metadata),
 * or unknown (neither of the previous has happened yet)
 * if chunk is known, it is in exactly one state (one of OWNED, AVAILABLE, DOWNLOADING)
 * once chunk becomes known (in scope of app run) it can never
 * become unknown again (metadata fetch can only add chunkns, never remove)
 *
 * Listener events (all sent to UI thred):
 * metadata fetch started / ended
 * new owned/available/downloaded chunk
 * remove owned/available/downloaded chunk
 * downloading chunk progress changed
 *
 * Queries:
 * owned/available/downloading(with percent done) chunks
 * metadata fetch in progress
 *
 * NOTE:
 * all the state checks in control methods are required as event though
 * all interaction with this service happens on single thread it is done
 * through events which may arrive in any (also mixed up) order, especially
 * if user clicks very fast, device is very busy etc.
 */
public class ChunksService implements AvailableChunksListener, ChunkDataListener {

    private static final String CHUNKS_ROOT = "chunks/";

    private static final String CHUNK_SOURCE_HOST = "localhost";
    private static final int CHUNK_SOURCE_PORT = 8000;

    private final Preferences cachedAvailable;
    private final Executor netPool;
    private final FileHandle chunksRoot;
    private boolean fetchInProgress;

    private final List<ChunksServiceListener> listeners;
    private final Map<Chunk, ChunkState> chunks;
    private final Map<Chunk, Entry<Integer, ChunkDataHandler>> downloadingExtra;

    public ChunksService(Executor netPool) {
        assert onUI();

        this.cachedAvailable = Gdx.app.getPreferences("panorama/available_chunks");
        this.netPool = netPool;
        this.chunksRoot = Gdx.files.external(CHUNKS_ROOT);
        this.chunksRoot.mkdirs();
        if (!this.chunksRoot.isDirectory()) {
            throw new IllegalArgumentException("Chunks root is not available");
        }
        this.chunks = new HashMap<Chunk, ChunkState>();
        this.downloadingExtra = new HashMap<Chunk, Entry<Integer, ChunkDataHandler>>();
        this.listeners = new CopyOnWriteArrayList<ChunksServiceListener>();
        this.fetchInProgress = false;
    }

    /**
     * Listeners may be added prior to calling this to obtain whole initial state as events
     */
    public void start() {
        assert onUI();

        List<ChunkEvent> events = new ArrayList<ChunkEvent>();

        // TODO some button to do this by user
        // or always download metadata version (which is cheap - one number) and update only if newer
        this.fetchAvailableChunks(); // FIXME for devel run fetch always
        if (this.cachedAvailable.get().isEmpty()) {
            log("No available cached chunks found, starting fetch");
            // this.fetchAvailableChunks();
        } else {
            log("Found cached available chunks: %s", this.cachedAvailable.get().keySet());
            for (String chunkName : this.cachedAvailable.get().keySet()) {
                Chunk chunk = Chunk.fromName(chunkName);
                this.chunks.put(chunk, ChunkState.AVAILABLE);
                events.add(new ChunkEventStateUpdate(chunk, ChunkState.AVAILABLE));
            }
        }

        List<Chunk> ownedChunks = new ArrayList<Chunk>();
        FileHandle[] ownedFiles = chunksRoot.list("chunk");
        for (FileHandle file : ownedFiles) {
            try {
                Chunk chunk = Chunk.fromName(file.name());
                ownedChunks.add(chunk);
                this.chunks.put(chunk, ChunkState.OWNED);
            } catch (Exception e) {
                log("Unrecognized file: %s", file.name());
            }
        }
        log("Owned chunks: %s", ownedChunks);

        this.postEvents(events);
    }

    public List<Entry<Chunk, ChunkState>> allChunks() {
        assert onUI();

        return unmodifiableList(new ArrayList<Entry<Chunk, ChunkState>>(this.chunks.entrySet()));
    }

    public List<Chunk> ownedChunks() {
        assert onUI();

        List<Chunk> ownedChunks = new ArrayList<Chunk>();
        for (Entry<Chunk, ChunkState> e : this.chunks.entrySet()) {
            if (e.getValue().equals(ChunkState.OWNED)) {
                ownedChunks.add(e.getKey());
            }
        }
        return unmodifiableList(ownedChunks);
    }

    public List<Chunk> availableChunks() {
        assert onUI();

        List<Chunk> availableChunks = new ArrayList<Chunk>();
        for (Entry<Chunk, ChunkState> e : this.chunks.entrySet()) {
            if (e.getValue().equals(ChunkState.AVAILABLE)) {
                availableChunks.add(e.getKey());
            }
        }
        return unmodifiableList(availableChunks);
    }

    public List<Entry<Chunk, Integer>> downloadingChunks() {
        assert onUI();

        List<Entry<Chunk, Integer>> downloadingChunks = new ArrayList<Entry<Chunk, Integer>>();
        for (Entry<Chunk, ChunkState> e : this.chunks.entrySet()) {
            if (e.getValue().equals(ChunkState.DOWNLOADING)) {
                int progress = this.downloadingExtra.get(e.getKey()).getKey();
                downloadingChunks.add(new AbstractMap.SimpleEntry<Chunk, Integer>(e.getKey(), progress));
            }
        }
        return unmodifiableList(downloadingChunks);
    }

    public boolean availableChunksFetchInProgress() {
        assert onUI();

        return this.fetchInProgress;
    }

    public void fetchAvailableChunks() {
        assert onUI();

        log("Starting available chunks fetch");
        this.fetchInProgress = true;
        this.postEvents(singletonList(new ChunkEventMetadataFetch(true)));
        this.netPool.execute(new AvailableChunksHandler(this, CHUNK_SOURCE_HOST, CHUNK_SOURCE_PORT));
    }

    public void downloadChunk(Chunk chunk) {
        assert onUI();

        ChunkState currentState = this.chunks.get(chunk);
        if (currentState != ChunkState.AVAILABLE) {
            log("Trying to download chunk which is not available: %s (%s)", chunk, currentState);
            return;
        }

        this.chunks.put(chunk, ChunkState.DOWNLOADING);

        FileHandle fileTmp = chunksRoot.child(chunk.name + ".tmp");
        FileHandle fileFinal = chunksRoot.child(chunk.name);
        log("Starting download of:%s to: %s", chunk, fileFinal);
        ChunkDataHandler handler = new ChunkDataHandler(fileTmp, fileFinal, this,
                                                        CHUNK_SOURCE_HOST, CHUNK_SOURCE_PORT, chunk);
        this.downloadingExtra.put(chunk, entry(0, handler));
        this.netPool.execute(handler);
        this.postEvents(singletonList(new ChunkEventStateUpdate(chunk, ChunkState.DOWNLOADING)));
    }

    public void cancelDonwloading(Chunk chunk) {
        assert onUI();

        ChunkState currentState = this.chunks.get(chunk);
        if (currentState != ChunkState.DOWNLOADING) {
            log("Not downloading chunk: %s (%s)", chunk, currentState);
            return;
        }

        log("Canceling download of: %s", chunk);
        this.chunks.put(chunk, ChunkState.AVAILABLE);
        Entry<Integer, ChunkDataHandler> extra = this.downloadingExtra.remove(chunk);
        extra.getValue().cancel();
        this.postEvents(singletonList(new ChunkEventStateUpdate(chunk, ChunkState.AVAILABLE)));
    }

    public void deleteChunk(Chunk chunk) {
        assert onUI();

        ChunkState currentState = this.chunks.get(chunk);
        if (currentState != ChunkState.OWNED) {
            log("Do not own chunk: %s (%s)", chunk, currentState);
            return;
        }

        log("Deleting: %s", chunk);
        boolean success = chunksRoot.child(chunk.name).delete();
        if (!success) {
            log("Failed to delete %s", chunk);
        }
        this.chunks.put(chunk, ChunkState.AVAILABLE);
        this.postEvents(singletonList(new ChunkEventStateUpdate(chunk, ChunkState.AVAILABLE)));
    }

    public void addListener(ChunksServiceListener listener) {
        assert onUI();

        this.listeners.add(listener);
    }

    public void removeListener(ChunksServiceListener listener) {
        assert onUI();

        boolean wasPresent = this.listeners.remove(listener);
        if (!wasPresent) {
            throw new IllegalStateException("Trying to remove listener not previously registered: " + listener);
        }
    }

    @Override
    public void availableFetchDone(List<Chunk> availableChunks) {
        assert onUI();

        log("Meta fetch done: %s", availableChunks);
        this.fetchInProgress = false;
        List<ChunkEvent> events = new ArrayList<ChunkEvent>();
        for (Chunk chunk : availableChunks) {
            ChunkState currentState = this.chunks.get(chunk);
            this.cachedAvailable.putString(chunk.name, "");
            if (currentState == null) {
                this.chunks.put(chunk, ChunkState.AVAILABLE);
                events.add(new ChunkEventStateUpdate(chunk, ChunkState.AVAILABLE));
            }
        }

        log("Persisting new cachedAvailable chunks: %s", cachedAvailable);
        this.cachedAvailable.flush();
        events.add(new ChunkEventMetadataFetch(false));
        this.postEvents(events);
    }

    @Override
    public void availableFetchFailed() {
        assert onUI();

        log("Meta fetch failed");
        this.fetchInProgress = false;
        this.postEvents(singletonList(new ChunkEventMetadataFetch(false)));
    }

    @Override
    public void chunkDataProgress(Chunk chunk, int percent) {
        assert onUI();

        Entry<Integer, ChunkDataHandler> prevPair = this.downloadingExtra.get(chunk);
        if (prevPair == null) {
            // this may happen if the progress update event is already enqueued as the download is cancelled
            log("Dropping progress update on chunk: %s", chunk);
            return;
        }
        if (prevPair.getKey().equals(percent)) {
            return;
        }
        Entry<Integer, ChunkDataHandler> newPair = entry(percent, prevPair.getValue());
        this.downloadingExtra.put(chunk, newPair);
        this.postEvents(singletonList(new ChunkEventDownloadProgress(chunk, percent)));
    }

    @Override
    public void chunkDataDone(Chunk chunk) {
        assert onUI();

        log("Chunk download done: %s", chunk);
        this.downloadingExtra.remove(chunk);
        this.chunks.put(chunk, ChunkState.OWNED);
        this.postEvents(singletonList(new ChunkEventStateUpdate(chunk, ChunkState.OWNED)));
    }

    @Override
    public void chunkDataFail(Chunk chunk) {
        assert onUI();

        log("Chunk download failed: %s", chunk);
        this.downloadingExtra.remove(chunk);
        this.chunks.put(chunk, ChunkState.AVAILABLE);
        this.postEvents(singletonList(new ChunkEventStateUpdate(chunk, ChunkState.AVAILABLE)));
    }

    private void postEvents(List<? extends ChunkEvent> eventsBatch) {
        assert onUI();

        List<ChunkEvent> unmodEventsBatch = unmodifiableList(eventsBatch);
        for (ChunksServiceListener listener : this.listeners) {
            listener.receive(unmodEventsBatch);
        }
    }

    public static void log(String message, Object... args) {
        Gdx.app.log("pano.chunks", String.format(message, args));
    }

    public interface ChunksServiceListener {

        void receive(List<ChunkEvent> events);
    }

    public static class ChunkEvent {}

    public static class ChunkEventMetadataFetch extends ChunkEvent {

        public final boolean inProgress;

        public ChunkEventMetadataFetch(boolean inProgress) {
            this.inProgress = inProgress;
        }
    }

    public static class ChunkEventDownloadProgress extends ChunkEvent {

        public final Chunk chunk;
        public final int percentDone;

        public ChunkEventDownloadProgress(Chunk chunk, int percentDone) {
            this.percentDone = percentDone;
            this.chunk = chunk;
        }
    }

    public static class ChunkEventStateUpdate extends ChunkEvent {

        public final Chunk chunk;
        public final ChunkState state;

        public ChunkEventStateUpdate(Chunk chunk, ChunkState state) {
            this.chunk = chunk;
            this.state = state;
        }
    }

    public enum ChunkState {
        OWNED,
        DOWNLOADING,
        AVAILABLE
    }
}
