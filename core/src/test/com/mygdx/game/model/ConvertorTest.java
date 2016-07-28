package com.mygdx.game.model;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConvertorTest {

    @Test
    public void foo(){
        short s = (short) 45000;
        System.out.println( Integer.toBinaryString(45000) );
        System.out.println( s );
    }

    @Test
    public void test() throws IOException {

        Gdx.app = mock(Application.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                System.out.println(invocationOnMock.getArguments()[0] + " " + invocationOnMock.getArguments()[1]);
                return null;
            }
        }).when(Gdx.app).log(anyString(), anyString());

        Convertor conv = new Convertor();

        Array<Vector3> pointsLandscape = new Array<Vector3>();
        for (int y = -60; y < 60; y++) {
            for (int x = -60; x < 60; x++) {
                pointsLandscape.add(new Vector3(
                        x,
                        (float) Math.random(),
                        y));
            }
        }

        String g3djLandscape = conv.pointsToLandscape(pointsLandscape);


    }

}
