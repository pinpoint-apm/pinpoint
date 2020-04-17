package com.navercorp.pinpoint.web.scatter;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class DragAreaTest {

    @Test
    public void normalize() {
        DragArea normalize = DragArea.normalize(2, 1, 20, 10);
        Assert.assertEquals(normalize.getXHigh(), 2);
        Assert.assertEquals(normalize.getXLow(), 1);
        Assert.assertEquals(normalize.getYHigh(), 20);
        Assert.assertEquals(normalize.getYLow(), 10);
    }
}