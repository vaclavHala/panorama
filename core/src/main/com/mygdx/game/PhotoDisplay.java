package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class PhotoDisplay {

    private final Stage stage;

    public boolean show = true;

    public PhotoDisplay(final TextureRegion tex) {
        this.stage = new Stage(new FillViewport(1, 1));

        Actor photo = new Actor() {

            @Override
            public void draw(Batch batch, float parentAlpha) {

                if (show) {
                    batch.setColor(1, 1, 1, .3F);
                    batch.draw(tex, 0, 0, 1, 1);
                }
            }

        };
        photo.setSize(1, 1);

        this.stage.addActor(photo);
    }

    public void resize(int width, int height) {
        stage.getViewport().setScreenSize(width, height);
        stage.getViewport().update(width, height, true);
    }

    public void render() {
        this.stage.act();
        this.stage.draw();
    }

}
