package com.mygdx.game;

import com.badlogic.gdx.Screen;

public interface Conductor {

    void screen(Class<? extends Screen> screen);

}
