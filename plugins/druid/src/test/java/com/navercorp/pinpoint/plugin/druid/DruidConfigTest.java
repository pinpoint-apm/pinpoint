package com.navercorp.pinpoint.plugin.druid;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import org.junit.Assert;
import org.junit.Test;

public class DruidConfigTest {

    @Test
    public void test() {

        DruidConfig config = new DruidConfig(new DefaultProfilerConfig());

        Assert.assertNotNull(config);
        Assert.assertFalse(config.isPluginEnable());
        Assert.assertFalse(config.isProfileClose());

    }

}