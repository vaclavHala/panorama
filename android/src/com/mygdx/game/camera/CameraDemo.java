package com.mygdx.game.camera;

import com.badlogic.gdx.graphics.GL20;
import java.nio.ByteBuffer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class CameraDemo implements ApplicationListener {

    public enum Mode {
        normal,
        prepare,
        preview,
        takePicture,
        waitForPictureReady,
    }

    public static final float vertexData[] = {
            1.0f,  1.0f,  1.0f, Color.toFloatBits(255,255,255,255),  0.0f, 0.0f, // quad/face 0/Vertex 0
            0.0f,  1.0f,  1.0f, Color.toFloatBits(255,255,255,255),  0.0f, 1.0f, // quad/face 0/Vertex 1
            0.0f,  0.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 1.0f, // quad/face 0/Vertex 2
            1.0f,  0.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 0.0f, // quad/face 0/Vertex 3

            1.0f,  1.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 1.0f, // quad/face 1/Vertex 4
            1.0f,  0.0f,  1.0f, Color.toFloatBits(255,255,255,255),  0.0f, 1.0f, // quad/face 1/Vertex 5
            1.0f,  0.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 0.0f, // quad/face 1/Vertex 6
            1.0f,  1.0f,  0.0f, Color.toFloatBits(255,255,255,255),  1.0f, 0.0f, // quad/face 1/Vertex 7

            1.0f,  1.0f,  1.0f, Color.toFloatBits(255,255,255,255),  0.0f, 0.0f, // quad/face 2/Vertex 8
            1.0f,  1.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 1.0f, // quad/face 2/Vertex 9
            0.0f,  1.0f,  0.0f, Color.toFloatBits(255,255,255,255),  1.0f, 1.0f, // quad/face 2/Vertex 10
            0.0f,  1.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 0.0f, // quad/face 2/Vertex 11

            1.0f,  0.0f,  0.0f, Color.toFloatBits(255,255,255,255),  1.0f, 1.0f, // quad/face 3/Vertex 12
            0.0f,  0.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 1.0f, // quad/face 3/Vertex 13
            0.0f,  1.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 0.0f, // quad/face 3/Vertex 14
            1.0f,  1.0f,  0.0f, Color.toFloatBits(255,255,255,255),  1.0f, 0.0f, // quad/face 3/Vertex 15

            0.0f,  1.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 1.0f, // quad/face 4/Vertex 16
            0.0f,  1.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 1.0f, // quad/face 4/Vertex 17
            0.0f,  0.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 0.0f, // quad/face 4/Vertex 18
            0.0f,  0.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 0.0f, // quad/face 4/Vertex 19

            0.0f,  0.0f,  0.0f, Color.toFloatBits(255,255,255,255),  1.0f, 1.0f, // quad/face 5/Vertex 20
            1.0f,  0.0f,  0.0f, Color.toFloatBits(255,255,255,255),  0.0f, 1.0f, // quad/face 5/Vertex 21
            1.0f,  0.0f,  1.0f, Color.toFloatBits(255,255,255,255),  0.0f, 0.0f, // quad/face 5/Vertex 22
            0.0f,  0.0f,  1.0f, Color.toFloatBits(255,255,255,255),  1.0f, 0.0f, // quad/face 5/Vertex 23
    };


    public static final short facesVerticesIndex[][] = {
            { 0, 1, 2, 3 },
            { 4, 5, 6, 7 },
            { 8, 9, 10, 11 },
            { 12, 13, 14, 15 },
            { 16, 17, 18, 19 },
            { 20, 21, 22, 23 }
    };

    private final static VertexAttribute verticesAttributes[] = new VertexAttribute[] {
            new VertexAttribute(Usage.Position, 3, "a_position"),
            new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
            new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoords"),
    };


    private Texture texture;

//
//    private Mesh[] mesh = new Mesh[6];

//
    private PerspectiveCamera camera;


    private Mode mode = Mode.normal;


    private final DeviceCameraControl deviceCameraControl;


    public CameraDemo(DeviceCameraControl cameraControl) {
        this.deviceCameraControl = cameraControl;
    }

    @Override
    public void create() {

        // Load the Libgdx splash screen texture
        texture = new Texture(Gdx.files.internal("data/libgdx.jpg"));
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

//        // Create the 6 faces of the Cube
//        for (int i=0;i<6;i++) {
//            mesh[i] = new Mesh(true, 24, 4, verticesAttributes);
//            mesh[i].setVertices(vertexData);
//            mesh[i].setIndices(facesVerticesIndex[i]);
//        }

        // Create the OpenGL Camera
        camera = new PerspectiveCamera(67.0f, 2.0f * Gdx.graphics.getWidth() / Gdx.graphics.getHeight(), 2.0f);
        camera.far = 100.0f;
        camera.near = 0.1f;
        camera.position.set(2.0f,2.0f,2.0f);
        camera.lookAt(0.0f, 0.0f, 0.0f);

    }

    @Override
    public void dispose() {
        texture.dispose();
//        for (int i=0;i<6;i++) {
//            mesh[i].dispose();
//            mesh[i] = null;
//        }
        texture = null;
    }

    @Override
    public void render() {
        if (Gdx.input.isTouched()) {
            if (mode == Mode.normal) {
                mode = Mode.prepare;
                if (deviceCameraControl != null) {
                    deviceCameraControl.prepareCameraAsync();
                }
            }
        } else { // touch removed
            if (mode == Mode.preview) {
                mode = Mode.takePicture;
            }
        }

//        Gdx.gl10.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
        if (mode == Mode.takePicture) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            if (deviceCameraControl != null) {
                deviceCameraControl.takePicture();
            }
            mode = Mode.waitForPictureReady;
        } else if (mode == Mode.waitForPictureReady) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else if (mode == Mode.prepare) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            if (deviceCameraControl != null) {
                if (deviceCameraControl.isReady()) {
                    deviceCameraControl.startPreviewAsync();
                    mode = Mode.preview;
                }
            }
        } else if (mode == Mode.preview) {
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        } else { // mode = normal
            Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_TEXTURE);
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);
//        Gdx.gl.glEnable(GL20.GL_LINE_SMOOTH);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glClearDepthf(1.0F);
        camera.update(true);
