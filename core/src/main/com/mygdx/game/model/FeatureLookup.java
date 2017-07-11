package com.mygdx.game.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;

public class FeatureLookup {

    private final ElevationResolution elevResolution;

    public FeatureLookup(ElevationResolution elevResolution) {
        this.elevResolution = elevResolution;
    }

    public List<Feature> lookup(float lon, float lat, float width, float height) {
        List<Feature> fs = new ArrayList<Feature>();
        for (RawFeature f : lookupRaw(lon, lat, width, height)) {
            float fLon = f.position.x;
            float fLat = f.position.y;
            // place each feature above actual landscape to avoid (partially..) landscape clipping
            float fElev = elevResolution.projectToLandscape(fLon, fLat) + 10;
            Vector3 position = new Vector3(fLon, fLat, fElev);
            fs.add(new Feature(f.name, position));
        }
        return fs;
    }

    List<RawFeature> lookupRaw(float lon, float lat, float width, float height) {
        return asList(new RawFeature("Klet", new Vector2(14.2834500F, 48.8649861F)),
                      new RawFeature("Bily Kamen", new Vector2(14.2940883F, 48.8532056F)),
                      new RawFeature("Ohrada", new Vector2(14.2736606F, 48.8518642F)),
                      new RawFeature("U Piskovny", new Vector2(14.2697553F, 48.8685222F)),
                      new RawFeature("Nad Javorem", new Vector2(14.2882947F, 48.8773289F)),
                      new RawFeature("Na Rovine", new Vector2(14.2646483F, 48.8788250F)));
    }

    public static class RawFeature {

        public final String name;
        public final Vector2 position;

        public RawFeature(String name, Vector2 position) {
            this.name = name;
            this.position = position;
        }
    }

}
