package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.Application;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

public class ApplicationValidator {
    static final int UNDEFINED = ServiceType.UNDEFINED.getCode();
    static final int MAX_LENGTH = PinpointConstants.AGENT_NAME_MAX_LEN;

    private final ServiceTypeRegistryService registry;
    private final int undefined;

    public ApplicationValidator(ServiceTypeRegistryService registry) {
        this(registry, UNDEFINED);
    }

    public ApplicationValidator(ServiceTypeRegistryService registry, int undefined) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.undefined = undefined;
    }

    public Application newApplication(String applicationName, int serviceTypeCode) {
        validateName(applicationName);
        if (serviceTypeCode == undefined) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeCode" + serviceTypeCode);
        }
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        return new Application(applicationName, serviceType);
    }

    public Application newApplication(String applicationName, String serviceTypeName) {
        validateName(applicationName);
        if (!StringUtils.hasLength(serviceTypeName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeName " + serviceTypeName);
        }
        ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        if (serviceType.getCode() == undefined) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeName " + serviceTypeName);
        }
        return new Application(applicationName, serviceType);
    }

    public Application newApplication(String applicationName, int serviceTypeCode, String serviceTypeName) {
        validateName(applicationName);

        ServiceType serviceType = null;
        if (serviceTypeCode != undefined) {
            serviceType = registry.findServiceType(serviceTypeCode);
        }
        if (StringUtils.hasLength(serviceTypeName)) {
            serviceType = registry.findServiceTypeByName(serviceTypeName);
        }
        if (serviceType != null && serviceType.getCode() != undefined) {
            return new Application(applicationName, serviceType);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeCode or serviceTypeName");
    }

    private void validateName(String applicationName) {
        if (!IdValidateUtils.validateId(applicationName, MAX_LENGTH)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName " + applicationName);
        }
    }
}
