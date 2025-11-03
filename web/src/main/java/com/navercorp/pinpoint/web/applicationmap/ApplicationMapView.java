/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterators;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.view.LinkRender;
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeRender;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class ApplicationMapView implements MapView {
    private final ApplicationMap applicationMap;

    private final NodeRender nodeRender;
    private final LinkRender linkRender;


    public ApplicationMapView(ApplicationMap applicationMap,
                              NodeRender nodeRender,
                              LinkRender linkRender) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.nodeRender = Objects.requireNonNull(nodeRender, "nodeRender");
        this.linkRender = Objects.requireNonNull(linkRender, "linkRender");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Range getRange() {
        return applicationMap.getRange();
    }

    @JsonProperty("nodeDataArray")
    public Iterator<NodeView> getNodes() {
        Collection<Node> nodes = applicationMap.getNodes();

        return Iterators.transform(nodes.iterator(), nodeRender::render);
    }

    @JsonProperty("linkDataArray")
    public Iterator<LinkView> getLinks() {
        Collection<Link> links = applicationMap.getLinks();

        return Iterators.transform(links.iterator(), linkRender::render);
    }

}
