package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.server.bo.Application;
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
    void newApplication_remoteAddressName() {
        // a non-WAS node is named by its remote call address, which is not a valid agent/application id
        ServiceType serviceType = ServiceType.UNKNOWN_DB;
        String remoteAddress = "dev-pinot-broker-external.n3r-project-pinot-dev.svc.pgds1.io.navercorp.com:8099";
        String url = "http://user:pw@host.example.com:8080/path/to/resource?query=1&x=y#frag";

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertEquals(new Application(remoteAddress, serviceType),
                validator.newApplication(remoteAddress, serviceType.getCode(), null));
        Assertions.assertEquals(new Application(url, serviceType),
                validator.newApplication(url, serviceType.getCode(), null));

        // but it must still be made of URL characters
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication(" ", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("host name", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("host<name>", serviceType.getCode(), null));
    }

    @Test
    void newApplication_was_rejectsAddressName() {
        // a WAS applicationName is an agent-registered id and must match the id pattern
        ServiceType serviceType = ServiceType.STAND_ALONE;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertEquals(new Application("app.name-1_2", serviceType),
                validator.newApplication("app.name-1_2", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("host.example.com:8080", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("app name", serviceType.getCode(), null));
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
        ServiceType serviceType = ServiceType.UNKNOWN_DB;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);
        ApplicationValidator validator = new ApplicationValidator(registry);

        String maxName = "a".repeat(ApplicationValidator.MAX_LENGTH);
        Assertions.assertEquals(new Application(maxName, serviceType),
                validator.newApplication(maxName, serviceType.getCode(), null));

        String appName = "a".repeat(ApplicationValidator.MAX_LENGTH + 1);
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication(appName, serviceType.getCode(), null));
    }

    @Test
    void newApplication_null_appName() {

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceTypeByName("TOMCAT")).thenReturn(ServiceType.STAND_ALONE);
        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication(null, -1, "TOMCAT"));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("", -1, "TOMCAT"));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication(" ", -1, "TOMCAT"));
    }

    @Test
    void newApplication_blank_typeName() {
        ServiceType serviceType = ServiceType.STAND_ALONE;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);
        ApplicationValidator validator = new ApplicationValidator(registry);

        // a blank serviceTypeName is treated as absent, so the serviceTypeCode is used
        Assertions.assertEquals(new Application("appName", serviceType),
                validator.newApplication("appName", serviceType.getCode(), " "));

        // without a serviceTypeCode a blank serviceTypeName is rejected
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("appName", -1, " "));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> validator.newApplication("appName", " "));
    }
}