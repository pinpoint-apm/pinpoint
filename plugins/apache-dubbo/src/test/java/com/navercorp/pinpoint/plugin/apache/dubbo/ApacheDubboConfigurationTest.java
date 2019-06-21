package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboConfigurationTest {

    @Mock
    ProfilerConfig config;

    @Test
    public void isDubboEnabled() {
        ApacheDubboConfiguration configuration = new ApacheDubboConfiguration(config);

        Assert.assertFalse(configuration.isDubboEnabled());
    }

    @Test
    public void getDubboBootstrapMains() {
        ApacheDubboConfiguration configuration = new ApacheDubboConfiguration(config);

        Assert.assertEquals(configuration.getDubboBootstrapMains().size(),0);
    }
}