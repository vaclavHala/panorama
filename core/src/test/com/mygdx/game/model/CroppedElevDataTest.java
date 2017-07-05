package com.mygdx.game.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class CroppedElevDataTest {

    @Test
    public void testExactMatch() {
        CroppedElevData cropped = new CroppedElevData(ArrayBackedElevData.incrementing(2, 2), 2, 2, 0, 0, 2, 2);
        assertEquals(0, cropped.next());
        assertEquals(1, cropped.next());
        assertEquals(2, cropped.next());
        assertEquals(3, cropped.next());
        try {
            cropped.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testUpperLeftCornerCrop() {
        CroppedElevData cropped = new CroppedElevData(ArrayBackedElevData.incrementing(3, 3), 3, 3, 0, 0, 2, 2);
        assertEquals(0, cropped.next());
        assertEquals(1, cropped.next());
        assertEquals(3, cropped.next());
        assertEquals(4, cropped.next());
        try {
            cropped.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testLowerRightCornerCrop() {
        CroppedElevData cropped = new CroppedElevData(ArrayBackedElevData.incrementing(3, 3), 3, 3, 1, 1, 3, 3);
        assertEquals(4, cropped.next());
        assertEquals(5, cropped.next());
        assertEquals(7, cropped.next());
        assertEquals(8, cropped.next());
        try {
            cropped.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testMidCrop() {
        CroppedElevData cropped = new CroppedElevData(ArrayBackedElevData.incrementing(4, 5), 5, 4, 1, 1, 3, 4);
        assertEquals(6, cropped.next());
        assertEquals(7, cropped.next());
        assertEquals(8, cropped.next());
        assertEquals(11, cropped.next());
        assertEquals(12, cropped.next());
        assertEquals(13, cropped.next());
        try {
            cropped.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

}
