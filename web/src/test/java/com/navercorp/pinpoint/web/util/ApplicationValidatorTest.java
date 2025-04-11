package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationValidatorTest {

    @Test
    void newApplication_typeCode() {
        ServiceType serviceType = ServiceType.STAND_ALONE;
        Application application = new Application("appName", serviceType);

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Application newApplication = validator.newApplication("appName", serviceType.getCode(), null);
        Assertions.assertEquals(application, newApplication);
    }

    @Test
    void newApplication_typeName() {
        ServiceType serviceType = ServiceType.STAND_ALONE;
        Application application = new Application("appName", serviceType);

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceTypeByName(serviceType.getName())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Application newApplication = validator.newApplication("appName", -1, serviceType.getName());
        Assertions.assertEquals(application, newApplication);
    }

    @Test
    void newApplication_invalid() {
        ServiceType serviceType = ServiceType.UNDEFINED;
        Application undefined = new Application("appName", serviceType);

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceTypeByName(serviceType.getName())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("appName", serviceType.getCode(), null));
    }

    @Test
    void newApplication_invalid_appName() {

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        ApplicationValidator validator = new ApplicationValidator(registry);

        String appName = "a".repeat(256);
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication(appName, -1, null));

    }
}