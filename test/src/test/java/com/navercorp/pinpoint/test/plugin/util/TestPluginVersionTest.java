package com.navercorp.pinpoint.test.plugin.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestPluginVersionTest {
    @Test
    public void test() {
        assertNotNull(TestPluginVersion.getVersion());
    }

}