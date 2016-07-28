package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector3;

public interface ColorModel {

    /**
     * @return RGB vector, RGB correspond to x,y,z in this order
     */
    public Vector3 color(Vector3 vertex);

}
