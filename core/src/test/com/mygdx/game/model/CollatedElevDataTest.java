package com.mygdx.game.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

public class CollatedElevDataTest {

    @Test
    public void testSingleComponent() {
        System.out.println(ArrayBackedElevData.incrementing(2, 2));
        CollatedElevData collated = new CollatedElevData(new ElevData[]{ArrayBackedElevData.incrementing(2, 2)},
                                                         1, 1, 2, 2);

        assertEquals(0, collated.next());
        assertEquals(1, collated.next());
        assertEquals(2, collated.next());
        assertEquals(3, collated.next());
        try {
            collated.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testVerticalOnly() {
        CollatedElevData collated = new CollatedElevData(new ElevData[]{ArrayBackedElevData.incrementing(2, 2, 0),
                                                                        ArrayBackedElevData.incrementing(2, 2, 10),
                                                                        ArrayBackedElevData.incrementing(2, 2, 20)},
                                                         1, 3, 2, 2);

        assertEquals(0, collated.next());
        assertEquals(1, collated.next());
        assertEquals(2, collated.next());
        assertEquals(3, collated.next());

        assertEquals(10, collated.next());
        assertEquals(11, collated.next());
        assertEquals(12, collated.next());
        assertEquals(13, collated.next());

        assertEquals(20, collated.next());
        assertEquals(21, collated.next());
        assertEquals(22, collated.next());
        assertEquals(23, collated.next());

        try {
            collated.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testHorizontalOnly() {
        CollatedElevData collated = new CollatedElevData(new ElevData[]{ArrayBackedElevData.incrementing(2, 2, 0),
                                                                        ArrayBackedElevData.incrementing(2, 2, 10),
                                                                        ArrayBackedElevData.incrementing(2, 2, 20)},
                                                         3, 1, 2, 2);

        assertEquals(0, collated.next());
        assertEquals(1, collated.next());
        assertEquals(10, collated.next());
        assertEquals(11, collated.next());
        assertEquals(20, collated.next());
        assertEquals(22, collated.next());

        assertEquals(2, collated.next());
        assertEquals(3, collated.next());
        assertEquals(12, collated.next());
        assertEquals(13, collated.next());
        assertEquals(22, collated.next());
        assertEquals(23, collated.next());

        try {
            collated.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

    @Test
    public void testRectangle() {
        CollatedElevData collated = new CollatedElevData(new ElevData[]{ArrayBackedElevData.incrementing(2, 2, 0),
                                                                        ArrayBackedElevData.incrementing(2, 2, 10),
                                                                        ArrayBackedElevData.incrementing(2, 2, 20),
                                                                        ArrayBackedElevData.incrementing(2, 2, 30),
                                                                        ArrayBackedElevData.incrementing(2, 2, 40),
                                                                        ArrayBackedElevData.incrementing(2, 2, 50)},
                                                         2, 3, 2, 2);

        assertEquals(0, collated.next());
        assertEquals(1, collated.next());
        assertEquals(10, collated.next());
        assertEquals(11, collated.next());
        assertEquals(2, collated.next());
        assertEquals(3, collated.next());
        assertEquals(12, collated.next());
        assertEquals(13, collated.next());

        assertEquals(20, collated.next());
        assertEquals(21, collated.next());
        assertEquals(30, collated.next());
        assertEquals(31, collated.next());
        assertEquals(22, collated.next());
        assertEquals(23, collated.next());
        assertEquals(32, collated.next());
        assertEquals(33, collated.next());

        assertEquals(40, collated.next());
        assertEquals(41, collated.next());
        assertEquals(50, collated.next());
        assertEquals(51, collated.next());
        assertEquals(42, collated.next());
        assertEquals(43, collated.next());
        assertEquals(52, collated.next());
        assertEquals(53, collated.next());

        try {
            collated.next();
            fail();
        } catch (Exception e) {
            //expected
        }
    }

}
