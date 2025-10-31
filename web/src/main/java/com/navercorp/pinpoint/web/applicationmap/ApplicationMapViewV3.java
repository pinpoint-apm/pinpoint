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

import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.view.AgentHistogramNodeView;
import com.navercorp.pinpoint.web.applicationmap.view.AgentLinkView;
import com.navercorp.pinpoint.web.applicationmap.view.AgentTimeSeriesHistogramNodeView;
import com.navercorp.pinpoint.web.applicationmap.view.ServerListNodeView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;

import java.util.List;
import java.util.Objects;

public class ApplicationMapViewV3 extends ApplicationMapView {

    private final TimeWindow timeWindow;

    public ApplicationMapViewV3(ApplicationMap applicationMap, TimeWindow timeWindow,
                                ServerListNodeView serverListNodeView,
                                AgentHistogramNodeView agentHistogramNodeView,
                                AgentTimeSeriesHistogramNodeView agentTimeSeriesHistogramNodeView,
                                AgentLinkView agentLinkView,
                                HyperLinkFactory hyperLinkFactory) {
        super(applicationMap,
                serverListNodeView,
                agentHistogramNodeView,
                agentTimeSeriesHistogramNodeView,
                agentLinkView,
                hyperLinkFactory, TimeHistogramFormat.V3);
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");

    }

    public List<Long> getTimestamp() {
        return timeWindow.getTimeseriesWindows();
    }

}
