package com.mygdx.game.common;

public class ChunkNamingScheme {

    public static String chunkFileName(int lon, int lat) {
        return String.format("%c%d_%c%d.chunk",
                             lon < 0 ? 's' : 'n', Math.abs(lon),
                             lat < 0 ? 'w' : 'e', Math.abs(lat));
    }

}
