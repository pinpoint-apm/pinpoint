package com.navercorp.pinpoint.metric.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Metrics implements Iterable<SystemMetric> {
    @Valid
    private final List<SystemMetric> metrics;

    public Metrics(@JsonProperty("metrics") List<SystemMetric> metrics) {
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
