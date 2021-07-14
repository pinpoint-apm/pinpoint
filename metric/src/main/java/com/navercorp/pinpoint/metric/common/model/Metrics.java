package com.navercorp.pinpoint.metric.common.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.metric.common.model.serialize.TelegrafHttpJsonDeserializer;

import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@JsonDeserialize(using = TelegrafHttpJsonDeserializer.class)
public class Metrics implements Iterable<SystemMetric> {
    @Valid
    private final List<SystemMetric> metrics;

    public Metrics(List<SystemMetric> metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public List<SystemMetric> getMetrics() {
        return metrics;
    }

    @Override
    public Iterator<SystemMetric> iterator() {
        return metrics.iterator();
    }

    public Stream<SystemMetric> stream() {
        return metrics.stream();
    }

    public int size() {
        return metrics.size();
    }

}
