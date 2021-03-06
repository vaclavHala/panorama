package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.Panorama;
import com.mygdx.game.desktop.service.MockLocationServicePush;
import com.mygdx.game.service.LocationServicePush;

public class DesktopLauncher {

    public static void main(String[] arg) {
        //        Settings settings = new Settings();
        //		settings.maxWidth = 512;
        //		settings.maxHeight = 512;
        //		TexturePacker.process(settings, "../images", "../game-android/assets", "game");

        System.out.println(DesktopLauncher.class.getResource("/uiskin.atlas"));

        LocationServicePush gps = new MockLocationServicePush(16.5905F, 49.215F);

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        new LwjglApplication(new Panorama(gps, null), config);
    }
}
