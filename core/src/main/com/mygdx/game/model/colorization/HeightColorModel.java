package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector3;

public class HeightColorModel implements ColorModel {

    private final float minHeight;
    private final float maxHeight;
    private final float range;

    public HeightColorModel(float gradientHeightLow, float gradientHeightHigh) {
        this.minHeight = gradientHeightLow;
        this.maxHeight = gradientHeightHigh;
        this.range = gradientHeightHigh - gradientHeightLow;
    }

    @Override
    public Vector3 color(Vector3 vertex) {
        return vertex.y < this.minHeight ? new Vector3(0, 0, 0) :
                (vertex.y > this.maxHeight ? new Vector3(1, 1, 1) :
                        new Vector3(
                                (vertex.y - minHeight) / this.range,
                                (vertex.y - minHeight) / this.range,
                                (vertex.y - minHeight) / this.range));
    }
}
