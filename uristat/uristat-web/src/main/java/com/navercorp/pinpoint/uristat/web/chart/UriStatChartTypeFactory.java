package com.navercorp.pinpoint.uristat.web.chart;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UriStatChartTypeFactory {
    private final List<UriStatChartType> uriStatCharts;

    public UriStatChartTypeFactory(UriStatChartType... uriStatCharts) {
        Objects.requireNonNull(uriStatCharts, "uriStatCharts");
        this.uriStatCharts = List.of(uriStatCharts);
    }

    public UriStatChartType valueOf(String type) {
        Objects.requireNonNull(type);
        for (UriStatChartType chart : uriStatCharts) {
            if (type.equals(chart.getType())) {
                return chart;
            }
        }
        throw new RuntimeException("Invalid uri stat chart type: " + type);
    }
}
