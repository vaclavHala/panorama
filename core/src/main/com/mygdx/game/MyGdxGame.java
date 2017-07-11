package com.mygdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.common.CoordTransform;
import com.mygdx.game.model.*;
import com.mygdx.game.ui.UI;
import java.util.ArrayList;
import static java.util.Arrays.asList;
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

    PerspectiveCamera cam;
    CamController control;

    Array<ModelInstance> instances = new Array<ModelInstance>();
    ModelBatch modelBatch;
    UI ui;
    FeaturesDisplay featuresDisplay;

    ModelBuilder builder = new ModelBuilder();

    ElevConfig elevCfg;
    CoordTransform coordTrans;

    public static boolean allFeaturesVisible = false;

    @Override
    public void create() {
        modelBatch = new ModelBatch();
        cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.up.set(0, 0, 1);
        cam.position.set(0, 0, 0);
        cam.lookAt(1, 0, 1);
        cam.near = .01F;
        cam.far = 10000F;

        control = new CamController(cam);

        //        environment = new Environment();
        //        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        //        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0f, -1f, 0f));

        // world coords are some sorta banana units very close to kilometers
        elevCfg = new ElevConfig(1, 1, 3601, 3601,
                                 11100F, 7800F, 0.1F);
        //                                 1110, 787F, 0.01F);

        log("Elev " + elevCfg.toString());
        Vector3 meReal = new Vector3(14.2834500F, 48.8649861F, 1100);

        coordTrans = new CoordTransform(elevCfg, meReal);
        LandscapeLoader loader = new LandscapeLoader(Gdx.files, elevCfg, coordTrans);

        ModelData landscapeModelData = loader.loadModelData(14.26F, 48.84F,
                                                            0.05F, 0.05F);
        ModelMesh landscapeMesh = landscapeModelData.meshes.first();
        ModelMeshPart landscapeTris = landscapeMesh.parts[0];
        int landscapeVertComponents = 4;
        //        for (VertexAttribute attr : landscapeMesh.attributes) {
        //            landscapeVertComponents += attr.numComponents;
        //        }
        log("Landscape vertComponents: " + landscapeVertComponents);

        ElevationResolution elevResolution =
                new ElevationResolution(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents, coordTrans);
        float meElev = elevResolution.projectToLandscape(meReal.x, meReal.y);
        log("My elevation: " + meElev);

        Visibility visibility = new Visibility(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents);
        //        visibility.isVisibleFrom(meWorld, toWorld(14.2736606F, 48.8518642F, 886F));
        //        addPoint(visibility.intersection, Color.BROWN);
        //        addPoint(meWorld, Color.GREEN);
        //        addPoint(toWorld(14.2736606F, 48.8518642F, 886F), Color.CYAN);

        TextureAtlas featuresAtlas = new TextureAtlas(Gdx.files.internal("features.atlas"));
        //        AtlasRegion region = atlas.findRegion("imagename");
        //        Sprite sprite = atlas.createSprite("otherimagename");
        //        NinePatch patch = atlas.createPatch("patchimagename");

        TextureAtlas uiAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"), Gdx.files.internal(""));
        Skin skin = new Skin();
        skin.addRegions(uiAtlas);
        skin.add("font", new BitmapFont(Gdx.files.internal("default.fnt"), Gdx.files.internal("default.png"), false));
        skin.add("default", new Label.LabelStyle(skin.getFont("font"), Color.BLACK));

        ui = new UI(Gdx.files);
        ui.create(skin);

        //        LandscapeProjection worldProject =
        //                new LandscapeProjection(cam,
        //                                        landscapeMesh.vertices,
        //                                        landscapeTris.indices,
        //                                        landscapeVertComponents);

        FeatureLookup featureLookup = new FeatureLookup(Gdx.files, elevResolution);

        // put enough margin around edges to avoid features not above landscape
        List<Feature> features = featureLookup.lookup(14.26F + 0.0005F, 48.84F + 0.0005F,
                                                      0.05F - 0.001F, 0.05F - 0.001F);
        featuresDisplay = new FeaturesDisplay(features,
                                              featuresAtlas, skin, cam,
                                              coordTrans, visibility);

        Model landscapeModel = new Model(landscapeModelData);
        System.out.println("Got landscape model");

        Material matX = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material matY = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material matZ = new Material(ColorAttribute.createDiffuse(Color.BLUE));

        builder.begin();
        MeshPartBuilder meshBuilder = builder.part("line", 1, 3, new Material());
        meshBuilder.setColor(Color.RED);
        meshBuilder.line(Vector3.Zero, new Vector3(1000, 0, 0));
        Model lineModel = builder.end();

        for (Feature f : features) {
            System.out.println("feature: " + f);
            ModelInstance lineInstance = new ModelInstance(lineModel);
            lineInstance.userData = f.position;

            instances.add(lineInstance);
            featureRays.add(lineInstance);
            addPoint(coordTrans.toInternal(f.position, new Vector3()), Color.GOLDENROD);
        }

        //        ModelInstance ball = new ModelInstance(builder.createBox(0.1F, 0.1F, 0.1F, matX, 1));
        //        ball.transform.trn(kletPos);
        //        instances.add(ball);

        //        addPoint(new Vector3(1582.8519F, 0, -5426.732F), Color.BROWN);
        //        addPoint(new Vector3(1588.3696F, 0, -5421.2144F), Color.CYAN);
        //        addPoint(new Vector3(MAX_VALUE, 0, MAX_VALUE), Color.);

        //
        //        Vector3 from = ball.transform.getTranslation(new Vector3());
        //        Vector3 to = new Vector3(from).add(0, -0.1F, 0);
        //        ModelInstance rayLine = new ModelInstance(builder.createArrow(from.x, from.y, from.z, to.x, to.y, to.z,
        //                                                                      0.1F, 0.1F, 16, 1,
        //                                                                      matY, 1));
        //        instances.add(rayLine);

        //        float minX = MAX_VALUE, maxX = MIN_VALUE, minY = MAX_VALUE, maxY = MIN_VALUE;
        //
        //        for (int i = 0; i < landscapeMesh.vertices.length / landscapeVertComponents; i++) {
        //            float x = landscapeMesh.vertices[landscapeVertComponents * i];
        //            float y = landscapeMesh.vertices[landscapeVertComponents * i + 2];
        //            minX = minX < x ? minX : x;
        //            maxX = maxX > x ? maxX : x;
        //            minY = minY < y ? minY : y;
        //            maxY = maxY > y ? maxY : y;
        //        }
        //
        //        log("minX: %s, maxX: %s, minY: %s, maxY: %s", minX, maxX, minY, maxY);

        //        Vector3 DOWN = new Vector3(0, -1, 0);
        //        Vector3 origin = new Vector3(from.x, 0, from.z);
        //        Vector3 targ = new Vector3(origin).add(DOWN);
        //        Model m = builder.createArrow(origin, targ, matZ, 1);
        //        instances.add(new ModelInstance(m));
        //        Ray ray = new Ray(origin, DOWN);
        //        Vector3 intersection = new Vector3();
        //        boolean hit = Intersector.intersectRayTriangles(ray, this.vertices, this.triIndices, this.vertComponnets, intersection);
        //        if (!hit) {
        //            throw new IllegalArgumentException("Position " + landscapePosition + " does not intersect landscape");
        //        }

        //        float[] vertices = {0.515F, 0, 0.512F,
        //                            0.53F, 0, 0.519F,
        //                            0.517F, 0, 0.528F};
        //        short[] indices = {0, 1, 2};

        //        ModelInstance i1 = new ModelInstance(builder.createBox(0.01F, 0.01F, 0.01F, matY, 1));
        //        i1.transform.setTranslation(0.47F, 0, 0.46F);
        //        instances.add(i1);
        //
        //        ModelInstance i2 = new ModelInstance(builder.createBox(0.01F, 0.01F, 0.01F, matY, 1));
        //        i2.transform.setTranslation(0.55F, 0, 0.51F);
        //        instances.add(i2);
        //
        //        ModelInstance i3 = new ModelInstance(builder.createBox(0.01F, 0.01F, 0.01F, matY, 1));
        //        i3.transform.setTranslation(0.51F, 0, 0.57F);
        //        instances.add(i3);
        //
        //        ModelInstance x = new ModelInstance(builder.createBox(0.01F, 0.01F, 0.01F, matX, 1));
        //        x.transform.setTranslation(0.52F, 0, 0.57F);
        //        instances.add(i3);

        //        ElevationResolution elevResolve = new ElevationResolution(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents);

        //        int triCount = landscapeTris.indices.length / 3;
        //        for(int t=0; t<triCount; t++){
        //            int i1 = tr
        //            Vector3 v1 = new  Vector3(
        //            landscapeMesh.vertices[t * landscapeVertComponents];
        //            landscapeMesh.vertices[t * landscapeVertComponents + 1];
        //            landscapeMesh.vertices[t * landscapeVertComponents + 2];
        //            );
        //            float x =
        //            Intersector.
        //        }

        //        ElevationResolution elevResolve = new ElevationResolution(vertices, indices, 3);
        //        ElevationResolution elevResolve = new ElevationResolution(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents);
        //        float height = elevResolve.projectToLandscape(new Vector2(from.x, from.z));
        //        ball.transform.setTranslation(from.x, height, from.z);

        landscapeInfo(landscapeModelData, elevCfg);
        ModelInstance landscapeInstance = new ModelInstance(landscapeModel);

        instances.add(landscapeInstance);

        InputMultiplexer inMux = new InputMultiplexer(debugInput, ui.input(), control);
        Gdx.input.setInputProcessor(inMux);

        addPoint(new Vector3(0, 0, 0), Color.GOLD);
        addPoint(new Vector3(20, 20, 10), Color.FOREST);

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

    private void landscapeInfo(ModelData landscapeData, ElevConfig elevCfg) {
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

        log("Landscape:\nX: min=%s (%s), max=%s (%s),\nY: min=%s (%s), max=%s (%s),\nZ: min=%s (%s), max=%s (%s)",
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

    @Override
    public void resize(int width, int height) {
        //        this.viewport.update(width, height);
        featuresDisplay.resize(width, height);
        ui.resize(width, height);
        super.resize(width, height);
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

        Vector2 projectedToGround = new Vector2(cam.direction.x, cam.direction.z);
        double camRot = -projectedToGround.angleRad(Vector2.X) * MathUtils.radiansToDegrees + 90;

        featuresDisplay.render();
        ui.render(Gdx.graphics.getDeltaTime(), camRot);
    }

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
