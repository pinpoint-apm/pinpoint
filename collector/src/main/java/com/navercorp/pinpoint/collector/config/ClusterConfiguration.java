package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.cluster.ClusterPoint;
import com.navercorp.pinpoint.collector.cluster.ClusterPointLocator;
import com.navercorp.pinpoint.collector.cluster.ClusterPointRepository;
import com.navercorp.pinpoint.collector.cluster.ClusterPointRouter;
import com.navercorp.pinpoint.collector.cluster.route.DefaultRouteHandler;
import com.navercorp.pinpoint.collector.cluster.route.RequestEvent;
import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.cluster.route.RouteFilterChain;
import com.navercorp.pinpoint.collector.cluster.route.StreamEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteCloseEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteHandler;
import com.navercorp.pinpoint.collector.manage.ClusterManager;
import com.navercorp.pinpoint.common.server.cluster.zookeeper.ZookeeperClusterProperties;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class ClusterConfiguration {

    private final Logger logger = LogManager.getLogger(ClusterConfiguration.class);

    public ClusterConfiguration() {
        logger.info("Install {}", ClusterConfiguration.class.getSimpleName());
    }

    @Bean
    public ClusterPointRepository targetClusterPointRepository() {
        return new ClusterPointRepository();
    }

    @Bean
    public DefaultRouteHandler defaultRouteHandler(
            @Qualifier("targetClusterPointRepository") ClusterPointLocator<ClusterPoint<?>> targetClusterPointLocator,
            @Qualifier("requestFilterChain") RouteFilterChain<RequestEvent> requestFilterChain,
            @Qualifier("responseFilterChain") RouteFilterChain<ResponseEvent> responseFilterChain) {
        return new DefaultRouteHandler(targetClusterPointLocator, requestFilterChain, responseFilterChain);
    }

    @Bean
    public StreamRouteHandler streamRouteHandler(
            @Qualifier("targetClusterPointRepository") ClusterPointLocator<ClusterPoint<?>> targetClusterPointLocator,
            @Qualifier("streamCreateFilterChain") RouteFilterChain<StreamEvent> streamCreateFilterChain,
            @Qualifier("streamResponseFilterChain") RouteFilterChain<ResponseEvent> responseFilterChain,
            @Qualifier("streamCloseFilterChain") RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain,
            @Qualifier("commandHeaderTBaseSerializerFactory") SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory) {
        return new StreamRouteHandler(targetClusterPointLocator, streamCreateFilterChain, responseFilterChain, streamCloseFilterChain, commandSerializerFactory);
    }

    @Bean
    public ClusterPointRouter clusterPointRouter(
            @Qualifier("targetClusterPointRepository") ClusterPointRepository<ClusterPoint<?>> targetClusterPointRepository,
            @Qualifier("defaultRouteHandler") DefaultRouteHandler defaultRouteHandler,
            @Qualifier("streamRouteHandler") StreamRouteHandler streamRouteHandler,
            @Qualifier("commandHeaderTBaseSerializerFactory") SerializerFactory<HeaderTBaseSerializer> commandSerializerFactory,
            @Qualifier("commandHeaderTBaseDeserializerFactory") DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory) {
        return new ClusterPointRouter(targetClusterPointRepository, defaultRouteHandler, streamRouteHandler, commandSerializerFactory, commandDeserializerFactory);
    }

    @Bean
    public ClusterManager clusterManager(@Qualifier("collectorClusterProperties") CollectorClusterProperties collectorClusterProperties,
                                         ClusterPointRepository targetClusterPointRepository) {
        return new ClusterManager(collectorClusterProperties, targetClusterPointRepository);
    }


    @Bean
    public CollectorClusterProperties collectorClusterProperties(
            @Qualifier("clusterProperties") ZookeeperClusterProperties clusterProperties,
            @Value("${cluster.listen.ip:}") String clusterListenIp,
            @Value("${cluster.listen.port:-1}") int clusterListenPort) {
        return new CollectorClusterProperties(clusterProperties, clusterListenIp, clusterListenPort);
    }
}
