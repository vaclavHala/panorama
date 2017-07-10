package com.mygdx.game.ui;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import static com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import static com.mygdx.game.ui.Tweaker.TweakerAction;

public class UI implements Disposable {

    private Stage ui;
    private Table layout;
    private Files files;
    private CompasStrip compas;

    public UI(Files files) {
        this.files = files;
    }

    public void create(Skin skin) {
        ui = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(ui);

        layout = new Table().top();
        layout.setFillParent(true);
        layout.setDebug(true);

        TextButtonStyle style = new TextButtonStyle();
        style.up = new NinePatchDrawable(skin.getPatch("default-round"));
        style.down = new NinePatchDrawable(skin.getPatch("default-round-down"));
        style.font = skin.getFont("font");

        TweakerAction lonTweakerAction = new TweakerAction() {

            @Override
            public void react(float newValue) {

            }
        };
        Tweaker lonTweaker = new Tweaker(lonTweakerAction, "LON 1 deg = N world", .5f, 1.5f, .1f, skin);

        TweakerAction dirTweakerAction = new TweakerAction() {

            @Override
            public void react(float newValue) {
                compas.update(newValue);
            }
        };
        Tweaker dirTweaker = new Tweaker(dirTweakerAction, "Cam dir", 0, 360, 1, skin);

        //should be updated in resize to be able to test how it works on different screens
        //        layout.add(compas).expandX().height(Gdx.graphics.getHeight() / 10.0f).row();
        float compasSize = Gdx.graphics.getHeight() / 7f;
        compas = new CompasStrip(67, compasSize, new TextureAtlas(files.internal("compas2.atlas")));
        //        compas.update(180);
        layout.add(compas).height(compasSize).expandX().row();
        layout.add(lonTweaker).left().row();
        layout.add(dirTweaker).left().row();

        //        label = new Label("Text here", skin);
        //        layout.add(label);

        ui.addActor(layout);
    }

    public InputProcessor input() {
        return ui;
    }

    public void resize(int width, int height) {
        // See below for what true means.
        ui.getViewport().update(width, height, true);
    }

    //    private Actor label;
    //    public float labelX;
    //    public float labelY;

    public void render(float delta, double camRot) {
        ui.act(delta);
        //        label.setPosition(labelX, labelY);
        compas.update(camRot);
        ui.draw();
    }

    @Override
    public void dispose() {
        ui.dispose();
        //        atlas
        //                compas
    }

}
