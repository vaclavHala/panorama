package com.mygdx.game.model;

import java.io.IOException;

/**
 * Given elev dataset reads only every N-th value, this simulates lower resolution
 * and allows for more data to be loaded at once at the cost of resolution
 */
public class CoarseElevData implements ElevData {

    private final ElevData base;
    private final int n;

    private final int chunkWidthCells;
    private final int chunkHeightCells;

    private int row;
    private int col;

    public CoarseElevData(ElevData base, int n, int chunkWidthCells, int chunkHeightCells) {
        this.base = base;
        this.n = n;

        this.row = 0;
        this.col = 0;

        this.chunkWidthCells = chunkWidthCells;
        this.chunkHeightCells = chunkHeightCells;
    }

    @Override
    public short next() {
        if (this.row >= this.chunkHeightCells) {
            throw new IllegalArgumentException("End of stream already reached");
        }

        if (col >= chunkWidthCells) {
            col = 0;
            row += n;
            // n-th row is the one we read earlier
            for (int i = 0; i < chunkWidthCells * (n - 1); i++) {
                base.next();
            }
        }
        short out = base.next();
        // n-th col is the one we read above
        int remainingInRow = Math.min(n - 1,
                                      this.chunkWidthCells - this.col - 1);
        for (int i = 0; i < remainingInRow; i++) {
            base.next();
        }
        col += n;
        return out;
    }

    @Override
    public void close() throws IOException {
        this.base.close();
    }

}
