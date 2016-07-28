package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import static java.lang.String.format;

public class Loader {

    private static final String TAG = Loader.class.getSimpleName();
    private static final int BUFFER_SIZE = 4096;

    private final float chunkWidthDeg;
    private final float chunkHeightDeg;
    private final int chunkWidthCells;
    private final int chunkHeightCells;
    private final float cellWidthDeg;
    private final float cellHeightDeg;

    private final Files files;

    public Loader(Files files, float fileWidthDeg, float chunkHeightDeg, int chunkWidthCells, int chunkHeightCells) {
        this.files = files;
        this.chunkWidthDeg = fileWidthDeg;
        this.chunkHeightDeg = chunkHeightDeg;
        this.chunkWidthCells = chunkWidthCells;
        this.chunkHeightCells = chunkHeightCells;
        this.cellWidthDeg = fileWidthDeg / chunkWidthCells;
        this.cellHeightDeg = chunkHeightDeg / chunkHeightCells;
        Gdx.app.log(TAG, format("chunkWidthDeg=%f, chunkHeightDeg=%f, " +
                        "chunkWidthCells=%d, chunkHeightCells=%d, " +
                        "cellWidthDeg=%f, cellHeightDeg=%f",
                this.chunkWidthDeg, this.chunkHeightDeg,
                this.chunkWidthCells, this.chunkHeightCells,
                this.cellWidthDeg, this.cellHeightDeg));
    }

    public void load() {

    }

    /**
     * @param boundingBox x = lon ,y = lat, extent in degrees
     * @return known points in the given box, each with associated elevation
     */
    public Array<Vector3> loadLandscape(Rectangle boundingBox) throws IOException {
        Gdx.app.log(TAG, "Loading landscape for box=" + boundingBox);
        Array<Vector3> landscape = new Array<Vector3>();
        for (Chunk chunk : requiredChunks(boundingBox)) {
            readChunk(chunk, boundingBox, landscape);
        }
        return landscape;
    }

    static Array<Chunk> requiredChunks(Rectangle boundingBox) {
        Array<Chunk> chunks = new Array<Chunk>();
        int lonFrom = (int) boundingBox.x;
        int lonTo = (int) (boundingBox.x + boundingBox.width);
        int latFrom = (int) boundingBox.y;
        int latTo = (int) (boundingBox.y + boundingBox.height);

        for (int lon = lonFrom; lon <= lonTo; lon++) {
            for (int lat = latFrom; lat <= latTo; lat++) {
                chunks.add(new Chunk(lon, lat));
            }
        }

        Gdx.app.log(TAG, "required chunks=" + chunks);
        return chunks;
    }

    /**
     * We dont want data from the whole file, just rectangle within the bounding box.
     * The algorithm goes like this:
     * - Skip the horizontal stripe above the bounding box, if any
     * - Around the box, skip from both sides if the box is in center
     * - Below the box again skip the horizontal stripe if nay
     *
     * @param boundingBox original input, this is in global coordinates
     *                    (lon lat [0,0] is in the Atlantic near Africa)
     * @param landscape   read data (within bounding box) is appended here
     */
    void readChunk(Chunk chunk,
                   Rectangle boundingBox,
                   Array<Vector3> landscape) throws IOException {
        int cellsBefore = landscape.size;

        String filename = chunk.filename();
        Gdx.app.log(TAG, "Loading file " + filename);
//        InputStream elevData = new GZIPInputStream(this.files.internal(filename).read(BUFFER_SIZE));
        InputStream elevData = this.files.internal(filename).read(BUFFER_SIZE);
        try {
            Rectangle chunkBoundingBox = this.chunkBoundingBox(boundingBox, chunk);

            // + (cellWidthDeg / 2f) mitigates rounding errors (most of the time anyway ... )
            int bbWidthCells = (int) ((chunkBoundingBox.width + (cellWidthDeg / 2f)) / cellWidthDeg);
            int bbHeightCells = (int) ((chunkBoundingBox.height + (cellWidthDeg / 2f)) / cellHeightDeg);

            int upperSkip = (int) ((chunkHeightDeg - (chunkBoundingBox.y + chunkBoundingBox.height)) / cellHeightDeg);
            int leftSkip = (int) (chunkBoundingBox.x / cellWidthDeg);
            int rightSkip = chunkWidthCells - leftSkip - bbWidthCells;
            if (rightSkip < 0)
                throw new IllegalStateException(format("Calculation blew up. " +
                        "upperSkip=%s, leftSkip=%s, rightSkip=%s", upperSkip, leftSkip, rightSkip));

            Gdx.app.log(TAG, "Will read " + bbWidthCells + "x" + bbHeightCells + " cells. " +
                    "upperSkip=" + upperSkip + ", leftSkip=" + leftSkip + ", rightSkip=" + rightSkip);

            doSkip(elevData, upperSkip * chunkWidthCells);

            //have to iterate by row because that is how elev data is stored
            for (int latCell = upperSkip; latCell < bbHeightCells + upperSkip; latCell++) {
                doSkip(elevData, leftSkip);
                for (int lonCell = leftSkip; lonCell < bbWidthCells + leftSkip; lonCell++) {
                    int elev = elevData.read();
                    if (elev == -1)
                        throw new IllegalStateException("Unexpected end of file reached");
                    landscape.add(this.makeLandscapePoint(chunk, lonCell, latCell, elev));
                }
                doSkip(elevData, rightSkip);
            }

            int readCells = landscape.size - cellsBefore;
            if (readCells != bbWidthCells * bbHeightCells)
                throw new IllegalStateException(format("Calculation blew up. " +
                                "readCells=%d, bbWidthCells=%d, bbHeightCells=%d",
                        readCells, bbWidthCells, bbHeightCells));

            Gdx.app.log(TAG,"Added "+readCells+" cells to landscape");
        } finally {
            elevData.close();
        }
    }

