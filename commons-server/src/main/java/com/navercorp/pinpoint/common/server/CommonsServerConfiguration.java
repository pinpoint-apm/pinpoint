package com.navercorp.pinpoint.common.server;

import com.navercorp.pinpoint.common.server.util.AgentEventMessageDeserializerV1;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializerV1;
import com.navercorp.pinpoint.common.server.util.DefaultTimeSlot;
import com.navercorp.pinpoint.common.server.util.TimeSlot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonsServerConfiguration {

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
    public TimeSlot timeSlot() {
        return new DefaultTimeSlot();
    }
}
