package com.mygdx.game.model;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.mygdx.game.common.CoordTransform;
import java.util.List;

public class FeaturesDisplay {

    private final Stage stage;

    public FeaturesDisplay(List<Feature> features,
            TextureAtlas atlas, Skin skin, Camera cam,
            CoordTransform coordTrans, Visibility visibility) {

        this.stage = new Stage();
        WorldToScreenProjection projection = new WorldToScreenProjection(cam);
        for (Feature feature : features) {
            System.out.println("Feature: " + feature);
            Vector3 featureWorldPosition = coordTrans.toInternal(feature.position, new Vector3());
            IsVisible isVisible = new IsVisible(visibility, cam, featureWorldPosition);
            Actor featureLabel = new FeatureLabel(feature.name, featureWorldPosition, atlas, skin, projection, isVisible);
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
