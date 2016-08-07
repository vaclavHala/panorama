package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ElevConfig;
import java.io.IOException;
import static java.lang.String.format;

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
     * - Below the box again skip the horizontal stripe if nay
     *
     * @param boundingBox x = lon ,y = lat, extent in degrees
     * @return known points in the given box, each with associated elevation
     */
    public Landscape loadLandscape(int lon0, int lat0, int width, int height) throws IOException {

        Gdx.app.log(TAG, "Loading landscape. Cell:" +
                " lon0_cells=" + lon0 + ", lat0_cells=" + lat0 +
                ", width_cells=" + width + ", height_cells=" + height);

        CollatedElevStream elevStream = new CollatedElevStream(files, elevCfg, lon0, lat0, width, height);

        Landscape landscape = new Landscape(width, height);

        int dataWidthCells = elevStream.chunksHorizontal() * elevCfg.chunkWidthCells;
        int dataHeightCells = elevStream.chunksVertical() * elevCfg.chunkHeightCells;
        int upperSkip = (elevStream.chunk0Lat() + dataHeightCells - (lat0 + height)) * dataWidthCells;
        int leftSkip = lon0 - elevStream.chunk0Lon();
        int rightSkip = dataWidthCells - leftSkip - width;
//        int upperSkip = (int) (elevStream.lat0() / elevStream.cellHeightDeg() + elevStream.dataHeightCells() - (boundingBox.y + boundingBox.height) / elevStream.cellHeightDeg());
//        int leftSkip = (int) ((boundingBox.x - elevStream.lon0()) / elevStream.cellWidthDeg());
//        int rightSkip = (elevStream.dataWidthCells() - leftSkip - bbWidthCells);
        if (upperSkip < 0 || leftSkip < 0 || rightSkip < 0)
            throw new IllegalStateException(format("Calculation blew up. " +
                    "upperSkip=%s, leftSkip=%s, rightSkip=%s", upperSkip, leftSkip, rightSkip));
        Gdx.app.log(TAG, "dataWidth=" + dataWidthCells + ", dataHeight=" + dataHeightCells +
                ", upperSkip=" + upperSkip + ", leftSkip=" + leftSkip + ", rightSkip=" + rightSkip);

        elevStream.skip(upperSkip);

        //have to iterate by row because that is how elev data is stored
        for (int lat = 0; lat < height; lat++) {
            elevStream.skip(leftSkip);
            for (int lon = 0; lon < width; lon++) {
                int elev = elevStream.readNext();
                landscape.setX(lon, lat, lon0 + lon);
                landscape.setY(lon, lat, elev);
                // we are reading chunk top to bottom (physically in file)
                // but in global coordinates 0 is at the bottom
                // so we reflect vertically here
                // - 1 because lat is <0, height-1>, we need cancel out height completely
                landscape.setZ(lon, lat, lat0 + height - lat - 1);
                // color will be filled in later;
            }
            elevStream.skip(rightSkip);
        }

        Gdx.app.log(TAG, "Loaded elevation data and assembled surface, closing files");
        elevStream.close();

        return landscape;
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


    Array<MapFeature> loadFeatures(String type, Vector2 center, float distance) {
        return null;
    }


}
