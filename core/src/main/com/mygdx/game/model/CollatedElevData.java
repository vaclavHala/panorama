package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
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
public class CollatedElevData implements ElevData {

    private static final String TAG = CollatedElevData.class.getSimpleName();

    private final ElevData[] ins;

    private int row;
    private int col;

    private final int chunksHorizontal;
    private final int chunksVertical;
    private final int chunksWidthCells;
    private final int chunksHeightCells;

    private final int totWidthCells;
    private final int totHeightCells;

    /**
     * @param ins by row, owned and closed by this collated stream
     * @param elevCfg
     */
    public CollatedElevData(ElevData[] ins,
            int chunksHorizontal, int chunksVertical,
            int chunksWidthCells, int chunksHeightCells) {
        this.ins = ins;
        this.chunksHorizontal = chunksHorizontal;
        this.chunksVertical = chunksVertical;
        this.chunksWidthCells = chunksWidthCells;
        this.chunksHeightCells = chunksHeightCells;

        this.totWidthCells = chunksHorizontal * chunksWidthCells;
        this.totHeightCells = chunksVertical * chunksHeightCells;

        this.row = 0;
        this.col = 0;
    }

    //    public CollatedElevStream(Files files, ElevConfig elevCfg, int lon0, int lat0, int width, int height) throws IOException {
    //        this.elevCfg = elevCfg;
    //        Chunk[][] requiredChunks = this.requiredChunks(lon0, lat0, width, height);
    //
    //        ins = new DataInputStream[chunksVertical][chunksHorizontal];
    //        for (int row = 0; row < chunksVertical; row++) {
    //            for (int col = 0; col < chunksVertical; col++) {
    //                Chunk chunk = requiredChunks[row][col];
    //                try {
    //                    ins[row][col] = new DataInputStream(/*new GZIPInputStream(*/files.internal(chunk.filename()).read(BUFFER_SIZE));
    //                } catch (GdxRuntimeException e) {
    //                    throw new IOException(e);
    //                }
    //            }
    //        }
    //
    //        //        cycler = new ChunkFileCycler<DataInputStream>(ins,
    //        //                chunkWidthCells, chunkHeightCells,
    //        //                chunksCounts[0], chunksCounts[1]);
    //
    //        this.at = 0;
    //    }

    @Override
    public short next() {
        if (this.row == this.totHeightCells) {
            throw new IllegalArgumentException("End of stream already reached");
        }

        int streamCol = this.col / this.chunksWidthCells;
        int streamRow = this.row / this.chunksHeightCells;
        short elev = this.ins[streamRow * this.chunksHorizontal + streamCol].next();

        this.col++;
        if (this.col == this.totWidthCells) {
            this.col = 0;
            this.row++;
        }
        return elev;
    }

    public void skip(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            this.next();
        }
        //        if (count == 0)
        //            return;
        //        if (count < 0)
        //            throw new IllegalStateException("count=" + count);
        //        //        //each short is two bytes
        //        int globLeft = count;
        //        while (globLeft > 0) {
        //            int locLeft = Math.min(globLeft, leftBeforeSwap());
        //            // short is 2 bytes
        //            int left = locLeft * 2;
        //            globLeft -= locLeft;
        //            while (left > 0) {
        //                left -= stream().skip(left);
        //            }
        //            move(locLeft);
        //        }
    }

    /**
     * Close all open files.
     */
    @Override
    public void close() {
        //        for (InputStream[] is : ins)
        //            for (InputStream i : is) {
        //                try {
        //                    i.close();
        //                } catch (IOException e) {
        //                    Gdx.app.log(TAG, "Error closing file: " + e.getMessage());
        //                }
        //            }
    }

    //    /**
    //     * Returns how many more cells (shorts) can be read before
    //     * next underlying stream will be used
    //     */
    //    int leftBeforeSwap() {
    //        int thisCol = at % elevCfg.chunkWidthCells;
    //        return (elevCfg.chunkWidthCells - thisCol);
    //    }

    //    /**
    //     * lon lat are in global coordinates, they represent lower left corner of the chunk
    //     */
    //    static class Chunk {
    //
    //        final int lon0Cells;
    //        final int lat0Cells;
    //
    //        public Chunk(int lon, int lat) {
    //            this.lon0Cells = lon;
    //            this.lat0Cells = lat;
    //        }
    //
    //        /**
    //         * Data files are all nxn degree squares.
    //         * filenames are in the form elev_'lon'_'lat'
    //         * where lon and lat are indices of bottom left cell in the file
    //         */
    //        public String filename() {
    //            return format("elev_%d_%d", lon0Cells, lat0Cells);
    //        }
    //
    //        @Override
    //        public String toString() {
    //            return "Chunk{" +
    //                   "lon=" + lon0Cells +
    //                   ", lat=" + lat0Cells +
    //                   '}';
    //        }
    //    }

}
