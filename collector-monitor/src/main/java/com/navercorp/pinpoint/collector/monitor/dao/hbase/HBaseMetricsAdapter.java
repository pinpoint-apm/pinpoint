package com.navercorp.pinpoint.collector.monitor.dao.hbase;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.Counter;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.Gauge;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.Histogram;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.MetricRegistry;
import org.apache.hadoop.hbase.shaded.com.codahale.metrics.Timer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

import static com.navercorp.pinpoint.collector.monitor.dao.hbase.MetricNameExtractor.extractName;
import static com.navercorp.pinpoint.collector.monitor.dao.hbase.MetricNameExtractor.extractTags;


public class HBaseMetricsAdapter {
    private final Logger logger = LogManager.getLogger(HBaseMetricsAdapter.class);
    private final MeterRegistry meterRegistry;
    private final Collection<MetricRegistry> metricRegistries;

    public HBaseMetricsAdapter(MeterRegistry meterRegistry, Collection<MetricRegistry> metricRegistries) {
        this.meterRegistry = meterRegistry;
        this.metricRegistries = metricRegistries;
        initialize();
    }

    private void initialize() {
        logger.info("initialize metricRegistries: {}", metricRegistries);

        for (MetricRegistry metricRegistry : metricRegistries) {
            if (metricRegistry != null) {
                logger.info(metricRegistry);
                metricRegistry.getMetrics().forEach((name, metric) -> {
                    if (metric instanceof Counter counter) {
                        registerCounterMetric(name, counter);
                    } else if (metric instanceof Timer timer) {
                        registerTimerMetric(name, timer);
                    } else if (metric instanceof Gauge<?> gauge) {
                        registerGaugeMetric(name, gauge);
                    } else if (metric instanceof Histogram histogram) {
                        registerHistogramMetric(name, histogram);
                    }
                });
            }
        }
    }

    private void registerCounterMetric(String name, Counter counter) {
        io.micrometer.core.instrument.Gauge.builder(extractName(name), counter, Counter::getCount)
                .tags(extractTags(name))
                .register(meterRegistry);
    }

    private void registerTimerMetric(String name, Timer timer) {
        io.micrometer.core.instrument.Gauge.builder(extractName(name), timer, Timer::getCount)
                .tags(extractTags(name))
                .register(meterRegistry);
    }

    private void registerGaugeMetric(String name, Gauge<?> gauge) {
        io.micrometer.core.instrument.Gauge.builder(extractName(name), gauge, HBaseMetricsAdapter::doubleValue)
                .tags(extractTags(name))
                .register(meterRegistry);
    }

    private void registerHistogramMetric(String name, Histogram histogram) {
        DistributionSummary.builder(extractName(name))
                .tags(extractTags(name))
                .register(meterRegistry);
    }

    public static double doubleValue(Gauge<?> gauge) {
        if (gauge == null || gauge.getValue() == null) {
            return Double.NaN;
        }
        Object value = gauge.getValue();
        return Double.parseDouble(value.toString());
    }


    @Override
    public String toString() {
        return "HBaseMetricsAdapter{" +
                "logger=" + logger +
                ", meterRegistry=" + meterRegistry +
                ", metricRegistries=" + metricRegistries +
                '}';
    }
}
