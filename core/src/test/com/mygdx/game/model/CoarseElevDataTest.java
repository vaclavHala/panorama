package com.mygdx.game.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class CoarseElevDataTest {

    @Test
    public void testSomeMethod() {
        CoarseElevData coarse = new CoarseElevData(ArrayBackedElevData.incrementing(2, 2), 2, 2, 2);

        assertEquals(0, coarse.next());
        try {
            coarse.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testSizeIsNotMultipleOfBounds() {
        CoarseElevData coarse = new CoarseElevData(ArrayBackedElevData.incrementing(5, 5), 2, 5, 5);

        assertEquals(0, coarse.next());
        assertEquals(2, coarse.next());
        assertEquals(4, coarse.next());
        assertEquals(10, coarse.next());
        assertEquals(12, coarse.next());
        assertEquals(14, coarse.next());
        assertEquals(20, coarse.next());
        assertEquals(22, coarse.next());
        assertEquals(24, coarse.next());
        try {
            coarse.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testBaseIsNotSquare() {
        CoarseElevData coarse = new CoarseElevData(ArrayBackedElevData.incrementing(3, 4), 2, 4, 3);

        assertEquals(0, coarse.next());
        assertEquals(2, coarse.next());
        assertEquals(8, coarse.next());
        assertEquals(10, coarse.next());
        try {
            coarse.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

}
