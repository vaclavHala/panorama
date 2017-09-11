package com.mygdx.game.demo;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.ChunksService;
import com.mygdx.game.PanoramaSkin;
import com.mygdx.game.ui.NewPanoramaPane;
import com.mygdx.game.ui.PopMenu;
import com.mygdx.game.ui.PopMenu.MenuSide;
import com.mygdx.game.ui.ResourcesPane;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map.Entry;

public class DemoUI extends ApplicationAdapter {

    ChunksService chunks;
    Stage uiStage;
    Skin skin;

    NewPanoramaPane newPanorama;
    ResourcesPane resources;

    PopMenu popRight;
    PopMenu popLeft;

    @Override
    public void create() {
        System.out.println("CREATE");

        uiStage = new Stage(new StretchViewport(100, Gdx.graphics.getWidth() / Gdx.graphics.getHeight() * 100));
        //        uiStage = new Stage();
        //        uiStage.getCamera().translate(-300, -300, 0);
        uiStage.getCamera().update();
        uiStage.setDebugAll(true);
        skin = PanoramaSkin.load();
        //        newPanorama = new NewPanoramaPane(skin);
        //        uiStage.addActor(newPanorama.pane());
        //        resources = new ResourcesPane(skin, new Chunks(null));
        //        uiStage.addActor(resources.actor());

        List<Entry<String, Runnable>> buttons = new ArrayList<Entry<String, Runnable>>();
        buttons.add(new SimpleEntry<String, Runnable>("red", null));
        buttons.add(new SimpleEntry<String, Runnable>("red", null));
        buttons.add(new SimpleEntry<String, Runnable>("red", null));
        popRight = new PopMenu(skin, MenuSide.RIGHT,
                               uiStage.getWidth(), uiStage.getHeight(),
                               buttons);
        popLeft = new PopMenu(skin, MenuSide.LEFT,
                              uiStage.getWidth(), uiStage.getHeight(),
                              buttons);

        Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pix.setColor(Color.RED);
        pix.fillRectangle(0, 0, 1, 1);
        Actor a = new Image(new Texture(pix));
        a.setWidth(10);
        a.setHeight(10);
        a.setPosition(0, 0);

        uiStage.addActor(popRight.actor());
        uiStage.addActor(popLeft.actor());
        uiStage.addActor(a);

        //        pop.actor().setPosition(200, uiStage.getHeight());

        Gdx.input.setInputProcessor(new InputMultiplexer(debugInput, uiStage));
    }

    private InputProcessor debugInput = new InputAdapter() {

        @Override
        public boolean keyDown(int keycode) {

            if (keycode == Input.Keys.E) {
                popRight.enable();
            } else if (keycode == Input.Keys.D) {
                popRight.disable();
            }

            return false;
        }

    };

    @Override
    public void resize(int width, int height) {
        System.out.println("RESIZE");
        uiStage.getViewport().update(width, height);
    }

    @Override
    public void render() {
        uiStage.act();
        uiStage.draw();
    }
}
