package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.server.uid.Service;
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
        if (serviceTypeCode == undefined) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeCode" + serviceTypeCode);
        }
        ServiceType serviceType = registry.findServiceType(serviceTypeCode);
        validateName(applicationName, serviceType);
        return new Application(applicationName, serviceType);
    }

    public Application newApplication(String applicationName, String serviceTypeName) {
        if (!StringUtils.hasText(serviceTypeName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeName " + serviceTypeName);
        }
        ServiceType serviceType = registry.findServiceTypeByName(serviceTypeName);
        if (serviceType.getCode() == undefined) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeName " + serviceTypeName);
        }
        validateName(applicationName, serviceType);
        return new Application(applicationName, serviceType);
    }

    public Application newApplication(String applicationName, int serviceTypeCode, String serviceTypeName) {
        return newApplication(Service.DEFAULT, applicationName, serviceTypeCode, serviceTypeName);
    }

    public Application newApplication(Service service, String applicationName, int serviceTypeCode, String serviceTypeName) {
        Objects.requireNonNull(service, "service");
        if (!StringUtils.hasText(applicationName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName " + applicationName);
        }

        ServiceType serviceType = null;
        if (serviceTypeCode != undefined) {
            serviceType = registry.findServiceType(serviceTypeCode);
        }
        if (StringUtils.hasText(serviceTypeName)) {
            serviceType = registry.findServiceTypeByName(serviceTypeName);
        }
        if (serviceType != null && serviceType.getCode() != undefined) {
            validateName(applicationName, serviceType);
            return new Application(service, applicationName, serviceType);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceTypeCode or serviceTypeName");
    }

    /**
     * A WAS applicationName is registered by an agent, which already enforces the id character pattern,
     * so the same pattern is required here. Any other node is named by a remote call address such as
     * a host:port or URL, so it is checked against the URL character set instead.
     */
    public void validateName(String applicationName, ServiceType serviceType) {
        Objects.requireNonNull(serviceType, "serviceType");
        if (!StringUtils.hasText(applicationName) || !IdValidateUtils.checkLength(applicationName, MAX_LENGTH)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName " + applicationName);
        }
        if (serviceType.isWas()) {
            if (!IdValidateUtils.checkPattern(applicationName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName(node) " + applicationName);
            }
        } else {
            if (!IdValidateUtils.checkUrlPattern(applicationName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName(url) " + applicationName);
            }
        }
    }
}