//        camera.apply(Gdx.gl10);
        texture.bind();
//        for (int i=0;i<6;i++) {
//            mesh[i].render(GL20.GL_TRIANGLE_FAN, 0 ,4);
//        }
        if (mode == Mode.waitForPictureReady) {
            if (deviceCameraControl.getPictureData() != null) { // camera picture was actually taken
                // take Gdx Screenshot
//                Pixmap screenshotPixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                Pixmap cameraPixmap = new Pixmap(deviceCameraControl.getPictureData(), 0, deviceCameraControl.getPictureData().length);
//                merge2Pixmaps(cameraPixmap, screenshotPixmap);
                // we could call PixmapIO.writePNG(pngfile, cameraPixmap);
//                FileHandle jpgfile = Gdx.files.external("libGdxSnapshot.jpg");
//                deviceCameraControl.saveAsJpeg(jpgfile, cameraPixmap);
                deviceCameraControl.stopPreviewAsync();
                mode = Mode.normal;
            }
        }
    }

//    private Pixmap merge2Pixmaps(Pixmap mainPixmap, Pixmap overlayedPixmap) {
//        // merge to data and Gdx screen shot - but fix Aspect Ratio issues between the screen and the camera
//        Pixmap.setFilter(Filter.BiLinear);
//        float mainPixmapAR = (float)mainPixmap.getWidth() / mainPixmap.getHeight();
//        float overlayedPixmapAR = (float)overlayedPixmap.getWidth() / overlayedPixmap.getHeight();
//        if (overlayedPixmapAR < mainPixmapAR) {
//            int overlayNewWidth = (int)(((float)mainPixmap.getHeight() / overlayedPixmap.getHeight()) * overlayedPixmap.getWidth());
//            int overlayStartX = (mainPixmap.getWidth() - overlayNewWidth)/2;
//            // Overlaying pixmaps
//            mainPixmap.drawPixmap(overlayedPixmap, 0, 0, overlayedPixmap.getWidth(), overlayedPixmap.getHeight(), overlayStartX, 0, overlayNewWidth, mainPixmap.getHeight());
//        } else {
//            int overlayNewHeight = (int)(((float)mainPixmap.getWidth() / overlayedPixmap.getWidth()) * overlayedPixmap.getHeight());
//            int overlayStartY = (mainPixmap.getHeight() - overlayNewHeight)/2;
//            // Overlaying pixmaps
//            mainPixmap.drawPixmap(overlayedPixmap, 0, 0, overlayedPixmap.getWidth(), overlayedPixmap.getHeight(), 0, overlayStartY, mainPixmap.getWidth(), overlayNewHeight);
//        }
//        return mainPixmap;
//    }

//    public Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY) {
//        Gdx.gl.glPixelStorei(GL10.GL_PACK_ALIGNMENT, 1);
//
//        final Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
//        ByteBuffer pixels = pixmap.getPixels();
//        Gdx.gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
//
//        final int numBytes = w * h * 4;
//        byte[] lines = new byte[numBytes];
//        if (flipY) {
//            final int numBytesPerLine = w * 4;
//            for (int i = 0; i < h; i++) {
//                pixels.position((h - i - 1) * numBytesPerLine);
//                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
//            }
//            pixels.clear();
//            pixels.put(lines);
//        } else {
//            pixels.clear();
//            pixels.get(lines);
//        }
//
//        return pixmap;
//    }

    @Override
    public void resize(int width, int height) {
        camera = new PerspectiveCamera(67.0f, 2.0f * width / height, 2.0f);
        camera.far = 100.0f;
        camera.near = 0.1f;
        camera.position.set(2.0f,2.0f,2.0f);
        camera.lookAt(0.0f, 0.0f, 0.0f);

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}