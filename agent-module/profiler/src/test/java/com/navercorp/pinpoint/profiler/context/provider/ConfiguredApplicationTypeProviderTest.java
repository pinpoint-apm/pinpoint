package com.navercorp.pinpoint.profiler.context.provider;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfiguredApplicationTypeProviderTest {

    private static final ServiceType TOMCAT = ServiceTypeFactory.of(1010, "TOMCAT", ServiceTypeProperty.RECORD_STATISTICS);

    private final ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);

    private ServiceType resolve(String configured) {
        InstrumentConfig config = mock(InstrumentConfig.class);
        when(config.getApplicationServerType()).thenReturn(configured);
        return new ConfiguredApplicationTypeProvider(config, registry).get();
    }

    @Test
    void configuredWasType() {
        when(registry.findServiceTypeByName("TOMCAT")).thenReturn(TOMCAT);

        assertThat(resolve("TOMCAT")).isEqualTo(TOMCAT);
    }

    @Test
    void configuredJava_isAccepted() {
        when(registry.findServiceTypeByName("JAVA")).thenReturn(ServiceType.JAVA);

        assertThat(resolve("JAVA")).isEqualTo(ServiceType.JAVA);
    }

    @Test
    void unset_keepsPluginDetection() {
        when(registry.findServiceTypeByName(null)).thenReturn(null);

        assertThat(resolve(null)).isEqualTo(ServiceType.UNDEFINED);
    }

    @Test
    void unknownType_keepsPluginDetection() {
        when(registry.findServiceTypeByName("NOPE")).thenReturn(null);

        assertThat(resolve("NOPE")).isEqualTo(ServiceType.UNDEFINED);
    }

    @Test
    void invalidNonWasType_keepsPluginDetection() {
        when(registry.findServiceTypeByName("MYSQL")).thenReturn(ServiceTypeFactory.of(2100, "MYSQL", ServiceTypeProperty.TERMINAL));

        assertThat(resolve("MYSQL")).isEqualTo(ServiceType.UNDEFINED);
    }
}
