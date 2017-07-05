package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

public class FileBackedElevDataTest {

    @Test
    public void testSomeMethod() throws IOException {
        InputStream is = new FileInputStream("/home/hala/Documents/toy/panorama/android/assets/chunk_w0_n0");
        assertTrue(is.available() > 0);
        FileBackedElevData fileData = new FileBackedElevData(is, "chunk_w0_n0");
        for (int i = 0; i < 3601 * 3601; i++) {
            fileData.next();
        }
        try {
            fileData.next();
            fail();
        } catch (Exception e) {
            // expected
        }

    }

}
