package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.cluster.route.DefaultRouteFilterChain;
import com.navercorp.pinpoint.collector.cluster.route.RequestEvent;
import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.cluster.route.RouteEvent;
import com.navercorp.pinpoint.collector.cluster.route.RouteFilterChain;
import com.navercorp.pinpoint.collector.cluster.route.StreamEvent;
import com.navercorp.pinpoint.collector.cluster.route.StreamRouteCloseEvent;
import com.navercorp.pinpoint.collector.cluster.route.filter.AgentEventHandlingFilter;
import com.navercorp.pinpoint.collector.cluster.route.filter.LoggingFilter;
import com.navercorp.pinpoint.collector.cluster.route.filter.RouteFilter;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.io.CommandHeaderTBaseSerializerFactory;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
@Configuration
public class ClusterFilterChainConfiguration {

    private final Logger logger = LogManager.getLogger(ClusterFilterChainConfiguration.class);

    public ClusterFilterChainConfiguration() {
        logger.info("Install {}", ClusterFilterChainConfiguration.class.getSimpleName());
    }

    @Bean
    public SerializerFactory<HeaderTBaseSerializer> commandHeaderTBaseSerializerFactory() {
        return CommandHeaderTBaseSerializerFactory.getDefaultInstance();
    }

    @Bean
    public DeserializerFactory<HeaderTBaseDeserializer> commandHeaderTBaseDeserializerFactory() {
        return CommandHeaderTBaseDeserializerFactory.getDefaultInstance();
    }

    // Route Filters
    @Bean
    public RouteFilter<? extends RouteEvent> loggingRouteFilter() {
        return new LoggingFilter<>();
    }

    @Bean
    public RouteFilter<ResponseEvent> agentEventHandlingFilter(
            AgentEventService agentEventService,
            @Qualifier("commandHeaderTBaseDeserializerFactory") DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory) {
        return new AgentEventHandlingFilter(agentEventService, commandDeserializerFactory);
    }

    // Filter Chains
    @Bean
    public RouteFilterChain<RequestEvent> requestFilterChain(
            @Qualifier("loggingRouteFilter") RouteFilter<? extends RouteEvent> loggingFilter) {
        return new DefaultRouteFilterChain(List.of(loggingFilter));
    }

    @Bean
    public RouteFilterChain<ResponseEvent> responseFilterChain(
            @Qualifier("loggingRouteFilter") RouteFilter<? extends RouteEvent> loggingFilter,
            @Qualifier("agentEventHandlingFilter") RouteFilter<ResponseEvent> agentEventHandlingFilter) {
        List<RouteFilter<? extends RouteEvent>> filterList = List.of(loggingFilter, agentEventHandlingFilter);
        return new DefaultRouteFilterChain(filterList);
    }


    @Bean
    public RouteFilterChain<StreamEvent> streamCreateFilterChain(
            @Qualifier("loggingRouteFilter") RouteFilter<? extends RouteEvent> loggingFilter) {
        List<RouteFilter<? extends RouteEvent>> filterList = List.of(loggingFilter);
        return new DefaultRouteFilterChain(filterList);
    }

    @Bean
    public RouteFilterChain<ResponseEvent> streamResponseFilterChain(
            @Qualifier("loggingRouteFilter") RouteFilter<? extends RouteEvent> loggingFilter) {
        List<RouteFilter<? extends RouteEvent>> filterList = List.of(loggingFilter);
        return new DefaultRouteFilterChain(filterList);
    }

    @Bean
    public RouteFilterChain<StreamRouteCloseEvent> streamCloseFilterChain() {
        List<RouteFilter<? extends RouteEvent>> filterList = List.of();
        return new DefaultRouteFilterChain(filterList);
    }

}
