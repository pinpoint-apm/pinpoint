package com.navercorp.pinpoint.plugin.dubbo;

import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DubboProviderDetectorTest {

    @Mock
    ConditionProvider conditionProvider;

    @Test
    public void getApplicationType() {

        DubboProviderDetector dubboProviderDetector = new DubboProviderDetector(null);

        Assert.assertEquals(dubboProviderDetector.getApplicationType().getCode(), 1110);
    }

    @Test
    public void detect() {

        DubboProviderDetector dubboProviderDetector = new DubboProviderDetector(null);

        Assert.assertFalse(dubboProviderDetector.detect(conditionProvider));
    }
}