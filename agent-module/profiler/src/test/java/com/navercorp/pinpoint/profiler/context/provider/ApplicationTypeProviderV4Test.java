package com.navercorp.pinpoint.profiler.context.provider;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.name.ObjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationTypeProviderV4Test {

    private static final ServiceType TOMCAT = ServiceTypeFactory.of(1010, "TOMCAT", ServiceTypeProperty.RECORD_STATISTICS);
    private static final String SERVICE = "order-service";

    private final ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);

    private ServiceType resolve(String configured, String serviceName) {
        InstrumentConfig config = mock(InstrumentConfig.class);
        when(config.getApplicationServerType()).thenReturn(configured);
        ObjectName objectName = mock(ObjectName.class);
        when(objectName.getServiceName()).thenReturn(serviceName);
        return new ApplicationTypeProviderV4(config, registry, objectName).get();
    }

    @Test
    void configuredWasType_wins_regardlessOfService() {
        when(registry.findServiceTypeByName("TOMCAT")).thenReturn(TOMCAT);

        assertThat(resolve("TOMCAT", SERVICE)).isEqualTo(TOMCAT);
        assertThat(resolve("TOMCAT", ObjectName.DEFAULT_SERVICE_NAME)).isEqualTo(TOMCAT);
        assertThat(resolve("TOMCAT", null)).isEqualTo(TOMCAT);
    }

    @Test
    void configuredJava_isAccepted() {
        when(registry.findServiceTypeByName("JAVA")).thenReturn(ServiceType.JAVA);

        assertThat(resolve("JAVA", ObjectName.DEFAULT_SERVICE_NAME)).isEqualTo(ServiceType.JAVA);
    }

    @Test
    void unknownType_fallsBackToService() {
        when(registry.findServiceTypeByName("NOPE")).thenReturn(null);

        assertThat(resolve("NOPE", SERVICE)).isEqualTo(ServiceType.JAVA);
        assertThat(resolve("NOPE", ObjectName.DEFAULT_SERVICE_NAME)).isEqualTo(ServiceType.UNDEFINED);
    }

    @Test
    void otherService_defaultsToJava() {
        when(registry.findServiceTypeByName(null)).thenReturn(null);

        assertThat(resolve(null, SERVICE)).isEqualTo(ServiceType.JAVA);
    }

    @Test
    void defaultService_keepsPluginDetection() {
        when(registry.findServiceTypeByName(null)).thenReturn(null);

        assertThat(resolve(null, ObjectName.DEFAULT_SERVICE_NAME)).isEqualTo(ServiceType.UNDEFINED);
    }

    @Test
    void v1WithoutServiceName_keepsPluginDetection() {
        when(registry.findServiceTypeByName(null)).thenReturn(null);

        assertThat(resolve(null, null)).isEqualTo(ServiceType.UNDEFINED);
    }

    @Test
    void defaultServiceName_isCaseSensitive_likeCollector() {
        when(registry.findServiceTypeByName(null)).thenReturn(null);

        assertThat(resolve(null, "default")).isEqualTo(ServiceType.JAVA);
    }

    @Test
    void invalidNonWasType_fallsBackToService() {
        when(registry.findServiceTypeByName("MYSQL")).thenReturn(ServiceTypeFactory.of(2100, "MYSQL", ServiceTypeProperty.TERMINAL));

        assertThat(resolve("MYSQL", SERVICE)).isEqualTo(ServiceType.JAVA);
        assertThat(resolve("MYSQL", ObjectName.DEFAULT_SERVICE_NAME)).isEqualTo(ServiceType.UNDEFINED);
    }
}
