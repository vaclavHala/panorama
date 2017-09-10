package com.mygdx.game;

import android.util.Log;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import static com.mygdx.game.MyGdxGame.metersToBanana;
import com.mygdx.game.common.CoordTransform;
import com.mygdx.game.model.*;
import com.mygdx.game.service.DebugFeedService;
import com.mygdx.game.service.DebugFeedService.DebugListener;
import com.mygdx.game.service.LocationServicePush;
import com.mygdx.game.ui.NewPanoramaPane;
import com.mygdx.game.ui.ResourcesPane;
import java.util.ArrayDeque;
import static java.util.Collections.unmodifiableList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Panorama extends Game implements Conductor, Terraformer {

    private final DebugFeedService debug;
    private Deque<String> debugLog;
    private String debugText = "";
    private Batch debugBatch;
    private BitmapFont debugFont;
    private int debugBufferSize = 34;

    private final LocationServicePush gps;

    private Skin skin;
    private Stage uiStage;
    private ExecutorService netPool;

    private CoordTransform coordTrans;
    private ElevConfig elevCfg;

    private Chunks chunks;

    private Model landscape;
    private List<Feature> features;
    private Visibility visibility;

    public Panorama(
            LocationServicePush gps,
            DebugFeedService debug) {
        this.gps = gps;
        this.debug = debug;
    }

    @Override
    public void create() {
        if (!Gdx.files.isExternalStorageAvailable()) {
            // TODO pretty popup
            throw new IllegalStateException("External storage not available");
        }
        netPool = Executors.newFixedThreadPool(4);
        skin = PanoramaSkin.load();
        uiStage = new Stage(new StretchViewport(800, 800 * Gdx.graphics.getHeight() / Gdx.graphics.getWidth()));
        uiStage.setDebugAll(true);
        chunks = new Chunks(netPool);
        chunks.fetchAvailableChunks();

        if (this.debug != null) {
            this.debugLog = new ArrayDeque<String>();
            this.debugBatch = new SpriteBatch();
            this.debugFont = skin.get("default-font", BitmapFont.class);
            this.debug.addListener(new DebugListener() {

                @Override
                public void update(String tag, String message) {
                    if (debugLog.size() > debugBufferSize) {
                        debugLog.removeFirst();
                    }
                    debugLog.addLast(String.format("%5s: %s", tag, message));
                    StringBuilder sb = new StringBuilder();
                    for (String line : debugLog) {
                        sb.append(line).append("\n");
                    }
                    debugText = sb.toString();
                }
            });
        }

        this.screen(NewPanoramaPane.class);
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("RESIZE");
        uiStage.getViewport().update(width, height);
        super.resize(width, height);
    }

    @Override
    public void dispose() {
        this.skin.dispose();
        this.netPool.shutdownNow();
        this.uiStage.dispose();
        super.dispose();
    }

    @Override
    public void screen(Class<? extends Screen> screen) {
        Screen currentScreen = this.getScreen();
        if (currentScreen != null && currentScreen.getClass().equals(screen)) {
            return;
        }
        Screen newScreen = null;
        if (screen.equals(ResourcesPane.class)) {
            newScreen = new ResourcesPane(this, skin, uiStage, chunks);
        } else if (screen.equals(NewPanoramaPane.class)) {
            newScreen = new NewPanoramaPane(this, this, gps, skin, uiStage);
        } else if (screen.equals(PanoramaPane.class)) {
            newScreen = new PanoramaPane(this, this, skin, uiStage);
        } else {
            throw new IllegalArgumentException("Can not transition to screen " + screen);
        }
        log("Switching screen from %s to %s", currentScreen, newScreen);
        this.setScreen(newScreen);
    }

    @Override
    public void render() {
        super.render();
        if (this.debug != null) {
            debugBatch.begin();
            debugFont.draw(debugBatch, debugText, 10, Gdx.graphics.getHeight() - 10);
            debugBatch.end();
        }
    }

    @Override
    public void rebuildLandscape(float lon, float lat, ProgressListener progress) {
        if (this.landscape != null) {
            this.landscape.dispose();
        }

        // TODO first check we have all required resources, if not take user to resource screen
        // FIXME offload to background thread so we can have pretty responsive loading screen

        Vector3 userPosition = new Vector3(lat, lon, 0);
        coordTrans = new CoordTransform(111000F, 111000F, 1F, userPosition);
        //        elevCfg = new ElevConfig(1, 1, 3601 / 10 + 1, 3601 / 10 + 1, 3601 / 10.0F, 3601 / 10.0F);
        elevCfg = new ElevConfig(1, 1, 3601, 3601, 3601, 3601);
        LandscapeLoader loader = new LandscapeLoader(new CoarsedElevDataFactory(elevCfg, 1), elevCfg, coordTrans);

        float sizeDeg = 0.05F;
        ModelData landscapeModelData = loader.loadModelData(userPosition.x - sizeDeg / 2.0F, userPosition.y - sizeDeg / 2.0F, sizeDeg, sizeDeg);
        this.landscape = new Model(landscapeModelData);

        ModelMesh landscapeMesh = landscapeModelData.meshes.first();
        ModelMeshPart landscapeTris = landscapeMesh.parts[0];
        int landscapeVertComponents = 4;

        this.visibility = new Visibility(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents);

        ElevationResolution elevResolution =
                new ElevationResolution(landscapeMesh.vertices, landscapeTris.indices, landscapeVertComponents, coordTrans);

        this.features = unmodifiableList(createFeatures(elevResolution, userPosition, sizeDeg));
    }

    private List<Feature> createFeatures(ElevationResolution elevResolution, Vector3 userPosition, float sizeDeg) {
        FeatureLookup featureLookup = new FileBackedFeatureLookup(Gdx.files, elevResolution);
        // put enough margin around edges to avoid features not above landscape
        return featureLookup.lookup(userPosition.x - sizeDeg / 2.0F + 0.0005F, userPosition.y - sizeDeg / 2.0F + 0.0005F,
                                    sizeDeg - 0.001F, sizeDeg - 0.001F);
    }

    @Override
    public boolean hasLandscape() {
        return this.landscape != null;
    }

    @Override
    public Model landscape() {
        return this.landscape;
    }

    @Override
    public List<Feature> features() {
        return this.features;
    }

    @Override
    public Visibility visibility() {
        return this.visibility;
    }

    private static void log(String message, Object... args) {
        Gdx.app.log("MAIN", String.format(message, args));
    }
}
