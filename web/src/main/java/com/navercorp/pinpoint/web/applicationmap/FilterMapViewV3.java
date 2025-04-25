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
import com.navercorp.pinpoint.web.applicationmap.view.FilteredHistogramView;
import com.navercorp.pinpoint.web.applicationmap.view.ScatterDataMapView;

import java.util.Objects;

/**
 * @author emeroad
 */
public class FilterMapViewV3 {
    private final ApplicationMapViewV3 applicationMapView;

    private final ScatterDataMapView scatterDataMapView;

    private final FilteredHistogramView filteredHistogramView;

    private final long lastFetchedTimestamp;

    public FilterMapViewV3(ApplicationMapViewV3 applicationMapView,
                           ScatterDataMapView scatterDataMapView,
                           FilteredHistogramView filteredHistogramView,
                           long lastFetchedTimestamp) {
        this.applicationMapView = Objects.requireNonNull(applicationMapView, "applicationMapView");
        this.scatterDataMapView = Objects.requireNonNull(scatterDataMapView, "scatterDataMapView");
        this.filteredHistogramView = filteredHistogramView;
        this.lastFetchedTimestamp = lastFetchedTimestamp;
    }

    public ApplicationMapViewV3 getApplicationMapData() {
        return applicationMapView;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public ScatterDataMapView getApplicationScatterData() {
        return scatterDataMapView;
    }

    public long getLastFetchedTimestamp() {
        return lastFetchedTimestamp;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public FilteredHistogramView getResponseTimeHistogram() {
        return filteredHistogramView;
    }
}
