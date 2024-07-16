package com.navercorp.pinpoint.collector.receiver.grpc.flow;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.github.bucket4j.Bandwidth;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServiceDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StreamRateLimitServerInterceptorTest {

    @Test
    void interceptCall() {
        Bandwidth bandwidth = bandwidth();
        Executor executor = MoreExecutors.directExecutor();

        RateLimitClientStreamServerInterceptor interceptor = new RateLimitClientStreamServerInterceptor("span-service", executor, bandwidth, 1);

        ServiceDescriptor desc = SpanGrpc.getServiceDescriptor();
        MethodDescriptor<PSpanMessage, Empty> methodDescriptor = (MethodDescriptor<PSpanMessage, Empty>) desc.getMethods().iterator().next();
        ServerCall<PSpanMessage, Empty> call = getServerCall(methodDescriptor);

        Metadata headers = getMetadata();

        ServerCallHandler<PSpanMessage, Empty> handler = Mockito.mock(ServerCallHandler.class);
        ServerCall.Listener<PSpanMessage> serverCallListener = Mockito.mock(ServerCall.Listener.class);

        when(handler.startCall(call, headers)).thenReturn(serverCallListener);

        ServerCall.Listener<PSpanMessage> listener = interceptor.interceptCall(call, headers, handler);
        listener.onMessage(PSpanMessage.newBuilder().build());

        verify(serverCallListener).onMessage(any());
    }



    @Test
    void interceptCall_reject() {
        Bandwidth bandwidth = bandwidth();
        Executor executor = command -> {
            throw new RejectedExecutionException("error");
        };
        RateLimitClientStreamServerInterceptor interceptor = new RateLimitClientStreamServerInterceptor("span-service", executor, bandwidth, 1);

        ServiceDescriptor desc = SpanGrpc.getServiceDescriptor();
        MethodDescriptor<PSpanMessage, Empty> methodDescriptor = (MethodDescriptor<PSpanMessage, Empty>) desc.getMethods().iterator().next();

        ServerCall<PSpanMessage, Empty> call = getServerCall(methodDescriptor);

        Metadata headers = getMetadata();

        ServerCallHandler<PSpanMessage, Empty> handler = Mockito.mock(ServerCallHandler.class);
        ServerCall.Listener<PSpanMessage> serverCallListener = Mockito.mock(ServerCall.Listener.class);

        when(handler.startCall(call, headers)).thenReturn(serverCallListener);

        ServerCall.Listener<PSpanMessage> listener = interceptor.interceptCall(call, headers, handler);
        listener.onMessage(PSpanMessage.newBuilder().build());

        verify(serverCallListener, never()).onMessage(any());
    }


    private Bandwidth bandwidth() {
        return Bandwidth.builder()
                .capacity(100)
                .refillGreedy(100, Duration.ofSeconds(1))
                .build();
    }


    private ServerCall<PSpanMessage, Empty> getServerCall(MethodDescriptor<PSpanMessage, Empty> methodDescriptor) {
        ServerCall<PSpanMessage, Empty> call = Mockito.mock(ServerCall.class);

        when(call.getMethodDescriptor()).thenReturn(methodDescriptor);

        SocketAddress inetSocketAddress = new InetSocketAddress(80);
        Attributes attributes = Attributes.newBuilder().set(Grpc.TRANSPORT_ATTR_REMOTE_ADDR, inetSocketAddress).build();
        when(call.getAttributes()).thenReturn(attributes);

        return call;
    }

    private Metadata getMetadata() {
        Metadata headers = new Metadata();
        headers.put(Header.AGENT_ID_KEY, "agent-1234");
        headers.put(Header.APPLICATION_NAME_KEY, "app-abc");
        return headers;
    }


    @Test
    void interceptCall_bandwidth() {
        Bandwidth bandwidth = Bandwidth.builder()
                .capacity(1)
                .refillGreedy(1, Duration.ofMinutes(1))
                .build();
        Executor executor = MoreExecutors.directExecutor();
        RateLimitClientStreamServerInterceptor interceptor = new RateLimitClientStreamServerInterceptor("span-service", executor, bandwidth, 1);

        ServiceDescriptor desc = SpanGrpc.getServiceDescriptor();
        MethodDescriptor<PSpanMessage, Empty> methodDescriptor = (MethodDescriptor<PSpanMessage, Empty>) desc.getMethods().iterator().next();

        ServerCall<PSpanMessage, Empty> call = getServerCall(methodDescriptor);

        Metadata headers = getMetadata();

        ServerCallHandler<PSpanMessage, Empty> handler = Mockito.mock(ServerCallHandler.class);
        ServerCall.Listener<PSpanMessage> serverCallListener = Mockito.mock(ServerCall.Listener.class);

        when(handler.startCall(call, headers)).thenReturn(serverCallListener);

        ServerCall.Listener<PSpanMessage> listener = interceptor.interceptCall(call, headers, handler);
        PSpanMessage spanMessage = PSpanMessage.newBuilder().build();
        listener.onMessage(spanMessage);
        listener.onMessage(PSpanMessage.newBuilder().build());

        verify(serverCallListener).onMessage(spanMessage);
    }
}