package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import java.util.List;

public interface HeightFieldService {

    List<Float> getFor(List<Vector2> coords);

}
