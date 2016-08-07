package com.mygdx.game.model;

public class Landscape {

    /**
     * width of the landscape in cells
     */
    final int widthCells;
    /**
     * height of the landscape in cells
     */
    final int heightCells;
    final float[] cells;

    public Landscape(int widthCells, int heightCells) {
        this.widthCells = widthCells;
        this.heightCells = heightCells;
        // * 4 : x,y,z,color
        this.cells = new float[widthCells * heightCells * 4];
    }

    public float getX(int col, int row) {
        return cells[row * 4 * widthCells + col * 4];
    }

    public void setX(int col, int row, float x) {
        cells[row * 4 * widthCells + col * 4] = x;
    }

    public float getY(int col, int row) {
        return cells[row * 4 * widthCells + col * 4 + 1];
    }

    public void setY(int col, int row, float y) {
        cells[row * 4 * widthCells + col * 4 + 1] = y;
    }

    public float getZ(int col, int row) {
        return cells[row * 4 * widthCells + col * 4 + 2];
    }

    public void setZ(int col, int row, float z) {
        cells[row * 4 * widthCells + col * 4 + 2] = z;
    }

    public float getC(int col, int row) {
        return cells[row * 4 * widthCells + col * 4 + 3];
    }

    public void setC(int col, int row, float c) {
        cells[row * 4 * widthCells + col * 4 + 3] = c;
    }

}
