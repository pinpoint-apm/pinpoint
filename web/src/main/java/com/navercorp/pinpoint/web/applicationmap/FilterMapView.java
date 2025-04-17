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
import com.navercorp.pinpoint.web.applicationmap.view.FilteredHistogramView;
import com.navercorp.pinpoint.web.applicationmap.view.ScatterDataMapView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author emeroad
 */
public class FilterMapView {
    private final ApplicationMap applicationMap;
    private final Class<?> activeView;
    private final TimeHistogramFormat timeHistogramFormat;
    private final HyperLinkFactory hyperLinkFactory;
    private Long lastFetchedTimestamp;
    private Function<ApplicationMap, FilteredHistogramView> filteredHistogramView;

    private Map<Application, ScatterData> scatterDataMap;

    public FilterMapView(ApplicationMap applicationMap, Class<?> activeView, HyperLinkFactory hyperLinkFactory, TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.timeHistogramFormat = Objects.requireNonNull(timeHistogramFormat, "timeHistogramFormat");
    }

    public void setLastFetchedTimestamp(Long lastFetchedTimestamp) {
        this.lastFetchedTimestamp = lastFetchedTimestamp;
    }

    public ApplicationMapView getApplicationMapData() {
        return new ApplicationMapView(applicationMap, activeView, hyperLinkFactory, timeHistogramFormat);
    }

    public Long getLastFetchedTimestamp() {
        return lastFetchedTimestamp;
    }

    public void setFilteredHistogram(Function<ApplicationMap, FilteredHistogramView> filteredHistogramView) {
        this.filteredHistogramView = filteredHistogramView;
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
        if (filteredHistogramView != null) {
            return filteredHistogramView.apply(applicationMap);
        }
        return null;
    }

}
