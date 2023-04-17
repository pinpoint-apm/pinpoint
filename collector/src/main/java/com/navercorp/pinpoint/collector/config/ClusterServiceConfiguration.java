package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.ClusterService;
import com.navercorp.pinpoint.collector.cluster.ClusterServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class ClusterServiceConfiguration {

    private final Logger logger = LogManager.getLogger(ClusterServiceConfiguration.class);

    public ClusterServiceConfiguration() {
        logger.info("Install {}", ClusterServiceConfiguration.class.getSimpleName());
    }

    @Bean
    public FactoryBean<ClusterService> clusterService(
            @Qualifier("collectorClusterProperties") CollectorClusterProperties collectorClusterProperties,
            @Qualifier("clusterPointRouter") ClusterPointRouter clusterPointRouter) {
        ClusterServiceFactory factoryBean = new ClusterServiceFactory();
        factoryBean.setClusterProperties(collectorClusterProperties);
        factoryBean.setClusterPointRouter(clusterPointRouter);

        return factoryBean;
    }
}
