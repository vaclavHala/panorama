package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.mygdx.game.model.Feature;
import com.mygdx.game.model.Visibility;
import java.util.List;

public interface Terraformer {

    void rebuildLandscape(float lon, float lat, ProgressListener progress);

    boolean hasLandscape();

    Model landscape();

    List<Feature> features();

    Visibility visibility();

}
