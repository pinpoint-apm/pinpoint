package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.common.server.util.json.JsonFields;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApplicationTimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.view.id.AgentNameView;
import com.navercorp.pinpoint.web.vo.ResponseTimeStatics;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NodeHistogramSummaryView {

    private final NodeHistogramSummary nodeHistogramSummary;
    private final NodeHistogram nodeHistogram;
    private final TimeHistogramFormat format;

    private final ServerGroupList serverGroupList;

    public NodeHistogramSummaryView(NodeHistogramSummary nodeHistogramSummary,
                                    ServerGroupList serverGroupList,
                                    TimeHistogramFormat format) {
        this.nodeHistogramSummary = Objects.requireNonNull(nodeHistogramSummary, "nodeHistogramSummary");
        this.nodeHistogram = nodeHistogramSummary.getNodeHistogram();

        this.serverGroupList = Objects.requireNonNull(serverGroupList, "serverGroupList");

        this.format = Objects.requireNonNull(format, "format");
    }

    public long getCurrentServerTime() {
        return System.currentTimeMillis();
    }

    public ServerGroupList getServerList() {
        return serverGroupList;
    }

    public ResponseTimeStatics getResponseStatistics() {
        return ResponseTimeStatics.fromHistogram(nodeHistogram.getApplicationHistogram());
    }

    public Histogram getHistogram() {
        return nodeHistogram.getApplicationHistogram();
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
        return applicationTimeHistogram.createViewModel(format);
    }

    public JsonFields<AgentNameView, List<TimeHistogramViewModel>> getAgentTimeSeriesHistogram() {
        return nodeHistogram.getAgentTimeHistogram().createViewModel(format);
    }
}

