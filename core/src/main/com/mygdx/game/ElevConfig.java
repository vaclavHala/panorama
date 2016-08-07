package com.mygdx.game;

public class ElevConfig {

    private static final int LAT_MAX = 90;
    private static final int LON_MAX = 180;

    public final int cellsPerDegHorizontal;
    public final int cellsPerDegVertical;
    public final int chunkWidthDeg;
    public final int chunkHeightDeg;
    public final int chunkWidthCells;
    public final int chunkHeightCells;
    public final double cellWidthDeg;
    public final double cellHeightDeg;

    public ElevConfig(int chunkWidthDeg, int chunkHeightDeg, int cellsPerDegHorizontal, int cellsPerDegVertical) {
        this.chunkWidthDeg = chunkWidthDeg;
        this.chunkHeightDeg = chunkHeightDeg;
        this.cellsPerDegHorizontal = cellsPerDegHorizontal;
        this.cellsPerDegVertical = cellsPerDegVertical;
        this.chunkWidthCells = chunkWidthDeg * cellsPerDegHorizontal;
        this.chunkHeightCells = chunkHeightDeg * cellsPerDegVertical;
        this.cellWidthDeg = 1.0 / cellsPerDegHorizontal;
        this.cellHeightDeg = 1.0 / cellsPerDegVertical;
    }

    public int lonToCell(double lon){
        return (int) (lon * cellsPerDegHorizontal);
    }

    public int latToCell(double lat){
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
                '}';
    }
}
