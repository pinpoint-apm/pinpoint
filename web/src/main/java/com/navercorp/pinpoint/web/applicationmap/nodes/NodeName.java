package com.navercorp.pinpoint.web.applicationmap.nodes;

import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class NodeName {
    public static final String NODE_DELIMITER = "^";

    private final String name;
    private final ServiceType serviceType;

    public static NodeName of(Application application) {
        Objects.requireNonNull(application, "application");
        return new NodeName(application.name(), application.serviceType());
    }

    public NodeName(String name, ServiceType serviceType) {
        this.name = Objects.requireNonNull(name, "name");
        this.serviceType = Objects.requireNonNull(serviceType, "serviceType");
    }

    @JsonValue
    public String getName() {
        return toNodeName(name, serviceType);
    }

    public static String toNodeName(String name, ServiceType serviceType) {
        return name + NODE_DELIMITER + serviceType.getDesc();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeName nodeName = (NodeName) o;

        if (!name.equals(nodeName.name)) return false;
        return serviceType.equals(nodeName.serviceType);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + serviceType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
