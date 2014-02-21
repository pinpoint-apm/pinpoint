package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.vo.LinkKey;

import java.util.*;

/**
 * @author emeroad
 */
public class LinkList {
    private final Map<LinkKey, Link> linkMap = new HashMap<LinkKey, Link>();

    public Collection<Link> getLinks() {
        return this.linkMap.values();
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

        final LinkKey id = sourceLink.getLinkKey();
        final Link find = this.linkMap.get(id);
        if (find != null) {
            find.addLink(sourceLink);
        } else {
            Link copy = new Link(sourceLink);
            this.linkMap.put(id, copy);
        }
    }
}
