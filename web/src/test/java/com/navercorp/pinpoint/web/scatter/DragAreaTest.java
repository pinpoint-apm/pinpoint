package com.navercorp.pinpoint.web.scatter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DragAreaTest {

    @Test
    public void normalize() {
        DragArea normalize = DragArea.normalize(2, 1, 20, 10);
        Assertions.assertEquals(normalize.getXHigh(), 2);
        Assertions.assertEquals(normalize.getXLow(), 1);
        Assertions.assertEquals(normalize.getYHigh(), 20);
        Assertions.assertEquals(normalize.getYLow(), 10);
    }
}