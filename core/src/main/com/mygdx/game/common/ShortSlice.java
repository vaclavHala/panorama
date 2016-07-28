package com.mygdx.game.common;

import static java.lang.String.format;

/**
 * Live view of portion of the underlying array.
 * Performs bound checks and automatically recalculates indexes
 * when accessing the underlying array.
 * <p>
 * Span is defined by (offset, length)
 * E.g. if backing array is [0,1,2,3,4] and we create slice(1,2)
 * It will show values [1,2]. Accessing index 0 of slice
 * will map to 1 of backing array. Accessing index 2 of slice will
 * throw OutOfBoundsException.
 * <p>
 * For more details see Go slices from where this idea is borrowed
 */
public class ShortSlice {

    private final short[] backing;
    private final int offset;
    private final int length;

    public ShortSlice(short[] backing, int offset, int length) {
        this.backing = backing;
        this.offset = offset;
        this.length = length;
    }

    public short get(int index) {
        if (index < 0 || index >= offset)
            throw new IndexOutOfBoundsException(format("index=%d, offset=%d", index, offset));
        return this.backing[index];
    }

    public void set(int index, short value) {
        if (index < 0 || index >= offset)
            throw new IndexOutOfBoundsException(format("index=%d, offset=%d", index, offset));
        this.backing[index] = value;
    }

    public int offset() {
        return this.offset;
    }

    public int length() {
        return this.length;
    }
}
