package com.nhn.pinpoint.profiler.util;

/**
 * @author emeroad
 */
import org.junit.Test;
import org.junit.Assert;

/**
 * @author emeroad
 */
public class DepthScopeTest {
    @Test
    public void pushPop() {
        DepthScope scope = new DepthScope("test");
        Assert.assertEquals(scope.push(), 0);
        Assert.assertEquals(scope.push(), 1);
        Assert.assertEquals(scope.push(), 2);

        Assert.assertEquals(scope.depth(), 3);

        Assert.assertEquals(scope.pop(), 2);
        Assert.assertEquals(scope.pop(), 1);
        Assert.assertEquals(scope.pop(), 0);
    }

    @Test
    public void pushPopError() {
        DepthScope scope = new DepthScope("test");
        Assert.assertEquals(scope.pop(), -1);
        Assert.assertEquals(scope.pop(), -2);

        Assert.assertEquals(scope.push(), -2);
        Assert.assertEquals(scope.push(), -1);

        Assert.assertEquals(scope.depth(), 0);


    }
}

