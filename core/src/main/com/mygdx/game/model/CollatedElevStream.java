package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.DataInput;
import com.mygdx.game.ElevConfig;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import static java.lang.String.format;

/**
 * Given desired bounding box to read opens all elev files required
 * to obtain data for the box. Elevation points are read sequentially
 * top left to bottom right by row
 */
public class CollatedElevStream {

    private static final String TAG = CollatedElevStream.class.getSimpleName();
    private static final int BUFFER_SIZE = 4096;

    private final ElevConfig elevCfg;
    private final DataInputStream[][] ins;

    private int at;
    private int chunksHorizontal;
    private int chunksVertical;
    private int chunk0Lon;
    private int chunk0Lat;

    public CollatedElevStream(Files files, ElevConfig elevCfg, int lon0, int lat0, int width, int height) throws IOException {
        this.elevCfg = elevCfg;
        Chunk[][] requiredChunks = this.requiredChunks(lon0, lat0, width, height);

        ins = new DataInputStream[chunksVertical][chunksHorizontal];
        for (int row = 0; row < chunksVertical; row++) {
            for (int col = 0; col < chunksVertical; col++) {
                Chunk chunk = requiredChunks[row][col];
                try {
                    ins[row][col] = new DataInputStream(/*new GZIPInputStream(*/files.internal(chunk.filename()).read(BUFFER_SIZE));
                } catch (GdxRuntimeException e){
                    throw new IOException(e);
                }
            }
        }

//        cycler = new ChunkFileCycler<DataInputStream>(ins,
//                chunkWidthCells, chunkHeightCells,
//                chunksCounts[0], chunksCounts[1]);

        this.at = 0;
    }

    public int chunksHorizontal() {
        return chunksHorizontal;
    }

    public int chunksVertical() {
        return chunksVertical;
    }

    public int chunk0Lon() {
        return chunk0Lon;
    }

    public int chunk0Lat() {
        return chunk0Lat;
    }

    public short readNext() throws IOException {
        short out = stream().readShort();
        move(1);
        return out;
    }

    public void skip(int count) throws IOException {
        if (count == 0) return;
        if (count < 0) throw new IllegalStateException("count=" + count);
//        //each short is two bytes
        int globLeft = count;
        while (globLeft > 0) {
            int locLeft = Math.min(globLeft, leftBeforeSwap());
            // short is 2 bytes
            int left = locLeft * 2;
            globLeft -= locLeft;
            while (left > 0) {
                left -= stream().skip(left);
            }
            move(locLeft);
        }
    }

    /**
     * Close all open files.
     */
    public void close() {
        for (InputStream[] is : ins)
            for (InputStream i : is) {
                try {
                    i.close();
                } catch (IOException e) {
                    Gdx.app.log(TAG, "Error closing file: " + e.getMessage());
                }
            }
    }

    /**
     * @return chunks to be loaded
     * [] are rows top to bottom
     * [][] are chunks in given row
     */
    Chunk[][] requiredChunks(int lonFrom, int latFrom, int width, int height) {
        // - 1 because we want _To to point to the last index, not count of cells
        int lonTo = lonFrom + width - 1;
        int latTo = latFrom + height - 1;
        //truncates to left/bottom most chunk boundary
        this.chunk0Lon = lonFrom / elevCfg.chunkWidthCells * elevCfg.chunkWidthCells;
        this.chunk0Lat = latFrom / elevCfg.chunkHeightCells * elevCfg.chunkHeightCells;

        int chunkNLat = latTo / elevCfg.chunkHeightCells * elevCfg.chunkHeightCells;
        int chunkNLon = lonTo / elevCfg.chunkWidthCells * elevCfg.chunkWidthCells;

        this.chunksHorizontal = (chunkNLon - chunk0Lon) / elevCfg.chunkWidthCells + 1;
        this.chunksVertical = (chunkNLat - chunk0Lat) / elevCfg.chunkHeightCells + 1;
        Gdx.app.log(TAG, "chunks" +
                " lon0=" + chunk0Lon + ", lat0=" + chunk0Lat +
                ", horizontal=" + chunksHorizontal + ", vertical=" + chunksVertical);
        Chunk[][] chunks = new Chunk[chunksVertical][chunksHorizontal];

        List<Chunk> requiredChunksLog = new ArrayList<Chunk>();
        for (int y = 0; y < chunksVertical; y++) {
            for (int x = 0; x < chunksHorizontal; x++) {
                int lon = chunk0Lon + x * elevCfg.chunkWidthCells;
                int lat = chunk0Lat + y * elevCfg.chunkHeightCells;
                Chunk c = new Chunk(lon, lat);
                //chunk with lowest lat has to be in lowermost row
                chunks[chunksVertical-y-1][x] = c;
                requiredChunksLog.add(c);
            }
        }

        Gdx.app.log(TAG, "required chunks=" + requiredChunksLog.toString());
        return chunks;
    }

    DataInputStream stream() {
        int row = at / (chunksHorizontal * elevCfg.chunkWidthCells * elevCfg.chunkHeightCells);
        int col = (at % (chunksHorizontal * elevCfg.chunkWidthCells)) / elevCfg.chunkWidthCells;
        return ins[row][col];
    }

    /**
     * Behaves as if next() was called i times, without returning the result
     */
    void move(int i) {
        at += i;
    }

    /**
     * Returns how many more cells (shorts) can be read before
     * next underlying stream will be used
     */
    int leftBeforeSwap() {
        int thisCol = at % elevCfg.chunkWidthCells;
        return (elevCfg.chunkWidthCells - thisCol);
    }

    /**
     * lon lat are in global coordinates, they represent lower left corner of the chunk
     */
    static class Chunk {

        final int lon0Cells;
        final int lat0Cells;

        public Chunk(int lon, int lat) {
            this.lon0Cells = lon;
            this.lat0Cells = lat;
        }

        /**
         * Data files are all nxn degree squares.
         * filenames are in the form elev_'lon'_'lat'
         * where lon and lat are indices of bottom left cell in the file
         */
        public String filename() {
            return format("elev_%d_%d", lon0Cells, lat0Cells);
        }

        @Override
        public String toString() {
            return "Chunk{" +
                    "lon=" + lon0Cells +
                    ", lat=" + lat0Cells +
                    '}';
        }
    }

    static class ChunkCounter {



    }

}
