package com.nhn.pinpoint.rpc;

import org.junit.Test;

/**
 * @author emeroad
 */
public class ClassPreLoaderTest {
    @Test
    public void testPreload() throws Exception {
        ClassPreLoader.preload();
    }
}
