package com.navercorp.pinpoint.plugin.apache.dubbo;

import org.junit.Test;

public class ApacheDubboProviderDetectorTest {

    @Test
    public void test() {
        ApacheDubboProviderDetector providerDetector = new ApacheDubboProviderDetector(null);
        providerDetector.detect();
    }
}
