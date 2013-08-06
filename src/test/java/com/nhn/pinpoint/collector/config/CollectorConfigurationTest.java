package com.nhn.pinpoint.collector.config;

import org.junit.Test;

import java.io.InputStream;

/**
 *
 */
public class CollectorConfigurationTest {
    @Test
    public void testReadConfigFile() throws Exception {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("pinpoint-collector.properties");


    }
}
