package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.mygdx.game.model.*;
import com.mygdx.game.model.LandscapeLoader.ElevDataFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipBackedElevDataFactory implements ElevDataFactory {

    private static final int BUFFER_SIZE = 4096;
    private final String chunksDir;
    private final ElevConfig elevCfg;
    private final Map<Chunk, InputStream> loadedStreams;

    public ZipBackedElevDataFactory(String chunksDir, ElevConfig elevCfg) {
        this.chunksDir = chunksDir;
        this.elevCfg = elevCfg;
        this.loadedStreams = new HashMap<Chunk, InputStream>();
    }

    //    public void init(List<Entry<Chunk, String>> requiredChunks) {
    //        for (Entry<Chunk, String> e : requiredChunks) {
    //            try {
    //
    //
    //            } catch (IOException ex) {
    //                throw new RuntimeException(ex);
    //            }
    //        }
    //    }

    @Override
    public ElevData chunk(Chunk chunk) throws IOException {
        InputStream in = Gdx.files.external(chunksDir + chunk.name).read(BUFFER_SIZE);
        ZipInputStream zip = new ZipInputStream(in);
        ZipEntry entryElev = zip.getNextEntry();
        if (!entryElev.getName().equals("chunk/elev")) {
            throw new IllegalArgumentException("Malformed chunk, expected to find elev entry, got: " + entryElev.getName());
        }

        return new FileBackedElevData(zip, chunk.name);
    }

}
