package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.HeaderReader;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import io.grpc.Metadata;
import io.grpc.ServerInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ServerInterceptorBuilder {

    final List<ServerInterceptor> interceptors = new ArrayList<>();

    public ServerInterceptorBuilder() {
    }


    public void addHeaderReaderInterceptor(String name) {
        HeaderPropagationInterceptor interceptor = newHeaderReaderInterceptor(name, AgentHeaderReader::emptyProperties);
        addServerInterceptor(interceptor);
    }

    public void addHeaderReaderInterceptor(String name, Function<Metadata, Map<String, Object>> metadataConverter) {
        HeaderPropagationInterceptor interceptor = newHeaderReaderInterceptor(name, metadataConverter);
        addServerInterceptor(interceptor);
    }

    public HeaderPropagationInterceptor newHeaderReaderInterceptor(String name, Function<Metadata, Map<String, Object>> metadataConverter) {
        HeaderReader<Header> headerReader = new AgentHeaderReader(name, metadataConverter);
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