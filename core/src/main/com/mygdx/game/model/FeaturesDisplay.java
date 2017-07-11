package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import java.util.List;

public class FeaturesDisplay {

    private final Stage stage;

    public FeaturesDisplay(List<Feature> features, Vector3 origin,
            TextureAtlas atlas, Skin skin, Camera cam) {

        this.stage = new Stage();
        WorldToScreenProjection projection = new WorldToScreenProjection(cam, origin);
        for (Feature feature : features) {
            Actor featureLabel = new FeatureLabel(atlas, skin, projection, feature);
            this.stage.addActor(featureLabel);
        }
    }

    public void resize(int width, int height) {
        // See below for what true means.
        stage.getViewport().update(width, height, true);
    }

    public void render() {
        this.stage.act();
        this.stage.draw();
    }

}
