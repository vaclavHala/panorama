package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ui.PopMenu;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PanoramaPane extends ScreenAdapter {

    private final Terraformer terraformer;

    private final PerspectiveCamera camera3D;
    private final CamController control;
    private final ModelBatch batch3D;
    private final Array<ModelInstance> instances3D;

    //        private final Stage featuresStage;
    private final Stage uiStage;

    private final PopMenu popLeft;
    private final PopMenu popRight;

    public PanoramaPane(Conductor conductor, Terraformer terraformer, Skin skin, Stage uiStage) {
        this.uiStage = uiStage;
        this.terraformer = terraformer;
        this.instances3D = new Array<ModelInstance>();
        this.camera3D = createCamera();
        this.control = new CamController(camera3D);
        this.batch3D = new ModelBatch();
        //        this.featuresStage = new

        this.popLeft = createViewControl(skin);
        this.popRight = createNavControl(skin, conductor);

    }

    private PerspectiveCamera createCamera() {
        PerspectiveCamera cam = new PerspectiveCamera(50, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.up.set(0, 0, 1);
        cam.position.set(0, 0, 0);
        cam.lookAt(1, 0, 0);
        cam.near = .01F;
        cam.far = 10000F;

        return cam;
    }

    private PopMenu createViewControl(Skin skin) {

        List<Map.Entry<String, Runnable>> buttonsLeft = new ArrayList<Map.Entry<String, Runnable>>();
        buttonsLeft.add(new AbstractMap.SimpleEntry<String, Runnable>("red", new Runnable() {

            @Override
            public void run() {
            }
        }));
        buttonsLeft.add(new AbstractMap.SimpleEntry<String, Runnable>("blue", new Runnable() {

            @Override
            public void run() {
            }
        }));
        return new PopMenu(skin, PopMenu.MenuSide.LEFT,
                           uiStage.getWidth(), uiStage.getHeight(),
                           buttonsLeft);
    }

    private PopMenu createNavControl(Skin skin, Conductor conductor) {
        List<Map.Entry<String, Runnable>> buttonsRight = new ArrayList<Map.Entry<String, Runnable>>();
        buttonsRight.add(new AbstractMap.SimpleEntry<String, Runnable>("red", new Runnable() {

            @Override
            public void run() {
            }
        }));
        buttonsRight.add(new AbstractMap.SimpleEntry<String, Runnable>("blue", new Runnable() {

            @Override
            public void run() {
                // this is resources button, we are already there
            }
        }));
        return new PopMenu(skin, PopMenu.MenuSide.RIGHT,
                           uiStage.getWidth(), uiStage.getHeight(),
                           buttonsRight);
    }

    @Override
    public void show() {
        uiStage.addActor(popLeft.actor());
        uiStage.addActor(popRight.actor());

        ModelInstance landscapeInstance = new ModelInstance(terraformer.landscape());

        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage));
    }

    @Override
    public void hide() {
        instances3D.clear();
        uiStage.clear();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        control.update();

        batch3D.begin(camera3D);
        batch3D.render(instances3D);
        batch3D.end();

        uiStage.draw();
    }

    private static void log(String format, Object... args) {
        Gdx.app.log("PANORAMA", String.format(format, args));
    }

}
