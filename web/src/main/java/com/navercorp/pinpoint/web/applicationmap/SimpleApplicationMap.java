package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;

import java.util.Collection;
import java.util.Objects;

public class SimpleApplicationMap implements ApplicationMap {
    private final Collection<Node> nodes;
    private final Collection<Link> links;

    public SimpleApplicationMap(Collection<Node> nodes, Collection<Link> links) {
        this.nodes = Objects.requireNonNull(nodes, "nodes");
        this.links = Objects.requireNonNull(links, "links");
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes;
    }

    @Override
    public Collection<Link> getLinks() {
        return links;
    }
}
