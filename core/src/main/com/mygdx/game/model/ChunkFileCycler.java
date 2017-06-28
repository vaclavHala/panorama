package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import static java.lang.String.format;

public class ChunkFileCycler<T> {

    private static final String TAG = ChunkFileCycler.class.getSimpleName();

    private final Array<T> ins;
    private final int inWidth;
    private final int insCountX;
    private final int insWidthTot;
    private final int insAreaTot;

    private int at;

    public ChunkFileCycler(Array<T> ins,
            int inWidth, int inHeight,
            int insCountX, int insCountY) {
        if (ins.size != insCountX * insCountY)
            throw new IllegalArgumentException(
                                               format("ins.size=%d, insCountX=%d, insCountY=%d",
                                                      ins.size, insCountX, insCountY));
        Gdx.app.log(TAG, "inWidth=" + inWidth +
                         "inHeight=" + inHeight +
                         "insCountX=" + insCountX +
                         "insCountY=" + insCountY);
        this.inWidth = inWidth;
        this.insCountX = insCountX;
        this.insWidthTot = insCountX * inWidth;
        this.insAreaTot = insCountX * inWidth * inHeight;
        this.ins = new Array<T>(ins);
        this.at = 0;
    }

    public T next() {
        int row = at / insAreaTot;
        int col = (at % insWidthTot) / inWidth;
        at++;
        return ins.get(row * insCountX + col);
    }

    /**
     * Behaves as if next() was called i times, without returning the result
     */
    public void move(int i) {
        at += i;
    }

    /**
     * Returns how many more times next can be called before
     * next underlying stream will be used
     */
    public int leftBeforeSwap() {
        int thisCol = at % insWidthTot;
        return (thisCol + 1) * insWidthTot - at;
    }
}
