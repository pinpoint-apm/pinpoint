package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

public class AgentHistogramView {
    private final String agentId;
    private final HistogramView histogramView;

    public AgentHistogramView(String agentId, HistogramView histogramView) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.histogramView = Objects.requireNonNull(histogramView, "histogramView");
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    @JsonUnwrapped
    public HistogramView getHistogramView() {
        return histogramView;
    }
}
