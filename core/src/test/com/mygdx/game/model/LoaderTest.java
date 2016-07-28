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
import java.io.StringReader;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoaderTest {

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

        Files files = mock(Files.class, RETURNS_DEEP_STUBS);
        byte[] fakeElevData = new byte[]{
                9, 19, 29, 39, 49, 59, 69, 79, 89, 99,
                8, 18, 28, 38, 48, 58, 68, 78, 88, 98,
                7, 17, 27, 37, 47, 57, 67, 77, 87, 97,
                6, 16, 26, 36, 46, 56, 66, 76, 86, 96,
                5, 15, 25, 35, 45, 55, 65, 75, 85, 95,
                4, 14, 24, 34, 44, 54, 64, 74, 84, 94,
                3, 13, 23, 33, 43, 53, 63, 73, 83, 93,
                2, 12, 22, 32, 42, 52, 62, 72, 82, 92,
                1, 11, 21, 31, 41, 51, 61, 71, 81, 91,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        };
        when(files.internal(anyString()).read(anyInt())).thenReturn(
                new BufferedInputStream(new ByteArrayInputStream(fakeElevData)),
                new BufferedInputStream(new ByteArrayInputStream(fakeElevData)),
                new BufferedInputStream(new ByteArrayInputStream(fakeElevData)),
                new BufferedInputStream(new ByteArrayInputStream(fakeElevData)));

        Loader sut = new Loader(files, 1, 1, 10, 10);
        Array<Vector3> land = sut.loadLandscape(new Rectangle(0.9f, 0.8f, 0.4f, 0.5f));

        System.out.println(land);
        System.out.println(land.size);
    }

}
