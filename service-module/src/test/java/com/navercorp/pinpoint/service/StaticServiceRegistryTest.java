package com.navercorp.pinpoint.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.component.StaticServiceRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StaticServiceRegistryTest {

    private static StaticServiceRegistry registry;

    @BeforeAll
    public static void setUp() {
        registry = new StaticServiceRegistry();
    }

    @Test
    public void staticServiceUidRegistryTest() {
        ServiceUid defaultServiceUid = registry.getServiceUid(ServiceUid.DEFAULT_SERVICE_UID_NAME);
        Assertions.assertThat(defaultServiceUid).isNotNull();
        Assertions.assertThat(defaultServiceUid).isEqualTo(ServiceUid.DEFAULT);

        String defaultServiceName = registry.getServiceName(ServiceUid.DEFAULT);
        String defaultServiceNameByUid = registry.getServiceName(ServiceUid.DEFAULT_SERVICE_UID_CODE);
        Assertions.assertThat(defaultServiceName).isNotNull();
        Assertions.assertThat(defaultServiceName).isEqualTo(defaultServiceNameByUid);
    }

    @Test
    public void defaultServiceNameTest() {
        String defaultServiceName = registry.getServiceName(ServiceUid.DEFAULT);
        Assertions.assertThat(defaultServiceName).isEqualToIgnoringCase(ServiceUid.DEFAULT_SERVICE_UID_NAME);

        ServiceUid serviceUid1 = registry.getServiceUid("default");
        Assertions.assertThat(serviceUid1).isEqualTo(ServiceUid.DEFAULT);

        ServiceUid serviceUid2 = registry.getServiceUid("Default");
        Assertions.assertThat(serviceUid2).isEqualTo(ServiceUid.DEFAULT);

        ServiceUid serviceUid3 = registry.getServiceUid("DEFAULT");
        Assertions.assertThat(serviceUid3).isEqualTo(ServiceUid.DEFAULT);
    }
}
