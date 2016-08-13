package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.mygdx.game.model.Convertor;
import com.mygdx.game.model.InMemoryFileHandle;
import com.mygdx.game.model.LandscapeLoader;
import com.mygdx.game.model.Loader;
import com.mygdx.game.ui.UI;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyGdxGame extends ApplicationAdapter {

    PerspectiveCamera cam;
    CameraInputController control;

    Array<ModelInstance> instances = new Array<ModelInstance>();
    ModelBatch modelBatch;
    UI ui;


    @Override
    public void create() {
        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 0, 0);
        cam.lookAt(0, 0, 0);
        cam.near = .1f;
        cam.far = 25000f;
        cam.update();
        control = new CameraInputController(cam);

        ui = new UI(Gdx.files);
        ui.create();


//        environment = new Environment();
//        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0f, -1f, 0f));

//        ElevConfig elevCfg = new ElevConfig(5,5,1200,1200);
        ElevConfig elevCfg = new ElevConfig(1,1,10,10);

        Gdx.app.log("MAIN", elevCfg.toString());

        LandscapeLoader loader = new LandscapeLoader(elevCfg);


//        try {
//            landscape = loader.loadLandscape(new Rectangle(14.3f, 48.8f, .1f, .1f));
//        } catch (IOException e){
//            throw new IllegalStateException(e);
//        }

//        Array<Vector3> pointsLandscape = new Array<Vector3>();
//        for (int y = -60; y < 60; y++) {
//            for (int x = -60; x < 60; x++) {
//                pointsLandscape.add(new Vector3(
//                        x,
//                        (float) Math.random(),
//                        y));
//            }
//        }

//        String g3djLandscape = null;//new Convertor().pointsToLandscape(landscape);

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
//        FileHandle modelDataHandle = new InMemoryFileHandle(g3djLandscape);

//        Model model = new G3dModelLoader(new JsonReader()).loadModel(modelDataHandle);
        ModelData landscapeModel=   loader.loadModelData(1, 1);
//        ModelData landscapeModel=   loader.loadModelData(15, 50);
        Model model= new Model(landscapeModel);
        System.out.println("Got landscape model");

        ModelBuilder builder = new ModelBuilder();

        Material matX = new Material(ColorAttribute.createDiffuse(Color.RED));
        ModelInstance arrX = new ModelInstance(builder.createArrow(new Vector3(0,0,0),new Vector3(1,0,0),matX, 1));
        instances.add(arrX);

        Material matY = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        ModelInstance arrY = new ModelInstance(builder.createArrow(new Vector3(0,0,0),new Vector3(0,1,0),matY, 1));
        instances.add(arrY);

        Material matZ = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        ModelInstance arrZ = new ModelInstance(builder.createArrow(new Vector3(0,0,0),new Vector3(0,0,1),matZ, 1));
        instances.add(arrZ);

        Material matGrid = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        ModelInstance grid = new ModelInstance(builder.createLineGrid(1000,1000,1,1,matGrid,1));
        instances.add(grid);

        ModelInstance landscapeInstance = new ModelInstance(model);
//        landscapeInstance.transform = new Matrix4(Vector3.Zero, new Quaternion(), new Vector3(1,.1f,1));
        instances.add(landscapeInstance);


        InputMultiplexer inMux = new InputMultiplexer(ui.input(), control);
        Gdx.input.setInputProcessor(inMux);

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
    public void resize(int width, int height) {
        ui.resize(width, height);
        super.resize(width, height);
    }

    @Override
    public void render() {

        control.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(this.instances);
        modelBatch.end();

//        System.out.format("x:%f y:%f z:%f\n",cam.direction.x, cam.direction.y, cam.direction.z);

        Vector2 projectedToGround = new Vector2(cam.direction.x, cam.direction.z);
        double camRot = - projectedToGround.angleRad(Vector2.X)* MathUtils.radiansToDegrees + 90;

        ui.render(Gdx.graphics.getDeltaTime(), camRot);
    }

    @Override
    public void dispose() {
        instances.clear();
        modelBatch.dispose();
        ui.dispose();
//        assets.dispose();
    }
}
