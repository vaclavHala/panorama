package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class CompositeColorModel implements ColorModel {

    private final Array<ColorModel> components;

    public CompositeColorModel(ColorModel... components) {
        this.components = new Array<ColorModel>(components);
    }

    @Override
    public Vector3 color(Vector3 vertex) {
        Vector3 composite = new Vector3();
        for(ColorModel c: this.components){
            composite.add(c.color(vertex));
        }
        composite.x /= this.components.size;
        composite.y /= this.components.size;
        composite.z /= this.components.size;
        return composite;
    }
}
