package com.navercorp.pinpoint.web.applicationmap.link;

import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeName;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LinkName {
    private static final String LINK_DELIMITER = "~";

    private final NodeName from;
    private final NodeName to;

    public static LinkName of(NodeName from, NodeName to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return new LinkName(from, to);
    }

    public LinkName(NodeName from, NodeName to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    @JsonValue
    public String getName() {
        return from + LINK_DELIMITER + to;
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
