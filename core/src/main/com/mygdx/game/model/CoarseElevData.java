package com.mygdx.game.model;

import java.io.IOException;

/**
 * Given elev dataset reads only every N-th value, this simulates lower resolution
 * and allows for more data to be loaded at once at the cost of resolution
 */
public class CoarseElevData implements ElevData {

    private final ElevData base;
    private final int n;

    public CoarseElevData(ElevData base, int n) {
        this.base = base;
        this.n = n;
    }

    @Override
    public short next() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
