package com.navercorp.pinpoint.metric.common.model;


import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Metrics implements Iterable<SystemMetric> {
    private final String id;

    @Valid
    private final List<SystemMetric> metrics;

    public Metrics(String id, List<SystemMetric> metrics) {
        this.id = Objects.requireNonNull(id, "id");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public String getId() {
        return id;
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
