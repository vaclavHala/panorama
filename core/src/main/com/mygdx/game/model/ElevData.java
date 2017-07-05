package com.mygdx.game.model;

import java.io.Closeable;

/**
 * Sequentially read grid of elevation data
 */
public interface ElevData extends Closeable {

    /**
     * @return  height of next point in meters above sea level
     */
    short next();

}
