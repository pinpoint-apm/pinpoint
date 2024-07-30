package com.navercorp.pinpoint.collector.config;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.monitor.dropwizard.BulkOperationMetrics;
import com.navercorp.pinpoint.collector.monitor.dropwizard.CollectorMetric;
import com.navercorp.pinpoint.collector.monitor.dropwizard.HBaseAsyncOperationMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
// FIXME: The MetricRegistry is currently used in other parts
//  so it cannot be removed at this time.
//  It needs to be separated in the future.
//@ConditionalOnProperty(
//        value = "pinpoint.modules.collector.monitor.metric",
//        havingValue = "dropwizard", matchIfMissing = true
//)
public class DropwizardConfiguration {
    private final Logger logger = LogManager.getLogger(DropwizardConfiguration.class);

    public DropwizardConfiguration() {
        logger.info("Install {}", DropwizardConfiguration.class.getSimpleName());
    }

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }


    @Bean
    public CollectorMetric collectorMetric(
            CollectorProperties collectorProperties,
            MetricRegistry metricRegistry,
            Optional<HBaseAsyncOperationMetrics> hBaseAsyncOperationMetrics,
            Optional<BulkOperationMetrics> cachedStatisticsDaoMetrics
    ) {
        return new CollectorMetric(collectorProperties, metricRegistry, hBaseAsyncOperationMetrics, cachedStatisticsDaoMetrics);
    }

}
