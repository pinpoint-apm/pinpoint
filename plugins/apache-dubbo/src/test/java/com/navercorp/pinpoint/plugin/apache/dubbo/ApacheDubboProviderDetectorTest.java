package com.navercorp.pinpoint.plugin.apache.dubbo;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboProviderDetectorTest {

    @Test
    public void getApplicationType() {
        ApacheDubboProviderDetector dubboProviderDetector = new ApacheDubboProviderDetector(null);

        Assert.assertNotNull(dubboProviderDetector);
    }

    @Test
    public void detect() {
        ApacheDubboProviderDetector dubboProviderDetector = new ApacheDubboProviderDetector(null);

        Assert.assertFalse(dubboProviderDetector.detect());
    }
}