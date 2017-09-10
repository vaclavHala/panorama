package com.mygdx.game.service;

public interface DebugFeedService {

    void addListener(DebugListener listener);

    void removeListener(DebugListener listener);

    public interface DebugListener {

        void update(String tag, String message);
    }

}
