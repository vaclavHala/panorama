package com.mygdx.game;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.model.Chunk;
import com.mygdx.game.model.CoarseElevData;
import com.mygdx.game.model.ElevData;
import com.mygdx.game.model.FileBackedElevData;
import com.mygdx.game.model.LandscapeLoader.ElevDataFactory;
import java.io.FileNotFoundException;

public class CoarsedElevDataFactory implements ElevDataFactory {

    private final ElevConfig elevCfg;
    private final int detail;

    public CoarsedElevDataFactory(ElevConfig elevCfg, int detail) {
        this.elevCfg = elevCfg;
        this.detail = detail;
    }

    @Override
    public ElevData chunk(Chunk chunk) throws FileNotFoundException {
        return new CoarseElevData(new FileBackedElevData(Gdx.files, chunk.name), detail,
                                  elevCfg.chunkWidthCells, elevCfg.chunkHeightCells);
    }
}
