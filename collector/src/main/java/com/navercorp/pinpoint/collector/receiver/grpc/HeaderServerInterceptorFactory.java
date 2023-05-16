package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import io.grpc.ServerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class HeaderServerInterceptorFactory {
    @Bean
    public List<ServerInterceptor> agentInterceptorList() {
        return newServerInterceptors("agent");
    }

    @Bean
    public List<ServerInterceptor> spanInterceptorList() {
        return newServerInterceptors("span");
    }

    @Bean
    public List<ServerInterceptor> statInterceptorList() {
        return newServerInterceptors("stat");
    }

    private List<ServerInterceptor> newServerInterceptors(String name) {
        HeaderReader<Header> headerReader = new AgentHeaderReader(name);
        ServerInterceptor interceptor = new HeaderPropagationInterceptor(headerReader);
        return Arrays.asList(interceptor);
    }
}
