package com.mygdx.game;

import com.badlogic.gdx.scenes.scene2d.EventListener;

/**
 * Service to which this listener is passed must make sure that all listener methods are invoked on UI thread
 */
public interface ProgressListener {

    void success();

    void fail();

    /**
     * @param percentDone == 100 exactly once when the task is complete,
     *      no further updates will be received after that
     */
    void update(int percentDone);

}
