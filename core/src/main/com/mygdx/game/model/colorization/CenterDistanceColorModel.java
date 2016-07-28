package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CenterDistanceColorModel implements ColorModel {

    private final float centerX;
    private final float centerY;
    private final float distMin;
    private final float distMax;
    private final float range;

    public CenterDistanceColorModel(Vector2 center, float gradientDistanceLow, float gradientDistanceHigh) {
        this.centerX = center.x;
        this.centerY = center.y;
        this.distMin = gradientDistanceLow;
        this.distMax = gradientDistanceHigh;
        this.range = gradientDistanceHigh - gradientDistanceLow;
    }

    @Override
    public Vector3 color(Vector3 vertex) {
        Vector2 xy = new Vector2(vertex.x, vertex.z);
        float dist = xy.dst(this.centerX, this.centerY);
        return dist < this.distMin ? new Vector3(1, 1, 1) :
                (dist > this.distMax ? new Vector3(0, 0, 0) :
                        new Vector3(
                                1 - (dist - distMin) / this.range,
                                1 - (dist - distMin) / this.range,
                                1 - (dist - distMin) / this.range));
    }
}
