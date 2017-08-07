package com.mygdx.game.demo;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.service.DeviceCameraControl;
import static com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createDiffuse;

public class CameraDemo implements ApplicationListener {



    private PerspectiveCamera camera;

    private final DeviceCameraControl deviceCameraControl;


    public CameraDemo(DeviceCameraControl cameraControl) {
        this.deviceCameraControl = cameraControl;
    }

    Array<ModelInstance> instances = new Array<ModelInstance>();

    ModelBatch batch;

    @Override
    public void create() {


        batch = new ModelBatch();
        ModelBuilder builder = new ModelBuilder();
        Model box = builder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position);
        ModelInstance boxInstance = new ModelInstance(box);
        instances.add(boxInstance);


        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10f, 10f, 10f);
        camera.lookAt(0, 0, 0);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        Gdx.input.setInputProcessor(new InputMultiplexer(new DebugInput(deviceCameraControl), new CameraInputController(camera)));
    }

    private static class DebugInput extends InputAdapter {

        boolean camOn = false;
        DeviceCameraControl ctrl;

        public DebugInput(DeviceCameraControl ctrl) {
            this.ctrl = ctrl;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            log("INPUT", "Topuch down");
            if(!camOn){
                ctrl.on();
                log("INPUT", "Cam on");
            } else {
                ctrl.off();
                log("INPUT", "Cam off");
            }
            camOn = !camOn;
            return false;
        }
    };

    @Override
    public void dispose() {

    }


    @Override
    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        batch.begin(camera);
        batch.render(instances);
        batch.end();

    }
    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private static final String TAG = CameraDemo.class.getSimpleName();

    private static void log(String format, Object... args) {
        Gdx.app.log(TAG, String.format(format, args));
    }
}
