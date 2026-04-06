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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.MapView;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ServiceMapView implements MapView {
    private final ApplicationMap applicationMap;
    private final TimeWindow timeWindow;
    private final List<NodeViewEntry> nodes;
    private final List<LinkViewEntry> links;

    ServiceMapView(ApplicationMap applicationMap, TimeWindow timeWindow,
                   List<NodeViewEntry> nodes, List<LinkViewEntry> links) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.nodes = Objects.requireNonNull(nodes, "nodes");
        this.links = Objects.requireNonNull(links, "links");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Range getRange() {
        return applicationMap.getRange();
    }

    @JsonProperty("nodeDataArray")
    public Iterator<NodeViewEntry> getNodes() {
        return nodes.iterator();
    }

    @JsonProperty("linkDataArray")
    public Iterator<LinkViewEntry> getLinks() {
        return links.iterator();
    }

    @JsonProperty("timestamp")
    public List<Long> getTimestamp() {
        return timeWindow.getTimeseriesWindows();
    }
}
