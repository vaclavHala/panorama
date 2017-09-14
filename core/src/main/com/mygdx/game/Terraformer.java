package com.mygdx.game;

import com.badlogic.gdx.graphics.g3d.Model;
import com.mygdx.game.model.Chunk;
import com.mygdx.game.model.Feature;
import com.mygdx.game.model.Visibility;
import java.util.ArrayList;
import static java.util.Collections.unmodifiableList;
import java.util.List;

public interface Terraformer {

    void rebuildLandscape(float lon, float lat) throws MissingChunksException;

    /** rebuildLandscape() first, when landscape is loaded and this returns true getters here can be used */
    boolean hasLandscape();

    /** throws if hasLandscape() is false */
    Model landscape();

    /** throws if hasLandscape() is false */
    List<Feature> features();

    /** throws if hasLandscape() is false */
    Visibility visibility();

    public static class MissingChunksException extends Exception {

        public final List<Chunk> missingChunks;

        public MissingChunksException(List<Chunk> chunks) {
            super("Missing chunks: " + chunks);
            this.missingChunks = unmodifiableList(new ArrayList<Chunk>(chunks));
        }
    }
}
