package com.mygdx.game.ui;

import android.util.Log;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.mygdx.game.Conductor;
import com.mygdx.game.PanoramaPane;
import com.mygdx.game.Terraformer;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import com.mygdx.game.service.DebugFeedService;
import com.mygdx.game.service.LocationServiceException;
import com.mygdx.game.service.LocationServicePush;
import com.mygdx.game.service.LocationServicePush.LocationListener;
import static java.lang.Float.parseFloat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.GroupLayout;

public class NewPanoramaPane extends ScreenAdapter {

    private final Terraformer terraformer;
    private final Conductor conductor;
    private final LocationServicePush gps;
    private final LocationListener gpsListener;
    private final Stage uiStage;
    private final Skin skin;

    private final Actor parent;
    //    private final WidgetGroup entryArea;

    private final PopMenu popLeft;

    public NewPanoramaPane(
            Conductor conductor,
            Terraformer terraformer,
            LocationServicePush gps,
            Skin skin,
            Stage uiStage) {
        this.conductor = conductor;
        this.terraformer = terraformer;
        this.skin = skin;
        this.uiStage = uiStage;
        this.gps = gps;
        //        Gdx.input.setInputProcessor(stage);

        List<Entry<String, Runnable>> buttonsLeft = new ArrayList<Entry<String, Runnable>>();
        buttonsLeft.add(new AbstractMap.SimpleEntry<String, Runnable>("red", new Runnable() {

            @Override
            public void run() {
            }
        }));
        buttonsLeft.add(new AbstractMap.SimpleEntry<String, Runnable>("blue", new Runnable() {

            @Override
            public void run() {
            }
        }));
        popLeft = new PopMenu(skin, PopMenu.MenuSide.LEFT,
                              uiStage.getWidth(), uiStage.getHeight(),
                              buttonsLeft);
        popLeft.actor().setZIndex(10);

        EventListener goListener = new ChangeListener() {

            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                GoParams params = (GoParams) actor.getUserObject();
                log("GO for lon=%s, lat=%s", params.longitude(), params.latitude());
                if (params.longitude() != null && params.latitude() != null) {
                    NewPanoramaPane.this.terraformer.rebuildLandscape(params.longitude(), params.latitude(), null);
                    // FIXME wait for terraformer to be ready
                    NewPanoramaPane.this.conductor.screen(PanoramaPane.class);
                }
            }
        };

        Table root = new Table(skin);
        parent = root;
        root.setFillParent(true);

        root.add(new Table().add(new TextButton("A", skin)).expand().fill().getTable()
                            .row().getTable()
                            .add(new TextButton("B", skin)).expand().fill().getTable()
                            .row().getTable()
                            .add(new TextButton("C", skin)).expand().fill().getTable()
                            .row().getTable()
            ).width(80).expandY().fill();

