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
    // a USER node is named by UserNodeUtils.newUserNodeName: <applicationName>_<serviceTypeName>
    // serviceType names are unbounded constants; 64 is double the longest one in the repo (32 chars)
    static final int USER_NODE_MAX_LENGTH = MAX_LENGTH + 1 + 64;
    // an address list such as kafka bootstrap servers grows with the number of brokers
    static final int LIST_MAX_LENGTH = 4096;

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

    public void validateName(String applicationName, ServiceType serviceType) {
        Objects.requireNonNull(serviceType, "serviceType");
        if (!StringUtils.hasText(applicationName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName " + applicationName);
        }
        if (serviceType.isUser()) {
            validateUserName(applicationName);
        } else if (serviceType.isWas()) {
            validateWasName(applicationName);
        } else {
            validateRemoteName(applicationName);
        }
    }

    // generated name: <applicationName>_<serviceTypeName>
    private void validateUserName(String applicationName) {
        validateLength(applicationName, USER_NODE_MAX_LENGTH);
        if (!IdValidateUtils.checkUrlPattern(applicationName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName(user) " + applicationName);
        }
    }

    // registered by an agent, which already enforces the id pattern
    private void validateWasName(String applicationName) {
        validateLength(applicationName, MAX_LENGTH);
        if (!IdValidateUtils.checkIdPattern(applicationName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName(node) " + applicationName);
        }
    }

    // named by a remote call address: host:port, URL, or a list such as kafka bootstrap servers [host1:9092, host2:9092]
    private void validateRemoteName(String applicationName) {
        if (IdValidateUtils.isBracketed(applicationName)) {
            validateLength(applicationName, LIST_MAX_LENGTH);
            if (!IdValidateUtils.checkListPattern(applicationName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName(list) " + applicationName);
            }
        } else {
            validateLength(applicationName, MAX_LENGTH);
            if (!IdValidateUtils.checkUrlPattern(applicationName)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName(url) " + applicationName);
            }
        }
    }

    private void validateLength(String applicationName, int maxLength) {
        if (!IdValidateUtils.checkLength(applicationName, maxLength)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid applicationName " + applicationName);
        }
    }
}
