package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.mygdx.game.model.Convertor;
import com.mygdx.game.model.InMemoryFileHandle;
import com.mygdx.game.model.Loader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyGdxGame extends ApplicationAdapter {

    PerspectiveCamera cam;
    CameraInputController control;

    Array<ModelInstance> instances = new Array<ModelInstance>();
    ModelBatch modelBatch;
//    Environment environment;
    //     AssetManager assets;
    boolean loading;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 10f, 0f);
        cam.lookAt(0, 0, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
        control = new CameraInputController(cam);
        Gdx.input.setInputProcessor(control);

//        environment = new Environment();
//        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0f, -1f, 0f));

        Loader loader = new Loader(
                Gdx.files,
                1, 1,
                1200, 1200);

        Array<Vector3> landscape;

        try {
            landscape = loader.loadLandscape(new Rectangle(14.3f, 48.8f, .1f, .1f));
        } catch (IOException e){
            throw new IllegalStateException(e);
        }

//        Array<Vector3> pointsLandscape = new Array<Vector3>();
//        for (int y = -60; y < 60; y++) {
//            for (int x = -60; x < 60; x++) {
//                pointsLandscape.add(new Vector3(
//                        x,
//                        (float) Math.random(),
//                        y));
//            }
//        }

        String g3djLandscape = new Convertor().pointsToLandscape(landscape);

//        try {
//            BufferedWriter w = Files.newBufferedWriter(Paths.get("gen.g3dj"));
//            w.write(g3djLandscape);
//            w.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        System.out.println("Got raw landscape");

//        String modelData = new Convertor().pointsToLandscape(new Array<Vector3>(new Vector3[]{
//                new Vector3(0,0,0),
//                new Vector3(0,0,1),
//                new Vector3(1,0,1),
//                new Vector3(1,0,0),
//        }));
        FileHandle modelDataHandle = new InMemoryFileHandle(g3djLandscape);

        Model model = new G3dModelLoader(new JsonReader()).loadModel(modelDataHandle);
        System.out.println("Got landscape model");

        Gdx.app.log("MAIN", ""+model.materials.get(0));

        instances.add(new ModelInstance(model));

//        assets = new AssetManager();
//        assets.load("gen.g3dj", Model.class);
//        loading = true;
    }

//    private void doneLoading() {
//        Model ship = assets.get("gen.g3dj", Model.class);
//        ModelInstance shipInstance = new ModelInstance(ship);
//        instances.add(shipInstance);
//        loading = false;
//    }

    @Override
    public void render() {

        control.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(this.instances);
        modelBatch.end();
    }

    @Override
    public void dispose() {
        instances.clear();
        modelBatch.dispose();
//        assets.dispose();
    }
}
