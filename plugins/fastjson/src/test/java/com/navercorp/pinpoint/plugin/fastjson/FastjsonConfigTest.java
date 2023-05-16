package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FastjsonConfigTest {
    @Test
    public void isProfile() {

        ProfilerConfig profilerConfig = new DefaultProfilerConfig();

        FastjsonConfig config = new FastjsonConfig(profilerConfig);

        Assertions.assertFalse(config.isProfile());

    }

}