package com.navercorp.pinpoint.collector.config;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.monitor.BulkOperationMetrics;
import com.navercorp.pinpoint.collector.monitor.CollectorMetric;
import com.navercorp.pinpoint.collector.monitor.HBaseAsyncOperationMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class MetricConfiguration {
    private final Logger logger = LogManager.getLogger(MetricConfiguration.class);

    public MetricConfiguration() {
        logger.info("Install {}", MetricConfiguration.class.getSimpleName());
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }


    @Bean
    public CollectorMetric collectorMetric(CollectorProperties collectorProperties,
                                           MetricRegistry metricRegistry,
                                           Optional<HBaseAsyncOperationMetrics> hBaseAsyncOperationMetrics,
                                           Optional<BulkOperationMetrics> cachedStatisticsDaoMetrics) {
        return new CollectorMetric(collectorProperties, metricRegistry, hBaseAsyncOperationMetrics, cachedStatisticsDaoMetrics);
    }

}
