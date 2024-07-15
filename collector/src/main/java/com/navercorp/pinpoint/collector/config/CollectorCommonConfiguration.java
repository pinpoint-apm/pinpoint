package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.manage.HandlerManager;
import com.navercorp.pinpoint.common.server.bo.filter.SequenceSpanEventFilter;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectorCommonConfiguration {

    @Bean
    public CollectorProperties collectorProperties() {
        return new CollectorProperties();
    }

    @Bean
    public HandlerManager handlerManager() {
        return new HandlerManager();
    }

    @Bean
    public ChannelPropertiesFactory ChannelPropertiesFactory(@Value("${collector.receiver.channel.properties.key:#{null}}")
                                                             String channelPropertiesKey) {
        return new ChannelPropertiesFactory(channelPropertiesKey);
    }

    @Bean
    public IgnoreAddressFilter ignoreAddressFilter(CollectorProperties properties) {
        return new IgnoreAddressFilter(properties.getL4IpList());
    }


    @Bean
    public SequenceSpanEventFilter sequenceSpanEventFilter(@Value("${collector.spanEvent.sequence.limit:5000}") int limit) {
        return new SequenceSpanEventFilter(limit);
    }

    // -------------------
//    @Bean
//    public AgentEventAsyncTaskService agentEventAsyncTask(AgentEventService agentEventService) {
//        return new AgentEventAsyncTaskService(agentEventService);
//    }
//
//    @Bean
//    public AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask(AgentLifeCycleService agentLifeCycleService,
//                                                                  StatisticsService statisticsService,
//                                                                  ServiceTypeRegistryService registry,
//                                                                  CollectorProperties collectorProperties) {
//        return new AgentLifeCycleAsyncTaskService(agentLifeCycleService, statisticsService, registry, collectorProperties);
//    }

//    @Bean
//    public AgentLifeCycleChangeEventHandler agentLifeCycleChangeEventHandler(AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTaskService,
//                                                                             AgentEventAsyncTaskService agentEventAsyncTaskService,
//                                                                             ChannelPropertiesFactory channelPropertiesFactory) {
//        return new AgentLifeCycleChangeEventHandler(agentLifeCycleAsyncTaskService, agentEventAsyncTaskService, channelPropertiesFactory);
//    }
}
