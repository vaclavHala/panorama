package com.mygdx.game;

public class ElevConfig {

    public final float cellsPerDegHorizontal;
    public final float cellsPerDegVertical;
    public final int chunkWidthDeg;
    public final int chunkHeightDeg;
    public final int chunkWidthCells;
    public final int chunkHeightCells;
    public final float cellWidthDeg;
    public final float cellHeightDeg;

    public ElevConfig(int chunkWidthDeg, int chunkHeightDeg,
            int chunkWidthCells, int chunkHeightCells,
            float cellsPerDegHorizontal, float cellsPerDegVertical) {
        this.chunkWidthDeg = chunkWidthDeg;
        this.chunkHeightDeg = chunkHeightDeg;
        this.cellsPerDegHorizontal = cellsPerDegHorizontal;
        this.cellsPerDegVertical = cellsPerDegVertical;
        this.chunkWidthCells = chunkWidthCells;
        this.chunkHeightCells = chunkHeightCells;
        this.cellWidthDeg = 1.0F / cellsPerDegHorizontal;
        this.cellHeightDeg = 1.0F / cellsPerDegVertical;
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
               '}';
    }
}
