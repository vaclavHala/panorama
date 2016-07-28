package com.mygdx.game.model.colorization;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.model.colorization.MaskedColorModel.Weight;
import static java.lang.String.format;

public class CenterDistanceWeight implements Weight {

    private final Vector2 center;
    private final float maxFull;
    private final float minEmpty;
    private final float delta;

    /**
     * @param maxFull  point with at most this distance from center will have weight 1
     * @param minEmpty point with at least this distance from center will have weight 0
     */
    public CenterDistanceWeight(float maxFull, float minEmpty, Vector2 center) {
        if (maxFull >= minEmpty || maxFull < 0)
            throw new IllegalStateException(format("maxFull=%f, minEmpty=%f", maxFull, minEmpty));
        this.minEmpty = minEmpty;
        this.maxFull = maxFull;
        this.center = center;
        this.delta = this.minEmpty - maxFull;
    }

    @Override
    public float weight(float x, float y) {
        float dist = center.dst(x, y);
        return dist < maxFull ? 1 :
                (dist > minEmpty ? 0 :
                        1 - (dist - maxFull) / delta);
    }
}
