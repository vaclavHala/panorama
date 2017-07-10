package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class FeatureLabel extends Actor {

    private static final String TAG = "FEATURE";

    private final NinePatch patch;
    private final BitmapFont font;
    private final WorldToScreenProjection projection;
    private final Vector3 featureWorldPosition;
    private final String featureName;
    private final Vector2 featureScreenPosition;

    public FeatureLabel(
            TextureAtlas atlat, Skin skin,
            WorldToScreenProjection projection,
            Feature feature) {
        this.patch = atlat.createPatch("label_leg.dark");
        this.font = skin.getFont("font");
        this.featureWorldPosition = feature.position;
        this.projection = projection;
        this.featureName = feature.name;
        this.featureScreenPosition = new Vector2();

    }

    private Vector3 origin;

    @Override
    public void act(float delta) {
        super.act(delta);
        Vector2 screenPos = this.projection.toScreen(featureWorldPosition);
        this.featureScreenPosition.x = screenPos.x;
        this.featureScreenPosition.y = screenPos.y;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (this.featureScreenPosition.y < 0) {
            return; // feature is offscreen
        }
        this.font.draw(batch, featureName,
                       this.featureScreenPosition.x, Gdx.graphics.getHeight() - 100 + 20);
        float legLength = Gdx.graphics.getHeight() - 100 - this.featureScreenPosition.y;
        this.patch.draw(batch,
                        this.featureScreenPosition.x, this.featureScreenPosition.y,
                        4, legLength);

    }

}
