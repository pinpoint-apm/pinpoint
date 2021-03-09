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
    @Bean("agentInterceptorList")
    public List<ServerInterceptor> getAgentServerInterceptor() {
        return newServerInterceptors("agent");
    }

    @Bean("spanInterceptorList")
    public List<ServerInterceptor> getSpanServerInterceptor() {
        return newServerInterceptors("span");
    }

    @Bean("statInterceptorList")
    public List<ServerInterceptor> getStatServerInterceptor() {
        return newServerInterceptors("stat");
    }

    private List<ServerInterceptor> newServerInterceptors(String name) {
        HeaderReader<Header> headerReader = new AgentHeaderReader(name);
        ServerInterceptor interceptor = new HeaderPropagationInterceptor(headerReader);
        return Arrays.asList(interceptor);
    }
}
