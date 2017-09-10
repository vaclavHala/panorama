package com.mygdx.game.desktop.demo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.demo.DemoTiming;
import com.mygdx.game.demo.DemoUI;

public class DemoTimingLauncher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        new LwjglApplication(new DemoTiming(), config);
    }

}
