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
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;

import java.util.Objects;

/**
 * @author emeroad
 */
@JsonInclude(content = JsonInclude.Include.NON_NULL)
public class MapViewV3 {
    private final ApplicationMap applicationMap;
    private final TimeWindow timeWindow;
    private final MapViews activeView;
    private final HyperLinkFactory hyperLinkFactory;


    public MapViewV3(ApplicationMap applicationMap, TimeWindow timeWindow, MapViews activeView, HyperLinkFactory hyperLinkFactory) {
        this.applicationMap = applicationMap;
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @JsonProperty("applicationMapData")
    public ApplicationMapViewV3 getApplicationMap() {
        return new ApplicationMapViewV3(this.applicationMap, timeWindow, activeView, hyperLinkFactory);
    }
}
