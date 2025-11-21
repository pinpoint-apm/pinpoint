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

package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeHistogramSummaryView {

    private final NodeHistogramSummary nodeHistogramSummary;
    private final NodeHistogram nodeHistogram;
    private final TimeWindow timeWindow;
    private final TimeHistogramView timeHistogramView;

    private final ServerGroupListView serverGroupListView;


    public NodeHistogramSummaryView(NodeHistogramSummary nodeHistogramSummary,
                                    TimeWindow timeWindow,
                                    ServerGroupListView serverGroupListView,
                                    TimeHistogramView timeHistogramView) {
        this.nodeHistogramSummary = Objects.requireNonNull(nodeHistogramSummary, "nodeHistogramSummary");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.nodeHistogram = nodeHistogramSummary.getNodeHistogram();

        this.serverGroupListView = Objects.requireNonNull(serverGroupListView, "serverGroupListView");

        this.timeHistogramView = Objects.requireNonNull(timeHistogramView, "timeHistogramView");
    }

    public long getCurrentServerTime() {
        return System.currentTimeMillis();
    }

    public ServerGroupListView getServerList() {
        return serverGroupListView;
    }

    public long getInstanceCount() {
        return serverGroupListView.getServerGroupList().getInstanceCount();
    }

    public long getInstanceErrorCount() {
        final Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        if (agentHistogramMap.isEmpty()) {
            return 0;
        }

        // do not cache
        return agentHistogramMap.values().stream()
                .filter(agentHistogram -> agentHistogram.getTotalErrorCount() > 0)
                .count();
    }

    public ResponseTimeStatics getResponseStatistics() {
        return ResponseTimeStatics.fromHistogram(nodeHistogram.getApplicationHistogram());
    }

    public Histogram getHistogram() {
        return nodeHistogram.getApplicationHistogram();
    }

    public List<Long> getTimestamp() {
        return timeWindow.getTimeseriesWindows();
    }

    public Map<String, Histogram> getAgentHistogram() {
        return nodeHistogram.getAgentHistogramMap();
    }

    public Map<String, ResponseTimeStatics> getAgentResponseStatistics() {
        return nodeHistogram.getAgentResponseStatisticsMap();
    }


    public List<TimeHistogramViewModel> getTimeSeriesHistogram() {
        ApplicationTimeHistogram applicationTimeHistogram = nodeHistogramSummary.getApplicationTimeHistogram();
        if (applicationTimeHistogram == null) {
            return List.of();
        }
        return timeHistogramView.build(applicationTimeHistogram);
    }

    public JsonFields<AgentNameView, List<TimeHistogramViewModel>> getAgentTimeSeriesHistogram() {
        return timeHistogramView.build(nodeHistogram.getAgentTimeHistogram());
    }
}

