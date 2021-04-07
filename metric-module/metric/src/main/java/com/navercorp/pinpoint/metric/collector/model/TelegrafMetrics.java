package com.navercorp.pinpoint.metric.collector.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navercorp.pinpoint.metric.collector.model.serialize.TelegrafJsonDeserializer;

import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@JsonDeserialize(using = TelegrafJsonDeserializer.class)
public class TelegrafMetrics implements Iterable<TelegrafMetric> {
    @Valid
    private final List<TelegrafMetric> metrics;

    public TelegrafMetrics(List<TelegrafMetric> metrics) {
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public List<TelegrafMetric> getMetrics() {
        return metrics;
    }

    @Override
    public Iterator<TelegrafMetric> iterator() {
        return metrics.iterator();
    }

    public Stream<TelegrafMetric> stream() {
        return metrics.stream();
    }

    public int size() {
        return metrics.size();
    }

    @Override
    public String toString() {
        return "TelegrafMetrics{" +
                "metrics=" + metrics +
                '}';
    }
}
