package com.navercorp.pinpoint.collector.grpc.channelz;

import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.collector.grpc.channelz.service",
        "com.navercorp.pinpoint.collector.grpc.channelz.controller"
})
public class ChannelzConfiguration {

    @Bean
    public ChannelzRegistry channelzRegistry() {
        return new DefaultChannelzRegistry();
    }
}
