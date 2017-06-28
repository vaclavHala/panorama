package com.mygdx.game.model;

import com.badlogic.gdx.files.FileHandle;
import java.io.Reader;
import java.io.StringReader;

/**
 * This is a hack to have G3DModelLoader load my dynamically generated file without having to store it to disk.
 * If user wishes to save the model for later, just dump data from here to file.
 */
public class InMemoryFileHandle extends FileHandle {

    private final String data;

    public InMemoryFileHandle(final String data) {
        super();
        this.data = data;
    }

    @Override
    public Reader reader(String charset) {
        return new StringReader(data);
    }

    @Override
    public FileHandle parent() {
        return new InMemoryFileHandle("");
    }

    @Override
    public String path() {
        return "Someplace in RAM";
    }
}
