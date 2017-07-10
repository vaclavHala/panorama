package com.mygdx.game.model;

import com.badlogic.gdx.Files;
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
import com.mygdx.game.ElevConfig;
import com.mygdx.game.model.colorization.ColorModel;
import com.mygdx.game.model.colorization.SolidColorModel;
import static java.lang.Math.min;
import static java.lang.String.format;
import java.util.Arrays;
import static java.util.Arrays.asList;

/**
 * Gutted {@link com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader} which
 * creates landscape model in memory (shape, vertex colorization)
 */
public class LandscapeLoader {

    private static final String TAG = Convertor.class.getSimpleName();

    //    private static final double TOTAL_WIDTH_DEG = 2;
    //    private static final double TOTAL_HEIGHT_DEG = 2;
    //
    private static final String MODEL_ID = "model";
    private static final String MATERIAL_ID = "material";
    private static final String NODE_ID = "node";
    private static final String NODE_CHILD_ID = "node_child";
    private static final String PART_ID = "part";
    //
    //    private static final int[] VERSION = new int[]{0, 1};
    //
    //    private static final G3DJTemplate.G3DJMaterial MATERIAL;
    //    private static final G3DJTemplate.G3DJNode NODE;
    //    private static final G3DJTemplate.G3DJAnimation ANIMATION;
    //
    //    static {
    //        MATERIAL = new G3DJTemplate.G3DJMaterial();
    //        MATERIAL.id = MATERIAL_ID;
    //        MATERIAL.ambient = new float[]{0.5f, 0.5f, 0.5f};
    //        MATERIAL.diffuse = new float[]{1f, 1f, 1f};
    //        MATERIAL.emissive = new float[]{0.5f, 0.5f, 0.5f};
    //        MATERIAL.opacity = 1;
    //        MATERIAL.specular = new float[]{0f, 0f, 0f};
    //        MATERIAL.shininess = 0;
    //
    //        NODE = new G3DJTemplate.G3DJNode();
    //        NODE.id = NODE_ID;
    //        G3DJTemplate.G3DJNodeChild nodeChild = new G3DJTemplate.G3DJNodeChild();
    //        nodeChild.id = NODE_CHILD_ID;
    //        G3DJTemplate.G3DJNodeChildPart nodeChildPart = new G3DJTemplate.G3DJNodeChildPart();
    //        nodeChildPart.meshpartid = PART_ID;
    //        nodeChildPart.materialid = MATERIAL_ID;
    //        nodeChild.parts = asList(nodeChildPart);
    //        NODE.children = asList(nodeChild);
    //
    //        ANIMATION = new G3DJTemplate.G3DJAnimation();
    //    }

    private final Files files;
    private final ElevConfig elevCfg;

    public LandscapeLoader(Files files, ElevConfig elevCfg) {
        this.files = files;
        this.elevCfg = elevCfg;
    }

    public ModelData loadModelData(
            float lon, float lat,
            float width, float height) {
        Gdx.app.log(TAG, "Loading landscape. Deg:" +
                         " lon=" + lon + ", lat=" + lat +
                         ", width=" + width + ", height=" + height);

        ModelData model = new ModelData();
        model.version[0] = 0;
        model.version[1] = 1;
        model.id = MODEL_ID;

        //            colorize(landscape, (float) lon, (float) lat, elevCfg);
        addMesh(model, lon, lat, width, height);
        addMaterial(model);
        addNode(model);

        return model;

    }

    //    private void colorize(Landscape input, float centerX, float centerY, ElevConfig elevCfg) {
    //        float minHeight = Float.MAX_VALUE;
    //        float maxHeight = Float.MIN_VALUE;
    //        for (int y = 0; y < input.heightCells; y++) {
    //            for (int x = 0; x < input.widthCells; x++) {
    //                minHeight = Math.min(minHeight, input.getY(x, y));
    //                maxHeight = Math.max(maxHeight, input.getY(x, y));
    //            }
    //        }
    //
    //        float widthDeg = input.widthCells / (float) elevCfg.cellsPerDegHorizontal;
    //        float heightDeg = input.heightCells / (float) elevCfg.cellsPerDegVertical;
    //        float radius = Math.min(widthDeg, heightDeg) / 2.0f;
    //
    //        //        ColorModel colors = new MaskedColorModel(
    //        //                new HeightColorModel(0.9f * minHeight, 0.9f * maxHeight),
    //        //                new SolidColorModel(Color.BLACK),
    //        //                new CenterDistanceWeight(0.3f * radius, 0.9f * radius,
    //        //                        new Vector2(centerX, centerY)));
    //        ColorModel colors = new SolidColorModel(Color.RED);
    //        // has to be in separate loop from the one above,
    //        // we first need to go through all points to find min/max heights
    //        // only then can we color the points based on that
    //        for (short y = 0; y < input.heightCells; y++) {
    //            for (short x = 0; x < input.widthCells; x++) {
    //                Color rawColor = colors.color(input.getX(x, y), input.getY(x, y), input.getZ(x, y));
    //                input.setC(x, y, rawColor.toFloatBits());
    //            }
    //        }
    //    }

