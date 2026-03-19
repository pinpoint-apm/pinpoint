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
        Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");

        int lastDelimiter = serviceNodeName.lastIndexOf(NodeName.NODE_DELIMITER_CHAR);
        if (lastDelimiter == -1) {
            throw new IllegalArgumentException("ServiceType not found :" + serviceNodeName);
        }
        String serviceTypeName = serviceNodeName.substring(lastDelimiter + 1);


        String serviceNameAndApp = serviceNodeName.substring(0, lastDelimiter);

        String serviceName;
        int firstDelimiter = findServiceDelimiter(serviceNameAndApp, lastDelimiter);
//        int firstDelimiter = serviceNameAndApp.indexOf(NodeName.NODE_DELIMITER_CHAR);
        if (firstDelimiter == -1) {
            serviceName = Service.DEFAULT.getServiceName();
        } else {
            serviceName = serviceNodeName.substring(0, firstDelimiter);
        }

        String escapedApplicationName = serviceNameAndApp.substring(firstDelimiter + 1);
        String applicationName = ServiceNodeName.unescapeApplicationName(escapedApplicationName);

        ServiceType serviceType = serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
        if (serviceType == null) {
            throw new IllegalArgumentException("Unknown serviceType :" + serviceType);
        }

        return new ServiceNodeName(serviceName, applicationName, serviceType);
    }

    private static int findServiceDelimiter(String serviceNameAndApp, int lastDelimiter) {
        for (int i = 0; i < lastDelimiter; i++) {
            if (serviceNameAndApp.charAt(i) == NodeName.NODE_DELIMITER_CHAR) {
                if (i == 0 || serviceNameAndApp.charAt(i - 1) != '\\') {
                    return i;
                }
            }
        }
        return -1;
    }

}
