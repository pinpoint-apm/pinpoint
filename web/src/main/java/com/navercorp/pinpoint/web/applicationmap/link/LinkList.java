/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class LinkList {

    private final Map<LinkKey, Link> linkMap = new HashMap<>();

    public Collection<Link> getLinkList() {
        return this.linkMap.values();
    }

    public void addLinkList(LinkList linkList) {
        Objects.requireNonNull(linkList, "linkList");

        for (Link link : linkList.getLinkList()) {
            addLink(link);
        }
    }

    /**
     * find all in links of toApplication
     * @param toApplication
     * @return
     */
    public List<Link> findToLink(Application toApplication) {
        Objects.requireNonNull(toApplication, "toApplication");

        List<Link> findList = new ArrayList<>();
        for (Link link : linkMap.values()) {
            Node toNode = link.getTo();
            // find all the out link of toApplication/destination
            if (toNode.getApplication().equals(toApplication)) {
                findList.add(link);
            }
        }
        return findList;
    }

    /**
     * find all out links of fromApplication
     * @param fromApplication
     * @return
     */
    public List<Link> findFromLink(Application fromApplication) {
        Objects.requireNonNull(fromApplication, "fromApplication");

        List<Link> findList = new ArrayList<>();
        for (Link link : linkMap.values()) {
            Node fromNode = link.getFrom();

            if (fromNode.getApplication().equals(fromApplication)) {
                findList.add(link);
            }
        }
        return findList;
    }

    public boolean addLink(Link link) {
        Objects.requireNonNull(link, "link");

        final LinkKey linkId = link.getLinkKey();
        final Link find = this.linkMap.get(linkId);
        if (find != null) {
            return false;
        }
        return this.linkMap.put(linkId, link) == null;
    }

    public Link getLink(LinkKey linkKey) {
        return linkMap.get(linkKey);
    }

    public boolean containsNode(Link link) {
        Objects.requireNonNull(link, "link");

        return linkMap.containsKey(link.getLinkKey());
    }

    public int size() {
        return this.linkMap.size();
    }
}
