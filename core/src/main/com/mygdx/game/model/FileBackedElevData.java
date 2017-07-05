package com.mygdx.game.model;

import com.badlogic.gdx.Files;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileBackedElevData implements ElevData {

    private static final int BUFFER_SIZE = 4096;

    private int at;
    private String chunkName;
    private InputStream in;

    public FileBackedElevData(Files files, String chunkName) {
        this.chunkName = chunkName;
        this.in = files.internal(chunkName).read(BUFFER_SIZE);
        this.at = 0;
    }

    public FileBackedElevData(InputStream is, String chunkName) {
        this.chunkName = chunkName;
        this.in = new BufferedInputStream(is, BUFFER_SIZE);
        this.at = 0;
    }

    @Override
    public short next() {
        // data is little endian
        try {
            int lsb = in.read();
            int msb = in.read();
            if (lsb == -1 || msb == -1) {
                throw new IllegalStateException("End of data reached");
            }
            this.at++;
            return (short) (lsb + (msb << 8));
        } catch (IOException e) {
            throw new IllegalStateException("Can not read more from " + this.chunkName, e);
        }
    }

    @Override
    public void close() throws IOException {
    }

}
