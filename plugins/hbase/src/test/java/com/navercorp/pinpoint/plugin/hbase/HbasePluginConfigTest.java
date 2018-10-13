package com.navercorp.pinpoint.plugin.hbase;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HbasePluginConfigTest {

    @Test
    public void isHbaseProfile() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        HbasePluginConfig config = new HbasePluginConfig(profilerConfig);
        assertTrue(config.isHbaseProfile());
        System.out.println(config);
    }

    @Test
    public void isOperationProfile() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        HbasePluginConfig config = new HbasePluginConfig(profilerConfig);
        assertTrue(config.isOperationProfile());
        System.out.println(config);
    }

}