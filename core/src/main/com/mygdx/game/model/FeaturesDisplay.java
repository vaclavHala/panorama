package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.mygdx.game.common.CoordTransform;
import java.util.List;

public class FeaturesDisplay {

    private final Stage stage;

    public FeaturesDisplay(List<Feature> features,
            TextureAtlas atlas, Skin skin, Camera cam,
            CoordTransform coordTrans, Visibility visibility) {

        Texture t;
        Pixmap p;

        this.stage = new Stage(new StretchViewport(1024, 480));
        WorldToScreenProjection projection = new WorldToScreenProjection(cam);
        for (Feature feature : features) {
            System.out.println("Feature: " + feature);
            Vector3 featureWorldPosition = coordTrans.toInternal(feature.position.cpy().add(0, 0, 20), new Vector3());
            IsVisible isVisible = new IsVisible(visibility, cam, featureWorldPosition);
            Actor featureLabel = new FeatureLabel(feature.name, featureWorldPosition, atlas, skin, projection, isVisible);
            this.stage.addActor(featureLabel);
        }
    }

    public void resize(int width, int height) {
        // See below for what true means.
        stage.getViewport().setScreenSize(width, height);
        stage.getViewport().setWorldSize(width, height);
        stage.getViewport().update(width, height, true);
        stage.getCamera().viewportWidth = width;
        stage.getCamera().viewportHeight = height;
    }

    public void render() {
        this.stage.act();
        this.stage.draw();
    }

}
