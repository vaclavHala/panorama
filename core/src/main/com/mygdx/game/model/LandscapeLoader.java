package com.mygdx.game.model;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;

/**
 * Gutted {@link com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader} which
 * creates landscape model in memory (shape, vertex colorization)
 */
public class LandscapeLoader extends ModelLoader<ModelLoader.ModelParameters> {

    public LandscapeLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public ModelData loadModelData(FileHandle fileHandle, ModelParameters parameters) {
        // TODO move stuff from Loader here
        return null;
    }
}
