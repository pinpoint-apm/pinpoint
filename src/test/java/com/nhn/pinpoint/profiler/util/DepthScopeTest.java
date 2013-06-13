package com.nhn.pinpoint.profiler.util;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class DepthScopeTest {
    @Test
    public void pushPop() {
        DepthScope scope = new DepthScope("test");
        Assert.assertEquals(scope.push(), 0);
        Assert.assertEquals(scope.push(), 1);
        Assert.assertEquals(scope.push(), 2);

        Assert.assertEquals(scope.depth(), 2);

        Assert.assertEquals(scope.pop(), 2);
        Assert.assertEquals(scope.pop(), 1);
        Assert.assertEquals(scope.pop(), 0);
    }

    @Test
    public void pushPopError() {
        DepthScope scope = new DepthScope("test");
        Assert.assertEquals(scope.pop(), -1);
        Assert.assertEquals(scope.pop(), -1);

        Assert.assertEquals(scope.push(), 0);
        Assert.assertEquals(scope.pop(), 0);

        Assert.assertEquals(scope.pop(), -1);


    }
}
