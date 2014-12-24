/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;

import java.util.*;

/**
 * @author emeroad
 */
public class LinkList {

    private final Map<LinkKey, Link> linkMap = new HashMap<LinkKey, Link>();

    public Collection<Link> getLinkList() {
        return this.linkMap.values();
    }

    public void addLinkList(LinkList linkList) {
        if (linkList == null) {
            throw new NullPointerException("linkList must not be null");
        }

        for (Link link : linkList.getLinkList()) {
            addLink(link);
        }
    }

    /**
     * toApplication을 가리키는(호출당하는) 모든 link를 찾음.
     * @param toApplication
     * @return
     */
    public List<Link> findToLink(Application toApplication) {
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }

        List<Link> findList = new ArrayList<Link>();
        for (Link link : linkMap.values()) {
            Node toNode = link.getTo();
            // destnation이 자신을 가리키는 모든 Link를 찾음.
            if (toNode.getApplication().equals(toApplication)) {
                findList.add(link);
            }
        }
        return findList;
    }

    /**
     * fromApplication 에서 나가는(호출하는) link를 모두 찾음.
     * @param fromApplication
     * @return
     */
    public List<Link> findFromLink(Application fromApplication) {
        if (fromApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }

        List<Link> findList = new ArrayList<Link>();
        for (Link link : linkMap.values()) {
            Node fromNode = link.getFrom();

            if (fromNode.getApplication().equals(fromApplication)) {
                findList.add(link);
            }
        }
        return findList;
    }

    public boolean addLink(Link link) {
        if (link == null) {
            throw new NullPointerException("link must not be null");
        }

        final LinkKey linkId = link.getLinkKey();
        final Link find = this.linkMap.get(linkId);
        if (find != null) {
            return false;
        }
        return this.linkMap.put(linkId, link) == null;
    }

    public boolean containsNode(Link link) {
        if (link == null) {
            throw new NullPointerException("linkKey must not be null");
        }
        return linkMap.containsKey(link.getLinkKey());
    }

    public int size() {
        return this.linkMap.size();
    }
}
