package com.navercorp.pinpoint.web.applicationmap.link;

import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LinkName {
    public static final String LINK_DELIMITER = "~";

    private final Application from;
    private final Application to;

    public static LinkName of(Application from, Application to) {
        return new LinkName(from, to);
    }

    public LinkName(Application from, Application to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    @JsonValue
    public String getName() {
        return toNodeName(from) + LINK_DELIMITER + toNodeName(to);
    }

    private String toNodeName(Application node) {
        return NodeName.toNodeName(node.name(), node.serviceType());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkName linkName = (LinkName) o;

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
