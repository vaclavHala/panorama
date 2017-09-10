package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import static com.mygdx.game.common.ChunkNamingScheme.chunkFileName;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import static java.lang.Integer.parseInt;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chunks {

    public static final Pattern NAME_READ_PATTERN = Pattern.compile("([ns])([\\d]+)_([ew])([\\d]+)\\.chunk");
    private static final String CHUNKS_ROOT = "chunks/";

    private static final String CHUNK_SOURCE_HOST = "localhost";
    private static final int CHUNK_SOURCE_PORT = 8000;

    private final Executor netPool;

    private final List<AvailableChunk> cachedAvailableChunks;
    private final FileHandle chunksRoot;

    private final Map<AvailableChunk, Integer> downloadingChunks;

    private boolean fetchInProgress;

    public Chunks(Executor netPool) {
        this.netPool = netPool;
        this.chunksRoot = Gdx.files.external(CHUNKS_ROOT);
        this.chunksRoot.mkdirs();
        if (!this.chunksRoot.isDirectory()) {
            throw new IllegalArgumentException("Chunks root is not available");
        }
        this.cachedAvailableChunks = new ArrayList<AvailableChunk>();
        this.downloadingChunks = new HashMap<AvailableChunk, Integer>();

        this.fetchInProgress = false;
    }

    public void fetchAvailableChunks() {
        log("Starting available chunks fetch");
        this.fetchInProgress = true;
        this.netPool.execute(new AvailableChunksHandler(this, CHUNK_SOURCE_HOST, CHUNK_SOURCE_PORT));
    }

    public boolean availableChunksFetchInProgress() {
        return this.fetchInProgress;
    }

    void availableChunksResult(List<AvailableChunk> chunks) {
        this.cachedAvailableChunks.clear();
        this.cachedAvailableChunks.addAll(chunks);
        this.fetchInProgress = false;
    }

    public List<Chunk> chunkList() {

        List<Chunk> chunks = new ArrayList<Chunk>();
        chunks.addAll(this.cachedAvailableChunks);
        FileHandle[] downloadedFiles = chunksRoot.list("chunk");
        List<String> chunkFiles = new ArrayList<String>();
        List<String> unexpectedFiles = new ArrayList<String>();

        for (FileHandle file : downloadedFiles) {
            try {
                Matcher m = NAME_READ_PATTERN.matcher(file.name());
                m.find();
                int lat = parseInt(m.group(2));
                if (m.group(1).equals("s")) {
                    lat *= -1;
                }
                int lon = parseInt(m.group(4));
                if (m.group(3).equals("w")) {
                    lon *= -1;
                }
                boolean wasPresent = chunks.remove(new AvailableChunk(lat, lon));
                chunks.add(new OwnedChunk(lat, lon));
                chunkFiles.add(file.name());
            } catch (Exception e) {
                log("Unrecognized file <%s>: %s", file.name(), formatException(e));
                unexpectedFiles.add(file.name());
            }
        }
        log("Chunk files: %s, Unrecognized files: %s", chunkFiles, unexpectedFiles);
        for (Entry<AvailableChunk, Integer> e : downloadingChunks.entrySet()) {
            int lat = e.getKey().lat;
            int lon = e.getKey().lon;
            int progressPercent = e.getValue();
            boolean wasPresent = chunks.remove(e.getKey());
            if (!wasPresent) {

            }
            chunks.add(new ObtainingChunk(lat, lon, progressPercent));
        }

        return chunks;
    }

    public void downloadChunk(int lon, int lat) {
        AvailableChunk toDownload = new AvailableChunk(lat, lon);
        if (!cachedAvailableChunks.contains(toDownload)) {
            log("Trying to download chunk which may not be available: lon=%s, lat=%s", lon, lat);
        }
        if (downloadingChunks.containsKey(toDownload)) {
            log("Already downloading this chunk: lon=%s, lat=%s", lon, lat);
            return;
        }
        String chunkFilename = chunkFileName(lon, lat);

        this.downloadingChunks.put(toDownload, 0);

        FileHandle fileTmp = chunksRoot.child(chunkFilename + ".tmp");
        FileHandle fileFinal = chunksRoot.child(chunkFilename);
        log("Downloading lon=%s, lat=%s to %s", lon, lat, fileFinal);
        this.netPool.execute(new ChunkDataHandler(fileTmp, fileFinal,
                                                  new ChunkDataDownloadProgressListener(toDownload),
                                                  CHUNK_SOURCE_HOST, CHUNK_SOURCE_PORT,
                                                  lat, lon));
    }

    public static void log(String message, Object... args) {
        Gdx.app.log("pano.chunks", String.format(message, args));
    }

    public static abstract class Chunk {

        public final int lat;
        public final int lon;

        public Chunk(int lat, int lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static class OwnedChunk extends Chunk {

        public OwnedChunk(int lat, int lon) {
            super(lat, lon);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof OwnedChunk)) {
                return false;
            }
            OwnedChunk other = (OwnedChunk) obj;
            return this.lat == other.lat && this.lon == other.lon;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.lon, this.lat);
        }

        @Override
        public String toString() {
            return "OwnedChunk{" + "lat=" + lat + ", lon=" + lon + '}';
        }
    }

    public static class AvailableChunk extends Chunk {

        public AvailableChunk(int lat, int lon) {
            super(lat, lon);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AvailableChunk)) {
                return false;
            }
            AvailableChunk other = (AvailableChunk) obj;
            return this.lat == other.lat && this.lon == other.lon;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.lon, this.lat);
        }

        @Override
        public String toString() {
            return "AvailableChunk{" + "lat=" + lat + ", lon=" + lon + '}';
        }

    }

    public static class ObtainingChunk extends Chunk {

        public final int progressPercent;

        public ObtainingChunk(int lat, int lon, int progressPercent) {
            super(lat, lon);
            this.progressPercent = progressPercent;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ObtainingChunk)) {
                return false;
            }
            ObtainingChunk other = (ObtainingChunk) obj;
            return this.lat == other.lat && this.lon == other.lon && this.progressPercent == other.progressPercent;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.lon, this.lat, this.progressPercent);
        }

        @Override
        public String toString() {
            return "ObtainingChunk{" + "lat=" + lat + ", lon=" + lon + ", progress=" + progressPercent + '}';
        }
    }

    private class ChunkDataDownloadProgressListener implements ProgressListener {

        private final AvailableChunk targetChunk;

        public ChunkDataDownloadProgressListener(AvailableChunk targetChunk) {
            this.targetChunk = targetChunk;
        }

        @Override
        public void success() {
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    downloadingChunks.remove(targetChunk);
                }
            });
        }

        @Override
        public void fail() {
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    downloadingChunks.remove(targetChunk);
                }
            });
        }

        @Override
        public void update(final int percentDone) {
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    downloadingChunks.put(targetChunk, percentDone);
                }
            });
        }
    }

}
