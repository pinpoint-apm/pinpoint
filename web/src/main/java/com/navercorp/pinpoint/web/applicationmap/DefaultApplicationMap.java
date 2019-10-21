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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.vo.Range;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Objects;

/**
 * Node map
 * 
 * @author netspider
 * @author emeroad
 */
public class DefaultApplicationMap implements ApplicationMap {

    private final NodeList nodeList;
    private final LinkList linkList;

    private final Range range;

//    private List<ApplicationScatterScanResult> applicationScatterScanResultList;

    public DefaultApplicationMap(Range range, NodeList nodeList, LinkList linkList) {
        this.range = Objects.requireNonNull(range, "range");
        this.nodeList = Objects.requireNonNull(nodeList, "nodeList");

        Objects.requireNonNull(linkList, "linkList");
        this.linkList = createNewLinkList(linkList);
    }

    private LinkList createNewLinkList(LinkList originalLinkList) {
        Collection<Link> linkList = originalLinkList.getLinkList();
        if (CollectionUtils.nullSafeSize(linkList) == 0) {
            return originalLinkList;
        }

        LinkList newLinkList = new LinkList();
        for (Link link : linkList) {
            if (link == null) {
                continue;
            }
            if (link.getHistogram().getTotalCount() == 0) {
                continue;

            }
            newLinkList.addLink(link);
        }

        return newLinkList;
    }

    @JsonProperty("nodeDataArray")
    public Collection<Node> getNodes() {
        return this.nodeList.getNodeList();
    }

    @JsonProperty("linkDataArray")
    public Collection<Link> getLinks() {
        return this.linkList.getLinkList();
    }

    public Range getRange() {
        return range;
    }
}
