package com.mygdx.game.desktop.demo;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Demo3d;
import java.io.File;
import java.nio.file.Path;

public class Demo3dLauncher {

    public static void main(String[] arg) {
        //        Settings settings = new Settings();
        //		settings.maxWidth = 512;
        //		settings.maxHeight = 512;
        //		TexturePacker.process(settings, "../images", "../game-android/assets", "game");

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new Demo3d(), config);
    }
}
