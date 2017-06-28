package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.DelaunayTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ShortArray;
import com.mygdx.game.model.G3DJTemplate.G3DJAnimation;
import com.mygdx.game.model.G3DJTemplate.G3DJMaterial;
import com.mygdx.game.model.G3DJTemplate.G3DJMesh;
import com.mygdx.game.model.G3DJTemplate.G3DJNode;
import com.mygdx.game.model.G3DJTemplate.G3DJNodeChild;
import com.mygdx.game.model.G3DJTemplate.G3DJNodeChildPart;
import com.mygdx.game.model.G3DJTemplate.G3DJPart;
import com.mygdx.game.model.colorization.CenterDistanceWeight;
import com.mygdx.game.model.colorization.ColorModel;
import com.mygdx.game.model.colorization.HeightColorModel;
import com.mygdx.game.model.colorization.MaskedColorModel;
import com.mygdx.game.model.colorization.SolidColorModel;
import static com.mygdx.game.model.G3DJTemplate.G3DJMeshAttribute.COLOR;
import static com.mygdx.game.model.G3DJTemplate.G3DJMeshAttribute.POSITION;
import static com.mygdx.game.model.G3DJTemplate.G3DJPartType.TRIANGLES;
import static java.util.Arrays.asList;

public class Convertor {

    private static final String TAG = Convertor.class.getSimpleName();

    private static final String MODEL_ID = "model";
    private static final String MATERIAL_ID = "material";
    private static final String NODE_ID = "node";
    private static final String NODE_CHILD_ID = "node_child";
    private static final String PART_ID = "part";

    private static final int[] VERSION = new int[]{0, 1};

    private static final G3DJMaterial MATERIAL;
    private static final G3DJNode NODE;
    private static final G3DJAnimation ANIMATION;

    static {
        MATERIAL = new G3DJMaterial();
        MATERIAL.id = MATERIAL_ID;
        MATERIAL.ambient = new float[]{0.5f, 0.5f, 0.5f};
        MATERIAL.diffuse = new float[]{1f, 1f, 1f};
        MATERIAL.emissive = new float[]{0.5f, 0.5f, 0.5f};
        MATERIAL.opacity = 1;
        MATERIAL.specular = new float[]{0f, 0f, 0f};
        MATERIAL.shininess = 0;

        NODE = new G3DJNode();
        NODE.id = NODE_ID;
        G3DJNodeChild nodeChild = new G3DJNodeChild();
        nodeChild.id = NODE_CHILD_ID;
        G3DJNodeChildPart nodeChildPart = new G3DJNodeChildPart();
        nodeChildPart.meshpartid = PART_ID;
        nodeChildPart.materialid = MATERIAL_ID;
        nodeChild.parts = asList(nodeChildPart);
        NODE.children = asList(nodeChild);

        ANIMATION = new G3DJAnimation();
    }

    private final Json json;

    public Convertor() {
        json = new Json(JsonWriter.OutputType.json);
        json.setTypeName(null);
    }

    /**
     * Returned model can either be stored to disk and loaded via regular means
     * or wrapped in {@link InMemoryFileHandle} and fed to
     * {@link com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader G3dModelLoader} for direct display
     *
     * @return String representation of the generated moedel in G3DJ format
     */
    public String pointsToLandscape(Array<Vector3> points) {

        Gdx.app.log(TAG, "Creating landscape model. points.size=" + points.size);

        Gdx.app.log(TAG, "Coloring terrain.");
        ColorModel colors = assembleColorModel(points);
        FloatArray coloredVertices = new FloatArray(points.size * 7);
        for (Vector3 p : points) {
            appendVertex(coloredVertices, p, colors);
        }

        Gdx.app.log(TAG, "Triangulating landscape.");
        ShortArray triangles = new DelaunayTriangulator().computeTriangles(extractVertices(points), 0, points.size * 2, false);
        Gdx.app.log(TAG, "triangles.count=" + triangles.size / 3);

        G3DJTemplate model = new G3DJTemplate();
        model.version = VERSION;
        model.id = MODEL_ID;

        G3DJMesh mesh = new G3DJMesh();
        mesh.attributes = asList(POSITION, COLOR);
        mesh.vertices = coloredVertices.toArray();
        //                asList(
        //                0f, 0f, 0f, 1f, 0f, 0f, 1f,
        //                0f, 0f, 1f, 0f, 0f, 1f, 1f,
        //                1f, 0f, 1f, 0f, 1f, 0f, 1f,
        //                1f, 0f, 0f, 0f, 1f, 0f, 1f
        //        );

        G3DJPart part = new G3DJPart();
        part.id = PART_ID;
        part.type = TRIANGLES;
        part.indices = triangles.toArray();
        //                new int[]{
        //                0, 2, 3
        //        };

        mesh.parts = asList(part);
        model.meshes = asList(mesh);

        model.materials = asList(MATERIAL);
        model.nodes = asList(NODE);
        model.animations = asList(ANIMATION);

        Gdx.app.log(TAG, "Model ready, writing to JSON.");
        return json.toJson(model);
    }

    private static float[] extractVertices(Array<Vector3> vertices) {
        float[] extracted = new float[vertices.size * 2];
        for (int i = 0; i < vertices.size; i++) {
            Vector3 v = vertices.get(i);
            //disregard height here
            extracted[i * 2] = v.x;
            extracted[i * 2 + 1] = v.z;
        }
        return extracted;
    }

    private ColorModel assembleColorModel(Array<Vector3> input) {
        //find center, min max
        //compose color models
        //        return new SolidColorModel();
        return new MaskedColorModel(
                                    new HeightColorModel(0.0f, .95f),
                                    new SolidColorModel(Color.BLACK),
                                    new CenterDistanceWeight(45.0f, 55.0f, new Vector2(0, 0)));
    }

    private void appendVertex(FloatArray to, Vector3 vertex, ColorModel colors) {
        //        to.addAll(vertex.x, vertex.y, vertex.z);
        //        Vector3 color = colors.color(vertex);
        //        to.addAll(color.x, color.y, color.z, 1.0f);
    }

    public void makeElevation() {

    }

    /**
     * Based on relative height, lowest verts are black, highest white.
     * Distance from origin is also considered, closer verts are lighter, further are darker.
     */
    public void makeColors() {

    }

}
