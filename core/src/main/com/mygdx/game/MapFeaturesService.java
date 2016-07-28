
package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.model.MapFeature;
import java.util.List;

public interface MapFeaturesService {

    List<MapFeature> findNear(Vector2 location);

}
