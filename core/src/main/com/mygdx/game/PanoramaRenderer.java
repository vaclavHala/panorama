
package com.mygdx.game;

import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.model.MapFeature;
import java.util.List;

public interface PanoramaRenderer {

    void render(List<MapFeature> features, List<Vector3> surface);

}
