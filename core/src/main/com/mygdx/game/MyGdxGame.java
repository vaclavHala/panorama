package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import static com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createDiffuse;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.camera.CameraService;
import com.mygdx.game.common.CoordTransform;
import com.mygdx.game.model.*;
import com.mygdx.game.model.HardwiredFeatureLookup.RawFeature;
import com.mygdx.game.ui.UI;
import java.util.ArrayList;
import java.util.List;

// Real world coordinates:
// lon, lat in degrees ~ (x, y)
// elevation in meters above sea level ~ (z)
// Length in meters of 1° of latitude = always 111.32 km
// Length in meters of 1° of longitude = 40075 km * cos( latitude ) / 360
// ~ 78.72 at 45°

// Banana coordinate system is mapped as follows:
// elevCfg and userOffset (position of user) are constant once established
// [0,0,0] ~ position of user
// X ~ longitude * elevCfg.scalerLon - userOffset.x
// Y ~ latitude * elevCfg.scalerLat - userOffsety.y
// Z ~ elevation * elevCfg.scalerElev - userOffset.z

public class MyGdxGame extends ApplicationAdapter {

    final CameraService camService;

    PerspectiveCamera cam;
    CamController control;

    Array<ModelInstance> instances = new Array<ModelInstance>();
    ModelBatch modelBatch;
    UI ui;
    PhotoDisplay photoDisplay;
    FeaturesDisplay featuresDisplay;

    Skin skin;

    ModelBuilder builder = new ModelBuilder();

    //    ElevConfig elevCfg;
    CoordTransform coordTrans;

    ModelInstance landscape1;
    ModelInstance landscape10;

    public static boolean allFeaturesVisible = false;

    public MyGdxGame(CameraService camService) {
        this.camService = camService;
    }

    static float metersToBanana(float meters) {
        return meters;
    }

    static Vector3 metersToBanana(Vector3 meters) {
        return new Vector3(metersToBanana(meters.x),
                           metersToBanana(meters.y),
                           metersToBanana(meters.z));
    }

