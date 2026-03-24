package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.vo.Service;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServiceNodeNameParser {

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public ServiceNodeNameParser(ServiceTypeRegistryService serviceTypeRegistryService) {
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    public ServiceNodeName parse(String serviceNodeName) {
        Objects.requireNonNull(serviceNodeName, "serviceNodeName");

        int lastDelimiter = serviceNodeName.lastIndexOf(NodeName.NODE_DELIMITER_CHAR);
        if (lastDelimiter == -1) {
            throw new IllegalArgumentException("ServiceType not found :" + serviceNodeName);
        }
        String serviceTypeName = serviceNodeName.substring(lastDelimiter + 1);


        String serviceNameAndApp = serviceNodeName.substring(0, lastDelimiter);

        String serviceName;
        String escapedApplicationName;
        final int firstDelimiter = findServiceDelimiter(serviceNameAndApp, lastDelimiter);
        if (firstDelimiter == -1) {
            serviceName = Service.DEFAULT.getServiceName();
            escapedApplicationName = serviceNameAndApp;
        } else {
            serviceName = serviceNodeName.substring(0, firstDelimiter);
            escapedApplicationName = serviceNameAndApp.substring(firstDelimiter + 1);
        }
        String applicationName = ApplicationNameEscaper.unescape(escapedApplicationName);

        ServiceType serviceType = serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
        if (serviceType == null) {
            throw new IllegalArgumentException("Unknown serviceType :" + serviceTypeName);
        }

        return new ServiceNodeName(serviceName, applicationName, serviceType);
    }

    static int findServiceDelimiter(String serviceNameAndApp, int lastDelimiter) {
        for (int i = 0; i < lastDelimiter; i++) {
            if (serviceNameAndApp.charAt(i) == NodeName.NODE_DELIMITER_CHAR) {
                int backslashCount = 0;
                for (int j = i - 1; j >= 0 && serviceNameAndApp.charAt(j) == '\\'; j--) {
                    backslashCount++;
                }
                if (backslashCount % 2 == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

}
