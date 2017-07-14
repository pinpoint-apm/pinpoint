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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.vo.Range;

import java.util.Collection;

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
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (nodeList == null) {
            throw new NullPointerException("nodeList must not be null");
        }
        if (linkList == null) {
            throw new NullPointerException("linkList must not be null");
        }
        this.range = range;
        this.nodeList = nodeList;
        this.linkList = linkList;
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
