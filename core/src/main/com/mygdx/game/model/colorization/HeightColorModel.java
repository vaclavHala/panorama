package com.mygdx.game.model.colorization;

import com.badlogic.gdx.graphics.Color;

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
    public Color color(float x, float y, float z) {
        if (y < this.minHeight)
            return Color.BLACK;
        if (y > this.maxHeight)
            return Color.WHITE;
        float c = (y - minHeight) / range;
        return new Color(c, c, c, 1.0f);
    }
}
