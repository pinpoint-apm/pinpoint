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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;

import java.util.Objects;

/**
 * @author emeroad
 */
@JsonInclude(content = JsonInclude.Include.NON_NULL)
public class MapView {
    private final ApplicationMap applicationMap;
    private final MapViews activeView;
    private final HyperLinkFactory hyperLinkFactory;
    private final TimeHistogramFormat timeHistogramFormat;

    public MapView(ApplicationMap applicationMap, MapViews activeView, HyperLinkFactory hyperLinkFactory, final TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = applicationMap;
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.timeHistogramFormat = Objects.requireNonNull(timeHistogramFormat, "timeHistogramFormat");
    }


    @JsonProperty("applicationMapData")
    public ApplicationMapView getApplicationMap() {
        return new ApplicationMapView(this.applicationMap, activeView, hyperLinkFactory, timeHistogramFormat);
    }
}
