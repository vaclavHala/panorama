package com.mygdx.game.camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import java.util.Random;

public class AndroidCameraService implements CameraService, Camera.PreviewCallback, Disposable {

    private static final String TAG = AndroidCameraService.class.getSimpleName();

    private final int previewWidth;
    private final int previewHeight;
    private final Runnable startCamera;
    private Bitmap bmp;
    private Texture tex;
    private TextureRegion reg;


    public AndroidCameraService(int width, int height, Runnable startCamera) {
        this.previewWidth = width;
        this.previewHeight = height;
        this.startCamera = startCamera;
    }

    @Override
    public void create() {
        int width = MathUtils.nextPowerOfTwo(previewWidth);
        int height = MathUtils.nextPowerOfTwo(previewHeight);
        log("Preview: %s (%s) x %s (%s)", previewWidth, width, previewHeight, height);
        this.tex = new Texture(width, height, Pixmap.Format.RGB888);
//       this. tex = new Texture(Gdx.files.internal("medlanky_letiste.png"));
        this.bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        this.reg = new TextureRegion(tex, previewWidth, previewHeight);
//        p = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < bmp.getHeight(); y++) {
            for (int x = 0; x < bmp.getWidth(); x++) {
                bmp.setPixel(x, y, android.graphics.Color.argb(255, 255, 0, 0));
            }
        }

        log("Create Thread: "+Thread.currentThread());
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        this.startCamera.run();
    }

    @Override
    public TextureRegion cameraView() {
        return this.reg;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        log("Preview: " + data.length);
        log("Preview Thread: "+Thread.currentThread());
//        bmp.setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height);
//
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex.getTextureObjectHandle());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

//        p.setColor(Color.RED);
//        p.fillRectangle(0, 0, p.getWidth(), p.getHeight());


//        this.tex.draw(p, 0, 0);

    }

//    Pixmap p ;

    private static void log(String format, Object... args) {
        Gdx.app.log(TAG, String.format(format, args));
    }

    @Override
    public void dispose() {
        tex.dispose();
        bmp.recycle();
    }
}
