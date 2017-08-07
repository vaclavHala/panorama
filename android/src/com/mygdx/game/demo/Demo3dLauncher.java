package com.mygdx.game.demo;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class Demo3dLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        initialize(new Demo3d(), config);
    }

}
