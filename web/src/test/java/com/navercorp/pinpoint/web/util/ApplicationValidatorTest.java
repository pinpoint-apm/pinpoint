package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.uid.Service;
import com.navercorp.pinpoint.common.server.util.UserNodeUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationValidatorTest {

    private static Application newApplication(ApplicationValidator validator, String applicationName, int serviceTypeCode, String serviceTypeName) {
        return validator.newApplication(Service.DEFAULT, applicationName, serviceTypeCode, serviceTypeName);
    }

    @Test
    void newApplication_typeCode() {
        ServiceType serviceType = ServiceType.STAND_ALONE;
        Application application = new Application("appName", serviceType);

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Application newApplication = newApplication(validator, "appName", serviceType.getCode(), null);
        Assertions.assertEquals(application, newApplication);
    }

    @Test
    void newApplication_remoteAddressName() {
        // a non-WAS node is named by its remote call address, which is not a valid agent/application id
        ServiceType serviceType = ServiceType.UNKNOWN_DB;
        String remoteAddress = "pinot-broker.pinot.svc.cluster.local:8099";
        String url = "http://user:pw@host.example.com:8080/path/to/resource?query=1&x=y#frag";

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertEquals(new Application(remoteAddress, serviceType),
                newApplication(validator, remoteAddress, serviceType.getCode(), null));
        Assertions.assertEquals(new Application(url, serviceType),
                newApplication(validator, url, serviceType.getCode(), null));

        // but it must still be made of URL characters
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, " ", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "host name", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "host<name>", serviceType.getCode(), null));
    }

    @Test
    void newApplication_addressListName() {
        // kafka bootstrap servers are recorded as List.toString()
        ServiceType serviceType = ServiceType.UNKNOWN_DB;
        String addressList = "[broker-002.example.com:9093, broker-003.example.com:9093, broker-004.example.com:9093]";

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertEquals(new Application(addressList, serviceType),
                newApplication(validator, addressList, serviceType.getCode(), null));

        // a list may exceed MAX_LENGTH, up to LIST_MAX_LENGTH
        String element = "a".repeat(ApplicationValidator.MAX_LENGTH);
        String longList = "[" + element + ", " + element + "]";
        Assertions.assertEquals(new Application(longList, serviceType),
                newApplication(validator, longList, serviceType.getCode(), null));
        String tooLongList = "[" + "a".repeat(ApplicationValidator.LIST_MAX_LENGTH) + "]";
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, tooLongList, serviceType.getCode(), null));
        // a non-list name keeps MAX_LENGTH
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "a".repeat(ApplicationValidator.MAX_LENGTH + 1), serviceType.getCode(), null));

        // a bracketed ipv6 literal is a single element list
        Assertions.assertEquals(new Application("[::1]", serviceType),
                newApplication(validator, "[::1]", serviceType.getCode(), null));
        Assertions.assertEquals(new Application("[::1]:8080", serviceType),
                newApplication(validator, "[::1]:8080", serviceType.getCode(), null));

        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "[]", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "[broker-002.example.com:9093, broker 003]", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "broker-002.example.com:9093, broker-003.example.com:9093", serviceType.getCode(), null));
    }

    @Test
    void newApplication_was_rejectsAddressName() {
        // a WAS applicationName is an agent-registered id and must match the id pattern
        ServiceType serviceType = ServiceType.STAND_ALONE;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertEquals(new Application("app.name-1_2", serviceType),
                newApplication(validator, "app.name-1_2", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "host.example.com:8080", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "app name", serviceType.getCode(), null));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "[app1, app2]", serviceType.getCode(), null));
    }

    @Test
    void newApplication_typeName() {
        ServiceType serviceType = ServiceType.STAND_ALONE;
        Application application = new Application("appName", serviceType);

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceTypeByName(serviceType.getName())).thenReturn(serviceType);

        ApplicationValidator validator = new ApplicationValidator(registry);

        Application newApplication = newApplication(validator, "appName", -1, serviceType.getName());
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
                () -> newApplication(validator, "appName", serviceType.getCode(), null));
    }

    @Test
    void newApplication_invalid_appName() {
        ServiceType serviceType = ServiceType.UNKNOWN_DB;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);
        ApplicationValidator validator = new ApplicationValidator(registry);

        String maxName = "a".repeat(ApplicationValidator.MAX_LENGTH);
        Assertions.assertEquals(new Application(maxName, serviceType),
                newApplication(validator, maxName, serviceType.getCode(), null));

        String appName = "a".repeat(ApplicationValidator.MAX_LENGTH + 1);
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, appName, serviceType.getCode(), null));
    }

    @Test
    void newApplication_null_appName() {

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceTypeByName("TOMCAT")).thenReturn(ServiceType.STAND_ALONE);
        ApplicationValidator validator = new ApplicationValidator(registry);

        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, null, -1, "TOMCAT"));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "", -1, "TOMCAT"));
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, " ", -1, "TOMCAT"));
    }

    @Test
    void newApplication_blank_typeName() {
        ServiceType serviceType = ServiceType.STAND_ALONE;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(serviceType.getCode())).thenReturn(serviceType);
        ApplicationValidator validator = new ApplicationValidator(registry);

        // a blank serviceTypeName is treated as absent, so the serviceTypeCode is used
        Assertions.assertEquals(new Application("appName", serviceType),
                newApplication(validator, "appName", serviceType.getCode(), " "));

        // without a serviceTypeCode a blank serviceTypeName is rejected
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, "appName", -1, " "));
    }

    @Test
    void newApplication_userNode_allowsGeneratedSuffix() {
        ServiceType was = ServiceType.STAND_ALONE;
        ServiceType user = ServiceType.USER;

        ServiceTypeRegistryService registry = mock(ServiceTypeRegistryService.class);
        when(registry.findServiceType(was.getCode())).thenReturn(was);
        when(registry.findServiceType(user.getCode())).thenReturn(user);
        ApplicationValidator validator = new ApplicationValidator(registry);

        // a WAS name at the V3 limit, and the USER node the server map derives from it
        String maxWasName = "a".repeat(PinpointConstants.APPLICATION_NAME_MAX_LEN_V3);
        String userNodeName = UserNodeUtils.newUserNodeName(maxWasName, was);
        Assertions.assertTrue(userNodeName.length() > ApplicationValidator.MAX_LENGTH);

        Assertions.assertEquals(new Application(maxWasName, was),
                newApplication(validator, maxWasName, was.getCode(), null));
        Assertions.assertEquals(new Application(userNodeName, user),
                newApplication(validator, userNodeName, user.getCode(), null));

        // the allowance is for USER nodes only
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, userNodeName, was.getCode(), null));
        String tooLong = "a".repeat(ApplicationValidator.USER_NODE_MAX_LENGTH + 1);
        Assertions.assertThrows(ResponseStatusException.class,
                () -> newApplication(validator, tooLong, user.getCode(), null));
    }
}
