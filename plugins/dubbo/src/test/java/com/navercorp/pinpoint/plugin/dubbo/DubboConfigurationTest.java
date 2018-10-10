package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DubboConfigurationTest {

    @Mock
    ProfilerConfig config;

    @Test
    public void isDubboEnabled() {

        DubboConfiguration configuration = new DubboConfiguration(config);

        Assert.assertFalse(configuration.isDubboEnabled());
    }

    @Test
    public void getDubboBootstrapMains() {

        DubboConfiguration configuration = new DubboConfiguration(config);

        Assert.assertEquals(configuration.getDubboBootstrapMains().size(),0);
    }

}