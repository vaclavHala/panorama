package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ElevConfig;
import java.io.DataInputStream;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Loader {

    private static final String TAG = Loader.class.getSimpleName();

    private final ElevConfig elevCfg;
    private final Files files;

    public Loader(ElevConfig elevCfg, Files files) {
        this.elevCfg = elevCfg;
        this.files = files;
        //        this.chunkWidthDeg = chunkWidthDeg;
        //        this.chunkHeightDeg = chunkHeightDeg;
        //        this.dataWidthCells = dataWidthCells;
        //        this.dataHeightCells = dataHeightCells;
        //        this.cellWidthDeg = chunkWidthDeg / dataWidthCells;
        //        this.cellHeightDeg = chunkHeightDeg / dataHeightCells;
        //        Gdx.app.log(TAG, format("chunkWidthDeg=%f, chunkHeightDeg=%f, " +
        //                        "dataWidthCells=%d, dataHeightCells=%d, " +
        //                        "cellWidthDeg=%f, cellHeightDeg=%f",
        //                this.chunkWidthDeg, this.chunkHeightDeg,
        //                this.dataWidthCells, this.dataHeightCells,
        //                this.cellWidthDeg, this.cellHeightDeg));
    }

    /**
     * We dont want data from the whole file, just rectangle within the bounding box.
     * The algorithm goes like this:
     * - Skip the horizontal stripe above the bounding box, if any
     * - Around the box, skip from both sides if the box is in center
     * - Below the box again skip the horizontal stripe if any
     *
     * @return known points in the given box, each with associated elevation
     */
    public short[][] loadLandscape(int lon, int lat, int width, int height) throws IOException {

        Vector2[][] chunks = this.requiredChunks(elevCfg, lon, lat, width, height);
        DataInputStream[][] ins = new DataInputStream[chunks.length][chunks[0].length];

        for (int row = 0; row < chunks.length; row++) {
            for (int col = 0; row < chunks[0].length; col++) {
                int chunkLon = (int) chunks[col][row].x;
                int chunkLat = (int) chunks[col][row].y;
                String chunkName = format("chunk_%d_%d", chunkLon, chunkLat);
                ins[col][row] = new DataInputStream(this.files.internal(chunkName).read(BUFFER_SIZE));
            }
        }

        //        CollatedElevData stream = new CollatedElevData(ins, elevCfg);

        //        Landscape landscape = new Landscape(width, height);
        //
        //        int dataWidthCells = elevStream.chunksHorizontal() * elevCfg.chunkWidthCells;
        //        int dataHeightCells = elevStream.chunksVertical() * elevCfg.chunkHeightCells;
        //        int upperSkip = (elevStream.chunk0Lat() + dataHeightCells - (lat0 + height)) * dataWidthCells;
        //        int leftSkip = lon0 - elevStream.chunk0Lon();
        //        int rightSkip = dataWidthCells - leftSkip - width;
        //        //        int upperSkip = (int) (elevStream.lat0() / elevStream.cellHeightDeg() + elevStream.dataHeightCells() - (boundingBox.y + boundingBox.height) / elevStream.cellHeightDeg());
        //        //        int leftSkip = (int) ((boundingBox.x - elevStream.lon0()) / elevStream.cellWidthDeg());
        //        //        int rightSkip = (elevStream.dataWidthCells() - leftSkip - bbWidthCells);
        //        if (upperSkip < 0 || leftSkip < 0 || rightSkip < 0) {
        //            throw new IllegalStateException(format("Calculation blew up. "
        //                    + "upperSkip=%s, leftSkip=%s, rightSkip=%s", upperSkip, leftSkip, rightSkip));
        //        }
        //        Gdx.app.log(TAG, "dataWidth=" + dataWidthCells + ", dataHeight=" + dataHeightCells
        //                    + ", upperSkip=" + upperSkip + ", leftSkip=" + leftSkip + ", rightSkip=" + rightSkip);
        //
        //        elevStream.skip(upperSkip);
        //
        //        //have to iterate by row because that is how elev data is stored
        //        for (int lat = 0; lat < height; lat++) {
        //            elevStream.skip(leftSkip);
        //            for (int lon = 0; lon < width; lon++) {
        //                int elev = elevStream.readNext();
        //                landscape.setX(lon, lat, lon0 + lon);
        //                landscape.setY(lon, lat, elev);
        //                // we are reading chunk top to bottom (physically in file)
        //                // but in global coordinates 0 is at the bottom
        //                // so we reflect vertically here
        //                // - 1 because lat is <0, height-1>, we need cancel out height completely
        //                landscape.setZ(lon, lat, lat0 + height - lat - 1);
        //                // color will be filled in later;
        //            }
        //            elevStream.skip(rightSkip);
        //        }
        //
        //        Gdx.app.log(TAG, "Loaded elevation data and assembled surface, closing files");
        //        elevStream.close();
        //
        //        return landscape;
        return null;
    }

    private static final int BUFFER_SIZE = 4096;

    /**
     * @return chunks to be loaded
     * [] are rows top to bottom
     * [][] are chunks in given row
     */
    public static Vector2[][] requiredChunks(ElevConfig elevCfg, int lonFrom, int latFrom, int width, int height) {
        // - 1 because we want _To to point to the last index, not count of cells
        int lonTo = lonFrom + width - 1;
        int latTo = latFrom + height - 1;
        //truncates to left/bottom most chunk boundary
        int chunk0Lon = lonFrom / elevCfg.chunkWidthDeg * elevCfg.chunkWidthDeg;
        int chunk0Lat = latFrom / elevCfg.chunkHeightDeg * elevCfg.chunkHeightDeg;

        int chunkNLat = latTo / elevCfg.chunkHeightDeg * elevCfg.chunkHeightDeg;
        int chunkNLon = lonTo / elevCfg.chunkWidthDeg * elevCfg.chunkWidthDeg;

        int chunksHorizontal = (chunkNLon - chunk0Lon) / elevCfg.chunkWidthDeg + 1;
        int chunksVertical = (chunkNLat - chunk0Lat) / elevCfg.chunkHeightDeg + 1;
        Gdx.app.log(TAG, "chunks"
                         + " lon0=" + chunk0Lon + ", lat0=" + chunk0Lat
                         + ", horizontal=" + chunksHorizontal + ", vertical=" + chunksVertical);

        Vector2[][] chunks = new Vector2[chunksVertical][chunksHorizontal];
        for (int row = 0; row < chunksVertical; row++) {
            for (int col = 0; row < chunksHorizontal; col++) {
                chunks[row][col] = new Vector2(chunk0Lon + row * elevCfg.chunkWidthDeg,
                                               chunk0Lat + col * elevCfg.chunkHeightDeg);
            }
        }
        //        CollatedElevStream.Chunk[][] chunks = new CollatedElevStream.Chunk[chunksVertical][chunksHorizontal];
        //
        //        List<CollatedElevStream.Chunk> requiredChunksLog = new ArrayList<CollatedElevStream.Chunk>();
        //        for (int y = 0; y < chunksVertical; y++) {
        //            for (int x = 0; x < chunksHorizontal; x++) {
        //                int lon = chunk0Lon + x * elevCfg.chunkWidthCells;
        //                int lat = chunk0Lat + y * elevCfg.chunkHeightCells;
        //                CollatedElevStream.Chunk c = new CollatedElevStream.Chunk(lon, lat);
        //                //chunk with lowest lat has to be in lowermost row
        //                chunks[chunksVertical - y - 1][x] = c;
        //                requiredChunksLog.add(c);
        //            }
        //        }
        //
        //        Gdx.app.log(TAG, "required chunks=" + requiredChunksLog.toString());
        return chunks;
    }

    //    /**
    //     * @param globalBoundingBox original input to Loader, in global coordinates
    //     * @return area of chunk to read ([0,0] is lower left of chunk)
    //     */
    //    private Rectangle chunkBoundingBox(Rectangle globalBoundingBox, Chunk chunk) {
    //        float bbRight = globalBoundingBox.x + globalBoundingBox.width;
    //        float chunkRight = chunk.lon + chunkWidthDeg;
    //        float bbTop = globalBoundingBox.y + globalBoundingBox.height;
    //        float chunkTop = chunk.lat + chunkHeightDeg;
    //
    //        //cropped in global coordinates
    //        Rectangle containedBoundingBox = new Rectangle(
    //                Math.max(globalBoundingBox.x, chunk.lon),
    //                Math.max(globalBoundingBox.y, chunk.lat),
    //                bbRight < chunkRight ?
    //                        (globalBoundingBox.x > chunk.lon ?
    //                                globalBoundingBox.width :
    //                                bbRight - chunk.lon) :
    //                        chunkRight - globalBoundingBox.x,
    //                bbTop < chunkTop ?
    //                        (globalBoundingBox.y > chunk.lat ?
    //                                globalBoundingBox.height :
    //                                bbTop - chunk.lat) :
    //                        chunkTop - globalBoundingBox.y
    //        );
    //
    //        //same area as containedBoundingBox but in coordinates relative to the chunk
    //        // ([0,0] is lower left corner of the chunk)
    //        Rectangle normalizedContainerBoundingBox = new Rectangle(
    //                containedBoundingBox.x - chunk.lon,
    //                containedBoundingBox.y - chunk.lat,
    //                containedBoundingBox.width,
    //                containedBoundingBox.height
    //        );
    //
    //        Gdx.app.log(TAG, format("chunk=%s, boundingBoxCropped=%s, boundingBoxNormalized=%s",
    //                chunk, containedBoundingBox, normalizedContainerBoundingBox));
    //
    //        return normalizedContainerBoundingBox;
    //    }
    //    private Vector3 makeLandscapePoint(Chunk chunk, int lonCell, int latCell, int elev) {
    //        return new Vector3(
    //                chunk.lon + lonCell * cellWidthDeg,
    //                // we are reading chunk top to bottom (physically in file)
    //                // but in global coordinates 0 is at the bottom
    //                // so we reflect vertically here
    //                chunk.lat + (dataHeightCells - (latCell + 1)) * cellHeightDeg,
    //                elev);
    //    }
}
