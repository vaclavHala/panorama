package com.mygdx.game.model;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.common.CoordTransform;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;

public class HardwiredFeatureLookup implements FeatureLookup {

    private final List<Feature> features;

    public HardwiredFeatureLookup(ElevationResolution elevResolution, RawFeature... features) {
        this.features = new ArrayList<Feature>();
        for (RawFeature f : features) {
            this.features.add(new Feature(f.name, new Vector3(f.lon,
                                                              f.lat,
                                                              elevResolution.projectToLandscape(f.lon, f.lat))
                         ));
        }
    }

    @Override
    public List<Feature> lookup(float lon, float lat, float width, float height) {
        return new ArrayList<Feature>(this.features);
    }

    public static class RawFeature {

        public final String name;
        public final float lon;
        public final float lat;

        public RawFeature(String name, double lon, double lat) {
            this.lon = (float) lon;
            this.lat = (float) lat;
            this.name = name;
        }
    }
}
