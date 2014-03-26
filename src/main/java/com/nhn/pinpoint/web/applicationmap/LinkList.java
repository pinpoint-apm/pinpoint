package com.nhn.pinpoint.web.applicationmap;

import com.nhn.pinpoint.web.vo.Application;
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

    public List<Link> findToLink(Application toApplication) {
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        List<Link> findList = new ArrayList();
        for (Link link : linkMap.values()) {
            Node toNode = link.getTo();
            // destnation이 자신을 가리킨다면 데이터를 머지함.
            if (toNode.getApplication().equals(toApplication)) {
                findList.add(link);
            }
        }
        return findList;
    }

    public List<Link> findFromLink(Application fromApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        List<Link> findList = new ArrayList();
        for (Link link : linkMap.values()) {
            Node fromNode = link.getFrom();
            // destnation이 자신을 가리킨다면 데이터를 머지함.
            if (fromNode.getApplication().equals(fromApplication)) {
                findList.add(link);
            }
        }
        return findList;
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
