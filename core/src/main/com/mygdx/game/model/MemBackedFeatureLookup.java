package com.mygdx.game.model;

import static android.content.ContentValues.TAG;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class MemBackedFeatureLookup implements FeatureLookup {

    // TODO some clever structure/index for spatial data here if load is slow
    private final List<Entry<Vector2, String>> features;
    private final ElevationResolution elevResolution;

    public MemBackedFeatureLookup(
            ElevationResolution elevResolution,
            List<Entry<Vector2, String>> features) {
        this.elevResolution = elevResolution;
        this.features = new ArrayList<Entry<Vector2, String>>(features);
    }

    @Override
    public List<Feature> lookup(float lon, float lat, float width, float height) {
        List<Feature> fs = new ArrayList<Feature>();

        float lonMax = lon + width;
        float latMax = lat + height;

        for (Entry<Vector2, String> f : features) {
            float fLon = f.getKey().x;
            float fLat = f.getKey().y;
            if (fLon < lon || fLon > lonMax || fLat < lat || fLat > latMax) {
                continue;
            }

            // place each feature above actual landscape to avoid (partially..) landscape clipping
            float fElev = elevResolution.projectToLandscape(fLon, fLat) + 10;
            Vector3 position = new Vector3(fLon, fLat, fElev);
            fs.add(new Feature(f.getValue(), position));
        }

        log("Loaded features: " + fs);
        return fs;
    }

    private static void log(String format, Object... args) {
        Gdx.app.log(TAG, String.format(format, args));
    }

}
