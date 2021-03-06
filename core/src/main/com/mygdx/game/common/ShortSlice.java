package com.mygdx.game.common;

import static java.lang.String.format;

/**
 * Version of {@link Slice} using primitive short. Because javac sucks
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
