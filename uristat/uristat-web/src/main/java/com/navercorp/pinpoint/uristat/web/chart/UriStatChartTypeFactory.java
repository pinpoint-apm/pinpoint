package com.navercorp.pinpoint.uristat.web.chart;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UriStatChartTypeFactory {
    private final Map<String, UriStatChartType> uriStatCharts;

    public UriStatChartTypeFactory(UriStatChartType... uriStatCharts) {
        Objects.requireNonNull(uriStatCharts, "uriStatCharts");

        this.uriStatCharts = Arrays.stream(uriStatCharts)
                .collect(Collectors.toMap(UriStatChartType::getType, Function.identity()));
    }

    public UriStatChartType valueOf(String type) {
        Objects.requireNonNull(type);

        final UriStatChartType uriStatChartType = uriStatCharts.get(type);
        if (uriStatChartType == null) {
            throw new RuntimeException("Invalid uri stat chart type: " + type);
        }
        return uriStatChartType;
    }
}
