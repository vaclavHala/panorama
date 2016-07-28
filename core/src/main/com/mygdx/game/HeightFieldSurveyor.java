
package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.model.MapFeature;
import java.util.List;

public interface HeightFieldSurveyor {

    List<Vector2> fieldFrom(Vector2 center, List<MapFeature> pointsOfInterest);

}
