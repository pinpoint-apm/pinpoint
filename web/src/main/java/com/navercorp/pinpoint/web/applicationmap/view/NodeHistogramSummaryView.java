package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
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
    private final TimeHistogramFormat format;

    private final ServerGroupListView serverGroupListView;


    public NodeHistogramSummaryView(NodeHistogramSummary nodeHistogramSummary,
                                    TimeWindow timeWindow,
                                    ServerGroupListView serverGroupListView,
                                    TimeHistogramFormat format) {
        this.nodeHistogramSummary = Objects.requireNonNull(nodeHistogramSummary, "nodeHistogramSummary");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.nodeHistogram = nodeHistogramSummary.getNodeHistogram();

        this.serverGroupListView = Objects.requireNonNull(serverGroupListView, "serverGroupListView");

        this.format = Objects.requireNonNull(format, "format");
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
        TimeHistogramBuilder builder = new TimeHistogramBuilder(format);
        return builder.build(applicationTimeHistogram);
    }

    public JsonFields<AgentNameView, List<TimeHistogramViewModel>> getAgentTimeSeriesHistogram() {
        return nodeHistogram.getAgentTimeHistogram().createViewModel(format);
    }
}

