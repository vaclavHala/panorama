package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Gutted {@link CameraInputController} modified to work with our coordinate system (Z is up)
 * @author hala
 */
public class CamController extends GestureDetector {

    /** The button for rotating the camera. */
    public int rotateButton = Input.Buttons.LEFT;
    /** The angle to rotate when moved the full width or height of the screen. */
    public float rotateAngle = 360f;
    /** The button for translating the camera along the up/right plane */
    public int translateButton = Input.Buttons.RIGHT;
    /** The units to translate the camera when moved the full width or height of the screen. */
    public float translateUnits = 100f; // FIXME auto calculate this based on the target
    /** The button for translating the camera along the direction axis */
    public int forwardButton = Input.Buttons.MIDDLE;
    /** The key which must be pressed to activate rotate, translate and forward or 0 to always activate. */
    public int activateKey = 0;
    /** Indicates if the activateKey is currently being pressed. */
    protected boolean activatePressed;
    /** Whether scrolling requires the activeKey to be pressed (false) or always allow scrolling (true). */
    public boolean alwaysScroll = true;
    /** The weight for each scrolled amount. */
    public float scrollFactor = -1f;
    /** World units per screen size */
    public float pinchZoomFactor = 10f;
    /** Whether to update the camera after it has been changed. */
    public boolean autoUpdate = true;
    /** The target to rotate around. */
    public Vector3 target = new Vector3();
    /** Whether to update the target on translation */
    public boolean translateTarget = true;
    /** Whether to update the target on forward */
    public boolean forwardTarget = true;
    /** Whether to update the target on scroll */
    public boolean scrollTarget = false;

    /** The camera. */
    public Camera camera;
    /** The current (first) button being pressed. */
    protected int button = -1;

    private float startX, startY;
    private final Vector3 tmpV1 = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

    protected static class CameraGestureListener extends GestureDetector.GestureAdapter {

        public CamController controller;
        private float previousZoom;

        @Override
        public boolean touchDown(float x, float y, int pointer, int button) {
            previousZoom = 0;
            return false;
        }

        @Override
        public boolean tap(float x, float y, int count, int button) {
            return false;
        }

        @Override
        public boolean longPress(float x, float y) {
            return false;
        }

        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return false;
        }

        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            float newZoom = distance - initialDistance;
            float amount = newZoom - previousZoom;
            previousZoom = newZoom;
            float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
            return controller.pinchZoom(amount / ((w > h) ? h : w));
        }

        @Override
        public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
            return false;
        }
    };

    protected final CameraGestureListener gestureListener;

    protected CamController(final CameraGestureListener gestureListener, final Camera camera) {
        super(gestureListener);
        this.gestureListener = gestureListener;
        this.gestureListener.controller = this;
        this.camera = camera;
    }

    public CamController(final Camera camera) {
        this(new CameraGestureListener(), camera);
    }

    public void update() {
    }

    private int touched;
    private boolean multiTouch;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touched |= (1 << pointer);
        multiTouch = !MathUtils.isPowerOfTwo(touched);
        if (multiTouch)
            this.button = -1;
        else if (this.button < 0 && (activateKey == 0 || activatePressed)) {
            startX = screenX;
            startY = screenY;
            this.button = button;
        }
        return super.touchDown(screenX, screenY, pointer, button) || (activateKey == 0 || activatePressed);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        touched &= -1 ^ (1 << pointer);
        multiTouch = !MathUtils.isPowerOfTwo(touched);
        if (button == this.button)
            this.button = -1;
        return super.touchUp(screenX, screenY, pointer, button) || activatePressed;
    }

    protected boolean process(float deltaX, float deltaY, int button) {
        if (button == rotateButton) {
            tmpV1.set(camera.direction).crs(camera.up).z = 0f;
            camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
            camera.rotateAround(target, Vector3.Z, deltaX * -rotateAngle);
        } else if (button == translateButton) {
            camera.translate(tmpV1.set(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits));
            camera.translate(tmpV2.set(camera.up).scl(-deltaY * translateUnits));
            if (translateTarget)
                target.add(tmpV1).add(tmpV2);
        } else if (button == forwardButton) {
            camera.translate(tmpV1.set(camera.direction).scl(deltaY * translateUnits));
            if (forwardTarget)
                target.add(tmpV1);
        }
        if (autoUpdate)
            camera.update();
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        boolean result = super.touchDragged(screenX, screenY, pointer);
        if (result || this.button < 0)
            return result;
        final float deltaX = (screenX - startX) / Gdx.graphics.getWidth();
        final float deltaY = (startY - screenY) / Gdx.graphics.getHeight();
        startX = screenX;
        startY = screenY;
        return process(deltaX, deltaY, button);
    }

    @Override
    public boolean scrolled(int amount) {
        return zoom(amount * scrollFactor * translateUnits);
    }

    public boolean zoom(float amount) {
        if (!alwaysScroll && activateKey != 0 && !activatePressed)
            return false;
        camera.translate(tmpV1.set(camera.direction).scl(amount));
        if (scrollTarget)
            target.add(tmpV1);
        if (autoUpdate)
            camera.update();
        return true;
    }

    protected boolean pinchZoom(float amount) {
        return zoom(pinchZoomFactor * amount);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }
}
