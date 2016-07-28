package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector3;

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
    public Vector3 color(Vector3 vertex) {
        float w = this.weight.weight(vertex.x, vertex.z );
        float wi = 1 - w;
        Vector3 b = this.base.color(vertex);
        Vector3 m = this.mask.color(vertex);
        return new Vector3(
                w * b.x + wi * m.x,
                w * b.y + wi * m.y,
                w * b.z + wi * m.z);
    }

    public interface Weight{
        float weight(float x, float y);
    }
}
