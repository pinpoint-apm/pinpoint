/*
 *  Copyright 2023 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeAgentHistogramList;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.view.histogram.ApplicationHistogramView;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ApplicationMapWithScatterDataV3 implements ApplicationMap {

    private final ApplicationMap applicationMap;
    private final Map<Application, ScatterData> applicationScatterDataMap;

    public ApplicationMapWithScatterDataV3(ApplicationMap applicationMap, Map<Application, ScatterData> applicationScatterDataMap) {
        this.applicationMap = applicationMap;
        this.applicationScatterDataMap = applicationScatterDataMap;
    }

    @Override
    public Collection<Node> getNodes() {
        return applicationMap.getNodes();
    }

    @Override
    public Collection<Link> getLinks() {
        return applicationMap.getLinks();
    }

    @JsonValue
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

    @JsonIgnore
    public List<ApplicationHistogramView> getNodeHistogramData() {
        final List<ApplicationHistogramView> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            NodeHistogram nodeHistogram = node.getNodeHistogram();
            HistogramView histogramView = new HistogramView(null, nodeHistogram.getApplicationTimeHistogramList());

            result.add(new ApplicationHistogramView(node.getNodeName().getName(), histogramView));
        }
        return result;
    }

    @JsonIgnore
    public List<ApplicationHistogramView> getLinkHistogramData() {
        final List<ApplicationHistogramView> result = new ArrayList<>();
        for (Link link : applicationMap.getLinks()) {
            HistogramView histogramView = new HistogramView(null, link.getLinkApplicationTimeHistogramList());

            result.add(new ApplicationHistogramView(link.getLinkName().getName(), histogramView));
        }
        return result;
    }

    @JsonIgnore
    public List<NodeAgentHistogramList> getAgentHistogramData() {
        final List<NodeAgentHistogramList> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            if (node.getServerGroupList().getInstanceCount() > 0) {
                result.add(new NodeAgentHistogramList(node.getNodeName().getName(), node.getNodeHistogram(), node.getServerGroupList()));
            }
        }
        return result;
    }

    @JsonIgnore
    public Map<Application, ScatterData> getApplicationScatterDataMap() {
        return applicationScatterDataMap;
    }

}
