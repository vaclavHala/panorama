package com.mygdx.game;

/**
 * x y are relative to global coordinates defined bu ElevConfig
 */
public class CellArea {

    public final int width;
    public final int height;
    public final int x0;
    public final int y0;

    public CellArea(int width, int height, int x0, int y0) {
        this.width = width;
        this.height = height;
        this.x0 = x0;
        this.y0 = y0;
    }

    @Override
    public String toString() {
        return "CellArea{" +
               "width=" + width +
               ", height=" + height +
               ", x0=" + x0 +
               ", y0=" + y0 +
               '}';
    }
}
