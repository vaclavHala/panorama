package com.mygdx.game.model.colorization;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

public interface ColorModel {

    /**
     * @return RGB vector, RGB correspond to x,y,z in this order
     */
    Color color(float x, float y, float z);

}
