package com.mygdx.game.model;

import java.io.IOException;
import static java.lang.String.format;

public class CroppedElevData implements ElevData {

    private final ElevData base;

    private final int cellsHorizontal;
    private final int cellsVertical;
    private final int cell0Row;
    private final int cell0Col;
    private final int cellNRow;
    private final int cellNCol;

    private int col;
    private int row;

    public CroppedElevData(
            ElevData base,
            int cellsHorizontal, int cellsVertical,
            int cell0Row, int cell0Col,
            int cellNRow, int cellNCol) {
        this.base = base;
        this.cellsHorizontal = cellsHorizontal;
        this.cellsVertical = cellsVertical;
        this.cell0Row = cell0Row;
        this.cell0Col = cell0Col;
        this.cellNRow = cellNRow;
        this.cellNCol = cellNCol;

        this.col = cell0Col;
        this.row = cell0Row;

        int skip = cell0Row * cellsHorizontal + cell0Col;
        for (int i = 0; i < skip; i++) {
            base.next();
        }
    }

    @Override
    public short next() {
        // skip before reading rather than after
        // this way there is no need for special case to
        // not run skip at end of input (which'd blow)
        if (this.col == this.cellNCol) {
            int skip = this.cell0Col + this.cellsHorizontal - this.cellNCol;
            for (int i = 0; i < skip; i++) {
                base.next();
            }
            this.col = this.cell0Col;
            this.row++;
        }
        if (this.row == this.cellNRow) {
            throw new IllegalStateException(format("End of data reached: row=%s, col=%s," +
                                                   " cellsHorizontal=%s, cellsVertical=%s," +
                                                   " cell0Row=%s, cell0Col=%s, cellNRow=%s, cellNCol=%s",
                                                   row, col, cellsHorizontal, cellsVertical,
                                                   cell0Row, cell0Col, cellNRow, cellNCol));
        }
        short elev = this.base.next();
        this.col++;
        return elev;
    }

    @Override
    public void close() throws IOException {
    }
}
