package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import java.io.BufferedReader;
import java.io.IOException;
import static java.lang.Float.parseFloat;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.List;

public class FileBackedFeatureLookup implements FeatureLookup {

    private static final int BUFFER_SIZE = 4096;

    private final Files files;
    private final ElevationResolution elevResolution;

    public FileBackedFeatureLookup(Files files, ElevationResolution elevResolution) {
        this.files = files;
        this.elevResolution = elevResolution;
    }

    @Override
    public List<Feature> lookup(float lon, float lat, float width, float height) {
        List<Feature> fs = new ArrayList<Feature>();

        float lonMax = lon + width;
        float latMax = lat + height;

        String featureFileName = "features/peak_n48_e14";
        BufferedReader r = null;
        try {
            log("Opening feature file: " + featureFileName);
            r = files.internal(featureFileName).reader(BUFFER_SIZE, UTF_8.name());
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = line.split(",");
                float fLat = parseFloat(f[0]);
                float fLon = parseFloat(f[1]);
                String name = f[2];

                if (fLon < lon || fLon > lonMax || fLat < lat || fLat > latMax) {
                    continue;
                }

                // place each feature above actual landscape to avoid (partially..) landscape clipping
                float fElev = elevResolution.projectToLandscape(fLon, fLat) + 10;
                Vector3 position = new Vector3(fLon, fLat, fElev);
                fs.add(new Feature(name, position));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException e) {
                // fuck it
            }
        }

        log("Loaded features: " + fs);
        return fs;
    }

    private static void log(String format, Object... args) {
        Gdx.app.log("pano.features.file", String.format(format, args));
    }

}