    @Override
    public void create() {
        camService.create();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        createCamera();

        modelBatch = new ModelBatch();

        ElevConfig elevCfg = new ElevConfig(1, 1, 3601, 3601, 3601, 3601);

        ElevConfig elevCfg1 = new ElevConfig(1, 1, 3601, 3601, 3601, 3601);
        ElevConfig elevCfg10 = new ElevConfig(1, 1, 3601 / 10 + 1, 3601 / 10 + 1, 3601 / 10.0F, 3601 / 10.0F);

        Vector3 meReal = new Vector3(16.5554625F, 49.2344711F, 290);
        //        Vector3 meReal = new Vector3(14.2834F, 48.8649F, 1100);

        coordTrans = new CoordTransform(metersToBanana(111000F), metersToBanana(111000F), metersToBanana(1F), meReal);
        log("COORD transform: " + coordTrans);

        LandscapeLoader loader1 = new LandscapeLoader(new CoarsedElevDataFactory(elevCfg, Gdx.files, 1), elevCfg1, coordTrans);
        LandscapeLoader loader10 = new LandscapeLoader(new CoarsedElevDataFactory(elevCfg, Gdx.files, 10), elevCfg10, coordTrans);

        float sizeDeg = 0.05F;
        ModelData landscapeModelData1 = loader1.loadModelData(meReal.x - sizeDeg / 2.0F, meReal.y - sizeDeg / 2.0F, sizeDeg, sizeDeg);
        ModelData landscapeModelData10 = loader10.loadModelData(meReal.x - sizeDeg / 2.0F, meReal.y - sizeDeg / 2.0F, sizeDeg, sizeDeg);

        ModelMesh landscapeMesh = landscapeModelData1.meshes.first();
        ModelMeshPart landscapeTris = landscapeMesh.parts[0];
        int landscapeVertComponents = 4;
        //        for (VertexAttribute attr : landscapeMesh.attributes) {
        //            landscapeVertComponents += attr.numComponents;
        //        }
        log("Landscape vertComponents: " + landscapeVertComponents);

        System.out.println("Got landscape model");
        landscapeInfo(landscapeModelData1);
        landscapeInfo(landscapeModelData10);
        //        Model landscapeModel1 = new Model(landscapeModelData1);
        Model landscapeModel10 = new Model(landscapeModelData10);
        //        landscapeModel1.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        //        landscapeModel1.materials.get(0).set(FloatAttribute.createAlphaTest(0.1f));
        //        landscapeModel10.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        //        landscape1 = new ModelInstance(landscapeModel1);
        landscape10 = new ModelInstance(landscapeModel10);
        //        instances.add(landscape1);
        instances.add(landscape10);

        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F + sizeDeg * 0.99F, meReal.y - sizeDeg / 2.0F, sizeDeg, sizeDeg))));
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F - sizeDeg * 0.99F, meReal.y - sizeDeg / 2.0F, sizeDeg, sizeDeg))));
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F, meReal.y - sizeDeg / 2.0F + sizeDeg * 0.99F, sizeDeg, sizeDeg))));
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F, meReal.y - sizeDeg / 2.0F - sizeDeg * 0.99F, sizeDeg, sizeDeg))));
        //
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F + sizeDeg * 0.99F,
        //                                                                        meReal.y - sizeDeg / 2.0F + sizeDeg * 0.99F,
        //                                                                        sizeDeg, sizeDeg))));
        //
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F + sizeDeg * 0.99F,
        //                                                                        meReal.y - sizeDeg / 2.0F - sizeDeg * 0.99F,
        //                                                                        sizeDeg, sizeDeg))));
        //
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F - sizeDeg * 0.99F,
        //                                                                        meReal.y - sizeDeg / 2.0F + sizeDeg * 0.99F,
        //                                                                        sizeDeg, sizeDeg))));
        //
        //        instances.add(new ModelInstance(new Model(loader1.loadModelData(meReal.x - sizeDeg / 2.0F - sizeDeg * 0.99F,
        //                                                                        meReal.y - sizeDeg / 2.0F - sizeDeg * 0.99F,
        //                                                                        sizeDeg, sizeDeg))));

        //        FrameBuffer fbo; fbo.
        createAxesAndGrid();

        ElevationResolution elevResolution =
                new ElevationResolution(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents, coordTrans);
        float meElev = elevResolution.projectToLandscape(meReal.x, meReal.y);
        log("My elevation: " + meElev);

        cam.position.z = coordTrans.toInternalElev(meElev + 2);
        control.target.set(cam.position);
        cam.update();

        //        addLine(new Vector3(0, 0, cam.position.z),
        //                new Vector3(0, 0, 0),
        //                Color.YELLOW);

        addLine(new Vector3(metersToBanana(1), metersToBanana(1), cam.position.z),
                new Vector3(metersToBanana(1), metersToBanana(1), cam.position.z - metersToBanana(1)),
                Color.RED);
        addLine(new Vector3(metersToBanana(1), metersToBanana(1), cam.position.z - metersToBanana(1)),
                new Vector3(metersToBanana(1), metersToBanana(1), cam.position.z - metersToBanana(2)),
                Color.GREEN);
        addLine(new Vector3(metersToBanana(1.5F), metersToBanana(1), cam.position.z),
                new Vector3(metersToBanana(1.5F), metersToBanana(1), cam.position.z - metersToBanana(10)),
                Color.BLUE);

        Visibility visibility = new Visibility(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents);

        TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"), Gdx.files.internal(""));
        skin = new Skin();
        skin.addRegions(uiAtlas);
        skin.add("font", new BitmapFont(Gdx.files.internal("default.fnt"), Gdx.files.internal("default.png"), false));
        skin.add("default", new Label.LabelStyle(skin.getFont("font"), Color.BLACK));

        ui = new UI(Gdx.files);
        ui.create(skin);

        createFeatures(elevResolution, visibility, meReal, sizeDeg);

        InputMultiplexer inMux = new InputMultiplexer(debugInput, ui.input(), control);
        Gdx.input.setInputProcessor(inMux);

    }

    private static class CoarsedElevDataFactory implements LandscapeLoader.ElevDataFactory {

        private final ElevConfig elevCfg;
        private final Files files;
        private final int detail;

        public CoarsedElevDataFactory(ElevConfig elevCfg, Files files, int detail) {
            this.elevCfg = elevCfg;
            this.files = files;
            this.detail = detail;
        }

        @Override
        public ElevData chunk(String chunkName) {
            return new CoarseElevData(new FileBackedElevData(files, chunkName), detail,
                                      elevCfg.chunkWidthCells, elevCfg.chunkHeightCells);
        }
    }

    private void createCamera() {
        cam = new PerspectiveCamera(50, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.up.set(0, 0, 1);
        cam.position.set(0, 0, 0);
        cam.lookAt(1, 0, 0);
        cam.near = .01F;
        cam.far = 10000F;

        control = new CamController(cam);
        control.target.set(0, 0, 2);
    }

    private void createFeatures(ElevationResolution elevResolution, Visibility visibility, Vector3 meReal, float sizeDeg) {
        //        FeatureLookup featureLookup = new FileBackedFeatureLookup(Gdx.files, elevResolution);
        FeatureLookup featureLookup = new HardwiredFeatureLookup(elevResolution,
                                                                 //                                                                 new RawFeature("Klet", 14.2834500, 48.8649861),
                                                                 //                                                                 new RawFeature("Bily Kamen", 14.2940883, 48.8532056),
                                                                 //                                                                 new RawFeature("Ohrada", 14.2736606, 48.8518642),
                                                                 //                                                                 new RawFeature("U Piskovny", 14.2697553, 48.8685222),
                                                                 //                                                                 new RawFeature("Nad Javorem", 14.2882947, 48.8773289),
                                                                 //                                                                 new RawFeature("Na Rovine", 14.2646483, 48.8788250)
                                                                 new RawFeature("Strelecky", 16.5679831F, 49.2343414F),
                                                                 new RawFeature("Palackeho", 16.5673683F, 49.2249864F)

                );

        // put enough margin around edges to avoid features not above landscape
        List<Feature> features = featureLookup.lookup(meReal.x - sizeDeg / 2.0F + 0.0005F, meReal.y - sizeDeg / 2.0F + 0.0005F,
                                                      sizeDeg - 0.001F, sizeDeg - 0.001F);

        TextureAtlas featuresAtlas = new TextureAtlas(Gdx.files.internal("features.atlas"));
        //        AtlasRegion region = atlas.findRegion("imagename");
        //        Sprite sprite = atlas.createSprite("otherimagename");
        //        NinePatch patch = atlas.createPatch("patchimagename");

        featuresDisplay = new FeaturesDisplay(features,
                                              featuresAtlas, skin, cam,
                                              coordTrans, visibility);
        photoDisplay = new PhotoDisplay(camService.cameraView());

        //        builder.begin();
        //        {
        //            MeshPartBuilder meshBuilder = builder.part("line", 1, 3, new Material());
        //            meshBuilder.setColor(Color.RED);
        //            meshBuilder.line(Vector3.Zero, new Vector3(1000, 0, 0));
        //        }
        //        Model lineModel = builder.end();
        //
        //        for (Feature f : features) {
        //            System.out.println("feature: " + f);
        //            ModelInstance lineInstance = new ModelInstance(lineModel);
        //            lineInstance.userData = f.position;
        //
        //            instances.add(lineInstance);
        //            featureRays.add(lineInstance);
        //            addPoint(coordTrans.toInternal(f.position, new Vector3()), Color.GOLDENROD);
        //        }
    }

    private void createAxesAndGrid() {
        Material matX = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material matY = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material matZ = new Material(ColorAttribute.createDiffuse(Color.BLUE));

        //        testIntersect();
        ModelInstance arrX = new ModelInstance(builder.createArrow(new Vector3(0, 0, 0), new Vector3(1, 0, 0), matX, 1));
        instances.add(arrX);

        ModelInstance arrY = new ModelInstance(builder.createArrow(new Vector3(0, 0, 0), new Vector3(0, 1, 0), matY, 1));
        instances.add(arrY);

        ModelInstance arrZ = new ModelInstance(builder.createArrow(new Vector3(0, 0, 0), new Vector3(0, 0, 1), matZ, 1));
        instances.add(arrZ);

        //        Material matGrid = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        //        ModelInstance grid = new ModelInstance(builder.createLineGrid(1000, 1000, 1, 1, matGrid, 1));
        //        grid.transform.rotate(Vector3.X, 90);
        //        instances.add(grid);
    }

    public static final Vector3 anchor = new Vector3();

    InputProcessor debugInput = new InputAdapter() {

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.A) {
                anchor.set(cam.position);
            } else if (keycode == Input.Keys.F) {
                allFeaturesVisible = !allFeaturesVisible;
            } else if (keycode == Input.Keys.P) {
                photoDisplay.show = !photoDisplay.show;
            } else if (keycode == Input.Keys.NUM_1) {
                landscape1.transform.setToScaling(1, 1, 1);
                landscape10.transform.setToScaling(0, 0, 0);
            } else if (keycode == Input.Keys.NUM_2) {
                landscape1.transform.setToScaling(0, 0, 0);
                landscape10.transform.setToScaling(1, 1, 1);
            } else if (keycode == Input.Keys.UP) {
                cam.position.z += 1F;
                cam.update();
            } else if (keycode == Input.Keys.DOWN) {
                cam.position.z -= 1F;
                cam.update();
            }
            return false;
        }

    };

    List<ModelInstance> featureRays = new ArrayList<ModelInstance>();

    //    private void doneLoading() {
    //        Model ship = assets.get("gen.g3dj", Model.class);
    //        ModelInstance shipInstance = new ModelInstance(ship);
    //        instances.add(shipInstance);
    //        loading = false;

    //    }

    //    void testIntersect() {
    //        float[] verts = {0, 0, 0,
    //                         1, 0.5F, 0,
    //                         1, 0, 1,
    //                         0, 0.5F, 1};
    //        short[] tris = {0, 1, 2,
    //                        0, 2, 3};
    //
    //        String MODEL_ID = "model";
    //        String MATERIAL_ID = "material";
    //        String NODE_ID = "node";
    //        String NODE_CHILD_ID = "node_child";
    //        String PART_ID = "part";
    //
    //        ModelData model = new ModelData();
    //        model.version[0] = 0;
    //        model.version[1] = 1;
    //        model.id = MODEL_ID;
    //
    //        ModelMesh mesh = new ModelMesh();
    //        //mesh.id = PART_ID;
    //
    //        mesh.attributes = new VertexAttribute[]{VertexAttribute.Position()};
    //
    //        mesh.vertices = verts;
    //        ModelMeshPart trianglesPart = new ModelMeshPart();
    //        trianglesPart.id = PART_ID;
    //        trianglesPart.primitiveType = GL20.GL_TRIANGLES;
    //        trianglesPart.indices = tris;
    //        mesh.parts = new ModelMeshPart[]{trianglesPart};
    //        model.meshes.add(mesh);
    //
    //        ModelMaterial material = new ModelMaterial();
    //        material.id = MATERIAL_ID;
    //
    //        material.diffuse = new Color(1f, 1f, 1f, 1.0f);
    //        material.ambient = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    //        material.emissive = new Color(0.5f, 0.5f, 0.5f, 1.0f);
    //        material.specular = new Color(0f, 0f, 0f, 1.0f);
    //        material.shininess = 0.0f;
    //        material.opacity = 1.0f;
    //        model.materials.add(material);
    //
    //        ModelNode node = new ModelNode();
    //        node.id = NODE_ID;
    //        ModelNodePart nodePart = new ModelNodePart();
    //
    //        nodePart.materialId = MATERIAL_ID;
    //        nodePart.meshPartId = PART_ID;
    //        node.parts = new ModelNodePart[]{nodePart};
    //
    //        model.nodes.add(node);
    //
    //        ModelInstance instance = new ModelInstance(new Model(model));
    //
    //        ModelBuilder builder = new ModelBuilder();
    //        Model box = builder.createBox(0.1F, 0.1F, 0.1F, new Material(ColorAttribute.createDiffuse(Color.RED)), 1);
    //        ModelInstance boxInstance = new ModelInstance(box);
    //
    //        ElevationResolution res = new ElevationResolution(verts, tris, 3);
    //
    //        float elev = res.projectToLandscape(new Vector2(.4F, .8F));
    //        boxInstance.transform.setTranslation(.4F, elev, .8F);
    //
    //        this.instances.clear();
    //        this.instances.add(instance);
    //        this.instances.add(boxInstance);
    //    }

    private void landscapeInfo(ModelData landscapeData) {
        ModelMesh landscapeMesh = landscapeData.meshes.first();

        int vCompos = 4;
        int vertCount = landscapeMesh.vertices.length / vCompos;

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;

        for (int i = 0; i < vertCount; i++) {
            float x = landscapeMesh.vertices[i * vCompos];
            float y = landscapeMesh.vertices[i * vCompos + 1];
            float z = landscapeMesh.vertices[i * vCompos + 2];

            minX = minX < x ? minX : x;
            minY = minY < y ? minY : y;
            minZ = minZ < z ? minZ : z;

            maxX = maxX > x ? maxX : x;
            maxY = maxY > y ? maxY : y;
            maxZ = maxZ > z ? maxZ : z;
        }

        log("Landscape:\nvert_count=%s, tri_count=%s\nX: min=%s (%s), max=%s (%s),\nY: min=%s (%s), max=%s (%s),\nZ: min=%s (%s), max=%s (%s)",
            vertCount, -1,
            minX, coordTrans.toExternalLon(minX), maxX, coordTrans.toExternalLon(maxX),
            minY, coordTrans.toExternalLat(minY), maxY, coordTrans.toExternalLat(maxY),
            minZ, coordTrans.toExternalElev(minZ), maxZ, coordTrans.toExternalElev(maxZ));
    }

    void addPoint(Vector3 point, Color color) {
        Material m = new Material(ColorAttribute.createDiffuse(color));
        ModelInstance i = new ModelInstance(builder.createBox(1F, 1F, 1F, m, 1));
        i.transform.setTranslation(point);
        instances.add(i);
    }

    void addLine(Vector3 a, Vector3 b, Color color) {
        instances.add(new ModelInstance(builder.createArrow(a, b, new Material(createDiffuse(color)), 1)));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        //        this.viewport.update(width, height);
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();

        featuresDisplay.resize(width, height);
        photoDisplay.resize(width, height);
        ui.resize(width, height);
    }

    @Override
    public void render() {

        for (ModelInstance ray : this.featureRays) {
            Vector3 pos = anchor.cpy();
            Vector3 targ = coordTrans.toInternal((Vector3) ray.userData, new Vector3());
            Vector3 rot = targ.cpy().sub(pos).nor();
            ray.transform.setToTranslation(pos);
            ray.transform.rotate(Vector3.X, rot);
        }

        control.update();
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(this.instances);
        modelBatch.end();

        //        System.out.format("x:%f y:%f z:%f\n",cam.direction.x, cam.direction.y, cam.direction.z);

        photoDisplay.render();

        featuresDisplay.render();
        ui.render(Gdx.graphics.getDeltaTime(), camTmp.set(cam.direction.x, cam.direction.y).angle(south) + 180);
    }

    Vector2 south = new Vector2(0, -1);
    Vector2 camTmp = new Vector2();

    @Override
    public void dispose() {
        instances.clear();
        modelBatch.dispose();
        ui.dispose();
        //        assets.dispose();
    }

    private static String TAG = "MAIN";

    private static void log(String format, Object... args) {
        Gdx.app.log(TAG, String.format(format, args));
    }

}
