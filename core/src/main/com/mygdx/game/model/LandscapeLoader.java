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
import com.mygdx.game.ElevConfig;
import com.mygdx.game.Terraformer.MissingChunksException;
import com.mygdx.game.common.CoordTransform;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;

/**
 * Gutted {@link com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader} which
 * creates landscape model in memory (shape, vertex colorization)
 */
public class LandscapeLoader {

    private static final String TAG = Convertor.class.getSimpleName();

    private static final String MODEL_ID = "model";
    private static final String MATERIAL_ID = "material";
    private static final String NODE_ID = "node";
    private static final String NODE_CHILD_ID = "node_child";
    private static final String PART_ID = "part";

    private final ElevConfig elevCfg;
    private final CoordTransform coordTrans;

    public LandscapeLoader(ElevConfig elevCfg, CoordTransform coordTrans) {
        this.elevCfg = elevCfg;
        this.coordTrans = coordTrans;
    }

    public ModelData loadModelData(
            ElevDataFactory factory,
            float lon, float lat,
            float width, float height) throws MissingChunksException {
        Gdx.app.log(TAG, "Loading landscape. Deg:" +
                         " lon=" + lon + ", lat=" + lat +
                         ", width=" + width + ", height=" + height);

        ModelData model = new ModelData();
        model.version[0] = 0;
        model.version[1] = 1;
        model.id = MODEL_ID;

        //            colorize(landscape, (float) lon, (float) lat, elevCfg);
        addMesh(factory, model, lon, lat, width, height);
        addMaterial(model);
        addNode(model);

        return model;

    }

    private void addMesh(
            ElevDataFactory factory, ModelData model,
            float lonFrom, float latFrom,
            float width, float height) throws MissingChunksException {

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

        float cell0Lon = (float) (chunk0Lon + cell0Col * elevCfg.cellWidthDeg);
        float cell0Lat = (float) (chunk0Lat + cell0Row * elevCfg.cellHeightDeg);

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
                         "cell0Lon=" + cell0Lon + ", " +
                         "cell0Lat=" + cell0Lat + ", " +
                         "cellNCol=" + cellNCol + ", " +
                         "cellNRow=" + cellNRow + "\n" +
                         "cellsHorizontal=" + cellsHorizontal + ", " +
                         "cellsVertical=" + cellsVertical + "\n" +
                         "vertCount=" + vertCount + ", " +
                         "triCount=" + triCount
               );

        ElevData[] chunks = new ElevData[chunksHorizontal * chunksVertical];
        List<Chunk> missingChunks = new ArrayList<Chunk>();
        for (int row = 0; row < chunksVertical; row++) {
            for (int col = 0; col < chunksHorizontal; col++) {
                int i = row * chunksHorizontal + col;
                int chunkLon = chunk0Lon + col * elevCfg.chunkWidthDeg;
                int chunkLat = chunk0Lat + row * elevCfg.chunkHeightDeg;
                Chunk chunk = new Chunk(chunkLat, chunkLon);
                Gdx.app.log(TAG, "[r" + row + ",c" + col + "] Opening chunk " + chunk);
                try {

                    chunks[i] = factory.chunk(chunk);
                    //                        new CoarseElevData(new FileBackedElevData(this.files, ),
                    //                                               detail, 3601, 3601);
                } catch (IOException ex) {
                    missingChunks.add(chunk);
                }
            }
        }
        if (!missingChunks.isEmpty()) {
            throw new MissingChunksException(missingChunks);
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

        int t = 0;
        for (int row = 0; row < cellsVertical; row++) {
            for (int col = 0; col < cellsHorizontal; col++) {
                float lon = coordTrans.toInternalLon(cell0Lon + col * elevCfg.cellWidthDeg);
                float lat = coordTrans.toInternalLat(cell0Lat + row * elevCfg.cellHeightDeg);
                //                float lon = col * elevCfg.cellWidthDeg;
                //                float lat = row * elevCfg.cellHeightDeg;
                float elev = coordTrans.toInternalElev(cropped.next());

                int v = (short) (row * cellsHorizontal + col);
                vertices[v * vComponents] = lon;
                vertices[v * vComponents + 1] = lat;
                vertices[v * vComponents + 2] = elev;
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

        colorize(vertices, vertCount, 3 + 1, 2, 3);
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
        //        trianglesPart.primitiveType = GL20.GL_TRIANGLES;
        trianglesPart.indices = triIndices;
        mesh.parts = new ModelMeshPart[]{trianglesPart};
        model.meshes.add(mesh);
    }

    // make loader statefull, calculate all the things into member vars, then use those in load and also use them here

    public List<Chunk> requiredChunks(
            float lonFrom, float latFrom,
            float width, float height) {
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

        List<Chunk> requiredChunks = new ArrayList<Chunk>();
        for (int row = 0; row < chunksVertical; row++) {
            for (int col = 0; col < chunksHorizontal; col++) {
                int i = row * chunksHorizontal + col;
                int chunkLon = chunk0Lon + col * elevCfg.chunkWidthDeg;
                int chunkLat = chunk0Lat + row * elevCfg.chunkHeightDeg;
                requiredChunks.add(new Chunk(chunkLat, chunkLon));

            }
        }
        return requiredChunks;
    }

    private void colorize(float[] vertices, int vertCount,
            int vComponents, int elevIndex, int colorIndex) {
        float minElev = Float.MAX_VALUE;
        float maxElev = -Float.MAX_VALUE;
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

    /**
     * Client first obtains list of chunks which will be required to load landscape at given coordinates.
     * Then, for these chunks, factory is created which contains prepared (unzipped etc.) files for these chunks.
     */
    public interface ElevDataFactory {

        ElevData chunk(Chunk chunk) throws IOException;
    }

}
