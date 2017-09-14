package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.Locale;

public class PanoramaI18n {

    public static I18NBundle load() {
        FileHandle baseFileHandle = Gdx.files.internal("i18n/bundle");
        return I18NBundle.createBundle(baseFileHandle, Locale.getDefault());
    }
}
