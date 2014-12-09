package com.navercorp.pinpoint.rpc;

import org.junit.Test;

import com.navercorp.pinpoint.rpc.ClassPreLoader;

/**
 * @author emeroad
 */
public class ClassPreLoaderTest {
    @Test
    public void testPreload() throws Exception {
        ClassPreLoader.preload();
    }
}
