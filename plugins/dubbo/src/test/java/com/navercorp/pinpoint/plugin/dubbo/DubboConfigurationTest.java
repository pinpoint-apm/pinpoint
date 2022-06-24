package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DubboConfigurationTest {

    @Mock
    ProfilerConfig config;

    @Test
    public void isDubboEnabled() {

        DubboConfiguration configuration = new DubboConfiguration(config);

        Assertions.assertFalse(configuration.isDubboEnabled());
    }

    @Test
    public void getDubboBootstrapMains() {

        DubboConfiguration configuration = new DubboConfiguration(config);

        Assertions.assertEquals(configuration.getDubboBootstrapMains().size(), 0);
    }

}