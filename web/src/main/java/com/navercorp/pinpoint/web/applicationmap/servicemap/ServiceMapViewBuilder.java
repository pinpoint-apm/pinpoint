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

package com.navercorp.pinpoint.web.applicationmap.servicemap;

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.view.LinkRender;
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeRender;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ServiceMapViewBuilder {
    private final ApplicationMap applicationMap;
    private final TimeWindow timeWindow;
    private final NodeRender nodeRender;
    private final LinkRender linkRender;
    private final Set<String> keepServiceNames;

    public ServiceMapViewBuilder(ApplicationMap applicationMap, TimeWindow timeWindow,
                                 NodeRender nodeRender, LinkRender linkRender,
                                 Set<String> keepServiceNames) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.nodeRender = Objects.requireNonNull(nodeRender, "nodeRender");
        this.linkRender = Objects.requireNonNull(linkRender, "linkRender");
        this.keepServiceNames = Objects.requireNonNull(keepServiceNames, "keepServiceNames");
    }

    public ServiceMapView build() {
        List<NodeViewEntry> nodes = buildNodes();
        List<LinkViewEntry> links = buildLinks();
        return new ServiceMapView(applicationMap, timeWindow, nodes, links);
    }

    private List<NodeViewEntry> buildNodes() {
        Collection<Node> nodes = applicationMap.getNodes().getNodeList();

        Map<String, List<Node>> serviceGroups = new LinkedHashMap<>();
        List<NodeViewEntry> result = new ArrayList<>();

        for (Node node : nodes) {
            String serviceName = node.getApplication().getService().getServiceName();
            if (keepServiceNames.contains(serviceName)) {
                result.add(nodeRender.render(node));
            } else {
                serviceGroups.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(node);
            }
        }

        for (Map.Entry<String, List<Node>> entry : serviceGroups.entrySet()) {
            List<NodeView> childViews = new ArrayList<>();
            for (Node node : entry.getValue()) {
                childViews.add(nodeRender.render(node));
            }
            result.add(new ServiceGroupNodeView(entry.getKey(), childViews));
        }

        return result;
    }

    private List<LinkViewEntry> buildLinks() {
        Collection<Link> links = applicationMap.getLinks().getLinkList();

        Map<LinkKeyPair, List<LinkView>> linkGroups = new LinkedHashMap<>();
        List<LinkViewEntry> result = new ArrayList<>();

        for (Link link : links) {
            String fromService = link.getFrom().getApplication().getService().getServiceName();
            String toService = link.getTo().getApplication().getService().getServiceName();

            boolean fromUngrouped = keepServiceNames.contains(fromService);
            boolean toUngrouped = keepServiceNames.contains(toService);

            LinkView linkView = linkRender.render(link);

            if (fromUngrouped && toUngrouped) {
                result.add(linkView);
            } else {
                LinkNodeKey fromKey = toLinkNodeKey(link.getFrom(), fromUngrouped);
                LinkNodeKey toKey = toLinkNodeKey(link.getTo(), toUngrouped);

                LinkKeyPair linkKeyPair = new LinkKeyPair(fromKey, toKey);
                List<LinkView> group = linkGroups.computeIfAbsent(linkKeyPair, k -> new ArrayList<>());
                group.add(linkView);
            }
        }

        for (Map.Entry<LinkKeyPair, List<LinkView>> entry : linkGroups.entrySet()) {
            LinkKeyPair keyPair = entry.getKey();
            result.add(new ServiceGroupLinkView(keyPair.from(), keyPair.to(), entry.getValue()));
        }

        return result;
    }

    private static LinkNodeKey toLinkNodeKey(Node node, boolean ungrouped) {
        if (ungrouped) {
            return LinkNodeKey.ofNode(node.getServiceNodeName());
        }
        return LinkNodeKey.ofService(node.getApplication().getService());
    }

    record LinkKeyPair(LinkNodeKey from, LinkNodeKey to) {
    }
}
