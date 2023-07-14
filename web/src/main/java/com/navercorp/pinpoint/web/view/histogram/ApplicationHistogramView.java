package com.navercorp.pinpoint.web.view.histogram;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.Objects;

public class ApplicationHistogramView {
    private final String histogramId;
    private final HistogramView histogramView;

    public ApplicationHistogramView(String histogramId, HistogramView histogramView) {
        this.histogramId = Objects.requireNonNull(histogramId, "histogramId");
        this.histogramView = Objects.requireNonNull(histogramView, "histogramView");
    }

    @JsonProperty("key")
    public String getHistogramId() {
        return histogramId;
    }

    @JsonUnwrapped()
    public HistogramView getHistogramView() {
        return histogramView;
    }
}
