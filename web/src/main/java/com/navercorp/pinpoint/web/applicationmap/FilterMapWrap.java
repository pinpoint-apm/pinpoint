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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.view.FilteredHistogramView;
import com.navercorp.pinpoint.web.applicationmap.view.ScatterDataMapView;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Map;
import java.util.Objects;

/**
 * @author emeroad
 */
public class FilterMapWrap {
    private final ApplicationMap applicationMap;
    private Long lastFetchedTimestamp;
    private boolean filteredHistogram = false;

    private Map<Application, ScatterData> scatterDataMap;

    public FilterMapWrap(ApplicationMap applicationMap, TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");

        for (Node node : applicationMap.getNodes()) {
            node.setTimeHistogramFormat(timeHistogramFormat);
        }
        for (Link link : applicationMap.getLinks()) {
            link.setTimeHistogramFormat(timeHistogramFormat);
        }
    }

    public void setLastFetchedTimestamp(Long lastFetchedTimestamp) {
        this.lastFetchedTimestamp = lastFetchedTimestamp;
    }

    public ApplicationMap getApplicationMapData() {
        return applicationMap;
    }

    public Long getLastFetchedTimestamp() {
        return lastFetchedTimestamp;
    }

    public void setFilteredHistogram(boolean filteredHistogram) {
        this.filteredHistogram = filteredHistogram;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Map<String, ScatterDataMapView.ScatterDataView> getApplicationScatterData() {
        if (scatterDataMap == null) {
            return null;
        }
        return new ScatterDataMapView(scatterDataMap).getDataMap();
    }

    public void setScatterDataMap(Map<Application, ScatterData> scatterDataMap) {
        this.scatterDataMap = scatterDataMap;
    }

    @JsonUnwrapped
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public FilteredHistogramView getFilteredHistogramView() {
        if (filteredHistogram) {
            return new FilteredHistogramView(applicationMap);
        }
        return null;
    }

}