        Table manualEntryPane = new Table(skin);
        Label manualLblLat = new Label("Latitude:", skin);
        manualLblLat.setAlignment(Align.right);
        Label manualLblLon = new Label("Longitude:", skin);
        manualLblLon.setAlignment(Align.right);
        final TextArea manualLon = new TextArea("", skin);
        final TextArea manualLat = new TextArea("", skin);
        manualEntryPane.add(new Table(skin).add(manualLblLon).fill().pad(10).getTable()
                                           .add(manualLon).expand().fill().pad(10).getTable()
                                           .row().getTable()
                                           .add(manualLblLat).fill().pad(10).getTable()
                                           .add(manualLat).expand().fill().pad(10).getTable()
                       ).expandX().fill();
        TextButton manualBtnGo = new TextButton("GO", skin);
        manualBtnGo.setUserObject(new GoParams() {

            @Override
            public Float longitude() {
                try {
                    return parseFloat(manualLon.getText());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public Float latitude() {
                try {
                    return parseFloat(manualLat.getText());
                } catch (Exception e) {
                    return null;
                }
            }
        });
        manualBtnGo.addListener(goListener);
        manualEntryPane.add(manualBtnGo).width(Value.percentHeight(1)).pad(10).fill();

        Table sensorEntryPane = new Table(skin);
        Label sensorLblLat = new Label("Latitude:", skin);
        sensorLblLat.setAlignment(Align.right);
        Label sensorLblLon = new Label("Longitude:", skin);
        sensorLblLon.setAlignment(Align.right);
        final Label sensorLon = new Label("", skin);
        final Label sensorLat = new Label("", skin);
        sensorEntryPane.add(new Table(skin).add(sensorLblLon).fill().pad(10).getTable()
                                           .add(sensorLon).expand().fill().pad(10).getTable()
                                           .row().getTable()
                                           .add(sensorLblLat).fill().pad(10).getTable()
                                           .add(sensorLat).expand().fill().pad(10).getTable()
                       ).expandX().fill();
        TextButton sensorBtnGo = new TextButton("GO", skin);
        sensorBtnGo.setUserObject(new GoParams() {

            @Override
            public Float longitude() {
                try {
                    return parseFloat(sensorLon.getText().toString());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public Float latitude() {
                try {
                    return parseFloat(sensorLat.getText().toString());
                } catch (Exception e) {
                    return null;
                }
            }
        });
        sensorBtnGo.addListener(goListener);
        sensorEntryPane.add(sensorBtnGo).width(Value.percentHeight(1)).pad(10).fill();

        //        Table savePane = new Table(skin);
        //        Label lblName = new Label("Name:", skin);
        //        lblName.setAlignment(Align.right);
        //        savePane.add(lblName).pad(10).fill();
        //        TextArea inName = new TextArea("Gimme name", skin);
        //        savePane.add(inName).pad(10).expand().fill();
        //        TextButton btnSave = new TextButton("Save", skin);
        //        savePane.add(btnSave).pad(10).fill();

        root.add(new Table().add(manualEntryPane).expandX().fill().getTable()
                            .row().getTable()
                            .add(sensorEntryPane).expandX().fill().getTable()
                            .row().getTable()
                            //                            .add(savePane).expandX().fill().getTable()
                            //                            .row().getTable()
                            .add(new Image(skin, "red")).expand().fill().getTable()
                            .row().getTable()
            ).expand().fill();

        gpsListener = new LocationListener() {

            @Override
            public void update(final float lon, final float lat) {
                Gdx.app.postRunnable(new Runnable() {

                    @Override
                    public void run() {
                        log("Location update: lon=%s, lat=%s", lon, lat);
                        sensorLon.setText("" + lon);
                        sensorLat.setText("" + lat);
                    }
                });
            }

            @Override
            public void error(LocationServiceException err) {
                log("Location error: %s", formatException(err));
            }
        };

    }

    private Actor buildManualEntryScreen() {
        Table table = new Table(skin);
        table.setZIndex(0);

        table.setBackground("menu-background");

        TextField lonText = new TextField("", skin);
        TextField latText = new TextField("", skin);
        TextButton goBtn = new TextButton("GO", skin);

        table.add(new Label("New Panorama", skin)).colspan(2);
        table.row();
        table.add(new Label("longitude:", skin));
        table.add(lonText).width(100);
        table.row();
        table.add(new Label("latitude:", skin));
        table.add(latText).width(100);
        table.row();
        table.add(goBtn).colspan(2);

        return table;
    }

    private Actor buildSensorEntryScreen() {
        Table table = new Table(skin);
        table.setFillParent(true);
        table.setZIndex(0);

        table.setBackground("menu-background");

        return table;
    }

    public InputProcessor debugInput = new InputAdapter() {

        @Override
        public boolean keyDown(int keycode) {

            return false;
        }

    };

    @Override
    public void show() {
        //        uiStage.addActor(manualEntry);
        //        uiStage.addActor(popLeft.actor());

        uiStage.addActor(parent);
        try {
            gps.addListener(this.gpsListener);
        } catch (LocationServiceException e) {
            log("Could not add location listener: %s", formatException(e));
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(debugInput, uiStage));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void hide() {
        uiStage.getActors().clear();
        gps.removeListener(this.gpsListener);
    }

    private static void log(String message, Object... args) {
        Gdx.app.log("pano.pane.new", String.format(message, args));
    }

    private static interface GoParams {

        public Float longitude();

        public Float latitude();
    }
}
