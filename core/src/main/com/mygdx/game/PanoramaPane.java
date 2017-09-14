package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.common.CoordTransform;
import com.mygdx.game.model.FeaturesDisplay;
import com.mygdx.game.model.Visibility;
import static java.lang.String.format;

public class PanoramaPane extends ScreenAdapter {

    private static final int LABEL_TOUCH_UP_LINGER_MS = 1000;

    private final Terraformer terraformer;
    private final Visibility visibility;

    private final PerspectiveCamera camera3D;
    private final CamController control;
    private final ModelBatch batch3D;
    private final Array<ModelInstance> instances3D;
    private final FeaturesDisplay featuresDisplay;
    private final CoordTransform coordTrans;

    private final Stage uiStage;
    // TODO put label into UI as actor
    private final BitmapFont font;

    private final Vector3 tmp1;
    private final Vector3 tmp2;

    //    private final PopMenu popLeft;
    //    private final PopMenu popRight;

    private SpriteBatch batch;
    private float touchUpTime;

    public PanoramaPane(
            Conductor conductor,
            Terraformer terraformer,
            Skin skin,
            Stage uiStage,
            CoordTransform coordTrans) {
        this.uiStage = uiStage;
        this.coordTrans = coordTrans;
        this.terraformer = terraformer;
        this.visibility = terraformer.visibility();
        this.instances3D = new Array<ModelInstance>();
        this.camera3D = createCamera();
        this.control = new CamController(camera3D);
        this.batch3D = new ModelBatch();
        //        this.featuresStage = new

        batch = new SpriteBatch();
        this.font = skin.getFont("default-font");
        featuresDisplay = new FeaturesDisplay(terraformer.features(),
                                              skin, camera3D,
                                              coordTrans, visibility);

        //        this.popLeft = createViewControl(skin);
        //        this.popRight = createNavControl(skin, conductor);

        this.tmp1 = new Vector3();
        this.tmp2 = new Vector3();
    }

    private PerspectiveCamera createCamera() {
        PerspectiveCamera cam = new PerspectiveCamera(50, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.up.set(0, 0, 1);
        cam.position.set(0, 0, 0);
        cam.lookAt(1, 0, 0);
        cam.near = .01F;
        cam.far = 10000F;
        cam.update();

        return cam;
    }

    private void createAxesAndGrid() {
        Material matX = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material matY = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material matZ = new Material(ColorAttribute.createDiffuse(Color.BLUE));

        ModelBuilder builder = new ModelBuilder();

        ModelInstance arrX = new ModelInstance(builder.createArrow(new Vector3(0, 0, 0), new Vector3(1, 0, 0), matX, 1));
        instances3D.add(arrX);

        ModelInstance arrY = new ModelInstance(builder.createArrow(new Vector3(0, 0, 0), new Vector3(0, 1, 0), matY, 1));
        instances3D.add(arrY);

        ModelInstance arrZ = new ModelInstance(builder.createArrow(new Vector3(0, 0, 0), new Vector3(0, 0, 1), matZ, 1));
        instances3D.add(arrZ);

        //        Material matGrid = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        //        ModelInstance grid = new ModelInstance(builder.createLineGrid(1000, 1000, 1, 1, matGrid, 1));
        //        grid.transform.rotate(Vector3.X, 90);
        //        instances.add(grid);
    }

    //    private PopMenu createViewControl(Skin skin) {
    //
    //        List<Map.Entry<String, Runnable>> buttonsLeft = new ArrayList<Map.Entry<String, Runnable>>();
    //        buttonsLeft.add(new AbstractMap.SimpleEntry<String, Runnable>("red", new Runnable() {
    //
    //            @Override
    //            public void run() {
    //            }
    //        }));
    //        buttonsLeft.add(new AbstractMap.SimpleEntry<String, Runnable>("blue", new Runnable() {
    //
    //            @Override
    //            public void run() {
    //            }
    //        }));
    //        return new PopMenu(skin, PopMenu.MenuSide.LEFT,
    //                           uiStage.getWidth(), uiStage.getHeight(),
    //                           buttonsLeft);
    //    }
    //
    //    private PopMenu createNavControl(Skin skin, Conductor conductor) {
    //        List<Map.Entry<String, Runnable>> buttonsRight = new ArrayList<Map.Entry<String, Runnable>>();
    //        buttonsRight.add(new AbstractMap.SimpleEntry<String, Runnable>("red", new Runnable() {
    //
    //            @Override
    //            public void run() {
    //            }
    //        }));
    //        buttonsRight.add(new AbstractMap.SimpleEntry<String, Runnable>("blue", new Runnable() {
    //
    //            @Override
    //            public void run() {
    //                // this is resources button, we are already there
    //            }
    //        }));
    //        return new PopMenu(skin, PopMenu.MenuSide.RIGHT,
    //                           uiStage.getWidth(), uiStage.getHeight(),
    //                           buttonsRight);
    //    }

    @Override
    public void show() {
        //        uiStage.addActor(popLeft.actor());
        //        uiStage.addActor(popRight.actor());

        this.instances3D.add(new ModelInstance(terraformer.landscape()));
        createAxesAndGrid();

        Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, control));
    }

    @Override
    public void hide() {
        instances3D.clear();
        uiStage.clear();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        control.update();

        batch3D.begin(camera3D);
        batch3D.render(instances3D);
        batch3D.end();

        featuresDisplay.render();
        uiStage.draw();

        tmp1.set(Gdx.input.getX(), Gdx.input.getY(), 1);
        camera3D.unproject(tmp1);
        Vector3 intersection = visibility.terrainIntersection(camera3D.position, tmp1, tmp2);
        if (Gdx.input.isTouched()) {
            touchUpTime = 0;
        } else {
            touchUpTime += delta;
        }
        if (intersection != null && touchUpTime < LABEL_TOUCH_UP_LINGER_MS) {
            batch.begin();
            float lat = coordTrans.toExternalLat(intersection.y);
            float lon = coordTrans.toExternalLon(intersection.x);
            String label = format("%c%f, %c%f",
                                  lat < 0 ? 's' : 'n', Math.abs(lat),
                                  lon < 0 ? 'w' : 'e', Math.abs(lon));
            font.draw(batch, label, Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
            batch.end();
        }

    }

    private static void log(String format, Object... args) {
        Gdx.app.log("PANORAMA", String.format(format, args));
    }

}
