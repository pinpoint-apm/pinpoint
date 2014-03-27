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
            addLink(link);
        }
    }

    public List<Link> findToLink(Application toApplication) {
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        List<Link> findList = new ArrayList<Link>();
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
        List<Link> findList = new ArrayList<Link>();
        for (Link link : linkMap.values()) {
            Node fromNode = link.getFrom();
            // destnation이 자신을 가리킨다면 데이터를 머지함.
            if (fromNode.getApplication().equals(fromApplication)) {
                findList.add(link);
            }
        }
        return findList;
    }

    public void addLink(Link newLink) {
        if (newLink == null) {
            throw new NullPointerException("newLink must not be null");
        }

        final LinkKey linkId = newLink.getLinkKey();
        final Link find = this.linkMap.get(linkId);
        if (find == null) {
            this.linkMap.put(linkId, newLink);
        }

    }
}
