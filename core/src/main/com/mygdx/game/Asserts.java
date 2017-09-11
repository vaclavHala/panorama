package com.mygdx.game;

public class Asserts {

    public static volatile Thread uiThread;

    public static boolean onUI() {
        return Thread.currentThread() == uiThread;
    }

    public static boolean notOnUI() {
        return Thread.currentThread() != uiThread;
    }

}
