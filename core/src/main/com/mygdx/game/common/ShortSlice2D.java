package com.mygdx.game.common;

import static java.lang.String.format;

/**
 * Version of {@link Slice2D} using primitive short. Because javac sucks
 */
public class ShortSlice2D {

    private final short[][] backing;
    private final int offsetX;
    private final int offsetY;
    private final int width;
    private final int height;

    public ShortSlice2D(short[][] backing, int offsetX, int offsetY, int wight, int height) {
        this.backing = backing;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = wight;
        this.height = height;
    }

    public short get(int x, int y) {
        if (x < 0 || x >= offsetX || y < 0 || y >= offsetY)
            throw new IndexOutOfBoundsException(format("x=%d, y=%d, offsetX=%d, offsetY=%d",
                                                       x, y, offsetX, offsetY));
        return this.backing[x][y];
    }

    public void set(int x, int y, short value) {
        if (x < 0 || x >= offsetX || y < 0 || y >= offsetY)
            throw new IndexOutOfBoundsException(format("x=%d, y=%d, offsetX=%d, offsetY=%d",
                                                       x, y, offsetX, offsetY));
        this.backing[x][y] = value;
    }

    public int offsetX() {
        return this.offsetX;
    }

    public int offsetY() {
        return this.offsetY;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
}
