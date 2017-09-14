package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import static com.mygdx.game.common.Helpers.entry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Float.parseFloat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFeatureLookupFactory {

    private static final int BUFFER_SIZE = 4096;

    private final String chunksDir;

    public ZipFeatureLookupFactory(String chunksDir) {
        this.chunksDir = chunksDir;
    }

    public FeatureLookup lookupFrom(Collection<Chunk> chunks, ElevationResolution elevResolution) {
        List<Entry<Vector2, String>> fs = new ArrayList<Entry<Vector2, String>>();

        BufferedReader r = null;
        try {
            for (Chunk chunk : chunks) {
                String featureFileName = chunksDir + chunk.name;
                log("Opening feature file: " + featureFileName);
                InputStream in = Gdx.files.external(featureFileName).read(BUFFER_SIZE);
                ZipInputStream zip = new ZipInputStream(in);
                ZipEntry entryElev = zip.getNextEntry();
                ZipEntry entryFeatures = zip.getNextEntry();
                if (!entryFeatures.getName().equals("chunk/features")) {
                    throw new IllegalArgumentException("Malformed chunk, expected to find features entry, got: " + entryElev.getName());
                }

                r = new BufferedReader(new InputStreamReader(zip));
                String line;
                while ((line = r.readLine()) != null) {
                    String[] raw = line.split(",");
                    float lat = parseFloat(raw[0]);
                    float lon = parseFloat(raw[1]);
                    String name = raw[2];

                    fs.add(entry(new Vector2(lon, lat), name));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException e) {
                log("Error closing file: %s", formatException(e));
            }
        }

        log("Loaded %s features", fs.size());
        return new MemBackedFeatureLookup(elevResolution, fs);

    }

    private static void log(String format, Object... args) {
        Gdx.app.log("pano.features.zip", String.format(format, args));
    }

}
