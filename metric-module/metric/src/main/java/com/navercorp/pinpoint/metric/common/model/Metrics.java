package com.navercorp.pinpoint.metric.common.model;


import javax.validation.Valid;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Metrics implements Iterable<SystemMetric> {
    private final String hostGroupName;
    private final String hostName;

    @Valid
    private final List<SystemMetric> metrics;

    public Metrics(String hostGroupName, String hostName, List<SystemMetric> metrics) {
        this.hostGroupName = Objects.requireNonNull(hostGroupName, "hostGroupName");
        this.hostName = Objects.requireNonNull(hostName, "hostName");
        this.metrics = Objects.requireNonNull(metrics, "metrics");
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public String getHostName() {
        return hostName;
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
