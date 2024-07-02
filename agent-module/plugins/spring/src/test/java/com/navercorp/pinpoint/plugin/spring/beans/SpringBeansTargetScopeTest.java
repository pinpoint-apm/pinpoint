package com.navercorp.pinpoint.plugin.spring.beans;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpringBeansTargetScopeTest {

    @Test
    void testGet() {
        Assertions.assertEquals(SpringBeansTargetScope.POST_PROCESSOR, SpringBeansTargetScope.get("post-processor"));
        Assertions.assertEquals(SpringBeansTargetScope.POST_PROCESSOR, SpringBeansTargetScope.get("POST-PROCESSOR"));
        Assertions.assertEquals(SpringBeansTargetScope.POST_PROCESSOR, SpringBeansTargetScope.get("POST-processor"));

        Assertions.assertEquals(SpringBeansTargetScope.COMPONENT_SCAN, SpringBeansTargetScope.get(null));
        Assertions.assertEquals(SpringBeansTargetScope.COMPONENT_SCAN, SpringBeansTargetScope.get("Test-processor"));
    }
}