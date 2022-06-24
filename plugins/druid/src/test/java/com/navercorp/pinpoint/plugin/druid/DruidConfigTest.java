package com.navercorp.pinpoint.plugin.druid;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DruidConfigTest {

    @Test
    public void test() {

        DruidConfig config = new DruidConfig(new DefaultProfilerConfig());

        Assertions.assertNotNull(config);
        Assertions.assertFalse(config.isPluginEnable());
        Assertions.assertFalse(config.isProfileClose());

    }

}