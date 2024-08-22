package com.navercorp.pinpoint.otlp.web.vo;

import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.model.AggreTemporality;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.otlp.common.model.MetricType;

import java.util.Objects;

public record FieldAttribute(
        String fieldName,
        MetricType metricType,
        DataType dataType,
        AggregationFunction aggregationFunction,
        AggreTemporality aggregationTemporality,
        String description,
        String unit,
        String version
) {
    public FieldAttribute {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(metricType);
        Objects.requireNonNull(dataType);
        Objects.requireNonNull(aggregationFunction);
        Objects.requireNonNull(aggregationTemporality);
    }
}
