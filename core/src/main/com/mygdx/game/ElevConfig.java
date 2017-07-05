package com.mygdx.game;

public class ElevConfig {

    public final int cellsPerDegHorizontal;
    public final int cellsPerDegVertical;
    public final int chunkWidthDeg;
    public final int chunkHeightDeg;
    public final int chunkWidthCells;
    public final int chunkHeightCells;
    public final float cellWidthDeg;
    public final float cellHeightDeg;
    public final float heightScaler;

    public ElevConfig(int chunkWidthDeg, int chunkHeightDeg, int cellsPerDegHorizontal, int cellsPerDegVertical, final float heightScaler) {
        this.chunkWidthDeg = chunkWidthDeg;
        this.chunkHeightDeg = chunkHeightDeg;
        this.cellsPerDegHorizontal = cellsPerDegHorizontal;
        this.cellsPerDegVertical = cellsPerDegVertical;
        this.chunkWidthCells = chunkWidthDeg * cellsPerDegHorizontal;
        this.chunkHeightCells = chunkHeightDeg * cellsPerDegVertical;
        this.cellWidthDeg = 1.0F / cellsPerDegHorizontal;
        this.cellHeightDeg = 1.0F / cellsPerDegVertical;
        this.heightScaler = heightScaler;
    }

    public int lonToCell(double lon) {
        return (int) (lon * cellsPerDegHorizontal);
    }

    public int latToCell(double lat) {
        return (int) (lat * cellsPerDegVertical);
    }

    @Override
    public String toString() {
        return "ElevConfig{" +
               "chunkWidthDeg=" + chunkWidthDeg +
               ", chunkHeightDeg=" + chunkHeightDeg +
               ", chunkWidthCells=" + chunkWidthCells +
               ", chunkHeightCells=" + chunkHeightCells +
               ", cellWidthDeg=" + cellWidthDeg +
               ", cellHeightDeg=" + cellHeightDeg +
               ", height scaler=" + heightScaler +
               '}';
    }
}
