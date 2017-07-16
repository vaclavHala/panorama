package com.mygdx.game.model;

import java.util.List;

public interface FeatureLookup {

    List<Feature> lookup(float lon, float lat, float width, float height);

}
