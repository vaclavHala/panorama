package com.mygdx.game;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.badlogic.gdx.Application;
import static com.mygdx.game.common.ExceptionFormatter.formatException;
import com.mygdx.game.service.DebugFeedService;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogcatDebugFeedService implements DebugFeedService, Runnable {

    private final List<DebugListener> listeners;
    private final Application sink;

    private final Thread scanner;

    public LogcatDebugFeedService(Application sink) {
        this.listeners = new CopyOnWriteArrayList<DebugListener>();
        this.sink = sink;
        this.scanner = new Thread(this);
        this.scanner.setDaemon(true);
    }

    public void start() {
        Log.d("pano.debug", "Starting logcat spy");
        this.scanner.start();
    }

    @Override
    public void addListener(DebugListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(DebugListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        try {
            Process process = new ProcessBuilder().command("logcat")
                                                  .redirectErrorStream(true)
                                                  .start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            Log.d("pano.debug", "Logcat spy started");
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String tag = "";
                final String msg = line;
                this.sink.postRunnable(new Runnable() {

                    @Override
                    public void run() {
                        for (DebugListener dl : listeners) {
                            dl.update(tag, msg);
                        }
                    }
                });
            }
            Log.d("pano.debug", "Logcat spy finished");
        } catch (Exception e) {
            Log.e("pano.debug", "Logcat spy error: " + formatException(e));
        }
    }
}
