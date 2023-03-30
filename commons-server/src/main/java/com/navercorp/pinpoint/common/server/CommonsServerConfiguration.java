package com.navercorp.pinpoint.common.server;

import com.navercorp.pinpoint.common.server.bo.thrift.SpanFactory;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializerV1;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializerV1;
import com.navercorp.pinpoint.common.server.util.ThreadLocalAcceptedTimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonsServerConfiguration {
    @Bean
    public AcceptedTimeService acceptedTimeService() {
        return new ThreadLocalAcceptedTimeService();
    }

    // thrift--------------
    @Bean
    public AgentEventMessageDeserializerV1 agentEventMessageDeserializerV1() {
        return new AgentEventMessageDeserializerV1();
    }

    @Bean
    public AgentEventMessageSerializerV1 agentEventMessageSerializerV1() {
        return new AgentEventMessageSerializerV1();
    }

    @Bean
    public SpanFactory spanFactory() {
        return new SpanFactory();
    }

}
