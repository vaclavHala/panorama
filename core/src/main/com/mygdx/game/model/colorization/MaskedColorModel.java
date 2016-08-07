package com.mygdx.game.model.colorization;

import com.badlogic.gdx.graphics.Color;

public class MaskedColorModel implements ColorModel {

    private final ColorModel base;
    private final ColorModel mask;
    private final Weight weight;

    public MaskedColorModel(ColorModel base, ColorModel mask, Weight weight) {
        this.base = base;
        this.mask = mask;
        this.weight = weight;
    }

    @Override
    public Color color(float x, float y, float z) {
        float w = this.weight.weight(x, z);
        float wi = 1 - w;
        Color b = this.base.color(x,y,z);
        Color m = this.mask.color(x,y,z);
        return new Color(
                w * b.r + wi * m.r,
                w * b.g + wi * m.g,
                w * b.b + wi * m.b,
                1.0f);
    }

    public interface Weight{
        float weight(float x, float y);
    }
}