    private void addMesh(ModelData model,
            float lonFrom, float latFrom,
            float width, float height) {

        int vComponents = 3 + 1;

        float lonTo = lonFrom + width;
        float latTo = latFrom + height;
        // to avoid negative numbers shift by 180 degrees, will be subtracted on output
        // in particular we always want to find boundary lower than given exact value in degrees
        // if there were negative numbers this would not work properly
        float normLonFrom = lonFrom + 180;
        float normLatFrom = latFrom + 180;
        float normLonTo = lonTo + 180;
        float normLatTo = latTo + 180;

        //truncates to left/bottom most chunk boundary
        int chunk0Lon = (int) (normLonFrom / elevCfg.chunkWidthDeg) * elevCfg.chunkWidthDeg - 180;
        int chunk0Lat = (int) (normLatFrom / elevCfg.chunkHeightDeg) * elevCfg.chunkHeightDeg - 180;

        int chunkNLon = (int) (normLonTo / elevCfg.chunkWidthDeg) * elevCfg.chunkWidthDeg - 180;
        int chunkNLat = (int) (normLatTo / elevCfg.chunkHeightDeg) * elevCfg.chunkHeightDeg - 180;

        int chunksHorizontal = (chunkNLon - chunk0Lon) / elevCfg.chunkWidthDeg + 1;
        int chunksVertical = (chunkNLat - chunk0Lat) / elevCfg.chunkHeightDeg + 1;

        int cell0Col = (int) ((lonFrom - chunk0Lon) * elevCfg.cellsPerDegHorizontal);
        int cell0Row = (int) ((latFrom - chunk0Lat) * elevCfg.cellsPerDegVertical);

        int cellNCol = (int) ((lonTo - chunk0Lon) * elevCfg.cellsPerDegHorizontal);
        int cellNRow = (int) ((latTo - chunk0Lat) * elevCfg.cellsPerDegVertical);

        int cellsHorizontal = (chunksHorizontal - 1) * elevCfg.chunkWidthCells + cellNCol - cell0Col;
        int cellsVertical = (chunksVertical - 1) * elevCfg.chunkHeightCells + cellNRow - cell0Row;

        int vertCount = cellsHorizontal * cellsVertical;
        int triCount = (cellsHorizontal - 1) * (cellsVertical - 1) * 2;

        Gdx.app.log(TAG, "elev=" + elevCfg + "\n" +
                         "lonFrom=" + lonFrom + ", " +
                         "latFrom=" + latFrom + ", " +
                         "lonTo=" + lonTo + ", " +
                         "latTo=" + latTo + "\n" +
                         "chunk0Lon=" + chunk0Lon + ", " +
                         "chunk0Lat=" + chunk0Lat + ", " +
                         "chunkNLon=" + chunkNLon + ", " +
                         "chunkNLat=" + chunkNLat + "\n" +
                         "chunksHorizontal=" + chunksHorizontal + ", " +
                         "chunksVertical=" + chunksVertical + "\n" +
                         "cell0Col=" + cell0Col + ", " +
                         "cell0Row=" + cell0Row + ", " +
                         "cellNCol=" + cellNCol + ", " +
                         "cellNRow=" + cellNRow + "\n" +
                         "cellsHorizontal=" + cellsHorizontal + ", " +
                         "cellsVertical=" + cellsVertical + "\n" +
                         "vertCount=" + vertCount + ", " +
                         "triCount=" + triCount
               );

        FileBackedElevData[] chunks = new FileBackedElevData[chunksHorizontal * chunksVertical];
        for (int row = 0; row < chunksVertical; row++) {
            for (int col = 0; col < chunksHorizontal; col++) {
                int i = row * chunksHorizontal + col;
                int chunkLon = chunk0Lon + col * elevCfg.chunkWidthDeg;
                int chunkLat = chunk0Lat + row * elevCfg.chunkHeightDeg;
                String chunkName = format("chunk_%c%d_%c%d",
                                          chunkLon < 0 ? 'w' : 'e', Math.abs(chunkLon),
                                          chunkLat < 0 ? 's' : 'n', Math.abs(chunkLat));
                Gdx.app.log(TAG, "[r" + row + ",c" + col + "] Opening chunk " + chunkName);
                chunks[i] = new FileBackedElevData(this.files, "chunks/" + chunkName);
            }
        }

        float[] vertices = new float[vComponents * vertCount];
        short[] triIndices = new short[3 * triCount];
        CollatedElevData collated = new CollatedElevData(chunks, chunksHorizontal, chunksVertical,
                                                         elevCfg.chunkWidthCells, elevCfg.chunkHeightCells);
        ElevData cropped = new CroppedElevData(collated,
                                               chunksHorizontal * elevCfg.chunkWidthCells,
                                               chunksVertical * elevCfg.chunkHeightCells,
                                               cell0Row, cell0Col,
                                               cellNRow, cellNCol);

        float cell0Lon = chunk0Lon + cell0Col * elevCfg.cellWidthDeg;
        float cell0Lat = chunk0Lat + cell0Row * elevCfg.cellHeightDeg;
        int t = 0;
        for (int row = 0; row < cellsVertical; row++) {
            for (int col = 0; col < cellsHorizontal; col++) {
                float lon = (cell0Lon + col * elevCfg.cellWidthDeg) * elevCfg.scalerLon;
                float lat = (cell0Lat + row * elevCfg.cellHeightDeg) * elevCfg.scalerLat;
                //                float lon = col * elevCfg.cellWidthDeg;
                //                float lat = row * elevCfg.cellHeightDeg;
                float elev = cropped.next() * elevCfg.scalerElev;

                int v = (short) (row * cellsHorizontal + col);
                vertices[v * vComponents] = lon;
                vertices[v * vComponents + 1] = elev;
                vertices[v * vComponents + 2] = lat;
                if (row > 0 && col > 0) { // nothing to triangulate on first row/col
                    triIndices[t * 3] = (short) v;
                    triIndices[t * 3 + 1] = (short) (v - cellsHorizontal - 1);
                    triIndices[t * 3 + 2] = (short) (v - cellsHorizontal);
                    t++;
                    triIndices[t * 3] = (short) v;
                    triIndices[t * 3 + 1] = (short) (v - 1);
                    triIndices[t * 3 + 2] = (short) (v - cellsHorizontal - 1);
                    t++;
                }
            }
        }

        colorize(vertices, vertCount, 3 + 1, 1, 3);
        //        Gdx.app.log(TAG, "verts: " + Arrays.toString(vertices) + "\ntris: " + Arrays.toString(triIndices));

        ModelMesh mesh = new ModelMesh();
        //mesh.id = PART_ID;

        mesh.attributes = new VertexAttribute[]{VertexAttribute.Position(),
                                                VertexAttribute.ColorPacked()
        };

        mesh.vertices = vertices;
        ModelMeshPart trianglesPart = new ModelMeshPart();
        trianglesPart.id = PART_ID;
        trianglesPart.primitiveType = GL20.GL_TRIANGLES;
        trianglesPart.indices = triIndices;
        mesh.parts = new ModelMeshPart[]{trianglesPart};
        model.meshes.add(mesh);
    }

    private void colorize(float[] vertices, int vertCount,
            int vComponents, int elevIndex, int colorIndex) {
        float minElev = Float.MAX_VALUE;
        float maxElev = Float.MIN_VALUE;
        for (int i = 0; i < vertCount; i++) {
            float elev = vertices[i * vComponents + elevIndex];
            minElev = minElev < elev ? minElev : elev;
            maxElev = maxElev > elev ? maxElev : elev;
        }
        float delta = maxElev - minElev;
        log("elev color: min=%s, max=%s", minElev, maxElev);
        for (int i = 0; i < vertCount; i++) {
            float elev = vertices[i * vComponents + elevIndex];
            float rel = (elev - minElev) / delta;
            int rgb = (int) (rel * 255);
            //            int rgb = 150;
            vertices[i * vComponents + colorIndex] = Color.toFloatBits(rgb, rgb, rgb, 1);
        }
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

    public static void log(String format, Object... args) {
        Gdx.app.log(TAG, String.format(format, args));
    }

}
