package com.mygdx.game.desktop.demo;

import com.mygdx.game.desktop.*;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Demo3d;
import com.mygdx.game.Panorama;
import com.mygdx.game.demo.DemoUI;

public class DemoUILauncher {

    public static void main(String[] arg) {
        //        Settings settings = new Settings();
        //		settings.maxWidth = 512;
        //		settings.maxHeight = 512;
        //		TexturePacker.process(settings, "../images", "../game-android/assets", "game");

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        new LwjglApplication(new DemoUI(), config);
    }

}
