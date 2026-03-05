package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import jakarta.annotation.Nullable;

import java.util.Objects;

public class SimpleApplicationMap implements ApplicationMap {
    private final NodeList nodes;
    private final LinkList links;

    @Nullable
    private final Range range;

    public SimpleApplicationMap(NodeList nodes, LinkList links) {
        this.nodes = Objects.requireNonNull(nodes, "nodes");
        this.links = Objects.requireNonNull(links, "links");
        this.range = null;
    }

    public SimpleApplicationMap(NodeList nodes, LinkList links, Range range) {
        this.nodes = Objects.requireNonNull(nodes, "nodes");
        this.links = Objects.requireNonNull(links, "links");
        this.range = Objects.requireNonNull(range, "range");
    }

    @Override
    public NodeList getNodes() {
        return nodes;
    }

    @Override
    public LinkList getLinks() {
        return links;
    }

    @Nullable
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
