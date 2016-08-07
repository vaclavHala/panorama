package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ElevConfig;
import com.mygdx.game.model.colorization.CenterDistanceWeight;
import com.mygdx.game.model.colorization.ColorModel;
import com.mygdx.game.model.colorization.HeightColorModel;
import com.mygdx.game.model.colorization.MaskedColorModel;
import com.mygdx.game.model.colorization.SolidColorModel;
import java.io.IOException;
import java.util.Arrays;
import static java.util.Arrays.asList;

/**
 * Gutted {@link com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader} which
 * creates landscape model in memory (shape, vertex colorization)
 */
public class LandscapeLoader {

    private static final String TAG = Convertor.class.getSimpleName();

    private static final double TOTAL_WIDTH_DEG = 2;
    private static final double TOTAL_HEIGHT_DEG = 2;

    private static final String MODEL_ID = "model";
    private static final String MATERIAL_ID = "material";
    private static final String NODE_ID = "node";
    private static final String NODE_CHILD_ID = "node_child";
    private static final String PART_ID = "part";

    private static final int[] VERSION = new int[]{0, 1};

    private static final G3DJTemplate.G3DJMaterial MATERIAL;
    private static final G3DJTemplate.G3DJNode NODE;
    private static final G3DJTemplate.G3DJAnimation ANIMATION;

    static {
        MATERIAL = new G3DJTemplate.G3DJMaterial();
        MATERIAL.id = MATERIAL_ID;
        MATERIAL.ambient = new float[]{0.5f, 0.5f, 0.5f};
        MATERIAL.diffuse = new float[]{1f, 1f, 1f};
        MATERIAL.emissive = new float[]{0.5f, 0.5f, 0.5f};
        MATERIAL.opacity = 1;
        MATERIAL.specular = new float[]{0f, 0f, 0f};
        MATERIAL.shininess = 0;

        NODE = new G3DJTemplate.G3DJNode();
        NODE.id = NODE_ID;
        G3DJTemplate.G3DJNodeChild nodeChild = new G3DJTemplate.G3DJNodeChild();
        nodeChild.id = NODE_CHILD_ID;
        G3DJTemplate.G3DJNodeChildPart nodeChildPart = new G3DJTemplate.G3DJNodeChildPart();
        nodeChildPart.meshpartid = PART_ID;
        nodeChildPart.materialid = MATERIAL_ID;
        nodeChild.parts = asList(nodeChildPart);
        NODE.children = asList(nodeChild);

        ANIMATION = new G3DJTemplate.G3DJAnimation();
    }

    private final ElevConfig elevCfg;

    public LandscapeLoader(ElevConfig elevCfg) {
        this.elevCfg = elevCfg;
    }

    public ModelData loadModelData(double lon, double lat) {
        double lon0 = lon - TOTAL_WIDTH_DEG / 2;
        double lat0 = lat - TOTAL_HEIGHT_DEG / 2;
        Gdx.app.log(TAG, "Loading landscape. Deg:" +
                " lon0=" + lon0 + ", lat0=" + lat0 +
                ", width=" + TOTAL_WIDTH_DEG + ", height=" + TOTAL_HEIGHT_DEG);
        Loader loader = new Loader(elevCfg, Gdx.files);
        try {
            Landscape landscape = loader.loadLandscape(
                    elevCfg.lonToCell(lon0), elevCfg.latToCell(lat0),
                    elevCfg.lonToCell(TOTAL_WIDTH_DEG), elevCfg.latToCell(TOTAL_HEIGHT_DEG));
//
            ModelData model = new ModelData();
            model.version[0] = 0;
            model.version[1] = 1;
            model.id = MODEL_ID;

            colorize(landscape, (float) lon, (float) lat, elevCfg);
            addMesh(model, landscape);
            addMaterial(model);
            addNode(model);


            return model;
        } catch (IOException e) {
            // TODO
            throw new IllegalStateException(e);
        }

    }

