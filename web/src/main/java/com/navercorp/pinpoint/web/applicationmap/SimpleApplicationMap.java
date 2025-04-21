package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;

public class SimpleApplicationMap implements ApplicationMap {
    private final Collection<Node> nodes;
    private final Collection<Link> links;

    @Nullable
    private final Range range;

    public SimpleApplicationMap(Collection<Node> nodes, Collection<Link> links) {
        this.nodes = Objects.requireNonNull(nodes, "nodes");
        this.links = Objects.requireNonNull(links, "links");
        this.range = null;
    }

    public SimpleApplicationMap(Collection<Node> nodes, Collection<Link> links, Range range) {
        this.nodes = Objects.requireNonNull(nodes, "nodes");
        this.links = Objects.requireNonNull(links, "links");
        this.range = Objects.requireNonNull(range, "range");
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes;
    }

    @Override
    public Collection<Link> getLinks() {
        return links;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public String toString() {
        return "SimpleApplicationMap{" +
                "nodes=" + nodes.size() +
                ", links=" + links.size() +
                "}";
    }
}
