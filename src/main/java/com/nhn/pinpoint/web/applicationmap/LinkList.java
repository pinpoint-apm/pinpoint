package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.service.NodeId;

import java.util.*;

/**
 * @author emeroad
 */
public class LinkList {
    private final Map<NodeId, Link> linkMap = new HashMap<NodeId, Link>();

    public List<Link> getLinks() {
        return new ArrayList<Link>(this.linkMap.values());
    }

    public void buildLink(List<Link> relationList) {
        for (Link link : relationList) {
            buildLink(link);
        }
    }

    public void buildLink(Link sourceLink) {
        if (sourceLink == null) {
            throw new NullPointerException("sourceLink must not be null");
        }

        final NodeId id = sourceLink.getId();
        final Link find = this.linkMap.get(id);
        if (find != null) {
            find.add(sourceLink);
        } else {
            Link copy = new Link(sourceLink);
            this.linkMap.put(id, copy);
        }
    }
}
