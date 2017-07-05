package com.mygdx.game.model;

import java.io.IOException;
import java.util.Arrays;

public class ArrayBackedElevData implements ElevData {

    private int at;
    private final int[] arr;

    public ArrayBackedElevData(int... arr) {
        this.arr = arr;
        this.at = 0;
    }

    @Override
    public short next() {
        if (this.at == this.arr.length) {
            throw new IllegalStateException("End of data reached");
        }
        return (short) this.arr[this.at++];
    }

    @Override
    public void close() throws IOException {
    }

    public static ElevData incrementing(int rows, int cols) {
        return incrementing(rows, cols, 0);
    }

    public static ElevData incrementing(int rows, int cols, int offset) {
        int[] elev = new int[rows * cols];
        for (int i = 0; i < elev.length; i++) {
            elev[i] = i + offset;
        }
        return new ArrayBackedElevData(elev);
    }

    public static ElevData constant(int rows, int cols, int val) {
        int[] elev = new int[rows * cols];
        for (int i = 0; i < elev.length; i++) {
            elev[i] = val;
        }
        return new ArrayBackedElevData(elev);
    }

    @Override
    public String toString() {
        return "at=" + this.at + ", arr=" + Arrays.toString(this.arr);
    }

}
