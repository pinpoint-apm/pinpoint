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
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.view.histogram.ServerHistogramView;
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
    public List<ServerHistogramView> getNodeServerHistogramData() {
        final List<ServerHistogramView> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            result.add(new ServerHistogramView(node.getNodeName(), node.getNodeHistogram(), node.getServerGroupList()));
        }
        return result;
    }

    @JsonIgnore
    public List<HistogramView> getNodeHistogramData() {
        final List<HistogramView> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            result.add(new HistogramView(node.getNodeName(), node.getNodeHistogram()));
        }
        return result;
    }

    @JsonIgnore
    public List<HistogramView> getLinkHistogramData() {
        final List<HistogramView> result = new ArrayList<>();
        for (Link link : applicationMap.getLinks()) {
            result.add(new HistogramView(link.getLinkName(), link.getHistogram(), link.getLinkApplicationTimeHistogram()));
        }
        return result;
    }

    @JsonIgnore
    public Map<Application, ScatterData> getApplicationScatterDataMap() {
        return applicationScatterDataMap;
    }

}