    /**
     * @param globalBoundingBox original input to Loader, in global coordinates
     * @return area of chunk to read ([0,0] is lower left of chunk)
     */
    private Rectangle chunkBoundingBox(Rectangle globalBoundingBox, Chunk chunk) {
        float bbRight = globalBoundingBox.x + globalBoundingBox.width;
        float chunkRight = chunk.lon + chunkWidthDeg;
        float bbTop = globalBoundingBox.y + globalBoundingBox.height;
        float chunkTop = chunk.lat + chunkHeightDeg;

        //cropped in global coordinates
        Rectangle containedBoundingBox = new Rectangle(
                Math.max(globalBoundingBox.x, chunk.lon),
                Math.max(globalBoundingBox.y, chunk.lat),
                bbRight < chunkRight ?
                        (globalBoundingBox.x > chunk.lon ?
                                globalBoundingBox.width :
                                bbRight - chunk.lon) :
                        chunkRight - globalBoundingBox.x,
                bbTop < chunkTop ?
                        (globalBoundingBox.y > chunk.lat ?
                                globalBoundingBox.height :
                                bbTop - chunk.lat) :
                        chunkTop - globalBoundingBox.y
        );

        //same area as containedBoundingBox but in coordinates relative to the chunk
        // ([0,0] is lower left corner of the chunk)
        Rectangle normalizedContainerBoundingBox = new Rectangle(
                containedBoundingBox.x - chunk.lon,
                containedBoundingBox.y - chunk.lat,
                containedBoundingBox.width,
                containedBoundingBox.height
        );

        Gdx.app.log(TAG, format("chunk=%s, boundingBoxCropped=%s, boundingBoxNormalized=%s",
                chunk, containedBoundingBox, normalizedContainerBoundingBox));

        return normalizedContainerBoundingBox;
    }

    private static void doSkip(InputStream stream, long skip) throws IOException {
        long skipLeft = skip;
        while (skipLeft > 0) {
            skipLeft -= stream.skip(skipLeft);
        }
//        long skipActual = stream.skip(skip);
//        if (skipActual != skip)
//            throw new IllegalStateException("Short skip, expected " + skip + " skipped " + skipActual);
    }

    private Vector3 makeLandscapePoint(Chunk chunk, int lonCell, int latCell, int elev) {
        return new Vector3(
                chunk.lon + lonCell * cellWidthDeg,
                // we are reading chunk top to bottom (physically in file)
                // but in global coordinates 0 is at the bottom
                // so we reflect vertically here
                chunk.lat + (chunkHeightCells - (latCell + 1)) * cellHeightDeg,
                elev);
    }

    Array<MapFeature> loadFeatures(String type, Vector2 center, float distance) {
        return null;
    }

    /**
     * lon lat are in global coordinates, they represent lower left corner of the chunk
     */
    static class Chunk {

        final int lon;
        final int lat;

        public Chunk(int lon, int lat) {
            this.lon = lon;
            this.lat = lat;
        }

        /**
         * Data files are all 1x1 degree squares.
         * filenames are in the form elev_'lon'_'lat'
         */
        public String filename() {
            return format("elev_%d_%d", lon, lat);
        }

        @Override
        public String toString() {
            return "Chunk{" +
                    "lon=" + lon +
                    ", lat=" + lat +
                    '}';
        }
    }


}
