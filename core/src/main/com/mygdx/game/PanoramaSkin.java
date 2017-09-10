package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class PanoramaSkin {

    public static Skin load() {
        TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"), Gdx.files.internal(""));
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"), uiAtlas);
        //        skin.addRegions(uiAtlas);
        //        skin.add("font", new BitmapFont(Gdx.files.internal("default.fnt"), Gdx.files.internal("default.png"), false));
        //        skin.add("default", new Label.LabelStyle(skin.getFont("font"), Color.BLACK));
        //        skin.add("default", new TextField.TextFieldStyle(skin.getFont("font"), Color.BLACK, ));

        Pixmap menuBackground = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        menuBackground.setColor(Color.CYAN);
        menuBackground.fillRectangle(0, 0, 1, 1);
        skin.add("menu-background", new Texture(menuBackground));

        Pixmap red = new Pixmap(25, 25, Pixmap.Format.RGBA8888);
        red.setColor(Color.RED);
        red.fillRectangle(0, 0, 25, 25);
        skin.add("red", new Texture(red));

        Pixmap blue = new Pixmap(25, 25, Pixmap.Format.RGBA8888);
        blue.setColor(Color.BLUE);
        blue.fillRectangle(0, 0, 25, 25);
        skin.add("blue", new Texture(blue));

        return skin;
    }

    private PanoramaSkin() {
    }

}
