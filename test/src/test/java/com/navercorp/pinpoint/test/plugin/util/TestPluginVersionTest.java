package com.navercorp.pinpoint.test.plugin.util;

import org.junit.Test;

public class TestPluginVersionTest {
    @Test
    public void test() {
        org.junit.Assert.assertNotNull(TestPluginVersion.getVersion());
    }

}