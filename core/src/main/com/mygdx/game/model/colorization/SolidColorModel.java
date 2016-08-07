package com.mygdx.game.model.colorization;

import com.badlogic.gdx.graphics.Color;

public class SolidColorModel implements ColorModel {

    private final Color color;

    public SolidColorModel(Color color) {
        this.color = color;
    }

    @Override
    public Color color(float x, float y, float z) {
        return this.color;
    }
}
