package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import java.util.Objects;

public class ProgressListenerWrapper {

    private static final ProgressListener NOOP = new NoopListener();

    private ProgressListenerWrapper() {
    }

    /**
     * Wraps progress in Gdx.app.postRunnable().
     * If progress is null returns noop listener.
     */
    public static ProgressListener postOnUI(ProgressListener progress) {
        if (progress == null) {
            return NOOP;
        } else if ((progress instanceof WrappedListener) || progress == NOOP) {
            return progress;
        } else {
            return new WrappedListener(progress);
        }
    }

    private static class WrappedListener implements ProgressListener {

        private final ProgressListener delegate;

        public WrappedListener(ProgressListener delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        @Override
        public void success() {
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    delegate.success();
                }
            });
        }

        @Override
        public void fail() {
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    delegate.fail();
                }
            });
        }

        @Override
        public void update(final int percentDone) {
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    delegate.update(percentDone);
                }
            });
        }

        @Override
        public String toString() {
            return "UI thread wrapped " + this.delegate;
        }
    }

    private static class NoopListener implements ProgressListener {

        @Override
        public void success() {
        }

        @Override
        public void fail() {
        }

        @Override
        public void update(int percentDone) {
        }

        @Override
        public String toString() {
            return "Noop ProgresListener";
        }
    }
}
