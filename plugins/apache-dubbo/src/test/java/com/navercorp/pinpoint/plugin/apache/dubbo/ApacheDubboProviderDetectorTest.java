package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApacheDubboProviderDetectorTest {

    @Mock
    ConditionProvider conditionProvider;

    @Test
    public void getApplicationType() {
        ApacheDubboProviderDetector dubboProviderDetector = new ApacheDubboProviderDetector(null);

        Assert.assertEquals(dubboProviderDetector.getApplicationType().getCode(), 1999);
    }

    @Test
    public void detect() {
        ApacheDubboProviderDetector dubboProviderDetector = new ApacheDubboProviderDetector(null);

        Assert.assertFalse(dubboProviderDetector.detect(conditionProvider));
    }
}