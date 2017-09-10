package com.mygdx.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.service.DeviceCameraControl;

public class FixedImageCameraService implements DeviceCameraControl {

    private final int previewWidth;
    private final int previewHeight;
    private Texture tex;
    private TextureRegion region;

    public FixedImageCameraService(int width, int height) {
        this.previewWidth = width;
        this.previewHeight = height;
    }

    public void create() {
        tex = new Texture(Gdx.files.internal("medlanky_letiste.png"));
        int width = MathUtils.nextPowerOfTwo(previewWidth);
        int height = MathUtils.nextPowerOfTwo(previewHeight);
        this.tex = new Texture(width, height, Pixmap.Format.RGB888);
        this.region = new TextureRegion(tex);

        Pixmap p = new Pixmap(32, 32, Pixmap.Format.RGBA8888);

        p.setColor(Color.RED);
        p.fillRectangle(0, 0, 32, 32);

        p.setColor(Color.GREEN);
        p.fillRectangle(8, 8, 16, 16);

        this.tex.draw(p, 200, 200);
    }

    @Override
    public void on() {
    }

    @Override
    public void off() {
    }

}