    /**
     * Returned model can either be stored to disk and loaded via regular means
     * or wrapped in {@link InMemoryFileHandle} and fed to
     * {@link com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader G3dModelLoader} for direct display
     *
     * @return String representation of the generated moedel in G3DJ format
     */
//    public ModelData pointsToLandscape(Landscape landscape) {
//
////        Gdx.app.log(TAG, "Creating landscape model. points.size=" + points.size);
//
//        Gdx.app.log(TAG, "Coloring terrain.");


//        G3DJTemplate model = new G3DJTemplate();
//        model.version = VERSION;
//        model.id = MODEL_ID;
//
//        G3DJTemplate.G3DJMesh mesh = new G3DJTemplate.G3DJMesh();
//        mesh.attributes = asList(POSITION, COLOR);
//        mesh.vertices = coloredVertices.toArray();
////                asList(
////                0f, 0f, 0f, 1f, 0f, 0f, 1f,
////                0f, 0f, 1f, 0f, 0f, 1f, 1f,
////                1f, 0f, 1f, 0f, 1f, 0f, 1f,
////                1f, 0f, 0f, 0f, 1f, 0f, 1f
////        );
//
//        G3DJTemplate.G3DJPart part = new G3DJTemplate.G3DJPart();
//        part.id = PART_ID;
//        part.type = TRIANGLES;
//        part.indices = triangles.toArray();
////                new int[]{
////                0, 2, 3
////        };
//
//        mesh.parts = asList(part);
//        model.meshes = asList(mesh);
//
//        model.materials = asList(MATERIAL);
//        model.nodes = asList(NODE);
//        model.animations = asList(ANIMATION);
//
//        Gdx.app.log(TAG, "Model ready, writing to JSON.");
//        return json.toJson(model);
//    }
    private void colorize(Landscape input, float centerX, float centerY, ElevConfig elevCfg) {
        float minHeight = Float.MAX_VALUE;
        float maxHeight = Float.MIN_VALUE;
        for (int y = 0; y < input.heightCells; y++) {
            for (int x = 0; x < input.widthCells; x++) {
                minHeight = Math.min(minHeight, input.getY(x, y));
                maxHeight = Math.max(maxHeight, input.getY(x, y));
            }
        }

        float widthDeg = input.widthCells / (float) elevCfg.cellsPerDegHorizontal;
        float heightDeg = input.heightCells / (float) elevCfg.cellsPerDegVertical;
        float radius = Math.min(widthDeg, heightDeg) / 2.0f;

//        ColorModel colors = new MaskedColorModel(
//                new HeightColorModel(0.9f * minHeight, 0.9f * maxHeight),
//                new SolidColorModel(Color.BLACK),
//                new CenterDistanceWeight(0.3f * radius, 0.9f * radius,
//                        new Vector2(centerX, centerY)));
        ColorModel colors = new SolidColorModel(Color.RED);
        // has to be in separate loop from the one above,
        // we first need to go through all points to find min/max heights
        // only then can we color the points based on that
        for (short y = 0; y < input.heightCells; y++) {
            for (short x = 0; x < input.widthCells; x++) {
                Color rawColor = colors.color(input.getX(x, y), input.getY(x, y), input.getZ(x, y));
                input.setC(x, y, rawColor.toFloatBits());
            }
        }
    }

    private void addMesh(ModelData model, Landscape landscape) {
        ModelMesh mesh = new ModelMesh();
        //mesh.id = PART_ID;

        mesh.attributes = new VertexAttribute[]{
                VertexAttribute.Position(),
                VertexAttribute.ColorPacked()
        };

        mesh.vertices = landscape.cells;
        ModelMeshPart trianglesPart = new ModelMeshPart();
        trianglesPart.id = PART_ID;
        trianglesPart.primitiveType = GL20.GL_TRIANGLES;
        trianglesPart.indices = triangles(landscape);
        mesh.parts = new ModelMeshPart[]{trianglesPart};
        model.meshes.add(mesh);
    }

    private short[] triangles(Landscape landscape) {
        // - 1 because there are n-1 edges in string of n vertices (cells are vertices)
        // * 2 because there are two tris per square
        int triCount = (landscape.widthCells - 1) * (landscape.heightCells - 1) * 2;
        Gdx.app.log(TAG, "landscape width=" + landscape.widthCells + ", height=" + landscape.heightCells +
                " will have tricount=" + triCount);
        // * 3 because 3 indices per triangle
        short[] indices = new short[triCount * 3];
        int i = 0;
        // - 1 because we do not want triangulation to touch outer edge loop
        for (short y = 0; y < landscape.heightCells - 1; y++) {
            for (short x = 0; x < landscape.widthCells - 1; x++) {
                short base = (short) (y * landscape.widthCells + x);
                //has to go clockwise
                indices[i] = base;
                indices[i + 1] = (short) (base + 1);
                indices[i + 2] = (short) (base + landscape.widthCells + 1);

                indices[i + 3] = base;
                indices[i + 4] = (short) (base + landscape.widthCells + 1);
                indices[i + 5] = (short) (base + landscape.widthCells);
                // + 6 because we add 6 indices for two triangles in each iteration
                i += 6;
            }
        }
        System.out.println(Arrays.toString(indices));
        return indices;
    }

    private void addMaterial(ModelData model) {
        ModelMaterial material = new ModelMaterial();
        material.id = MATERIAL_ID;

        material.diffuse = new Color(1f, 1f, 1f, 1.0f);
        material.ambient = new Color(0.5f, 0.5f, 0.5f, 1.0f);
        material.emissive = new Color(0.5f, 0.5f, 0.5f, 1.0f);
        material.specular = new Color(0f, 0f, 0f, 1.0f);
        material.shininess = 0.0f;
        material.opacity = 1.0f;
        model.materials.add(material);
    }

    private void addNode(ModelData model) {
        ModelNode node = new ModelNode();
        node.id = NODE_ID;
        ModelNodePart nodePart = new ModelNodePart();

        nodePart.materialId = MATERIAL_ID;
        nodePart.meshPartId = PART_ID;
        node.parts = new ModelNodePart[]{nodePart};

        model.nodes.add(node);
    }

}
