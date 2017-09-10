package com.mygdx.game.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class DemoTiming extends ApplicationAdapter {

    private ModelBatch batch3D;
    private Stage stage;

    @Override
    public void create() {
        long start;
        long end;

        start = System.currentTimeMillis();
        batch3D = new ModelBatch();
        end = System.currentTimeMillis();
        System.out.println("batch3D: " + (end - start));

        start = System.currentTimeMillis();
        stage = new Stage();
        end = System.currentTimeMillis();
        System.out.println("stage: " + (end - start));

    }

}
