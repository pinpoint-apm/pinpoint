package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.nodes.ServiceNodeName;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServiceLinkName {
    public static final String LINK_DELIMITER = "~";

    private final Application from;
    private final Application to;

    public static ServiceLinkName of(Application from, Application to) {
        return new ServiceLinkName(from, to);
    }

    public ServiceLinkName(Application from, Application to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    public String getName() {
        return toNodeName(from) + LINK_DELIMITER + toNodeName(to);
    }

    private String toNodeName(Application node) {
        return ServiceNodeName.toServiceNodeName(node.getService().getServiceName(), node.getApplicationName(), node.getServiceType());
    }

    public String getLinkKey() {
        return toNodeKey(from) + LINK_DELIMITER + toNodeKey(to);
    }

    private String toNodeKey(Application node) {
        return ServiceNodeName.toServiceNodeKey(node.getService().getServiceName(), node.getApplicationName(), node.getServiceType());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceLinkName linkName = (ServiceLinkName) o;

        if (!from.equals(linkName.from)) return false;
        return to.equals(linkName.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
