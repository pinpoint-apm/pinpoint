package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;

public class FastjsonConfigTest {

    @Test
    public void isProfile() {

        ProfilerConfig profilerConfig = new DefaultProfilerConfig();

        FastjsonConfig config = new FastjsonConfig(profilerConfig);

        Assert.assertFalse(config.isProfile());

        System.out.println(config.toString());
    }

}