package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import io.grpc.ServerInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerInterceptorBuilder {


    final List<ServerInterceptor> interceptors = new ArrayList<>();

    public ServerInterceptorBuilder() {
    }


    public void addHeaderReaderInterceptor(String name) {
        HeaderPropagationInterceptor interceptor = newHeaderReaderInterceptor(name);
        addServerInterceptor(interceptor);
    }

    public HeaderPropagationInterceptor newHeaderReaderInterceptor(String name) {
        HeaderReader<Header> headerReader = new AgentHeaderReader(name);
        return new HeaderPropagationInterceptor(headerReader);
    }

    public void addServerInterceptor(ServerInterceptor interceptor) {
        Objects.requireNonNull(interceptor, "interceptor");
        interceptors.add(interceptor);
    }

    public List<ServerInterceptor> build() {
        return List.copyOf(interceptors);
    }

}
