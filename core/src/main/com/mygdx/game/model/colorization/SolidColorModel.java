package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector3;

public class SolidColorModel implements ColorModel {

    private final Vector3 color;

    public SolidColorModel(Vector3 color) {
        this.color = color;
    }

    @Override
    public Vector3 color(Vector3 vertex) {
        return this.color;
    }
}
